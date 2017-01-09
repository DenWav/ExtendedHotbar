package com.demonwav.extendedhotbar;

import com.mumfrey.liteloader.LiteMod;

import java.io.File;

public class LiteModExtendedHotbar implements LiteMod {

    @Override
    public String getName() {
        return "ExtendedHotbar";
    }

    @Override
    public String getVersion() {
        return "1.0-SNAPSHOT";
    }

    @Override
    public void init(File configPath) {
    }

    @Override
    public void upgradeSettings(String version, File configPath, File oldConfigPath) {
    }
}
