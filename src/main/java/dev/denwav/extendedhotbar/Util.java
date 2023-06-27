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

import dev.denwav.extendedhotbar.mixin.MixinCreativeInventoryScreen;
import me.shedaniel.autoconfig.ConfigHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.SlotActionType;

public final class Util {

    public static final int LEFT_BOTTOM_ROW_SLOT_INDEX = 27;

    private static final int LEFT_HOTBAR_SLOT_INDEX = 36;
    private static final int BOTTOM_RIGHT_CRAFTING_SLOT_INDEX = 4;

    public static final int DISTANCE = -22;

    public static ConfigHolder<ModConfig> configHolder = null;

    private Util() {}

    public static boolean isEnabled() {
        return configHolder != null && configHolder.getConfig().enabled;
    }

    public static void moveUp(MatrixStack matrixStack) {
        if (isEnabled()) {
            matrixStack.push();
            matrixStack.translate(0, DISTANCE, 0);
        }
    }

    public static void reset(MatrixStack matrixStack) {
        if (isEnabled()) {
            matrixStack.pop();
        }
    }

    public static void performSwap(final MinecraftClient client, final boolean fullRow) {
        final ClientPlayerEntity player = client.player;
        if (player == null) {
            return;
        }

        final InventoryScreen inventory = new InventoryScreen(player);
        client.setScreen(inventory);

        final Screen currentScreen = client.currentScreen;
        if (currentScreen == null) {
            return;
        }

        // For the switcheroo to work, we need to be in the inventory window
        final ItemGroup group;
        if (currentScreen instanceof CreativeInventoryScreen) {
            group = MixinCreativeInventoryScreen.getSelectedTab();
            if (group.getType() != ItemGroup.Type.INVENTORY) {
                ((MixinCreativeInventoryScreen) currentScreen).callSetSelectedTab(Registries.ITEM_GROUP.get(ItemGroups.INVENTORY));
            }
        } else {
            group = null;
        }

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

        // If group == null then it's not a creative inventory, if its type is INVENTORY then there's no need to change it back to itself
        if (group != null && group.getType() != ItemGroup.Type.INVENTORY) {
            ((MixinCreativeInventoryScreen) currentScreen).callSetSelectedTab(group);
        }
        client.setScreen(null);
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

    private static void swapItem(final ClientPlayerInteractionManager interactionManager, ClientPlayerEntity player, final int syncId, final int slotId) {
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
