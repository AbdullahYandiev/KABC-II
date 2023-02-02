package com.kabc.desktop.controller;

import com.kabc.desktop.database.UsersDatabase;
import com.kabc.desktop.database.tableitems.DoctorsList;
import com.kabc.desktop.database.tableitems.UsersMap;
import com.kabc.desktop.date.DateTextField;
import com.kabc.desktop.unit.Doctor;
import com.kabc.desktop.main.Main;
import com.kabc.desktop.unit.User;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;

public class UsersListController {

    @FXML
    private ChoiceBox<Doctor> doctorField;
    @FXML
    private Button editDoctorsButton;
    @FXML
    private Button addUserButton;

    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, String> loginColumn;
    @FXML
    private TableColumn<User, String> surnameColumn;
    @FXML
    private TableColumn<User, String> nameColumn;
    @FXML
    private TableColumn<User, String> lastnameColumn;
    @FXML
    private TableColumn<User, String> birthDateColumn;
    @FXML
    private TableColumn<User, User> editColumn;

    @FXML
    void initialize() {
        doctorField.setItems(DoctorsList.get());

        setCellValueFactories();
        birthDateColumn.setComparator(Comparator.comparing(date -> LocalDate.parse(date, DateTextField.FORMAT)));
        doctorField.valueProperty().addListener((v,t,doctor)->usersTable.setItems(UsersMap.getByDoctorId(doctor.getId())));
        setUpEditColumn();

        editDoctorsButton.setOnAction(event -> openDoctorListWindow());
        addUserButton.setOnAction(event -> openUserEditorWindow(null));
    }

    private void setCellValueFactories() {
        loginColumn.setCellValueFactory(new PropertyValueFactory<>("login"));
        surnameColumn.setCellValueFactory(new PropertyValueFactory<>("surname"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        lastnameColumn.setCellValueFactory(new PropertyValueFactory<>("lastname"));
        birthDateColumn.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        editColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
    }

    private void setUpEditColumn() {
        editColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(User user, boolean isEmpty) {
                super.updateItem(user, isEmpty);
                setGraphic(!isEmpty ? makeUserEditView(user) : null);
                setText(null);
            }
        });
    }

    private HBox makeUserEditView(User user) {
        Button deleteButton = makeUserDeleteButton(user);
        Button editButton = makeUserEditButton(user);
        HBox editView = new HBox(editButton, deleteButton);

        editView.setStyle("-fx-alignment:center");
        HBox.setMargin(deleteButton, new Insets(2, 2, 0, 3));
        HBox.setMargin(editButton, new Insets(2, 3, 0, 2));
        return editView;
    }

    private Button makeUserDeleteButton(User user) {
        Image image = new Image("file:src/main/resources/drawable/delete.png", 10, 10, false, false);
        ImageView deleteView = new ImageView(image);
        Button deleteButton = new Button();
        deleteButton.setGraphic(deleteView);
        deleteButton.setOnAction(event -> UsersDatabase.delete(user));
        return deleteButton;
    }

    private Button makeUserEditButton(User user) {
        Image image = new Image("file:src/main/resources/drawable/edit.png", 10, 10, false, false);
        ImageView editView = new ImageView(image);
        Button editButton = new Button();
        editButton.setGraphic(editView);
        editButton.setOnAction(event -> openUserEditorWindow(user));
        return editButton;
    }

    private void openUserEditorWindow(User user) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/layout/user_editor.fxml"));
            Parent root = fxmlLoader.load();

            UserEditorController userEditorController = fxmlLoader.getController();
            userEditorController.update(user);

            Scene scene = new Scene(root, 400, 530);
            Stage stage = new Stage();

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Информация о пользователе");
            stage.setResizable(false);
            stage.setScene(scene);
            stage.show();
        } catch (IOException ignored) {}
    }

    private void openDoctorListWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/layout/doctors_list.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 450, 480);
            Stage stage = new Stage();

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Информация о специалисте");
            stage.setResizable(false);
            stage.setScene(scene);
            stage.show();
        } catch (IOException ignored) {}
    }
}