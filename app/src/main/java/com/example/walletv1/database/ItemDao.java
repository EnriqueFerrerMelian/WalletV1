package com.example.walletv1.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.walletv1.models.Item;

import java.util.List;

@Dao
public interface ItemDao {
    @Query("SELECT * FROM items")
    List<Item> getAllItems();

    @Query("SELECT * FROM items WHERE date BETWEEN :dateStart AND :dateFinish")
    List<Item> getItemsByDate(String dateStart, String dateFinish);

    @Query("DELETE FROM items")
    void deleteAll();
    @Insert
    Long insert(Item item);

    @Update
    void update(Item item);

    @Delete
    void delete(Item item);

}
