package com.kiszka.kiddify.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.kiszka.kiddify.models.Media;

import java.util.List;

@Dao
public interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMedia(Media media);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMediaList(List<Media> mediaList);
    @Update
    void updateMedia(Media media);
    @Delete
    void deleteMedia(Media media);
    @Query("DELETE FROM media")
    void deleteAllMedia();
    @Query("SELECT * FROM media")
    LiveData<List<Media>> getAllMedia();
    @Query("SELECT * FROM media WHERE id = :id LIMIT 1")
    Media getMediaById(int id);
}
