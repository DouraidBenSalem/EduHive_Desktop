package com.eduhive.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;
import com.eduhive.entity.Evenement;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

public class GoogleCalendarService {
    private static final String APPLICATION_NAME = "EduHive Calendar";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "/client_secret_243086149210-7fa725ibefen0oshvj0pdf6tmpg20shk.apps.googleusercontent.com.json";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String EDUHIVE_CALENDAR_NAME = "EduHive Events";

    private final Calendar service;
    private String calendarId;

    public GoogleCalendarService() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        
        initializeCalendar();
    }

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Create tokens directory if it doesn't exist
        Path tokensPath = Paths.get(TOKENS_DIRECTORY_PATH);
        if (!Files.exists(tokensPath)) {
            Files.createDirectories(tokensPath);
        }

        InputStream in = GoogleCalendarService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(tokensPath.toFile()))
                .setAccessType("offline")
                .build();
        
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    private void initializeCalendar() throws IOException {
        // Look for existing EduHive calendar
        String pageToken = null;
        do {
            CalendarList calendarList = service.calendarList().list().setPageToken(pageToken).execute();
            List<CalendarListEntry> items = calendarList.getItems();
            for (CalendarListEntry calendarListEntry : items) {
                if (EDUHIVE_CALENDAR_NAME.equals(calendarListEntry.getSummary())) {
                    calendarId = calendarListEntry.getId();
                    return;
                }
            }
            pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);

        // If no calendar exists, create one
        com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
        calendar.setSummary(EDUHIVE_CALENDAR_NAME);
        calendar.setTimeZone(ZoneId.systemDefault().getId());
        
        com.google.api.services.calendar.model.Calendar createdCalendar = service.calendars().insert(calendar).execute();
        calendarId = createdCalendar.getId();
    }

    public void createEvent(Evenement evenement) throws IOException {
        Event event = new Event()
                .setSummary(evenement.getNom())
                .setDescription(evenement.getDescription())
                .setLocation(evenement.getLieu());

        LocalDate eventDate = LocalDate.parse(evenement.getDate());
        LocalDateTime startDateTime = eventDate.atStartOfDay();
        LocalDateTime endDateTime = startDateTime.plusDays(1);

        DateTime start = new DateTime(startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        DateTime end = new DateTime(endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

        event.setStart(new EventDateTime().setDateTime(start).setTimeZone(ZoneId.systemDefault().getId()));
        event.setEnd(new EventDateTime().setDateTime(end).setTimeZone(ZoneId.systemDefault().getId()));

        // Add custom properties to link with our database
        event.setDescription(String.format("%s\n\nOrganisateur: %s\nID: %s", 
            evenement.getDescription(),
            evenement.getOrganisateur(),
            evenement.getId()));

        service.events().insert(calendarId, event).execute();
    }

    public void updateEvent(Evenement evenement) throws IOException {
        // Search for the event by description containing our ID
        String pageToken = null;
        Event existingEvent = null;
        String searchQuery = "ID: " + evenement.getId();

        do {
            Events events = service.events().list(calendarId)
                    .setPageToken(pageToken)
                    .setQ(searchQuery)
                    .execute();
            List<Event> items = events.getItems();
            if (!items.isEmpty()) {
                existingEvent = items.get(0);
                break;
            }
            pageToken = events.getNextPageToken();
        } while (pageToken != null);

        if (existingEvent == null) {
            // If event doesn't exist in Google Calendar, create it
            createEvent(evenement);
            return;
        }

        // Update existing event
        existingEvent.setSummary(evenement.getNom())
                    .setDescription(String.format("%s\n\nOrganisateur: %s\nID: %s", 
                        evenement.getDescription(),
                        evenement.getOrganisateur(),
                        evenement.getId()))
                    .setLocation(evenement.getLieu());

        LocalDate eventDate = LocalDate.parse(evenement.getDate());
        LocalDateTime startDateTime = eventDate.atStartOfDay();
        LocalDateTime endDateTime = startDateTime.plusDays(1);

        DateTime start = new DateTime(startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        DateTime end = new DateTime(endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

        existingEvent.setStart(new EventDateTime().setDateTime(start).setTimeZone(ZoneId.systemDefault().getId()));
        existingEvent.setEnd(new EventDateTime().setDateTime(end).setTimeZone(ZoneId.systemDefault().getId()));

        service.events().update(calendarId, existingEvent.getId(), existingEvent).execute();
    }

    public void deleteEvent(String eventId) throws IOException {
        // Search for the event by description containing our ID
        String pageToken = null;
        String searchQuery = "ID: " + eventId;

        do {
            Events events = service.events().list(calendarId)
                    .setPageToken(pageToken)
                    .setQ(searchQuery)
                    .execute();
            List<Event> items = events.getItems();
            if (!items.isEmpty()) {
                service.events().delete(calendarId, items.get(0).getId()).execute();
                return;
            }
            pageToken = events.getNextPageToken();
        } while (pageToken != null);
    }
} 