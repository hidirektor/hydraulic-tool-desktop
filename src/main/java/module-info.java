module com.hidirektor.dashboard {
    requires javafx.fxml;
    requires javafx.controls;
    requires me.t3sl4.util.os;
    requires java.management;
    requires java.desktop;

    exports com.hidirektor.dashboard;
    opens com.hidirektor.dashboard to javafx.base, javafx.fxml;

    exports com.hidirektor.dashboard.app;
    opens com.hidirektor.dashboard.app to javafx.fxml;

    exports com.hidirektor.dashboard.controllers;
    opens com.hidirektor.dashboard.controllers to javafx.fxml;

    exports com.hidirektor.dashboard.controllers.pages;
    opens com.hidirektor.dashboard.controllers.pages to javafx.fxml;

    exports com.hidirektor.dashboard.controllers.notification;
    opens com.hidirektor.dashboard.controllers.notification to javafx.fxml;

    exports com.hidirektor.dashboard.utils;
    opens com.hidirektor.dashboard.utils to javafx.fxml;
}