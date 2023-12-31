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
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public abstract class MixinPlayerInventoryFluent {

    @Shadow public int selectedSlot;

    @Inject(
        method = "scrollInHotbar",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/player/PlayerInventory;selectedSlot:I",
            ordinal = 0,
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER
        )
    )
    private void swapOnScroll(final double scrollAmount, final CallbackInfo ci) {
        if (Util.isFluent()) {
            if (this.selectedSlot < 0 || this.selectedSlot >= 9) {
                Util.switchFluentPosition();
                Util.performSwap(MinecraftClient.getInstance(), true);
            }
        }
    }
}
