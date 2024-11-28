package com.example.walletv1.ui.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.walletv1.R;
import com.example.walletv1.adapters.ItemAdapter;
import com.example.walletv1.database.AppDatabase;
import com.example.walletv1.databinding.FragmentWalletBinding;
import com.example.walletv1.models.Item;
import com.example.walletv1.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WalletFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WalletFragment extends Fragment {
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "appPrefs";
    private FragmentWalletBinding binding;
    private ItemAdapter adapter;
    private String type;
    RadioButton selectedRadioButton;
    private AppDatabase db;
    String begining = "", end = "";
    List<Item> listItems = new ArrayList<>();
    List<Item> listItemsByMonth = new ArrayList<>();
    Boolean autoCalc;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public WalletFragment() {
        // Required empty public constructor
    }

    public static WalletFragment newInstance(String param1, String param2) {
        WalletFragment fragment = new WalletFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        type = sharedPreferences.getString("displayLimit", "daily");
        autoCalc = sharedPreferences.getBoolean("automateCalcs", false);
        db = AppDatabase.getInstance(requireContext());
        //DatabaseUtils.deleteAllItems(requireContext()); // debugging

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWalletBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        System.out.println(type);
        showItemlistByTypeSelected(type);
        binding.rvItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ItemAdapter();
        adapter.setItems(listItems);
        binding.rvItems.setAdapter(adapter);

        binding.limitDigits.setText(String.valueOf(roundToTwoDecimals(((double) sharedPreferences.getInt(type, 0) / 100))));
        System.out.println("mostrando: " + type);
        binding.limitMarker.setOnClickListener(v -> showSetLimit());
        binding.floatingActionButton.setOnClickListener(v -> showAddItem());
    }

    private void showItemlistByTypeSelected(String type) {
        switch (type) {
            case "daily":
                begining = DateUtils.formatDate(DateUtils.getStartOfDay());
                end = DateUtils.formatDate(DateUtils.getEndOfDay());
                binding.tvLimit.setText("Diario");
                break;
            case "weekly":
                begining = DateUtils.formatDate(DateUtils.getStartOfWeek());
                end = DateUtils.formatDate(DateUtils.getEndOfWeek());
                binding.tvLimit.setText("Semanal");
                break;
            case "monthly":
                begining = DateUtils.formatDate(DateUtils.getStartOfMonth());
                end = DateUtils.formatDate(DateUtils.getEndOfMonth());
                binding.tvLimit.setText("Mensual");
                break;
            case "optional":
                begining = DateUtils.formatDate(DateUtils.getStartOfDay());
                end = DateUtils.formatDate(DateUtils.getEndOfDay());
                binding.tvLimit.setText("Opcional");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    listItems = db.itemDao().getItemsByDate(begining, end);
                    requireActivity().runOnUiThread(() -> {
                        adapter.setItems(listItems);
                        adapter.notifyDataSetChanged();
                        if (autoCalc) {
                            autoCalcLimitResult(listItems);
                        } else {
                            calculateLimitResult(listItems);
                        }
                    });
                } catch (Exception e) {
                    Log.e("AppDatabase", "Error inserting default items", e);
                }
            }
        }).start();
    }

    private void autoCalcLimitResult(List<Item> itemList) {
        System.out.println("autoCalcLimitResult");
        int limtMonthly =sharedPreferences.getInt("monthly", 0);
        int limtWeekly =sharedPreferences.getInt("weekly", 0);
        int limtDaily =sharedPreferences.getInt("daily", 0);
        if (limtMonthly==0){
            showAlertPopup();
        } else {
            int resultDays,resultWeeks,resultMonth;
            int tempSum = itemList.stream().mapToInt(Item::getPrice).sum();
            int color = 0;
            binding.limitTotalDigits.setText(String.valueOf(roundToTwoDecimals(((double) (tempSum) / 100))));
            resultDays = (limtMonthly/getDaysInCurrentMonth())-tempSum;
            resultWeeks = (limtMonthly/getWeeksInCurrentMonth())-tempSum; // gastos mensuales / 4
            System.out.println(getWeeksInCurrentMonth());
            resultMonth = limtMonthly-tempSum;
            switch (type) {
                case "daily":
                    binding.limitSumDigits.setText(String.valueOf(roundToTwoDecimals(((double) (resultDays) / 100))));
                    color = resultDays<0? Color.parseColor("#ff2d00"):Color.parseColor("#000000");
                    break;
                case "weekly":
                    binding.limitSumDigits.setText(String.valueOf(roundToTwoDecimals(((double) (resultWeeks) / 100))));
                    color = resultWeeks<0? Color.parseColor("#ff2d00"):Color.parseColor("#000000");
                    break;
                case "monthly":
                    binding.limitSumDigits.setText(String.valueOf(roundToTwoDecimals(((double) (resultMonth) / 100))));
                    color = resultMonth<0? Color.parseColor("#ff2d00"):Color.parseColor("#000000");
                    break;
                case "optional":
                    binding.limitSumDigits.setText(String.valueOf(roundToTwoDecimals(((double) (resultDays) / 100))));
            }
            binding.limitDigits.setTextColor(color);
        }
    }

    public void calculateLimitResult(List<Item> listItem){
        System.out.println("calculateLimitResult");
        try {
            int priceLimit = sharedPreferences.getInt(type, 0);
            int tempSum = listItem.stream().mapToInt(Item::getPrice).sum();
            binding.limitTotalDigits.setText(String.valueOf(roundToTwoDecimals(((double) (tempSum) / 100))));
            int color = priceLimit<tempSum? Color.parseColor("#ff2d00"):Color.parseColor("#000000");
            binding.limitDigits.setTextColor(color);
            binding.limitDigits.setText(String.valueOf(roundToTwoDecimals(((double) (priceLimit) / 100))));
            binding.limitSumDigits.setText(String.valueOf(roundToTwoDecimals(((double) (priceLimit-tempSum) / 100))));
        } catch (Exception e) {
            Log.e("AppDatabase", "Error inserting default items", e);
        }
    }

    private void showAlertPopup() {
        System.out.println("showAlertPopup");
        requireActivity().runOnUiThread(() -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Aviso")
                    .setMessage("Debes configurar un límite mensual")
                    .setPositiveButton("Aceptar", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });
    }

    public int getDaysInCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        return daysInMonth;
    }

    public int getWeeksInCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        int weeksInMonth = calendar.getActualMaximum(Calendar.WEEK_OF_MONTH);
        return weeksInMonth;
    }

    private void showAddItem() {
        System.out.println("showAddItem");
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_item, null);
        EditText itemName = dialogView.findViewById(R.id.etItemName);
        EditText itemPrice = dialogView.findViewById(R.id.etItemPrice);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Configuración de límites")
                .setView(dialogView)
                .setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handleAddItem(itemName, itemPrice);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .create();
        dialog.show();
    }

    private void handleAddItem(EditText itemName, EditText itemPrice) {
        System.out.println("handleAddItem");
        new Thread(new Runnable() {
            @Override
            public void run() {
                int price = (int)(Double.parseDouble(itemPrice.getText().toString().trim())*100);
                Item item = new Item(itemName.getText().toString().trim(), obtainTodaysDate(), price);
                try {
                    db.itemDao().insert(item);
                    listItems = db.itemDao().getItemsByDate(begining, end);
                    requireActivity().runOnUiThread(() -> {
                        calculateLimitResult(listItems);
                        adapter.setItems(listItems);
                        adapter.notifyDataSetChanged();
                    });
                } catch (Exception e) {
                    Log.e("AppDatabase", "Error inserting default categories", e);
                }
            }
        }).start();
    }

    public String obtainTodaysDate(){
        System.out.println("obtainTodaysDate");
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(calendar.getTime());
    }

    private void showSetLimit() {
        System.out.println("showSetLimit");
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_set_limit, null);
        EditText insertTv = dialogView.findViewById(R.id.insertTv);
        insertTv.setAlpha(0.5f);
        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == R.id.radioOption1) {
                    insertTv.setEnabled(true);
                    insertTv.setAlpha(1.0f);
                } else {
                    insertTv.setEnabled(false);
                    insertTv.setAlpha(0.5f);
                }
                selectedRadioButton = dialogView.findViewById(checkedId);
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Configuración de límites")
                .setView(dialogView)
                .setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handleSetLimit(insertTv, selectedRadioButton);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .create();
        dialog.show();
    }

    private void handleSetLimit(EditText insertTv, RadioButton selectedRadioId) {
        System.out.println("handleSetLimit");
        if (insertTv != null && !insertTv.getText().toString().isEmpty()) {
            binding.limitDigits.setText(String.valueOf(Double.parseDouble(insertTv.getText().toString())));
            binding.tvLimit.setText(selectedRadioId.getText().toString());
            safeTypeOnSharedPreferences("optional", Double.parseDouble(insertTv.getText().toString()));
        }
        if (selectedRadioId.isChecked()) {
            switch (selectedRadioId.getText().toString()) {
                case "Diario":
                    begining = DateUtils.formatDate(DateUtils.getStartOfDay());
                    end = DateUtils.formatDate(DateUtils.getEndOfDay());
                    type = "daily";
                    binding.tvLimit.setText(selectedRadioId.getText().toString());
                    safeTypeOnSharedPreferences("daily", 0);
                    break;
                case "Semanal":
                    begining = DateUtils.formatDate(DateUtils.getStartOfWeek());
                    end = DateUtils.formatDate(DateUtils.getEndOfWeek());
                    type = "weekly";
                    binding.tvLimit.setText(selectedRadioId.getText().toString());
                    safeTypeOnSharedPreferences("weekly", 0);
                    break;
                case "Mensual":
                    begining = DateUtils.formatDate(DateUtils.getStartOfMonth());
                    end = DateUtils.formatDate(DateUtils.getEndOfMonth());
                    type = "monthly";
                    binding.tvLimit.setText(selectedRadioId.getText().toString());
                    safeTypeOnSharedPreferences("monthly", 0);
                    break;
            }
            binding.limitDigits.setText(String.valueOf(roundToTwoDecimals(((double) sharedPreferences.getInt(type, 0) / 100))));
        } else {
            Toast.makeText(requireActivity(), "Ninguna opción seleccionada", Toast.LENGTH_SHORT).show();
        }
        new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        listItems = db.itemDao().getItemsByDate(begining, end);
                        requireActivity().runOnUiThread(() -> {
                            adapter.setItems(listItems);
                            adapter.notifyDataSetChanged();
                            if (autoCalc) {
                                autoCalcLimitResult(listItems);
                            } else {
                                calculateLimitResult(listItems);
                            }
                        });
                    } catch (Exception e) {
                        Log.e("AppDatabase", "Error inserting default categories", e);
                    }
                }
        }).start();
        System.out.println(binding.limitSumDigits.getText().toString());
    }

    private void safeTypeOnSharedPreferences(String limitType, double limit) {
        System.out.println("safeTypeOnSharedPreferences");
        int transformedLimit = (int)(limit * 100);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("displayLimit", limitType);
        editor.putInt("optional", transformedLimit);
        editor.apply();
        System.out.println(binding.limitSumDigits.getText().toString());
    }

    private static double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}