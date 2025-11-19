package com.hidirektor.hydraulic.controllers;

import com.sun.management.OperatingSystemMXBean;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.net.URL;
import java.util.ResourceBundle;

public class ResourceUsageController implements Initializable {

    @FXML
    private LineChart<Number, Number> ramChart;
    @FXML
    private LineChart<Number, Number> cpuChart;
    @FXML
    private LineChart<Number, Number> diskChart;
    
    @FXML
    private NumberAxis ramTimeAxis, ramUsageAxis;
    @FXML
    private NumberAxis cpuTimeAxis, cpuUsageAxis;
    @FXML
    private NumberAxis diskTimeAxis, diskUsageAxis;
    
    @FXML
    private Label ramLabel, cpuLabel, diskLabel;
    
    @FXML
    private ImageView closeButton;
    
    private XYChart.Series<Number, Number> ramSeries;
    private XYChart.Series<Number, Number> cpuSeries;
    private XYChart.Series<Number, Number> diskSeries;
    
    private OperatingSystemMXBean osBean;
    private MemoryMXBean memoryBean;
    private long startTime;
    private double maxRamMB = 0;
    private double maxDiskGB = 0;
    
    private AnimationTimer updateTimer;
    private boolean isRunning = false;
    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL_MS = 500; // 0.5 saniyede bir güncelle
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        memoryBean = ManagementFactory.getMemoryMXBean();
        startTime = System.currentTimeMillis();
        
        // Grafik serilerini oluştur
        ramSeries = new XYChart.Series<>();
        cpuSeries = new XYChart.Series<>();
        diskSeries = new XYChart.Series<>();
        
        ramChart.getData().add(ramSeries);
        cpuChart.getData().add(cpuSeries);
        diskChart.getData().add(diskSeries);
        
        // Grafik stillerini ayarla
        setupChartStyles();
        
        // İlk verileri al
        updateResourceUsage();
        
        // Periyodik güncelleme başlat
        startMonitoring();
    }
    
    private void setupChartStyles() {
        // Grafik stillerini ayarla - Platform.runLater kullan çünkü node'lar henüz oluşturulmamış olabilir
        Platform.runLater(() -> {
            if (ramSeries.getNode() != null) {
                ramSeries.getNode().setStyle("-fx-stroke: #4CAF50; -fx-stroke-width: 2px;");
            }
            if (cpuSeries.getNode() != null) {
                cpuSeries.getNode().setStyle("-fx-stroke: #2196F3; -fx-stroke-width: 2px;");
            }
            if (diskSeries.getNode() != null) {
                diskSeries.getNode().setStyle("-fx-stroke: #FF9800; -fx-stroke-width: 2px;");
            }
        });
    }
    
    private void startMonitoring() {
        isRunning = true;
        lastUpdateTime = System.currentTimeMillis();
        updateTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isRunning) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastUpdateTime >= UPDATE_INTERVAL_MS) {
                        updateResourceUsage();
                        lastUpdateTime = currentTime;
                    }
                }
            }
        };
        updateTimer.start();
    }
    
    private void stopMonitoring() {
        isRunning = false;
        if (updateTimer != null) {
            updateTimer.stop();
        }
    }
    
    private void updateResourceUsage() {
        Platform.runLater(() -> {
            long currentTime = System.currentTimeMillis();
            double elapsedSeconds = (currentTime - startTime) / 1000.0;
            
            // RAM kullanımı
            long totalMemory = Runtime.getRuntime().maxMemory();
            long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
            
            double usedMemoryMB = usedMemory / (1024.0 * 1024.0);
            double totalMemoryMB = totalMemory / (1024.0 * 1024.0);
            double ramUsagePercent = (usedMemory * 100.0) / totalMemory;
            
            if (totalMemoryMB > maxRamMB) {
                maxRamMB = totalMemoryMB;
                ramUsageAxis.setUpperBound(maxRamMB * 1.1);
            }
            
            ramLabel.setText(String.format("RAM Kullanımı: %.1f MB / %.1f MB (%.1f%%)", 
                usedMemoryMB, totalMemoryMB, ramUsagePercent));
            
            // CPU kullanımı
            double cpuUsage = osBean.getProcessCpuLoad() * 100.0;
            // İlk birkaç ölçümde -1 dönebilir, bu durumda 0 kullan
            if (cpuUsage < 0 || Double.isNaN(cpuUsage)) {
                cpuUsage = 0;
            }
            if (cpuUsage > 100) {
                cpuUsage = 100;
            }
            
            cpuLabel.setText(String.format("CPU Kullanımı: %.1f%%", cpuUsage));
            
            // Disk kullanımı (uygulamanın çalıştığı disk)
            File root = new File("/");
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                root = new File("C:\\");
            }
            
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            
            double usedSpaceGB = usedSpace / (1024.0 * 1024.0 * 1024.0);
            double totalSpaceGB = totalSpace / (1024.0 * 1024.0 * 1024.0);
            double diskUsagePercent = (usedSpace * 100.0) / totalSpace;
            
            if (totalSpaceGB > maxDiskGB) {
                maxDiskGB = totalSpaceGB;
                diskUsageAxis.setUpperBound(maxDiskGB * 1.1);
            }
            
            diskLabel.setText(String.format("Disk Kullanımı: %.2f GB / %.2f GB (%.1f%%)", 
                usedSpaceGB, totalSpaceGB, diskUsagePercent));
            
            // Grafiklere veri ekle (son 60 saniye)
            if (elapsedSeconds <= 60) {
                ramTimeAxis.setUpperBound(60);
                cpuTimeAxis.setUpperBound(60);
                diskTimeAxis.setUpperBound(60);
                
                ramSeries.getData().add(new XYChart.Data<>(elapsedSeconds, usedMemoryMB));
                cpuSeries.getData().add(new XYChart.Data<>(elapsedSeconds, cpuUsage));
                diskSeries.getData().add(new XYChart.Data<>(elapsedSeconds, usedSpaceGB));
            } else {
                // 60 saniyeyi aştıysa, eski verileri kaldır ve zaman eksenini kaydır
                ramTimeAxis.setLowerBound(elapsedSeconds - 60);
                ramTimeAxis.setUpperBound(elapsedSeconds);
                cpuTimeAxis.setLowerBound(elapsedSeconds - 60);
                cpuTimeAxis.setUpperBound(elapsedSeconds);
                diskTimeAxis.setLowerBound(elapsedSeconds - 60);
                diskTimeAxis.setUpperBound(elapsedSeconds);
                
                // Eski verileri temizle (60 saniyeden eski olanları)
                ramSeries.getData().removeIf(data -> data.getXValue().doubleValue() < elapsedSeconds - 60);
                cpuSeries.getData().removeIf(data -> data.getXValue().doubleValue() < elapsedSeconds - 60);
                diskSeries.getData().removeIf(data -> data.getXValue().doubleValue() < elapsedSeconds - 60);
                
                // Yeni veriyi ekle
                ramSeries.getData().add(new XYChart.Data<>(elapsedSeconds, usedMemoryMB));
                cpuSeries.getData().add(new XYChart.Data<>(elapsedSeconds, cpuUsage));
                diskSeries.getData().add(new XYChart.Data<>(elapsedSeconds, usedSpaceGB));
            }
        });
    }
    
    @FXML
    private void handleClose(MouseEvent event) {
        stopMonitoring();
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}

