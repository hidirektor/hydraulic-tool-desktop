package com.hidirektor.dashboard.utils.Model.Table;

import javafx.beans.property.SimpleStringProperty;

public class DataControlTable {
    private final SimpleStringProperty programParameter;
    private final SimpleStringProperty selectedParameterValue;

    public DataControlTable(String programParameter, String selectedParameterValue) {
        this.programParameter = new SimpleStringProperty(programParameter);
        this.selectedParameterValue = new SimpleStringProperty(selectedParameterValue);
    }

    public String getProgramParameter() {
        return programParameter.get();
    }

    public void setProgramParameter(String newProgramParameter) {
        programParameter.set(newProgramParameter);
    }

    public String getSelectedParameterValue() {
        return selectedParameterValue.get();
    }

    public void setSelectedParameterValue(String selectedParameter) {
        selectedParameterValue.set(selectedParameter);
    }
}
