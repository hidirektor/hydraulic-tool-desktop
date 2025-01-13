package com.hidirektor.dashboard;

import com.hidirektor.dashboard.app.Main;
import me.t3sl4.util.os.desktop.DesktopUtil;

import java.io.IOException;

public class Launcher {
    public static void main(String[] args) throws IOException {
        DesktopUtil.configureSystemProperties();

        Main.main(args);
    }
}
