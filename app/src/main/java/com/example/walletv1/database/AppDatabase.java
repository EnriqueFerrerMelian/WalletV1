package com.example.walletv1.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.walletv1.models.Item;


@Database(entities = {Item.class}, version = 1, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {
    // abstract methods
    public abstract ItemDao itemDao();

    // singleton
    private static volatile AppDatabase instance;

    // obtain the instance of the database
    public static AppDatabase getInstance(final Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "walletv1")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}