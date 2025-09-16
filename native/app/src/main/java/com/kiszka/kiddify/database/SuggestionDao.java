package com.kiszka.kiddify.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kiszka.kiddify.models.Suggestion;

import java.util.List;

@Dao
public interface SuggestionDao {
    @Query("SELECT * FROM suggestions ORDER BY proposedStart DESC")
    LiveData<List<Suggestion>> getAllSuggestions();
    @Query("SELECT * FROM suggestions")
    List<Suggestion> getAllSuggestionsSync();
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSuggestions(List<Suggestion> suggestions);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSuggestion(Suggestion suggestion);
    @Delete
    void deleteSuggestion(Suggestion suggestion);
}
