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

import dev.denwav.extendedhotbar.Util;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static net.minecraft.client.gui.widget.ClickableWidget.WIDGETS_TEXTURE;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {

    @Shadow private int scaledWidth;
    @Shadow private int scaledHeight;

    @Shadow protected abstract void renderHotbarItem(DrawContext context, int x, int y, float f, PlayerEntity player, ItemStack stack, int seed);

    @Inject(
        method = "renderHotbar",
        at = @At(
            value = "INVOKE",
            shift = At.Shift.AFTER,
            ordinal = 0,
            target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"
        )
    )
    private void drawTopHotbarBackground(final float tickDelta, final DrawContext context, final CallbackInfo ci) {
        if (Util.isEnabled()) {
            final int i = this.scaledWidth / 2;
            context.drawTexture(WIDGETS_TEXTURE, i - 91, this.scaledHeight - 22 + Util.DISTANCE, 0, 0, 182, 22);
        }
    }

    @Inject(
        method = "renderHotbar",
        at = @At(
            value = "INVOKE",
            shift = At.Shift.AFTER,
            ordinal = 0,
            target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IIFLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V"
        ),
        locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private void drawTopHotbarItems(final float tickDelta, final DrawContext context, final CallbackInfo ci,
                                    // locals
                                    PlayerEntity player,
                                    ItemStack _itemStack, Arm _arm, int _i, int _j, int _k, // ignored locals
                                    int seed, int loopIndex, int x, int y) {
        if (Util.isEnabled()) {
            this.renderHotbarItem(context, x, y + Util.DISTANCE, tickDelta, player, player.getInventory().main.get(loopIndex + 27), seed);
        }
    }

    @Inject(
        id = "move",
        method = {
            "renderStatusBars",
            "renderHeldItemTooltip"
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
}
