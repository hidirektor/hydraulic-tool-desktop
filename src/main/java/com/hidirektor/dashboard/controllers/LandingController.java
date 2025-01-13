package com.hidirektor.dashboard.controllers;

import com.hidirektor.dashboard.Launcher;
import com.hidirektor.dashboard.utils.SceneUtil;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class LandingController implements Initializable {

    @FXML
    public AnchorPane hamburgerMenu;

    @FXML
    public Circle profilePhoto;

    @FXML
    public ImageView mainLogo;

    @FXML
    public ImageView closeIcon, minimizeIcon, expandIcon;

    @FXML
    public Pane currentPagePane;

    private boolean isMenuVisible = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Image templateProfilePhoto = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/images/template/profile-photo.jpg")));
        profilePhoto.setFill(new ImagePattern(templateProfilePhoto, 0, 0, 0, 0, false));

        Platform.runLater(() -> {
            addHoverEffect(closeIcon, minimizeIcon, expandIcon);
            SceneUtil.loadFXMLIntoPane(currentPagePane, "fxml/Dashboard.fxml");
        });
    }

    @FXML
    public void closeProgram() {
        Stage stage = (Stage) (profilePhoto.getScene().getWindow());
        stage.close();
    }

    @FXML
    public void minimizeProgram() {
        Stage stage = (Stage) (profilePhoto.getScene().getWindow());
        boolean isMaximized = stage.isMaximized();

        if(isMaximized) {
            stage.setMaximized(false);
            applyClipToRoot();
        }

        stage.setIconified(true);
    }

    @FXML
    public void expandProgram() {
        Stage stage = (Stage) (profilePhoto.getScene().getWindow());
        boolean isMaximized = stage.isMaximized();

        Node root = profilePhoto.getScene().getRoot();

        if (isMaximized) {
            applyClipToRoot();
        } else {
            root.setClip(null);
        }

        stage.setMaximized(!isMaximized);
    }

    @FXML
    private void collapseMenu() {
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), hamburgerMenu);

        Image collapsedLogo = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/images/logos/onderlift-logo-mini-beyaz.png")));
        Image expandedLogo = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/images/logos/onderlift-logo-beyaz.png")));

        if (isMenuVisible) {
            hamburgerMenu.setPrefWidth(45.0);
            hamburgerMenu.setMinWidth(hamburgerMenu.getPrefWidth());

            isMenuVisible = false;

            mainLogo.setImage(collapsedLogo);
            mainLogo.setFitWidth(32.0);
            mainLogo.setFitHeight(32.0);
        } else {
            hamburgerMenu.setMinWidth(Region.USE_COMPUTED_SIZE);
            hamburgerMenu.setPrefWidth(Region.USE_COMPUTED_SIZE);
            isMenuVisible = true;

            mainLogo.setImage(expandedLogo);
            mainLogo.setFitWidth(140.0);
            mainLogo.setFitHeight(50.0);
        }

        transition.play();
    }

    private void addHoverEffect(ImageView... imageViews) {
        ColorAdjust darkenEffect = new ColorAdjust();
        darkenEffect.setBrightness(-0.5);

        for (ImageView imageView : imageViews) {
            imageView.setOnMouseEntered(event -> imageView.setEffect(darkenEffect));
            imageView.setOnMouseExited(event -> imageView.setEffect(null));
        }
    }

    private void applyClipToRoot() {
        Node root = profilePhoto.getScene().getRoot();
        Rectangle clip = new Rectangle();
        clip.setWidth(1280);
        clip.setHeight(720);
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        root.setClip(clip);
    }
}