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

package dev.denwav.extendedhotbar.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.denwav.extendedhotbar.Util;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {

    @Shadow protected abstract void renderHotbarItem(DrawContext context, int x, int y, float f, PlayerEntity player, ItemStack stack, int seed);

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

        if (Util.isEnabled()) {
            context.drawGuiTexture(texture, x, y + Util.DISTANCE, width, height);
        }
    }

    @WrapOperation(
        method = "renderHotbar",
        at = @At(
            value = "INVOKE",
            ordinal = 0,
            target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IIFLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V"
        )
    )
    private void drawTopHotbarItem(
        final InGameHud instance,
        final DrawContext context,
        final int x,
        final int y,
        final float tickDelta,
        final PlayerEntity player,
        final ItemStack stack,
        final int seed,
        final Operation<Void> original,
        @Local(ordinal = 4) final int loopIndex
    ) {
        original.call(instance, context, x, y, tickDelta, player, stack, seed);

        if (Util.isEnabled()) {
            this.renderHotbarItem(context, x, y + Util.DISTANCE, tickDelta, player, player.getInventory().main.get(loopIndex + 27), seed);
        }
    }

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            shift = At.Shift.AFTER,
            ordinal = 0,
            target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"
        )
    )
    private void moveActionBarTextUp(final DrawContext context, final float tickDelta, final CallbackInfo ci) {
        // We don't need to push a matrix or reset, because the surrounding code we are injecting in
        // to does that for us.
        if (Util.isEnabled()) {
            context.getMatrices().translate(0, Util.DISTANCE, 0);
        }
    }

    @Inject(
        id = "move",
        method = {
            "renderMountHealth",
            "renderStatusBars",
            "renderHeldItemTooltip",
        },
        at = {
            @At(value = "HEAD", id = "head"),
            @At(value = "RETURN", id = "return")
        }
    )
    private void moveHud(final DrawContext context, final CallbackInfo ci) {
        if ("move:head".equals(ci.getId())) {
            Util.moveUp(context.getMatrices());
        } else {
            Util.reset(context.getMatrices());
        }
    }

    @Inject(
        id = "move",
        method = "renderExperienceBar",
        at = {
            @At(value = "HEAD", id = "head"),
            @At(value = "RETURN", id = "return")
        }
    )
    private void moveExpBar(final DrawContext context, final int x, final CallbackInfo ci) {
        if ("move:head".equals(ci.getId())) {
            Util.moveUp(context.getMatrices());
        } else {
            Util.reset(context.getMatrices());
        }
    }

    @Inject(
        id = "move",
        method = "renderMountJumpBar",
        at = {
            @At(value = "HEAD", id = "head"),
            @At(value = "RETURN", id = "return")
        }
    )
    private void moveMountJumpBarUp(final JumpingMount mount, final DrawContext context, final int x, final CallbackInfo ci) {
        if ("move:head".equals(ci.getId())) {
            Util.moveUp(context.getMatrices());
        } else {
            Util.reset(context.getMatrices());
        }
    }
}
