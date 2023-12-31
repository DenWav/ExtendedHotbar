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
import net.minecraft.screen.slot.SlotActionType;

public final class Util {

    public static final int LEFT_BOTTOM_ROW_SLOT_INDEX = 27;

    private static final int LEFT_HOTBAR_SLOT_INDEX = 36;
    private static final int BOTTOM_RIGHT_CRAFTING_SLOT_INDEX = 4;

    public static final int DISTANCE = -22;

    public static final int SLOT_OFFSET = LEFT_BOTTOM_ROW_SLOT_INDEX;

    public static ConfigHolder<ModConfig> configHolder = null;

    public static ConfigHolder<ExtendedHotbarState> stateHolder = null;

    private static boolean swapRender = false;

    private Util() {}

    public static boolean isEnabled() {
        return configHolder != null && configHolder.getConfig().enabled;
    }

    public static boolean isSwappingEnabled() {
        if (configHolder == null) {
            return false;
        }
        final ModConfig config = configHolder.getConfig();
        return config.enabled && !config.fluent;
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
    }

    public static void moveUp(final MatrixStack matrixStack) {
        matrixStack.push();
        matrixStack.translate(0, DISTANCE, 0);
    }

    public static void reset(final MatrixStack matrixStack) {
        matrixStack.pop();
    }

    public static void performSwap(final MinecraftClient client, final boolean fullRow) {
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
