/*
 * This file is part of ExtendedHotbar, a FabricMC mod.
 * Copyright (C) 2023 Kyle Wood (DenWav)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.denwav.extendedhotbar;

import me.shedaniel.autoconfig.ConfigHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.SlotActionType;
import org.joml.Matrix3x2fStack;

public final class Util {

    public static final int LEFT_BOTTOM_ROW_SLOT_INDEX = 27;
    private static int currentHotbarIndex = 0;
    private static int previousHotbarIndex = 0;
    private static final int LEFT_HOTBAR_SLOT_INDEX = 36;
    private static final int BOTTOM_RIGHT_CRAFTING_SLOT_INDEX = 4;
    public static final int DISTANCE = -22;

    // Fixed: Use the correct slot offset for the second row of inventory (slots 27-35)
    public static final int SLOT_OFFSET = LEFT_BOTTOM_ROW_SLOT_INDEX;

    private static boolean hotbarJustChanged = false;
    private static boolean fluentInventorySwapped = false;
    private static boolean swapRender = false;

    public static ConfigHolder<ModConfig> configHolder = null;
    public static ConfigHolder<ExtendedHotbarState> stateHolder = null;

    private Util() {}

    public static boolean isEnabled() {
        return configHolder != null && configHolder.getConfig().enabled;
    }

    public static boolean isFluent() {
        if (configHolder == null) {
            return false;
        }
        final ModConfig config = configHolder.getConfig();
        return config.enabled && config.fluent;
    }

    public static boolean isSwappingEnabled() {
        if (configHolder == null) {
            return false;
        }
        final ModConfig config = configHolder.getConfig();
        return config.enabled && !config.fluent;
    }

    public static int getCurrentHotbarIndex() {
        return currentHotbarIndex;
    }

    public static boolean hasHotbarJustChanged() {
        return hotbarJustChanged;
    }

    public static void markHotbarChangeProcessed() {
        hotbarJustChanged = false;
    }

    public static void setCurrentHotbarIndex(int index) {
        int maxHotbars = Math.min(4, Math.max(2, configHolder.getConfig().numberOfHotbars));
        if (index != currentHotbarIndex) {
            previousHotbarIndex = currentHotbarIndex;
            currentHotbarIndex = Math.max(0, Math.min(index, maxHotbars - 1));
            hotbarJustChanged = true;
        }
    }

    public static void switchToNextHotbar() {
        int maxHotbars = Math.min(4, Math.max(2, configHolder.getConfig().numberOfHotbars));
        previousHotbarIndex = currentHotbarIndex;
        currentHotbarIndex = (currentHotbarIndex + 1) % maxHotbars;
        hotbarJustChanged = true;
    }

    public static void switchToPreviousHotbar() {
        int maxHotbars = Math.min(4, Math.max(2, configHolder.getConfig().numberOfHotbars));
        previousHotbarIndex = currentHotbarIndex;
        currentHotbarIndex = (currentHotbarIndex - 1 + maxHotbars) % maxHotbars;
        hotbarJustChanged = true;
    }

    // Add method to get the actual inventory slot for a given hotbar and slot
    public static int getInventorySlotForHotbar(int hotbarIndex, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= 9) {
            return -1;
        }

        switch (hotbarIndex) {
            case 0: // Normal hotbar (slots 0-8)
                return slotIndex;
            case 1: // 2nd row of inventory (slots 27-35)
                return slotIndex + 27;
            case 2: // 3rd row of inventory (slots 18-26)
                return slotIndex + 18;
            case 3: // 4th row of inventory (slots 9-17)
                return slotIndex + 9;
            default:
                return -1;
        }
    }

    public static void performMultiHotbarSwap(MinecraftClient client, boolean isScrolling) {
        if (client.player == null || client.interactionManager == null) return;

        PlayerInventory inventory = client.player.getInventory();

        if (currentHotbarIndex == previousHotbarIndex) return;

        final InventoryScreen screen = new InventoryScreen(client.player);
        final int syncId = screen.getScreenHandler().syncId;
        final ClientPlayerInteractionManager im = client.interactionManager;
        final ClientPlayerEntity player = client.player;

        int prevRowStart = getInventorySlotForHotbar(previousHotbarIndex, 0);
        int currentRowStart = getInventorySlotForHotbar(currentHotbarIndex, 0);

        // Swap out previous hotbar (back into its row)
        if (previousHotbarIndex != 0 && prevRowStart != -1) {
            for (int i = 0; i < 9; i++) {
                int hotbarSlotId = LEFT_HOTBAR_SLOT_INDEX + i;
                int rowSlotId = prevRowStart + i;

                // Hotbar slot -> pick up
                im.clickSlot(syncId, hotbarSlotId, 0, SlotActionType.PICKUP, player);
                // Row slot -> swap
                im.clickSlot(syncId, rowSlotId, 0, SlotActionType.PICKUP, player);
                // Place original hotbar item
                im.clickSlot(syncId, hotbarSlotId, 0, SlotActionType.PICKUP, player);
            }
        }

        // Swap in new hotbar (from its row into main hotbar)
        if (currentHotbarIndex != 0 && currentRowStart != -1) {
            for (int i = 0; i < 9; i++) {
                int hotbarSlotId = LEFT_HOTBAR_SLOT_INDEX + i;
                int rowSlotId = currentRowStart + i;

                im.clickSlot(syncId, hotbarSlotId, 0, SlotActionType.PICKUP, player);
                im.clickSlot(syncId, rowSlotId, 0, SlotActionType.PICKUP, player);
                im.clickSlot(syncId, hotbarSlotId, 0, SlotActionType.PICKUP, player);
            }
        }
    }

    public static boolean isRenderSwapped() {
        return swapRender;
    }

    public static void resetRenderedPosition() {
        swapRender = false;
    }

    public static void switchFluentPosition() {
        if (stateHolder == null) {
            return;
        }
        stateHolder.getConfig().position = switch (stateHolder.getConfig().position) {
            case LEFT -> ExtendedHotbarState.Position.RIGHT;
            case RIGHT -> ExtendedHotbarState.Position.LEFT;
        };
        stateHolder.save();

        // Reset the fluent inventory state when switching positions
        fluentInventorySwapped = false;
    }

    public static void setFluentInventorySwapped(boolean swapped) {
        fluentInventorySwapped = swapped;
    }

    public static void moveUp(final Matrix3x2fStack matrixStack) {
        matrixStack.pushMatrix();
        matrixStack.translate(0, DISTANCE);
    }

    public static void reset(final Matrix3x2fStack matrixStack) {
        matrixStack.popMatrix();
    }

    public static void performSwap(final MinecraftClient client, final boolean fullRow) {
        // If we're in multi-hotbar mode, handle it differently
        if (isFluent() && configHolder.getConfig().numberOfHotbars > 2) {
            performMultiHotbarSwap(client, fullRow);
            return;
        }

        final ClientPlayerEntity player = client.player;
        if (player == null) {
            return;
        }

        final InventoryScreen inventory = new InventoryScreen(player);
        final int syncId = inventory.getScreenHandler().syncId;

        if (fullRow) {
            swapRows(client, syncId);
        } else {
            final ClientPlayerInteractionManager interactionManager = client.interactionManager;
            if (interactionManager != null) {
                final int currentItem = player.getInventory().getSelectedSlot();
                swapItem(interactionManager, player, syncId, currentItem);
            }
        }
    }

    private static void swapRows(final MinecraftClient client, final int syncId) {
        final ClientPlayerInteractionManager interactionManager = client.interactionManager;
        final ClientPlayerEntity player = client.player;
        if (interactionManager == null || player == null)  {
            return;
        }

        for (int i = 0; i < 9; i++) {
            swapItem(interactionManager, player, syncId, i);
        }
    }

    private static void swapItem(
            final ClientPlayerInteractionManager interactionManager,
            final ClientPlayerEntity player,
            final int syncId,
            final int slotId
    ) {
        // Move hotbar item to crafting slot
        interactionManager.clickSlot(syncId, slotId + Util.LEFT_HOTBAR_SLOT_INDEX, 0, SlotActionType.PICKUP, player);
        interactionManager.clickSlot(syncId, Util.BOTTOM_RIGHT_CRAFTING_SLOT_INDEX, 0, SlotActionType.PICKUP, player);
        // Move bottom row item to hotbar
        interactionManager.clickSlot(syncId, slotId + Util.LEFT_BOTTOM_ROW_SLOT_INDEX, 0, SlotActionType.PICKUP, player);
        interactionManager.clickSlot(syncId, slotId + Util.LEFT_HOTBAR_SLOT_INDEX, 0, SlotActionType.PICKUP, player);
        // Move crafting slot item to bottom row
        interactionManager.clickSlot(syncId, Util.BOTTOM_RIGHT_CRAFTING_SLOT_INDEX, 0, SlotActionType.PICKUP, player);
        interactionManager.clickSlot(syncId, slotId + Util.LEFT_BOTTOM_ROW_SLOT_INDEX, 0, SlotActionType.PICKUP, player);
    }
}
