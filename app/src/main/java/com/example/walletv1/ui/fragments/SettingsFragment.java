package com.example.walletv1.ui.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.walletv1.R;
import com.example.walletv1.databinding.FragmentSettingsBinding;
import com.example.walletv1.databinding.FragmentWalletBinding;


public class SettingsFragment extends Fragment {
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "appPrefs";
    private FragmentSettingsBinding binding;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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
        //resetPreferences(); // debuging
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        binding.dailyQuantity.setText(String.valueOf(roundToTwoDecimals(((double) sharedPreferences.getInt("daily", 0) / 100))));
        binding.weeklyQuantity.setText(String.valueOf(roundToTwoDecimals(((double) sharedPreferences.getInt("weekly", 0) / 100))));
        binding.monthlyQuantity.setText(String.valueOf(roundToTwoDecimals(((double) sharedPreferences.getInt("monthly", 0) / 100))));
        binding.checkBox.setChecked(sharedPreferences.getBoolean("automateCalcs", false));

        binding.modifyButton.setOnClickListener(v->{
            showSettingsLimits();
        });
        binding.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                automateCalc(binding.checkBox.isChecked());
            }
        });
        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    private void showSettingsLimits() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_settings_limits, null);
        EditText daily = dialogView.findViewById(R.id.etDaily);
        EditText weekly = dialogView.findViewById(R.id.etWeekly);
        EditText monthly = dialogView.findViewById(R.id.etMonthly);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Configuración de límites")
                .setView(dialogView)
                .setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveLimits(daily, weekly, monthly);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .create();
        dialog.show();
    }

    private void saveLimits(EditText daily, EditText weekly, EditText monthly) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String monthlyLimitStr = monthly.getText().toString();
        String weeklyLimitStr = weekly.getText().toString();
        String dailyLimitStr = daily.getText().toString();

        double monthlyLimit = isValidNumber(monthlyLimitStr) ? Double.parseDouble(monthlyLimitStr) : 0;
        double weeklyLimit = isValidNumber(weeklyLimitStr) ? Double.parseDouble(weeklyLimitStr) : 0;
        double dailyLimit = isValidNumber(dailyLimitStr) ? Double.parseDouble(dailyLimitStr) : 0;

        if (binding.checkBox.isChecked()) {
            calcLimits(monthlyLimit, weeklyLimit, dailyLimit, editor);
        } else {
            noCalcLimits(monthlyLimit, weeklyLimit, dailyLimit, editor);
        }

        editor.apply();

        Toast.makeText(requireContext(), "Límites guardados", Toast.LENGTH_SHORT).show();
    }
    public void noCalcLimits (double monthlyLimit, double weeklyLimit, double dailyLimit, SharedPreferences.Editor editor) {
        if (monthlyLimit>0) {
            editor.putInt("monthly", (int) monthlyLimit*100);
            binding.monthlyQuantity.setText(String.valueOf(roundToTwoDecimals(monthlyLimit)));
        }
        if (weeklyLimit>0) {
            editor.putInt("weekly", (int) weeklyLimit*100);
            binding.weeklyQuantity.setText(String.valueOf(roundToTwoDecimals(weeklyLimit)));
        }
        if (dailyLimit>0) {
            editor.putInt("daily", (int) dailyLimit*100);
            binding.dailyQuantity.setText(String.valueOf(roundToTwoDecimals(dailyLimit)));
        }
    }
    public void calcLimits(double monthlyLimit, double weeklyLimit, double dailyLimit, SharedPreferences.Editor editor) {
        if (monthlyLimit > 0) {
            editor.putInt("monthly", (int) monthlyLimit*100);
            binding.monthlyQuantity.setText(String.valueOf(roundToTwoDecimals(monthlyLimit)));
        } else if (weeklyLimit > 0) {
            monthlyLimit = weeklyLimit * 4;
            binding.monthlyQuantity.setText(String.valueOf(roundToTwoDecimals(monthlyLimit)));
            editor.putInt("monthly", (int) monthlyLimit*100);
        } else if (dailyLimit > 0) {
            monthlyLimit = dailyLimit * 30;
            binding.monthlyQuantity.setText(String.valueOf(roundToTwoDecimals(monthlyLimit)));
            editor.putInt("monthly", (int) monthlyLimit*100);
        }

        if (weeklyLimit > 0) {
            editor.putInt("weekly", (int)(weeklyLimit*100));
            binding.weeklyQuantity.setText(String.valueOf(roundToTwoDecimals(weeklyLimit)));
        } else if (dailyLimit > 0) {
            weeklyLimit = dailyLimit * 7;
            binding.weeklyQuantity.setText(String.valueOf(roundToTwoDecimals(weeklyLimit)));
            editor.putInt("weekly", (int)(weeklyLimit*100));
        } else if (monthlyLimit > 0) {
            weeklyLimit = monthlyLimit / 4;
            binding.weeklyQuantity.setText(String.valueOf(roundToTwoDecimals(weeklyLimit)));
            editor.putInt("weekly", (int)(weeklyLimit*100));
        }

        if (dailyLimit > 0) {
            editor.putInt("daily", (int)(dailyLimit*100));
            binding.dailyQuantity.setText(String.valueOf(roundToTwoDecimals(dailyLimit)));
        } else if (weeklyLimit > 0) {
            dailyLimit = weeklyLimit / 7;
            binding.dailyQuantity.setText(String.valueOf(roundToTwoDecimals(dailyLimit)));
            editor.putInt("daily",(int)(dailyLimit*100));
        }
    }

    private static boolean isValidNumber(String value) {
        if (value == null || value.isEmpty()) return false;
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    private static double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public void automateCalc(Boolean checked){
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("automateCalcs", checked);
        editor.apply();
    }
    public void resetPreferences() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE); SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Esto elimina todos los valores almacenados en "Settings"
        editor.apply();
    }
}