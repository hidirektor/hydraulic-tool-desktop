package com.hidirektor.hydraulic.controllers.pages;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.io.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class DebugController {
    @FXML
    private TextArea consoleOutput;

    @FXML
    public void initialize() {
        redirectStandardOutput(consoleOutput);
        redirectStandardError(consoleOutput);

        System.out.println("Debug Tool v1.0");
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
}