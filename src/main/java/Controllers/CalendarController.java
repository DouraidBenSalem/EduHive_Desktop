package Controllers;

import Entities.Evenement;
import Services.EvenementService;
import Services.GoogleCalendarService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class CalendarController implements Initializable {
    @FXML private DatePicker datePicker;
    @FXML private Label selectedDateLabel;
    @FXML private ListView<Evenement> eventsList;
    @FXML private Button syncButton;

    private final EvenementService evenementService = new EvenementService();
    private final GoogleCalendarService googleCalendarService;
    private final ObservableList<Evenement> events = FXCollections.observableArrayList();

    public CalendarController() {
        try {
            googleCalendarService = new GoogleCalendarService();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Failed to initialize Google Calendar service", e);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupCalendar();
        loadEvents();
        
        syncButton.setOnAction(e -> {
            try {
                syncWithGoogleCalendar();
                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                    "Synchronisation avec Google Calendar réussie!");
            } catch (IOException ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Erreur lors de la synchronisation: " + ex.getMessage());
            }
        });
    }

    private void setupCalendar() {
        datePicker.setValue(LocalDate.now());
        datePicker.setOnAction(event -> handleDateSelection());
        eventsList.setItems(events);
        eventsList.setCellFactory(param -> new javafx.scene.control.ListCell<Evenement>() {
            @Override
            protected void updateItem(Evenement event, boolean empty) {
                super.updateItem(event, empty);
                if (empty || event == null) {
                    setText(null);
                } else {
                    setText(String.format("%s - %s\nOrganisé par: %s\nLieu: %s\n%s",
                        event.getNom(),
                        event.getDate(),
                        event.getOrganisateur(),
                        event.getLieu(),
                        event.getDescription()));
                }
            }
        });
    }

    private void loadEvents() {
        try {
            List<Evenement> allEvents = evenementService.readAll();
            events.setAll(allEvents);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                "Erreur lors du chargement des événements: " + e.getMessage());
        }
    }

    private void handleDateSelection() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null) {
            selectedDateLabel.setText("Événements du " + selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            filterEventsByDate(selectedDate);
        }
    }

    private void filterEventsByDate(LocalDate date) {
        String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        events.clear();
        try {
            List<Evenement> allEvents = evenementService.readAll();
            for (Evenement event : allEvents) {
                if (event.getDate().equals(formattedDate)) {
                    events.add(event);
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                "Erreur lors du filtrage des événements: " + e.getMessage());
        }
    }

    private void syncWithGoogleCalendar() throws IOException {
        try {
            List<Evenement> allEvents = evenementService.readAll();
            for (Evenement event : allEvents) {
                googleCalendarService.createEvent(event);
            }
        } catch (SQLException e) {
            throw new IOException("Erreur lors de la récupération des événements: " + e.getMessage(), e);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 