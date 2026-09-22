package com.wac.autocore.gui.launcher;

import com.wac.autocore.service.GarageSystem;

public class GarageServiceBridge {
    private static final GarageSystem instance = new GarageSystem();

    public static GarageSystem getInstance() {
        return instance;
    }
}