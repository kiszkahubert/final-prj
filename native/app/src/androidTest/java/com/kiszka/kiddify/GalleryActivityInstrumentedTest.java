package com.kiszka.kiddify;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.kiszka.kiddify.database.AppDatabase;
import com.kiszka.kiddify.databinding.ActivityGalleryBinding;
import com.kiszka.kiddify.models.Media;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class GalleryActivityInstrumentedTest {
    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    @Test
    public void does_items_shows() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        AppDatabase.getDatabase(context).mediaDao().insertMediaList(List.of(new Media(1, "image", "123.png", "2025-11-11T10:00:00.000000", "Ala")));
        try (ActivityScenario<GalleryActivity> scenario = ActivityScenario.launch(GalleryActivity.class)) {
            Thread.sleep(400);
            scenario.onActivity(activity -> {
                try {
                    Field field = GalleryActivity.class.getDeclaredField("binding");
                    field.setAccessible(true);
                    ActivityGalleryBinding binding = (ActivityGalleryBinding) field.get(activity);
                    assertNotNull(binding);
                    RecyclerView rv = binding.recyclerViewGallery;
                    int count = rv.getAdapter() == null ? 0 : rv.getAdapter().getItemCount();
                    assertTrue("RecyclerView should show at least 1 item", count >= 1);
                } catch (Exception e) {
                    throw new AssertionError("Unable to access binding via reflection", e);
                }
            });
        }
    }
}
