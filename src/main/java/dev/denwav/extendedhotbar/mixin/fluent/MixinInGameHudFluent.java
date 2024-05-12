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
import com.llamalad7.mixinextras.sugar.Local;
import dev.denwav.extendedhotbar.ExtendedHotbarState.Position;
import dev.denwav.extendedhotbar.Util;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(InGameHud.class)
public abstract class MixinInGameHudFluent {

    @Shadow protected abstract void renderHotbarItem(DrawContext context, int x, int y, float f, PlayerEntity player, ItemStack stack, int seed);

    @Unique private int offset;

    @WrapOperation(
        method = "renderHotbar",
        at = @At(
            value = "INVOKE",
            ordinal = 0,
            target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"
        )
    )
    private void drawExtraHotbarBackground(
        final DrawContext context,
        final Identifier texture,
        final int x,
        final int y,
        final int u,
        final int v,
        final int width,
        final int height,
        final Operation<Void> original
    ) {
        if (!Util.isFluent()) {
            this.offset = 0;
            original.call(context, texture, x, y, u, v, width, height);
            return;
        }

        this.offset = width / 2;

        original.call(context, texture, x - this.offset, y, u, v, width, height);
        context.drawTexture(texture, x + this.offset, y, u, v, width, height);
    }

    @ModifyArg(
        method = "renderHotbar",
        at = @At(
            value = "INVOKE",
            ordinal = 1,
            target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"
        ),
        index = 1
    )
    private int drawHotbarSelection(final int x) {
        if (this.offset == 0) {
            return x;
        }

        final Position position = Util.getRenderedFluentPosition();
        return switch (position) {
            case LEFT -> x - this.offset;
            case RIGHT -> x + this.offset;
        };
    }

    @WrapOperation(
        method = "renderHotbar",
        at = @At(
            value = "INVOKE",
            ordinal = 0,
            target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IIFLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V"
        )
    )
    private void drawExtraHotbarItem(
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
        if (this.offset == 0) {
            original.call(instance, context, x, y, tickDelta, player, stack, seed);
            return;
        }

        final Position position = Util.getRenderedFluentPosition();

        final int originalX;
        final int newX;
        switch (position) {
            case LEFT -> {
                originalX = x - this.offset;
                newX = x + this.offset;
            }
            case RIGHT -> {
                originalX = x + this.offset;
                newX = x - this.offset;
            }
            default -> throw new IllegalStateException("unknown position");
        }

        original.call(instance, context, originalX, y, tickDelta, player, stack, seed);
        this.renderHotbarItem(context, newX, y, tickDelta, player, player.getInventory().main.get(loopIndex + Util.SLOT_OFFSET), seed);
    }

    @ModifyArg(
        method = "renderHotbar",
        at = @At(
            value = "INVOKE",
            ordinal = 2,
            target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"
        ),
        index = 1
    )
    private int drawOffhandItemBackgroundLeft(final int x) {
        return x - this.offset;
    }

    @ModifyArg(
        method = "renderHotbar",
        at = @At(
            value = "INVOKE",
            ordinal = 1,
            target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IIFLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V"
        ),
        index = 1
    )
    private int drawOffhandItemLeft(final int x) {
        return x - this.offset;
    }

    @ModifyArg(
        method = "renderHotbar",
        at = @At(
            value = "INVOKE",
            ordinal = 3,
            target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"
        ),
        index = 1
    )
    private int drawOffhandItemBackgroundRight(final int x) {
        return x + this.offset;
    }

    @ModifyArg(
        method = "renderHotbar",
        at = @At(
            value = "INVOKE",
            ordinal = 2,
            target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IIFLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V"
        ),
        index = 1
    )
    private int drawOffhandItemRight(final int x) {
        return x - this.offset;
    }
}
