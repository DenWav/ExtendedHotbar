package com.demonwav.extendedhotbar;

import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.core.LiteLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

import java.io.File;

public class LiteModExtendedHotbar implements Tickable {

    private static final KeyBinding swapKeyBinding = new KeyBinding("key.hotbar.toggle", Keyboard.KEY_R, "key.categories.litemods");

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
        LiteLoader.getInput().registerKeyBinding(swapKeyBinding);
    }

    @Override
    public void upgradeSettings(String version, File configPath, File oldConfigPath) {
    }

    @Override
    public void onTick(Minecraft minecraft, float partialTicks, boolean inGame, boolean clock) {
        if (!inGame || minecraft.currentScreen != null || !Minecraft.isGuiEnabled()) {
            return;
        }

        if (swapKeyBinding.isPressed()) {

        }
    }
}
