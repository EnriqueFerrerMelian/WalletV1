package com.example.walletv1.utils;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;

import com.example.walletv1.database.AppDatabase;
import com.example.walletv1.database.ItemDao;

public class DatabaseUtils {
    static ItemDao itemdao;
    AppDatabase db;

    public DatabaseUtils(Context context) {
        db = AppDatabase.getInstance(context);
        itemdao = db.itemDao();
    }

    public static void deleteAllItems(Context context) {
        DatabaseUtils databaseUtils = new DatabaseUtils(context);
        new Thread(() -> {
            itemdao.deleteAll();
        }).start();

    }

}
