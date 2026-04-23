package com.tuition.desktopapp.ui;

import com.tuition.desktopapp.DesktopAttendanceApplication;
import com.tuition.desktopapp.service.AttendanceService;
import com.tuition.desktopapp.service.StudentService;
import com.tuition.desktopapp.service.SyncService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class BiometricDesktopApplication extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        context = new SpringApplicationBuilder(DesktopAttendanceApplication.class)
                .headless(false)
                .run();
    }

    @Override
    public void start(Stage stage) {
        MainView mainView = new MainView(
                context.getBean(StudentService.class),
                context.getBean(AttendanceService.class),
                context.getBean(SyncService.class),
                stage
        );
        Scene scene = new Scene(mainView.getRoot(), 1200, 780);
        stage.setTitle("Biometric Attendance System");
        stage.setScene(scene);
        stage.setMinWidth(980);
        stage.setMinHeight(680);
        stage.show();
        mainView.refreshAll();
    }

    @Override
    public void stop() {
        if (context != null) {
            context.close();
        }
        Platform.exit();
    }
}
