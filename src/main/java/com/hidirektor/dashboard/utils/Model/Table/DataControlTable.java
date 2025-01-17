package com.hidirektor.dashboard.utils.Model.Table;

import javafx.beans.property.SimpleStringProperty;

public class DataControlTable {
    private final SimpleStringProperty dataKey;
    private final SimpleStringProperty dataValue;

    public DataControlTable(String dataKey, String dataValue) {
        this.dataKey = new SimpleStringProperty(dataKey);
        this.dataValue = new SimpleStringProperty(dataValue);
    }

    public String getDataKey() {
        return dataKey.get();
    }

    public void setDataKey(String newDataKey) {
        dataKey.set(newDataKey);
    }

    public String getDataValue() {
        return dataValue.get();
    }

    public void setDataValue(String newDataValue) {
        dataValue.set(newDataValue);
    }
}
