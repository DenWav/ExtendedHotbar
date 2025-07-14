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
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public final class Util {

    public static final int LEFT_BOTTOM_ROW_SLOT_INDEX = 27;
    private static int currentHotbarIndex = 0;
    private static int previousHotbarIndex = 0;
    private static boolean hotbarJustChanged = false;
    private static boolean fluentInventorySwapped = false;

    private static final int LEFT_HOTBAR_SLOT_INDEX = 36;
    private static final int BOTTOM_RIGHT_CRAFTING_SLOT_INDEX = 4;

    public static final int DISTANCE = -22;

    // Fixed: Use the correct slot offset for the second row of inventory (slots 27-35)
    public static final int SLOT_OFFSET = LEFT_BOTTOM_ROW_SLOT_INDEX;

    public static ConfigHolder<ModConfig> configHolder = null;

    public static ConfigHolder<ExtendedHotbarState> stateHolder = null;

    private static boolean swapRender = false;

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

    public static ExtendedHotbarState.Position getFluentPosition() {
        if (stateHolder == null) {
            return ExtendedHotbarState.Position.LEFT;
        }
        return stateHolder.getConfig().position;
    }

    public static ExtendedHotbarState.Position getRenderedFluentPosition() {
        if (stateHolder == null) {
            return ExtendedHotbarState.Position.LEFT;
        }
        final ExtendedHotbarState state = stateHolder.getConfig();
        if (swapRender) {
            return switch (state.position) {
                case LEFT -> ExtendedHotbarState.Position.RIGHT ;
                case RIGHT -> ExtendedHotbarState.Position.LEFT;
            };
        } else {
            return state.position;
        }
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

    public static int getPreviousHotbarIndex() {
        return previousHotbarIndex;
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

    // Add method to detect which hotbar a slot belongs to
    public static int getHotbarIndexForSlot(int inventorySlot) {
        if (inventorySlot >= 0 && inventorySlot <= 8) {
            return 0; // Normal hotbar
        } else if (inventorySlot >= 9 && inventorySlot <= 17) {
            return 1; // 2nd row
        } else if (inventorySlot >= 18 && inventorySlot <= 26) {
            return 2; // 3rd row
        } else if (inventorySlot >= 27 && inventorySlot <= 35) {
            return 3; // 4th row
        }
        return -1; // Not a hotbar slot
    }

    public static void performMultiHotbarSwap(MinecraftClient client, boolean isScrolling) {
        if (client.player == null) return;

        PlayerInventory inventory = client.player.getInventory();

        // Only swap if we actually changed hotbars
        if (currentHotbarIndex == previousHotbarIndex) {
            return;
        }

        // Get the inventory slot ranges for both hotbars
        int prevRowStart = getInventorySlotForHotbar(previousHotbarIndex, 0);
        int currentRowStart = getInventorySlotForHotbar(currentHotbarIndex, 0);

        // If previous hotbar was not the main hotbar (0), swap it back
        if (previousHotbarIndex != 0 && prevRowStart != -1) {
            for (int i = 0; i < 9; i++) {
                int hotbarSlot = i;
                int prevSlot = prevRowStart + i;

                if (prevSlot < inventory.main.size()) {
                    ItemStack hotbarItem = inventory.main.get(hotbarSlot);
                    ItemStack prevRowItem = inventory.main.get(prevSlot);

                    inventory.main.set(hotbarSlot, prevRowItem);
                    inventory.main.set(prevSlot, hotbarItem);
                }
            }
        }

        // If current hotbar is not the main hotbar (0), swap it in
        if (currentHotbarIndex != 0 && currentRowStart != -1) {
            for (int i = 0; i < 9; i++) {
                int hotbarSlot = i;
                int currentSlot = currentRowStart + i;

                if (currentSlot < inventory.main.size()) {
                    ItemStack hotbarItem = inventory.main.get(hotbarSlot);
                    ItemStack currentRowItem = inventory.main.get(currentSlot);

                    inventory.main.set(hotbarSlot, currentRowItem);
                    inventory.main.set(currentSlot, hotbarItem);
                }
            }
        }
    }

    public static void swapRenderedPosition() {
        swapRender = true;
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

    public static boolean isFluentInventorySwapped() {
        return fluentInventorySwapped;
    }

    public static void setFluentInventorySwapped(boolean swapped) {
        fluentInventorySwapped = swapped;
    }

    public static void moveUp(final MatrixStack matrixStack) {
        matrixStack.push();
        matrixStack.translate(0, DISTANCE, 0);
    }

    public static void reset(final MatrixStack matrixStack) {
        matrixStack.pop();
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
                final int currentItem = player.getInventory().selectedSlot;
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
        /*
         * Implementation note:
         * There are fancy click mechanisms to swap item stacks without using a temporary slot, but when swapping between two identical item
         * stacks, things can get messed up. Using a temporary slot that we know is guaranteed to be empty is the safest option.
         */

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
