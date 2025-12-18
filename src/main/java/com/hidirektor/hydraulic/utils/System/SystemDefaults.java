package com.hidirektor.hydraulic.utils.System;

import com.hidirektor.hydraulic.utils.Model.Hydraulic.HydraulicData;
import com.hidirektor.hydraulic.utils.Model.User.User;

public class SystemDefaults {

    public static String CURRENT_VERSION = "v2.3.1";

    public static final String PREF_NODE_NAME = "ondergrup";
    public static final String DEFAULT_DISPLAY_PREF_KEY = "default_display";
    public static final String PREF_UPDATER_KEY = "hydraulic_tool";

    //Klasör Yolları Yolları
    public static String basePath;
    public static String baseFolderPath;
    public static String userDataFolderPath;
    public static String hydraulicFileDataFolderPath;
    public static String programDataPath;

    public static String userDataPDFFolderPath;
    public static String userDataExcelFolderPath;

    //Dosya Yolları
    public static String accountDataFilePath;
    public static String accountLicenseFilePath;
    public static String userLocalUnitDataFilePath;

    //Program verisi için dosya yolları
    public static String generalDBPath;
    public static String cabinsDBPath;

    public static String classicComboDBPath;
    public static String powerPackComboDBPath;
    public static String blainComboDBPath;
    public static String classicPartsDBPath;
    public static String blainPartsDBPath;
    public static String powerPackPartsHidrosDBPath;
    public static String powerPackPartsIthalDBPath;
    public static String schematicTextsDBPath;
    public static String partOriginsClassicDBPath;
    public static String partOriginsPowerPackDBPath;

    //Active User
    public static User loggedInUser = null;

    //Yerel hydraulic verileri
    public static HydraulicData localHydraulicData = new HydraulicData();

    public static String getCurrentVersion() {
        return CURRENT_VERSION;
    }

    public static HydraulicData getLocalHydraulicData() {
        return localHydraulicData;
    }
}
