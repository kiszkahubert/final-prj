package com.kiszka.prj.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.kiszka.prj.DTOs.TaskDTO;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.ZoneId;
import java.util.Date;

@Service
public class GoogleCalendarService {
    private static final String APPLICATION_NAME = "Kiddify";
    public void createEventFromTask(TaskDTO taskDto, String accessToken) throws IOException, GeneralSecurityException {
        Credential credential = new GoogleCredential().setAccessToken(accessToken);
        Calendar service = new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        Event event = new Event()
                .setSummary(taskDto.getTitle())
                .setDescription(buildDescription(taskDto));
        Date startDate = Date.from(taskDto.getTaskStart().atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(taskDto.getTaskEnd().atZone(ZoneId.systemDefault()).toInstant());
        DateTime startDateTime = new DateTime(startDate);
        EventDateTime start = new EventDateTime().setDateTime(startDateTime).setTimeZone(ZoneId.systemDefault().toString());
        event.setStart(start);
        DateTime endDateTime = new DateTime(endDate);
        EventDateTime end = new EventDateTime().setDateTime(endDateTime).setTimeZone(ZoneId.systemDefault().toString());
        event.setEnd(end);
        String calendarId = "primary";
        service.events().insert(calendarId, event).execute();
    }
    private String buildDescription(TaskDTO taskDto) {
        StringBuilder description = new StringBuilder();
        if (taskDto.getDescription() != null) {
            description.append(taskDto.getDescription()).append("\n\n");
        }
        if (taskDto.getNote() != null) {
            description.append("Notatki: ").append(taskDto.getNote()).append("\n");
        }
        return description.toString();
    }
}