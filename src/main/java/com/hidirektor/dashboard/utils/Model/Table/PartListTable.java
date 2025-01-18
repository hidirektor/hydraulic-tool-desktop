package com.hidirektor.dashboard.utils.Model.Table;

import javafx.beans.property.SimpleStringProperty;

public class PartListTable {
    private final SimpleStringProperty malzemeKoduProperty;
    private final SimpleStringProperty malzemeAdiProperty;
    private final SimpleStringProperty malzemeAdetProperty;

    public PartListTable(String malzemeKodu, String malzemeAdi, String malzemeAdet) {
        this.malzemeKoduProperty = new SimpleStringProperty(malzemeKodu);
        this.malzemeAdiProperty = new SimpleStringProperty(malzemeAdi);
        this.malzemeAdetProperty = new SimpleStringProperty(malzemeAdet);
    }

    public String getMalzemeKoduProperty() {
        return malzemeKoduProperty.get();
    }

    public void setMalzemeKoduProperty(String malzemeKodu) {
        malzemeKoduProperty.set(malzemeKodu);
    }

    public String getMalzemeAdiProperty() {
        return malzemeAdiProperty.get();
    }

    public void setMalzemeAdiProperty(String malzemeAdi) {
        malzemeAdiProperty.set(malzemeAdi);
    }

    public String getMalzemeAdetProperty() {
        return malzemeAdetProperty.get();
    }

    public void setMalzemeAdetProperty(String malzemeAdet) {
        malzemeAdetProperty.set(malzemeAdet);
    }
}
