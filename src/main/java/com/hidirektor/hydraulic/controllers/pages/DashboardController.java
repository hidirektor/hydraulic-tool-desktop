package com.hidirektor.hydraulic.controllers.pages;

import com.hidirektor.hydraulic.Launcher;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.Objects;

public class DashboardController {

    private int currentIndex = 0;
    private final String[] imagePaths = {
            "/assets/images/template/carousel/carousel-1.png",
            "/assets/images/template/carousel/carousel-2.png",
            "/assets/images/template/carousel/carousel-3.png"
    };

    private final String[] titles = {
            "Klasik Hesaplama",
            "PowerPack Hesaplama",
            "Blain Hesaplama"
    };

    private final String[] descriptions = {
            "Açıklama 1: Bu hesaplama türü A yöntemi ile çalışır.",
            "Açıklama 2: Bu hesaplama türü B yöntemi ile çalışır.",
            "Açıklama 3: Bu hesaplama türü C yöntemi ile çalışır."
    };

    @FXML
    private ImageView imageView;
    @FXML
    private Label titleLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;

    private Timeline timeline;

    @FXML
    public void initialize() {
        updateSlider();

        // Önceki ve sonraki butonların event'leri
        prevButton.setOnAction(e -> showPreviousImage());
        nextButton.setOnAction(e -> showNextImage());

        // Otomatik kaydırma için Timeline
        timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> showNextImage()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateSlider() {
        imageView.setImage(new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream(imagePaths[currentIndex]))));
        titleLabel.setText(titles[currentIndex]);
        descriptionLabel.setText(descriptions[currentIndex]);
    }

    private void showNextImage() {
        currentIndex = (currentIndex + 1) % imagePaths.length;
        updateSlider();
    }

    private void showPreviousImage() {
        currentIndex = (currentIndex - 1 + imagePaths.length) % imagePaths.length;
        updateSlider();
    }
}