package com.hidirektor.dashboard.utils.Process;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.util.function.Consumer;

public class UIProcess {

    public static void changeInputDataForTextField(TextField targetField, Consumer<String> successConsumer) {
        targetField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (!newValue.equals(oldValue)) {
                    successConsumer.accept(newValue);
                }
            }
        });
    }

    public static void changeInputDataForComboBox(ComboBox targetCombo, Consumer<String> successConsumer, Runnable isDataExistRunnable) {
        targetCombo.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            if(!targetCombo.getItems().isEmpty()) {
                if(newValue != null) {
                    if(oldValue != newValue) {
                        if(oldValue == null) {
                            if(isDataExistRunnable != null) {
                                isDataExistRunnable.run();
                            }
                        }

                        successConsumer.accept(newValue.toString());
                    }
                }
            }
        });
    }
}
