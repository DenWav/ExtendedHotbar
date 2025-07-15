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

package dev.denwav.extendedhotbar.mixin.fluent;

import dev.denwav.extendedhotbar.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.objectweb.asm.Opcodes;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClientFluent {

    @Unique
    private int previousHotbarIndex = -1;
    @Unique
    private boolean inventoryWasOpen = false;

    @Inject(
            method = "handleInputEvents",
            at = @At(
                    value = "JUMP",
                    shift = At.Shift.AFTER
            ),
            slice = @Slice(
                    from = @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/client/option/GameOptions;inventoryKey:Lnet/minecraft/client/option/KeyBinding;",
                            opcode = Opcodes.GETFIELD
                    ),
                    to = @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/client/MinecraftClient;interactionManager:Lnet/minecraft/client/network/ClientPlayerInteractionManager;",
                            ordinal = 0,
                            opcode = Opcodes.GETFIELD
                    )
            )
    )
    private void onInventoryOpened(final CallbackInfo ci) {
        if (!Util.isFluent()) {
            return;
        }

        MinecraftClient client = (MinecraftClient) (Object) this;

        // Check if inventory is being opened
        if (client.currentScreen == null && !this.inventoryWasOpen) {
            // Store the current hotbar index before switching
            this.previousHotbarIndex = Util.getCurrentHotbarIndex();

            // Only switch if we're not already on the first hotbar
            if (this.previousHotbarIndex != 0) {
                // Switch to the first hotbar (index 0) when inventory is opened
                Util.setCurrentHotbarIndex(0);

                // Perform the swap to update the display
                Util.performMultiHotbarSwap(client, true);
            }

            this.inventoryWasOpen = true;
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void onScreenChange(Screen screen, CallbackInfo ci) {
        if (!Util.isFluent()) {
            return;
        }

        MinecraftClient client = (MinecraftClient) (Object) this;

        // Check if we're closing the inventory (screen becomes null and inventory was open)
        if (screen == null && this.inventoryWasOpen && this.previousHotbarIndex != -1) {
            // Only restore if we actually changed from the first hotbar
            if (this.previousHotbarIndex != 0) {
                // Restore the previous hotbar index when inventory is closed
                Util.setCurrentHotbarIndex(this.previousHotbarIndex);

                // Perform the swap to update the display
                Util.performMultiHotbarSwap(client, true);
            }

            // Reset the stored values
            this.previousHotbarIndex = -1;
            this.inventoryWasOpen = false;
        }
        // Check if we're opening the inventory
        else if (screen instanceof InventoryScreen && !this.inventoryWasOpen) {
            // Store the current hotbar index before switching
            this.previousHotbarIndex = Util.getCurrentHotbarIndex();

            // Only switch if we're not already on the first hotbar
            if (this.previousHotbarIndex != 0) {
                // Switch to the first hotbar (index 0) when inventory is opened
                Util.setCurrentHotbarIndex(0);

                // Perform the swap to update the display
                Util.performMultiHotbarSwap(client, true);
            }

            this.inventoryWasOpen = true;
        }
    }
}
