package com.kabc.desktop.controller;

import com.kabc.desktop.database.UsersDatabase;
import com.kabc.desktop.database.tableitems.DoctorsList;
import com.kabc.desktop.date.DateTextField;
import com.kabc.desktop.unit.Doctor;
import com.kabc.desktop.unit.User;
import com.kabc.desktop.date.TextDate;
import com.kabc.desktop.database.firebase.FirebaseUsers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class UserEditorController {

    private static final String EMPTY_FIELD_ERROR = "Обязательно для заполнения";
    private static final String INVALID_DATE_ERROR = "Недопустимая дата рождения";
    private static final String LOGIN_TAKEN_ERROR = "Логин занят";

    @FXML
    private TextField loginField;
    @FXML
    private Label loginErrorLabel;

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
    private DatePicker datePicker;
    @FXML
    private DateTextField birthDateField;
    @FXML
    private Label birthDateErrorLabel;

    @FXML
    private ChoiceBox<Doctor> doctorField;
    @FXML
    private Label doctorErrorLabel;

    @FXML
    private Button cancelButton;
    @FXML
    private Button saveButton;

    private User currentUser;

    private String inputLogin;
    private String inputSurname;
    private String inputName;
    private String inputLastname;
    private String inputBirthDate;
    private Doctor inputDoctor;

    @FXML
    void initialize() {
        setWarningsOffListeners();

        redirectDatePickerToTextField();
        doctorField.setItems(DoctorsList.get());

        cancelButton.setOnAction(event -> closeWindow());
        saveButton.setOnAction(event -> saveButtonListener());
    }

    public void update(User user) {
        if (user != null) {
            currentUser = user;
            setFields(user);
        }
    }

    private void setFields(User user) {
        loginField.setText(user.getLogin());
        surnameField.setText(user.getSurname());
        nameField.setText(user.getName());
        lastnameField.setText(user.getLastname());
        birthDateField.setText(user.getBirthDate());
        doctorField.setValue(
                DoctorsList.get().stream().filter(d -> d.getId() == user.getDoctorId()).findFirst().orElse(null)
        );
    }

    private void setWarningsOffListeners() {
        loginField.textProperty().addListener((v, s, t1) -> warningOff(loginField, loginErrorLabel));
        surnameField.textProperty().addListener((v, s, t1) -> warningOff(surnameField, surnameErrorLabel));
        nameField.textProperty().addListener((v, s, t1) -> warningOff(nameField, nameErrorLabel));
        birthDateField.textProperty().addListener((v, s, t1) -> warningOff(birthDateField, birthDateErrorLabel));
        doctorField.valueProperty().addListener((v, n, t1) -> warningOff(doctorField, doctorErrorLabel));
    }

    private void warningOn(Control field, Label errorLabel, String errorLabelText) {
        errorLabel.setText(errorLabelText);
        warningOn(field, errorLabel);
    }

    private void warningOn(Control field, Label errorLabel) {
        field.setStyle("-fx-border-color: red; -fx-background-insets: 0,0px; -fx-border-radius: 3");
        errorLabel.setVisible(true);
    }

    private void warningOff(Control field, Label errorLabel) {
        field.setStyle("");
        errorLabel.setVisible(false);
    }

    private void redirectDatePickerToTextField() {
        datePicker.getEditor().setOpacity(0);
        datePicker.setOnKeyReleased(keyEvent -> birthDateField.requestFocus());
        datePicker.setOnAction(e -> birthDateField.setText(datePicker.getValue().format(DateTextField.FORMAT)));
    }

    private void closeWindow() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    private void saveButtonListener() {
        readInput();
        if (inputLogin.equals("")) {
            warningOn(loginField, loginErrorLabel, EMPTY_FIELD_ERROR);
            checkInputWithoutLogin();
            return;
        }

        if (currentUser != null && inputLogin.equals(currentUser.getLogin())) {
            if (checkInputWithoutLogin()) {
                saveUser();
            }
        } else {
            FirebaseUsers.checkLogin(inputLogin, loginExists -> Platform.runLater(() -> {
                if (checkInputWithoutLogin() && !loginExists) {
                    saveUser();
                } else if (loginExists) {
                    warningOn(loginField, loginErrorLabel, LOGIN_TAKEN_ERROR);
                }
            }));
        }
    }

    private void readInput() {
        inputLogin = loginField.getText().trim();
        inputSurname = surnameField.getText().trim();
        inputName = nameField.getText().trim();
        inputLastname = lastnameField.getText().trim();
        inputBirthDate = birthDateField.getText();
        inputDoctor = doctorField.getValue();
    }

    private boolean checkInputWithoutLogin() {
        boolean success = true;
        if (inputSurname.equals("")) {
            warningOn(surnameField, surnameErrorLabel);
            success = false;
        }
        if (inputName.equals("")) {
            warningOn(nameField, nameErrorLabel);
            success = false;
        }
        if (inputBirthDate.equals(DateTextField.INIT_DATE)) {
            warningOn(birthDateField, birthDateErrorLabel, EMPTY_FIELD_ERROR);
            success = false;
        } else if (!TextDate.isValidBirthDate(inputBirthDate)) {
            warningOn(birthDateField, birthDateErrorLabel, INVALID_DATE_ERROR);
            success = false;
        }
        if (inputDoctor == null) {
            warningOn(doctorField, doctorErrorLabel);
            success = false;
        }
        return success;
    }

    private void saveUser() {
        User user = new User(inputLogin, inputSurname, inputName, inputLastname, inputBirthDate, inputDoctor.getId());
        if (currentUser != null) {
            UsersDatabase.edit(currentUser, user);
        } else {
            UsersDatabase.add(user);
        }
        closeWindow();
    }
}