package com.demonwav.extendedhotbar.client;

import com.demonwav.extendedhotbar.ModConfig;
import com.demonwav.extendedhotbar.Util;
import com.demonwav.extendedhotbar.mixin.MixinCreativeInventoryScreen;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.slot.SlotActionType;

import static org.lwjgl.glfw.GLFW.*;

@Environment(EnvType.CLIENT)
public class ExtendedHotbarClient implements ClientModInitializer {

    private static final KeyBinding swapKeyBinding = new KeyBinding(
        "key.extendedhotbar.switch",
        InputUtil.Type.KEYSYM,
        GLFW_KEY_R,
        "key.extendedhotbar"
    );

    private static final KeyBinding toggleKeyBinding = new KeyBinding(
        "key.extendedhotbar.toggle",
        InputUtil.Type.KEYSYM,
        GLFW_KEY_EQUAL,
        "key.extendedhotbar"
    );

    private static final int INVENTORY_TAB_INDEX = ItemGroup.INVENTORY.getIndex();

    private static final int LEFT_BOTTOM_ROW_SLOT_INDEX = 27;
    private static final int LEFT_HOTBAR_SLOT_INDEX = 36;
    private static final int BOTTOM_RIGHT_CRAFTING_SLOT_INDEX = 4;

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(swapKeyBinding);
        KeyBindingHelper.registerKeyBinding(toggleKeyBinding);

        Util.configHolder = AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(final MinecraftClient client) {
        final ModConfig config = Util.configHolder.getConfig();
        if (toggleKeyBinding.wasPressed()) {
            config.enabled = !config.enabled;
            Util.configHolder.save();
            return;
        }

        if (!config.enabled) {
            return;
        }

        if (client.world == null || client.currentScreen != null || !MinecraftClient.isHudEnabled()) {
            return;
        }

        if (!swapKeyBinding.wasPressed()) {
            return;
        }

        boolean singleSwap;
        if (config.enableModifier) {
            final long window = client.getWindow().getHandle();
            singleSwap = glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) != GLFW_PRESS && glfwGetKey(window, GLFW_KEY_RIGHT_SHIFT) != GLFW_PRESS;

            if (config.invert) {
                singleSwap = !singleSwap;
            }
        } else {
            singleSwap = !config.invert;
        }

        performSwap(client, singleSwap);
    }

    private void performSwap(final MinecraftClient client, final boolean fullRow) {
        final ClientPlayerEntity player = client.player;
        if (player == null) {
            return;
        }

        final InventoryScreen inventory = new InventoryScreen(player);
        client.openScreen(inventory);

        final Screen currentScreen = client.currentScreen;
        if (currentScreen == null) {
            return;
        }

        // For the switcheroo to work, we need to be in the inventory window
        final int index;
        if (currentScreen instanceof CreativeInventoryScreen) {
            index = ((CreativeInventoryScreen) currentScreen).getSelectedTab();

            if (index != INVENTORY_TAB_INDEX) {
                ((MixinCreativeInventoryScreen) currentScreen).callSetSelectedTab(ItemGroup.INVENTORY);
            }
        } else {
            index = -1;
        }

        final int syncId = inventory.getScreenHandler().syncId;

        if (fullRow) {
            swapRows(client, syncId);
        } else {
            final ClientPlayerInteractionManager interactionManager = client.interactionManager;
            if (interactionManager != null) {
                final int currentItem = player.inventory.selectedSlot;
                swapItem(interactionManager, player, syncId, currentItem);
            }
        }

        // If index == -1 then it's not a creative inventory, if it's INVENTORY_TAB_INDEX then there's no need to change it back to itself
        if (index != -1 && index != INVENTORY_TAB_INDEX) {
            ((MixinCreativeInventoryScreen) currentScreen).callSetSelectedTab(ItemGroup.GROUPS[index]);
        }
        client.openScreen(null);
    }

    private void swapRows(final MinecraftClient client, final int syncId) {
        final ClientPlayerInteractionManager interactionManager = client.interactionManager;
        final ClientPlayerEntity player = client.player;
        if (interactionManager == null || player == null)  {
            return;
        }

        for (int i = 0; i < 9; i++) {
            swapItem(interactionManager, player, syncId, i);
        }
    }

    private void swapItem(final ClientPlayerInteractionManager interactionManager, ClientPlayerEntity player, final int syncId, final int slotId) {
        /*
         * Implementation note:
         * There are fancy click mechanisms to swap item stacks without using a temporary slot, but when swapping between two identical item
         * stacks, things can get messed up. Using a temporary slot that we know is guaranteed to be empty is the safest option.
         */

        // Move hotbar item to crafting slot
        interactionManager.clickSlot(syncId, slotId + LEFT_HOTBAR_SLOT_INDEX, 0, SlotActionType.PICKUP, player);
        interactionManager.clickSlot(syncId, BOTTOM_RIGHT_CRAFTING_SLOT_INDEX, 0, SlotActionType.PICKUP, player);
        // Move bottom row item to hotbar
        interactionManager.clickSlot(syncId, slotId + LEFT_BOTTOM_ROW_SLOT_INDEX, 0, SlotActionType.PICKUP, player);
        interactionManager.clickSlot(syncId, slotId + LEFT_HOTBAR_SLOT_INDEX, 0, SlotActionType.PICKUP, player);
        // Move crafting slot item to bottom row
        interactionManager.clickSlot(syncId, BOTTOM_RIGHT_CRAFTING_SLOT_INDEX, 0, SlotActionType.PICKUP, player);
        interactionManager.clickSlot(syncId, slotId + LEFT_BOTTOM_ROW_SLOT_INDEX, 0, SlotActionType.PICKUP, player);
    }
}
