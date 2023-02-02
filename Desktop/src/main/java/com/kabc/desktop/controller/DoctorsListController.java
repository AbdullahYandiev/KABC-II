package com.kabc.desktop.controller;

import com.kabc.desktop.database.DoctorsDatabase;
import com.kabc.desktop.database.tableitems.DoctorsList;
import com.kabc.desktop.database.tableitems.UsersMap;
import com.kabc.desktop.unit.Doctor;
import com.kabc.desktop.main.Main;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class DoctorsListController {

    @FXML
    private TableView<Doctor> doctorsTable;
    @FXML
    private TableColumn<Doctor, String> nameColumn;
    @FXML
    private TableColumn<Doctor, Doctor> editColumn;

    @FXML
    private Button closeButton;
    @FXML
    private Button addDoctorButton;

    @FXML
    void initialize() {
        setCellValueFactories();
        doctorsTable.setItems(DoctorsList.get());
        setUpEditColumn();

        closeButton.setOnAction(event -> closeWindow());
        addDoctorButton.setOnAction(event -> openDoctorEditorWindow(null));
    }

    private void setCellValueFactories() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        editColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
    }

    private void setUpEditColumn() {
        editColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Doctor doctor, boolean isEmpty) {
                super.updateItem(doctor, isEmpty);
                setGraphic(!isEmpty ? makeDoctorEditView(doctor) : null);
                setText(null);
            }
        });
    }

    private HBox makeDoctorEditView(Doctor doctor) {
        Button deleteButton = makeDoctorDeleteButton(doctor);
        Button editButton = makeDoctorEditButton(doctor);
        HBox editView = new HBox(editButton, deleteButton);

        editView.setStyle("-fx-alignment:center");
        HBox.setMargin(deleteButton, new Insets(2, 2, 0, 3));
        HBox.setMargin(editButton, new Insets(2, 3, 0, 2));
        return editView;
    }

    private Button makeDoctorDeleteButton(Doctor doctor) {
        Image image = new Image("file:src/main/resources/drawable/delete.png", 10, 10, false, false);
        ImageView deleteView = new ImageView(image);
        Button deleteButton = new Button();
        deleteButton.setGraphic(deleteView);
        if (UsersMap.getByDoctorId(doctor.getId()).size() == 0)
            deleteButton.setOnAction(event -> DoctorsDatabase.delete(doctor));
        else {
            deleteButton.setDisable(true);
        }
        return deleteButton;
    }

    private Button makeDoctorEditButton(Doctor doctor) {
        Image image = new Image("file:src/main/resources/drawable/edit.png", 10, 10, false, false);
        ImageView editView = new ImageView(image);
        Button editButton = new Button();
        editButton.setGraphic(editView);
        editButton.setOnAction(event -> openDoctorEditorWindow(doctor));
        return editButton;
    }

    private void openDoctorEditorWindow(Doctor doctor) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/layout/doctor_editor.fxml"));
            Parent root = fxmlLoader.load();

            DoctorEditorController doctorEditorController = fxmlLoader.getController();
            doctorEditorController.update(doctor);

            Scene scene = new Scene(root, 400, 340);
            Stage stage = new Stage();

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Информация о специалисте");
            stage.setResizable(false);
            stage.setScene(scene);
            stage.show();
        } catch (IOException ignored) {}
    }

    private void closeWindow() {
        ((Stage) closeButton.getScene().getWindow()).close();
    }
}