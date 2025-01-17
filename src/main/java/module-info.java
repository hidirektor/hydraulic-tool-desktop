module com.hidirektor.dashboard {
    requires javafx.fxml;
    requires javafx.controls;
    requires me.t3sl4.util.os;
    requires java.management;
    requires java.desktop;
    requires org.yaml.snakeyaml;
    requires me.t3sl4.util.file;
    requires org.json;
    requires itextpdf;
    requires java.logging;
    requires annotations;

    exports com.hidirektor.dashboard;
    opens com.hidirektor.dashboard to javafx.base, javafx.fxml;

    exports com.hidirektor.dashboard.app;
    opens com.hidirektor.dashboard.app to javafx.fxml;

    exports com.hidirektor.dashboard.controllers;
    opens com.hidirektor.dashboard.controllers to javafx.fxml;
    exports com.hidirektor.dashboard.controllers.pages;
    opens com.hidirektor.dashboard.controllers.pages to javafx.fxml;
    exports com.hidirektor.dashboard.controllers.pages.calculation;
    opens com.hidirektor.dashboard.controllers.pages.calculation to javafx.fxml;
    exports com.hidirektor.dashboard.controllers.pages.settings;
    opens com.hidirektor.dashboard.controllers.pages.settings to javafx.fxml;
    exports com.hidirektor.dashboard.controllers.notification;
    opens com.hidirektor.dashboard.controllers.notification to javafx.fxml;

    exports com.hidirektor.dashboard.utils;
    opens com.hidirektor.dashboard.utils to javafx.fxml;
    exports com.hidirektor.dashboard.utils.File;
    opens com.hidirektor.dashboard.utils.File to javafx.fxml;
    exports com.hidirektor.dashboard.utils.Model.Hydraulic;
    opens com.hidirektor.dashboard.utils.Model.Hydraulic to javafx.fxml;
    exports com.hidirektor.dashboard.utils.Model.Table;
    opens com.hidirektor.dashboard.utils.Model.Table to javafx.fxml;
    exports com.hidirektor.dashboard.utils.Model.User;
    opens com.hidirektor.dashboard.utils.Model.User to javafx.fxml;
    exports com.hidirektor.dashboard.utils.Notification;
    opens com.hidirektor.dashboard.utils.Notification to javafx.fxml;
    exports com.hidirektor.dashboard.utils.System;
    opens com.hidirektor.dashboard.utils.System to javafx.fxml;
    exports com.hidirektor.dashboard.utils.Validation;
    opens com.hidirektor.dashboard.utils.Validation to javafx.fxml;
}