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

package dev.denwav.extendedhotbar.mixin.swapping;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.denwav.extendedhotbar.Util;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class MixinInGameHudSwapping {

    @Shadow protected abstract void renderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed);

    @WrapOperation(
        method = "renderHotbar",
        at = @At(
            value = "INVOKE",
            ordinal = 0,
            target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"
        )
    )
    private void drawTopHotbarBackground(
        final DrawContext context,
        final Identifier texture,
        final int x,
        final int y,
        final int width,
        final int height,
        final Operation<Void> original
    ) {
        original.call(context, texture, x, y, width, height);

        if (Util.isSwappingEnabled()) {
            context.drawGuiTexture(texture, x, y + Util.DISTANCE, width, height);
        }
    }

    @WrapOperation(
        method = "renderHotbar",
        at = @At(
            value = "INVOKE",
            ordinal = 0,
            target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V"
        )
    )
    private void drawTopHotbarItem(
        final InGameHud instance,
        final DrawContext context,
        final int x,
        final int y,
        final RenderTickCounter tickCounter,
        final PlayerEntity player,
        final ItemStack stack,
        final int seed,
        final Operation<Void> original,
        @Local(ordinal = 4) final int loopIndex
    ) {
        original.call(instance, context, x, y, tickCounter, player, stack, seed);

        if (Util.isSwappingEnabled()) {
            this.renderHotbarItem(context, x, y + Util.DISTANCE, tickCounter, player, player.getInventory().main.get(loopIndex + Util.SLOT_OFFSET), seed);
        }
    }

    @Inject(
        method = "renderOverlayMessage",
        at = @At(
            value = "INVOKE",
            shift = At.Shift.AFTER,
            ordinal = 0,
            target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"
        )
    )
    private void moveActionBarTextUp(final CallbackInfo ci, @Local(argsOnly = true) final DrawContext context) {
        // We don't need to push a matrix or reset, because the surrounding code we are injecting in
        // to does that for us.
        if (Util.isSwappingEnabled()) {
            context.getMatrices().translate(0, Util.DISTANCE, 0);
        }
    }

    @Inject(method = "renderMainHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;getScaledWindowWidth()I", ordinal = 0))
    private void moveHudUp(final CallbackInfo ci, @Local(argsOnly = true) final DrawContext context) {
        if (Util.isSwappingEnabled()) {
            Util.moveUp(context.getMatrices());
        }
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/LayeredDrawer;addLayer(Lnet/minecraft/client/gui/LayeredDrawer$Layer;)Lnet/minecraft/client/gui/LayeredDrawer;", ordinal = 3))
    private LayeredDrawer moveHudDown(final LayeredDrawer instance, final LayeredDrawer.Layer layer, final Operation<LayeredDrawer> original) {
        return original.call(original.call(instance, layer), (LayeredDrawer.Layer) (context, tickCounter) -> {
            if (Util.isSwappingEnabled()) {
                Util.reset(context.getMatrices());
            }
		});
    }
}
