package com.kiszka.kiddify.database;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.kiszka.kiddify.models.Media;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class MediaDaoInstrumentedTest {
    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();
    private AppDatabase appDatabase;
    private MediaDao mediaDao;

    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        appDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        mediaDao = appDatabase.mediaDao();
    }

    @Test
    public void insert_and_observe_media() throws InterruptedException {
        Media media = new Media(1, "image", "123.png", "2025-11-11T10:00:00.000000", "Ala");
        mediaDao.insertMediaList(List.of(media));

        final CountDownLatch latch = new CountDownLatch(1);
        final int[] size = new int[1];

        mediaDao.getAllMedia().observeForever(list -> {
            size[0] = list == null ? 0 : list.size();
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(1, size[0]);
    }

    @After
    public void tearDown() {
        appDatabase.close();
    }
}
