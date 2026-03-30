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
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public abstract class MixinPlayerInventoryFluent {

    @Shadow
    private int selectedSlot;

    @Inject(
            method = "setSelectedSlot",
            at = @At("HEAD")
    )
    private void onSetSelectedSlot(int slot, CallbackInfo ci) {
        if (!Util.isFluent()) {
            return;
        }

        // Check if the new slot is out of bounds (this happens during scrolling)
        if (slot < 0 || slot >= 9) {
            // Switch position and swap hotbars
            Util.switchFluentPosition();
            Util.performSwap(MinecraftClient.getInstance(), true);
        }
    }

    @Inject(
            method = "setSelectedSlot",
            at = @At("TAIL")
    )
    private void afterSetSelectedSlot(int slot, CallbackInfo ci) {
        if (!Util.isFluent()) {
            return;
        }

        // Normalize the selected slot back to 0-8 range if it went out of bounds
        if (this.selectedSlot < 0) {
            this.selectedSlot = 8;
        } else if (this.selectedSlot >= 9) {
            this.selectedSlot = 0;
        }
    }
}
