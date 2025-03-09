package com.hidirektor.hydraulic.controllers.pages;

import com.hidirektor.hydraulic.controllers.LandingController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class DebugController {
    @FXML
    private TextArea consoleOutput;

    Stage currentStage;

    @FXML
    public void initialize() {
        redirectStandardOutput(consoleOutput);
        redirectStandardError(consoleOutput);

        Platform.runLater(() -> {
            currentStage = (Stage) consoleOutput.getScene().getWindow();
            currentStage.setOnCloseRequest(event -> {
                LandingController.isDebugOpened = false;
            });
        });

        System.out.println("Debug Tool v1.0");
    }

    @FXML
    public void closeProgram() {
        LandingController.isDebugOpened = false;
        currentStage.close();
    }

    @FXML
    public void minimizeProgram() {
        boolean isMaximized = currentStage.isMaximized();

        if(isMaximized) {
            currentStage.setMaximized(false);
            applyClipToRoot();
        }

        currentStage.setIconified(true);
    }

    @FXML
    public void expandProgram() {
        boolean isMaximized = currentStage.isMaximized();

        Node root = consoleOutput.getScene().getRoot();

        if (isMaximized) {
            applyClipToRoot();
        } else {
            root.setClip(null);
        }

        currentStage.setMaximized(!isMaximized);
    }

    private void redirectStandardOutput(TextArea area) {
        redirectStream(System.out, area);
    }

    private void redirectStandardError(TextArea area) {
        redirectStream(System.err, area);
    }

    private void redirectStream(PrintStream originalStream, TextArea area) {
        try {
            PipedInputStream in = new PipedInputStream();
            PrintStream pipedOut = new PrintStream(new PipedOutputStream(in), true, UTF_8);
            if (originalStream == System.out) {
                System.setOut(pipedOut);
            } else if (originalStream == System.err) {
                System.setErr(pipedOut);
            }

            Thread thread = new Thread(new StreamReader(in, area));
            thread.setDaemon(true);
            thread.start();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static class StreamReader implements Runnable {

        private final StringBuilder buffer = new StringBuilder();
        private boolean notify = true;

        private final BufferedReader reader;
        private final TextArea textArea;

        StreamReader(InputStream input, TextArea textArea) {
            this.reader = new BufferedReader(new InputStreamReader(input, UTF_8));
            this.textArea = textArea;
        }

        @Override
        public void run() {
            try (reader) {
                int charAsInt;
                while ((charAsInt = reader.read()) != -1) {
                    synchronized (buffer) {
                        buffer.append((char) charAsInt);
                        if (notify) {
                            notify = false;
                            Platform.runLater(this::appendTextToTextArea);
                        }
                    }
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        private void appendTextToTextArea() {
            synchronized (buffer) {
                textArea.appendText(buffer.toString());
                buffer.delete(0, buffer.length());
                notify = true;
            }
        }
    }

    private void applyClipToRoot() {
        Node root = consoleOutput.getScene().getRoot();
        Rectangle clip = new Rectangle();
        clip.setWidth(1280);
        clip.setHeight(720);
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        root.setClip(clip);
    }
}