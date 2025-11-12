package com.kiszka.kiddify.database;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.kiszka.kiddify.models.Suggestion;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class SuggestionDaoInstrumentedTest {
    private AppDatabase appDatabase;
    private SuggestionDao suggestionDao;

    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        appDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        suggestionDao = appDatabase.suggestionDao();
    }
    @Test
    public void insert_and_query_suggestions() {
        Suggestion suggestion = new Suggestion("Test title", "Test description", "2025-11-11 10:00:00", "2025-11-11 11:00:00", "PENDING", "2025-11-11 09:00:00.000000", 1);
        suggestion.setId(1);
        suggestionDao.insertSuggestion(suggestion);

        List<Suggestion> all = suggestionDao.getAllSuggestionsSync();
        assertEquals(1, all.size());
        assertEquals("Test title", all.get(0).getTitle());

        Suggestion suggestionUpdated = new Suggestion("Title 2", "Description 2", "2025-11-11 10:00:00", "2025-11-11 11:00:00", "PENDING", "2025-11-11 09:00:00.000000", 1);
        suggestionUpdated.setId(1);
        suggestionDao.insertSuggestion(suggestionUpdated);

        all = suggestionDao.getAllSuggestionsSync();
        assertEquals(1, all.size());
        assertEquals("Title 2", all.get(0).getTitle());
    }
    @After
    public void tearDown() {
        appDatabase.close();
    }
}
