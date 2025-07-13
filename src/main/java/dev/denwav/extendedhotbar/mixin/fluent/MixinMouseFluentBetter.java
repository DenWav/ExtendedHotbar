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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.denwav.extendedhotbar.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mouse.class)
public class MixinMouseFluentBetter {

    @Shadow @Final private MinecraftClient client;

    @WrapOperation(
        method = "onMouseScroll",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerInventory;setSelectedSlot(I)V"
        )
    )
    private void wrapSetSelectedSlot(PlayerInventory inventory, int newSlot, Operation<Void> original) {
        if (!Util.isFluent()) {
            original.call(inventory, newSlot);
            return;
        }

        int currentSlot = inventory.selectedSlot;

        // Check if we're trying to scroll past the bounds
        if (newSlot < 0 || newSlot >= 9) {
            // Switch position and swap hotbars
            Util.switchFluentPosition();
            Util.performSwap(this.client, true);

            // Normalize the slot to stay within bounds
            if (newSlot < 0) {
                newSlot = 8;
            } else if (newSlot >= 9) {
                newSlot = 0;
            }
        }

        original.call(inventory, newSlot);
    }
}
