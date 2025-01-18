package com.hidirektor.hydraulic.utils.Validation;

import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.regex.Pattern;

public class ValidationUtil {

    public enum ValidationType {
        NUMERIC("^[0-9]*$"),
        DECIMAL("^[0-9]*\\.?[0-9]+$"),
        ALPHABETIC("^[a-zA-Z]*$"),
        ALPHANUMERIC("^[a-zA-Z0-9]*$"),
        EMAIL("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

        private final String regex;

        ValidationType(String regex) {
            this.regex = regex;
        }

        public String getRegex() {
            return regex;
        }
    }

    private static final Border ERROR_BORDER = new Border(
            new BorderStroke(
                    Color.web("#EA4643"),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(100),
                    new BorderWidths(2)
            )
    );

    private static final Border DEFAULT_BORDER = null;

    public static void applyValidation(TextField textField, ValidationType validationType) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                textField.setBorder(DEFAULT_BORDER);
                return;
            }

            Pattern pattern = Pattern.compile(validationType.getRegex());
            if (!pattern.matcher(newValue).matches()) {
                textField.setBorder(ERROR_BORDER);
            } else {
                textField.setBorder(DEFAULT_BORDER);
            }
        });
    }
}
