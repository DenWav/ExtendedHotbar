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
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MixinMouseFluent {

    @Shadow @Final private MinecraftClient client;

    @Inject(
            method = "onMouseScroll",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerInventory;setSelectedSlot(I)V"
            )
    )
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (!Util.isFluent()) {
            return;
        }

        if (this.client.player == null) {
            return;
        }

        // Get the current selected slot before it changes
        int currentSlot = this.client.player.getInventory().getSelectedSlot();

        // Check if we would scroll past the hotbar bounds
        if ((currentSlot == 0 && vertical > 0) || (currentSlot == 8 && vertical < 0)) {
            // Switch to next/previous hotbar based on scroll direction
            if (vertical > 0) {
                Util.switchToPreviousHotbar();
            } else {
                Util.switchToNextHotbar();
            }

            // Only perform swap when actually switching hotbars, not just changing the index
            Util.performMultiHotbarSwap(this.client, true);
        }
    }
}
