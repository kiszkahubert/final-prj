package com.kiszka.kiddify.models;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.List;

public class ModelsUnitTest {
    @Test
    public void message_isCorrect() {
        Message message = new Message("KID", 2, "Ala ma kota", "2025-11-11 13:41:06.529141");
        assertEquals("KID", message.getSenderType());
        assertEquals(2, message.getSenderId());
        assertEquals("Ala ma kota", message.getContent());
        assertEquals("2025-11-11 13:41:06.529141", message.getSentAt());
        message.setId(3);
        assertEquals(3, message.getId());
    }
    @Test
    public void kid_isCorrect() {
        Kid kid = new Kid();
        kid.setId(2);
        kid.setName("Ala");
        kid.setBirthDate("2009-01-01");
        kid.setParents(List.of(1));
        assertEquals(2, kid.getId());
        assertEquals("Ala", kid.getName());
        assertEquals("2009-01-01", kid.getBirthDate());
        assertEquals(List.of(1), kid.getParents());
    }
    @Test
    public void login_isCorrect() {
        LoginResponse loginResponse = new LoginResponse();

        Kid kid = new Kid();
        kid.setId(1);
        kid.setName("Ala");
        kid.setBirthDate("2007-01-01");
        kid.setParents(List.of(1));
        TaskData taskData = new TaskData(1, "Test title", "Test description", "2025-11-11 15:00:00", "2025-11-11 16:00:00", "PENDING", "xyz", 1);

        loginResponse.setToken("alamakota");
        loginResponse.setExpiresIn(3600L);
        loginResponse.setKid(kid);
        loginResponse.setTasks(List.of(taskData));

        assertEquals("alamakota", loginResponse.getToken());
        assertEquals(3600L, loginResponse.getExpiresIn());
        assertNotNull(loginResponse.getKid());
        assertEquals(1, loginResponse.getKid().getId());
        assertEquals("Ala", loginResponse.getKid().getName());
        assertEquals("2007-01-01", loginResponse.getKid().getBirthDate());
        assertEquals(List.of(1), loginResponse.getKid().getParents());
        assertEquals(1, loginResponse.getTasks().size());
        assertEquals("Test title", loginResponse.getTasks().get(0).getTitle());
    }
    @Test
    public void media_isCorrect() {
        Media media = new Media(1, "image", "123.png", "2025-11-11 14:00:00.000000", "Ala");
        assertEquals(1, media.getId());
        assertEquals("image", media.getType());
        assertEquals("123.png", media.getUrl());
        assertEquals("2025-11-11 14:00:00.000000", media.getUploadedAt());
        assertEquals("Ala", media.getUploadedByUsername());
    }
    @Test
    public void peopleInfo_isCorrect() {
        PeopleInfo peopleInfo = new PeopleInfo();
        peopleInfo.setId(1);
        peopleInfo.setName("Jan");
        peopleInfo.setType("PARENT");

        assertEquals(1, peopleInfo.getId());
        assertEquals("Jan", peopleInfo.getName());
        assertEquals("PARENT", peopleInfo.getType());
    }
    @Test
    public void suggestion_isCorrect() {
        Suggestion suggestion = new Suggestion("Test title", "Test description", "2025-11-11 13:00:00", "2025-11-11 14:00:00", "PENDING", "2025-11-11 12:38:59.089434", 1);

        assertEquals("Test title", suggestion.getTitle());
        assertEquals("Test description", suggestion.getDescription());
        assertEquals("2025-11-11 13:00:00", suggestion.getProposedStart());
        assertEquals("2025-11-11 14:00:00", suggestion.getProposedEnd());
        assertEquals("PENDING", suggestion.getStatus());
        assertEquals("2025-11-11 12:38:59.089434", suggestion.getCreatedAt());
        assertEquals(1, suggestion.getCreatedById());
        assertNull(suggestion.getReviewedAt());
        assertNull(suggestion.getReviewedById());

        suggestion.setId(1);
        suggestion.setReviewedAt("2025-11-11 14:17:25.560239");
        suggestion.setReviewedById(1);
        suggestion.setStatus("REJECTED");

        assertEquals(1, suggestion.getId());
        assertEquals("2025-11-11 14:17:25.560239", suggestion.getReviewedAt());
        assertEquals(Integer.valueOf(1), suggestion.getReviewedById());
        assertEquals("REJECTED", suggestion.getStatus());
    }
    @Test
    public void task_isCorrect() {
        TaskData task = new TaskData(10, "Test title", "Test description", "2025-11-11 12:00:00", "2025-11-11 13:00:00", "PENDING", "", 1);

        assertEquals(10, task.getTaskId());
        assertEquals("Test title", task.getTitle());
        assertEquals("Test description", task.getDescription());
        assertEquals("2025-11-11 12:00:00", task.getTaskStart());
        assertEquals("2025-11-11 13:00:00", task.getTaskEnd());
        assertEquals("PENDING", task.getStatus());
        assertEquals("", task.getNote());
        assertEquals(1, task.getParentId());

        task.setStatus("DONE");

        assertEquals("DONE", task.getStatus());
    }
}
