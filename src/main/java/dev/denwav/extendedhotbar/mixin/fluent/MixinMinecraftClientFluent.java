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

import dev.denwav.extendedhotbar.ExtendedHotbarState;
import dev.denwav.extendedhotbar.Util;
import net.minecraft.client.MinecraftClient;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClientFluent {

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
        // Using this instead of ScreenEvents.BEFORE_INIT because we need to do the swap before the inventory is
        // actually opened (before setScreen is called), specifically for horse inventories
        // If we wait until setScreen, or when that even is fired, then it's already too late and we can't do the swap.
        if (!Util.isFluent() || Util.getFluentPosition() == ExtendedHotbarState.Position.LEFT) {
            return;
        }

        Util.swapRenderedPosition();
        Util.performSwap(MinecraftClient.getInstance(), true);
    }
}
