package com.kiszka.kiddify.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.kiszka.kiddify.models.Message;

import java.util.List;

@Dao
public interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessage(Message message);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessages(List<Message> messages);
    @Delete
    void deleteMessage(Message message);
    @Query("SELECT * FROM messages ORDER BY sent_at ASC")
    LiveData<List<Message>> getAllMessages();
    @Query("SELECT * FROM messages ORDER BY sent_at ASC")
    List<Message> getAllMessagesSync();
}
