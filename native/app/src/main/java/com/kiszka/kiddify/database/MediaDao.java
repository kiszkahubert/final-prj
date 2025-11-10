package com.kiszka.kiddify.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kiszka.kiddify.models.Media;

import java.util.List;

@Dao
public interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // if there is a conflict, replace it
    void insertMediaList(List<Media> mediaList);
    @Query("SELECT * FROM media")
    List<Media> getAllMediaSync();
    @Delete
    void deleteMedia(Media media);
    @Query("SELECT * FROM media")
    LiveData<List<Media>> getAllMedia();
}
