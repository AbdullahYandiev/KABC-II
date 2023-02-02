package com.kabc.desktop.controller;

import com.kabc.desktop.database.DoctorsDatabase;
import com.kabc.desktop.database.firebase.FirebaseDoctors;
import com.kabc.desktop.unit.Doctor;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class DoctorEditorController {

    @FXML
    private TextField surnameField;
    @FXML
    private Label surnameErrorLabel;

    @FXML
    private TextField nameField;
    @FXML
    private Label nameErrorLabel;

    @FXML
    private TextField lastnameField;

    @FXML
    private Button cancelButton;
    @FXML
    private Button saveButton;

    private Doctor currentDoctor;

    private String inputSurname;
    private String inputName;
    private String inputLastname;

    @FXML
    void initialize() {
        setWarningsOffListeners();
        cancelButton.setOnAction(event -> closeWindow());
        saveButton.setOnAction(event -> saveButtonListener());
    }

    public void update(Doctor doctor) {
        if (doctor != null) {
            currentDoctor = doctor;
            setFields(doctor);
        }
    }

    private void setFields(Doctor doctor) {
        surnameField.setText(doctor.getSurname());
        nameField.setText(doctor.getName());
        lastnameField.setText(doctor.getLastname());
    }

    private void setWarningsOffListeners() {
        surnameField.textProperty().addListener((v, s, t1) -> warningOff(surnameField, surnameErrorLabel));
        nameField.textProperty().addListener((v, s, t1) -> warningOff(nameField, nameErrorLabel));
    }

    private boolean warningOn(Control field, Label errorLabel) {
        field.setStyle("-fx-border-color: red; -fx-background-insets: 0,0px; -fx-border-radius: 3");
        errorLabel.setVisible(true);
        return false;
    }

    private void warningOff(Control field, Label errorLabel) {
        field.setStyle("");
        errorLabel.setVisible(false);
    }

    private void closeWindow() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    private void saveButtonListener() {
        readInput();
        if (checkInput()) {
            saveDoctor();
        }
    }

    private void readInput() {
        inputSurname = surnameField.getText().trim();
        inputName = nameField.getText().trim();
        inputLastname = lastnameField.getText().trim();
    }

    private boolean checkInput() {
        boolean success = true;
        if (inputName.equals("")) {
            success = warningOn(surnameField, surnameErrorLabel);
        }
        if (inputName.equals("")) {
            success = warningOn(nameField, nameErrorLabel);
        }
        return success;
    }

    private void saveDoctor() {
        if (currentDoctor != null) {
            editDoctor();
        } else {
            addDoctor();
        }
        closeWindow();
    }

    private void editDoctor() {
        Doctor doctor = new Doctor(currentDoctor.getId(), inputSurname, inputName, inputLastname);
        DoctorsDatabase.edit(currentDoctor, doctor);
    }

    private void addDoctor() {
        FirebaseDoctors.getNewId(doctorId -> Platform.runLater(() -> {
            Doctor doctor = new Doctor(doctorId, inputSurname, inputName, inputLastname);
            DoctorsDatabase.add(doctor);
        }));
    }
}