package com.hidirektor.hydraulic.controllers.pages;

import com.hidirektor.hydraulic.Launcher;
import com.hidirektor.hydraulic.controllers.LandingController;
import com.hidirektor.hydraulic.utils.SceneUtil;
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
            "Önder Grup bünyesinde standartlaştırılmış kabin hesaplaması için tasarlanan hesaplama yöntemidir.",
            "Önder Grup bünyesinde powerpack türünde ki siparişlerin hidrolik seçimi için tasarlanan hesaplama yöntemidir.",
            "Önder Grup bünyesinde blain türünde ki siparişlerin hidrolik seçimi için tasarlanan hesaplama yöntemidir."
    };

    @FXML
    private ImageView imageView;
    @FXML
    private Label titleLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Button startCalculationButton;

    private Timeline timeline;

    @FXML
    public void initialize() {
        updateSlider();

        // Önceki ve sonraki butonların event'leri
        startCalculationButton.setOnAction(e -> startCalculation());

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

    private void startCalculation() {
        if(currentIndex == 0) {
            /*
            add start method for calculation methods
             */
        } else if(currentIndex == 1) {
            /*
            add start method for calculation methods
             */
        } else {
            /*
            add start method for calculation methods
             */
        }
    }

    private void showNextImage() {
        currentIndex = (currentIndex + 1) % imagePaths.length;
        updateSlider();
    }
}