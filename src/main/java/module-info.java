module com.hidirektor.hydraulic {
    requires javafx.fxml;
    requires javafx.controls;
    requires me.t3sl4.util.os;
    requires java.management;
    requires org.yaml.snakeyaml;
    requires me.t3sl4.util.file;
    requires org.json;
    requires itextpdf;
    requires annotations;
    requires com.google.common;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires org.apache.pdfbox;
    requires javafx.swing;

    exports com.hidirektor.hydraulic;
    opens com.hidirektor.hydraulic to javafx.base, javafx.fxml;

    exports com.hidirektor.hydraulic.app;
    opens com.hidirektor.hydraulic.app to javafx.fxml;

    exports com.hidirektor.hydraulic.controllers;
    opens com.hidirektor.hydraulic.controllers to javafx.fxml;
    exports com.hidirektor.hydraulic.controllers.pages;
    opens com.hidirektor.hydraulic.controllers.pages to javafx.fxml;
    exports com.hidirektor.hydraulic.controllers.pages.calculation;
    opens com.hidirektor.hydraulic.controllers.pages.calculation to javafx.fxml;
    exports com.hidirektor.hydraulic.controllers.pages.settings;
    opens com.hidirektor.hydraulic.controllers.pages.settings to javafx.fxml;
    exports com.hidirektor.hydraulic.controllers.notification;
    opens com.hidirektor.hydraulic.controllers.notification to javafx.fxml;

    exports com.hidirektor.hydraulic.utils;
    opens com.hidirektor.hydraulic.utils to javafx.fxml;
    exports com.hidirektor.hydraulic.utils.File;
    opens com.hidirektor.hydraulic.utils.File to javafx.fxml;
    exports com.hidirektor.hydraulic.utils.Model.Hydraulic;
    opens com.hidirektor.hydraulic.utils.Model.Hydraulic to javafx.fxml;
    exports com.hidirektor.hydraulic.utils.Model.Table;
    opens com.hidirektor.hydraulic.utils.Model.Table to javafx.fxml;
    exports com.hidirektor.hydraulic.utils.Model.User;
    opens com.hidirektor.hydraulic.utils.Model.User to javafx.fxml;
    exports com.hidirektor.hydraulic.utils.Notification;
    opens com.hidirektor.hydraulic.utils.Notification to javafx.fxml;
    exports com.hidirektor.hydraulic.utils.Process;
    opens com.hidirektor.hydraulic.utils.Process to javafx.fxml;
    exports com.hidirektor.hydraulic.utils.System;
    opens com.hidirektor.hydraulic.utils.System to javafx.fxml;
    exports com.hidirektor.hydraulic.utils.Validation;
    opens com.hidirektor.hydraulic.utils.Validation to javafx.fxml;
}