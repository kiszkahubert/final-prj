package com.kiszka.kiddify.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.kiszka.kiddify.models.Media;
import com.kiszka.kiddify.models.Suggestion;
import com.kiszka.kiddify.models.TaskData;

@Database(entities = {TaskData.class, Suggestion.class, Media.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
    public abstract SuggestionDao suggestionDao();
    public abstract MediaDao mediaDao();
    private static volatile AppDatabase INSTANCE;
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "kiddify_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
