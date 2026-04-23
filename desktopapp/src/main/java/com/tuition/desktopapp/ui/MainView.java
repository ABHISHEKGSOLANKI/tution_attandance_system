package com.tuition.desktopapp.ui;

import com.tuition.desktopapp.dto.ApiDtos;
import com.tuition.desktopapp.exception.DeviceUnavailableException;
import com.tuition.desktopapp.exception.InvalidFingerprintException;
import com.tuition.desktopapp.service.AttendanceService;
import com.tuition.desktopapp.service.StudentService;
import com.tuition.desktopapp.service.SyncService;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MainView {

    private final StudentService studentService;
    private final AttendanceService attendanceService;
    private final SyncService syncService;

    private final Stage stage;
    private final BorderPane root = new BorderPane();

    private final TextField studentIdField = new TextField();
    private final TextField studentNameField = new TextField();
    private final ComboBox<String> studentClassField = new ComboBox<>();
    private final TableView<ApiDtos.StudentListItem> studentTable = new TableView<>();

    private final Label scanMessageLabel = new Label("Ready to scan fingerprint");
    private final TextField mockTemplateField = new TextField();

    private final Label totalPresentLabel = new Label("0");
    private final Label syncStatusLabel = new Label("Pending");
    private final Label pendingSyncLabel = new Label("0");
    private final TableView<ApiDtos.DashboardAttendanceItem> todayAttendanceTable = new TableView<>();

    public MainView(StudentService studentService,
                    AttendanceService attendanceService,
                    SyncService syncService, Stage stage) {
        this.studentService = studentService;
        this.attendanceService = attendanceService;
        this.syncService = syncService;
        this.stage = stage;
        buildUi();
    }

    public Parent getRoot() {
        return root;
    }

    public void refreshAll() {
        refreshStudents();
        refreshDashboard();
    }

    private void buildUi() {
        root.setPadding(new Insets(18));
        root.setTop(buildHeader());
        root.setCenter(buildTabs());
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #f4efe5, #e6eef8);");
        stage.setMaximized(true);
    }

    private Parent buildHeader() {
        Label title = new Label("Biometric Attendance System");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #15355d;");

        Label subtitle = new Label("Simple biometric registration, attendance scanning, and sync monitoring");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #47607b;");

        VBox box = new VBox(4, title, subtitle);
        box.setPadding(new Insets(0, 0, 16, 0));
        return box;
    }

    private Parent buildTabs() {
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
                new Tab("Admin Dashboard", buildAdminTab()),
                new Tab("Attendance", buildAttendanceTab()),
                new Tab("Dashboard", buildDashboardTab())
        );
        tabPane.getTabs().forEach(tab -> tab.setClosable(false));
        return tabPane;
    }

    private Parent buildAdminTab() {
        studentClassField.setItems(FXCollections.observableArrayList("9TH", "10TH"));
        studentClassField.getSelectionModel().selectFirst();
        studentIdField.setPromptText("Student ID");
        studentNameField.setPromptText("Student Name");

        Button registerButton = new Button("Capture Fingerprint");
        registerButton.setPrefHeight(46);
        registerButton.setStyle(primaryButtonStyle());
        registerButton.setOnAction(event -> attemptStudentRegistration());

        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(14);
        form.add(labeledField("Student ID", studentIdField), 0, 0);
        form.add(labeledField("Name", studentNameField), 1, 0);
        form.add(labeledField("Class", studentClassField), 2, 0);
        form.add(registerButton, 0, 1);
        GridPane.setColumnSpan(registerButton, 3);

        configureStudentTable();
        VBox container = card("Register Student", form, studentTable);
        return container;
    }

    private Parent buildAttendanceTab() {
        scanMessageLabel.setWrapText(true);
        scanMessageLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #15355d;");

        Button scanButton = new Button("Scan Finger");
        scanButton.setMinHeight(130);
        scanButton.setMaxWidth(Double.MAX_VALUE);
        scanButton.setStyle("-fx-background-color: #163d68; -fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold; -fx-background-radius: 18;");
        scanButton.setOnAction(event -> attemptScan());

        mockTemplateField.setPromptText("Optional mock template, e.g. MOCK:STU-1001");
        mockTemplateField.setStyle("-fx-font-size: 15px;");

        VBox box = card(
                "Attendance Capture",
                scanButton,
                new Label("Mock mode support for testing"),
                mockTemplateField,
                scanMessageLabel
        );
        box.setAlignment(Pos.TOP_CENTER);
        return box;
    }

    private Parent buildDashboardTab() {
        HBox cards = new HBox(16,
                statCard("Total Present Today", totalPresentLabel),
                statCard("Sync Status", syncStatusLabel),
                statCard("Pending Sync", pendingSyncLabel)
        );

        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle(primaryButtonStyle());
        refreshButton.setOnAction(event -> refreshDashboard());

        Button syncButton = new Button("Sync Now");
        syncButton.setStyle(secondaryButtonStyle());
        syncButton.setOnAction(event -> attemptManualSync());

        HBox actions = new HBox(12, refreshButton, syncButton);

        configureDashboardTable();
        VBox container = card("Today Dashboard", cards, actions, todayAttendanceTable);
        return container;
    }

    private void attemptStudentRegistration() {
        Runnable action = () -> {
            ApiDtos.StudentRegistrationRequest request = new ApiDtos.StudentRegistrationRequest(
                    studentIdField.getText().trim(),
                    studentNameField.getText().trim(),
                    studentClassField.getValue()
            );
            ApiDtos.StudentRegistrationResponse response = studentService.registerStudent(request);
            Platform.runLater(() -> {
                showInfo("Registration Successful", "Fingerprint captured and student registered: " + response.studentId());
                clearRegistrationForm();
                refreshAll();
            });
        };
        runWithRetry("Student Registration", "Unable to register student right now.", action);
    }

    private void attemptScan() {
        Runnable action = () -> {
            ApiDtos.AttendanceTriggerResponse response = attendanceService.triggerAttendanceScan(mockTemplateField.getText().trim());
            Platform.runLater(() -> {
                if (response.matched()) {
                    scanMessageLabel.setText("Success: Attendance marked for " + response.name() + " (" + response.studentId() + ")");
                    scanMessageLabel.setTextFill(Color.web("#0f7b3e"));
                } else {
                    scanMessageLabel.setText("Failure: " + response.message());
                    scanMessageLabel.setTextFill(Color.web("#b42318"));
                }
                refreshDashboard();
            });
        };
        runWithRetry("Fingerprint Scan", "Fingerprint device is unavailable.", action);
    }

    private void attemptManualSync() {
        Runnable action = () -> {
            ApiDtos.SyncResult result = syncService.syncUnsyncedAttendance();
            Platform.runLater(() -> {
                showInfo("Sync Result", result.message());
                refreshDashboard();
            });
        };
        runWithRetry("Attendance Sync", "Unable to sync attendance at the moment.", action);
    }

    private void refreshStudents() {
        studentTable.setItems(FXCollections.observableArrayList(studentService.getAllStudents()));
    }

    private void refreshDashboard() {
        ApiDtos.DashboardSummary summary = attendanceService.getTodayDashboardSummary();
        totalPresentLabel.setText(String.valueOf(summary.totalPresentToday()));
        syncStatusLabel.setText(summary.syncStatus());
        pendingSyncLabel.setText(String.valueOf(summary.pendingSyncToday()));
        todayAttendanceTable.setItems(FXCollections.observableArrayList(summary.todayAttendance()));
    }

    private void configureStudentTable() {
        studentTable.getColumns().setAll(
                studentColumn("Student ID", ApiDtos.StudentListItem::studentId, 150),
                studentColumn("Name", ApiDtos.StudentListItem::name, 220),
                studentColumn("Class", ApiDtos.StudentListItem::studentClass, 120),
                studentColumn("Created", item -> item.createdAt(), 220)
        );
        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(studentTable, Priority.ALWAYS);
    }

    private void configureDashboardTable() {
        todayAttendanceTable.getColumns().setAll(
                dashboardColumn("Student ID", ApiDtos.DashboardAttendanceItem::studentId, 140),
                dashboardColumn("Name", ApiDtos.DashboardAttendanceItem::name, 220),
                dashboardColumn("Class", ApiDtos.DashboardAttendanceItem::studentClass, 100),
                dashboardColumn("Attendance Date", item -> item.attendanceDate(), 140),
                dashboardColumn("Time", item -> item.timestamp(), 180),
                dashboardColumn("Synced", item -> item.synced(), 100)
        );
        todayAttendanceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(todayAttendanceTable, Priority.ALWAYS);
    }

    private TableColumn<ApiDtos.StudentListItem, Object> studentColumn(String title,
                                                                       java.util.function.Function<ApiDtos.StudentListItem, Object> extractor,
                                                                       double minWidth) {
        TableColumn<ApiDtos.StudentListItem, Object> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(extractor.apply(data.getValue())));
        column.setMinWidth(minWidth);
        return column;
    }

    private TableColumn<ApiDtos.DashboardAttendanceItem, Object> dashboardColumn(String title,
                                                                                 java.util.function.Function<ApiDtos.DashboardAttendanceItem, Object> extractor,
                                                                                 double minWidth) {
        TableColumn<ApiDtos.DashboardAttendanceItem, Object> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(extractor.apply(data.getValue())));
        column.setMinWidth(minWidth);
        return column;
    }

    private VBox card(String title, javafx.scene.Node... children) {
        Label header = new Label(title);
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #15355d;");
        VBox box = new VBox(16);
        box.getChildren().add(header);
        box.getChildren().addAll(children);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: rgba(255,255,255,0.88); -fx-background-radius: 18; -fx-border-radius: 18; -fx-border-color: #d8e2ee;");
        VBox.setVgrow(box, Priority.ALWAYS);
        return box;
    }

    private VBox labeledField(String label, javafx.scene.Node field) {
        Label text = new Label(label);
        text.setStyle("-fx-font-weight: bold; -fx-text-fill: #284b6a;");
        VBox box = new VBox(8, text, field);
        VBox.setVgrow(field, Priority.NEVER);
        if (field instanceof Region region) {
            region.setPrefWidth(260);
        }
        return box;
    }

    private VBox statCard(String title, Label valueLabel) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4f6277;");
        valueLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #15355d;");
        VBox box = new VBox(10, titleLabel, valueLabel);
        box.setPadding(new Insets(18));
        box.setPrefWidth(240);
        box.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 18; -fx-border-radius: 18; -fx-border-color: #d8e2ee;");
        return box;
    }

    private void runWithRetry(String title, String defaultMessage, Runnable action) {
        try {
            action.run();
        } catch (DeviceUnavailableException | InvalidFingerprintException ex) {
            showRetryDialog(title, ex.getMessage(), action);
        } catch (Exception ex) {
            String message = ex.getMessage() == null || ex.getMessage().isBlank() ? defaultMessage : ex.getMessage();
            showError(title, message);
        }
    }

    private void showRetryDialog(String title, String message, Runnable retryAction) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        ButtonType retry = new ButtonType("Retry", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(retry, cancel);
        alert.setTitle(title);
        alert.setHeaderText("Fingerprint device issue");
        alert.setContentText(message);
        alert.showAndWait().ifPresent(result -> {
            if (result == retry) {
                retryAction.run();
            }
        });
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Operation failed");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearRegistrationForm() {
        studentIdField.clear();
        studentNameField.clear();
        studentClassField.getSelectionModel().selectFirst();
    }

    private String primaryButtonStyle() {
        return "-fx-background-color: #173f6a; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 14;";
    }

    private String secondaryButtonStyle() {
        return "-fx-background-color: #dbe7f5; -fx-text-fill: #173f6a; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 14;";
    }
}
