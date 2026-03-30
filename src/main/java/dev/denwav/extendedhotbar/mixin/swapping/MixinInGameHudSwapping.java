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
import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.denwav.extendedhotbar.Util;
import net.minecraft.client.gui.DrawContext;
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

    @Shadow
    protected abstract void renderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed);

    @WrapOperation(
            method = "renderHotbar",
            at = @At(
                    value = "INVOKE",
                    ordinal = 0,
                    target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V"
            )
    )
    private void drawTopHotbarBackground(
            DrawContext instance, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, Operation<Void> original, @Local(argsOnly = true) DrawContext context
    ) {
        // Draw the normal hotbar background at its original position
        original.call(instance, pipeline, sprite, x, y, width, height);

        if (Util.isSwappingEnabled()) {
            // Draw the bottom row hotbar background below the normal hotbar
            context.drawGuiTexture(pipeline, sprite, x, y - Util.DISTANCE + 2, width, height);
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
            InGameHud instance, DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed, Operation<Void> original, @Local(ordinal = 4) int loopIndex
    ) {
        // Draw the normal hotbar items at their original position
        original.call(instance, context, x, y, tickCounter, player, stack, seed);

        if (Util.isSwappingEnabled()) {
            // Draw the bottom row items below the normal hotbar
            this.renderHotbarItem(context, x, y - Util.DISTANCE, tickCounter, player, player.getInventory().getStack(loopIndex + Util.SLOT_OFFSET), seed);
        }
    }

    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (Util.isSwappingEnabled()) {
            Util.moveUp(context.getMatrices());
        }
    }

    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void afterRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (Util.isSwappingEnabled()) {
            Util.reset(context.getMatrices());
        }
    }
}



