package com.demonwav.extendedhotbar;

import com.google.gson.annotations.Expose;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.modconfig.ConfigStrategy;
import com.mumfrey.liteloader.modconfig.ExposableOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.ClickType;
import org.lwjgl.input.Keyboard;

import java.io.File;

@ExposableOptions(strategy = ConfigStrategy.Versioned, filename = "extendedhotbar.json")
public class LiteModExtendedHotbar implements Tickable {

    private static final KeyBinding swapKeyBinding = new KeyBinding("key.hotbar.switch", Keyboard.KEY_R, "key.categories.litemods");
    private static final KeyBinding toggleKeyBinding = new KeyBinding("key.hotbar.toggle", Keyboard.KEY_P, "key.categories.litemods");

    @Expose
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getName() {
        return "ExtendedHotbar";
    }

    @Override
    public String getVersion() {
        return "1.1.4";
    }

    @Override
    public void init(final File configPath) {
        LiteLoader.getInput().registerKeyBinding(swapKeyBinding);
        LiteLoader.getInput().registerKeyBinding(toggleKeyBinding);
    }

    @Override
    public void upgradeSettings(final String version, final File configPath, final File oldConfigPath) {
    }

    @Override
    public void onTick(final Minecraft minecraft, final float partialTicks, final boolean inGame, final boolean clock) {
        if (toggleKeyBinding.isPressed()) {
            enabled = !enabled;
            LiteLoader.getInstance().writeConfig(this);
            return;
        }

        if (!enabled) {
            return;
        }

        if (!inGame || minecraft.currentScreen != null || !Minecraft.isGuiEnabled()) {
            return;
        }

        if (swapKeyBinding.isPressed()) {
            final GuiInventory inventory = new GuiInventory(minecraft.player);
            minecraft.displayGuiScreen(inventory);

            final int windowId = inventory.inventorySlots.windowId;

            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                final int currentItem = minecraft.player.inventory.currentItem;
                swapItem(minecraft, windowId, currentItem);
            } else {
                for (int i = 0; i < 9; i++) {
                    swapItem(minecraft, windowId, i);
                }
            }

            minecraft.displayGuiScreen(null);
        }
    }

    private void swapItem(final Minecraft minecraft, final int windowId, final int slotId) {
        minecraft.playerController.windowClick(windowId, slotId + 36, 0, ClickType.PICKUP, minecraft.player);
        minecraft.playerController.windowClick(windowId, 4, 0, ClickType.PICKUP, minecraft.player);
        minecraft.playerController.windowClick(windowId, slotId + 27, 0, ClickType.PICKUP, minecraft.player);
        minecraft.playerController.windowClick(windowId, slotId + 36, 0, ClickType.PICKUP, minecraft.player);
        minecraft.playerController.windowClick(windowId, 4, 0, ClickType.PICKUP, minecraft.player);
        minecraft.playerController.windowClick(windowId, slotId + 27, 0, ClickType.PICKUP, minecraft.player);
    }
}
