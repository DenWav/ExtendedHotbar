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
import dev.denwav.extendedhotbar.ExtendedHotbarState.Position;
import dev.denwav.extendedhotbar.Util;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Function;

@Mixin(InGameHud.class)
public abstract class MixinInGameHudFluent {

    @Shadow protected abstract void renderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed);

    @Unique private int hotbarOffset;
    @Unique private int hotbarCenterX;

    @WrapOperation(
            method = "renderHotbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V",
                    ordinal = 0
            )
    )
    private void drawExtraHotbarBackground(
            final DrawContext context,
            final Function<Identifier, RenderLayer> renderLayerGetter,
            final Identifier texture,
            final int x,
            final int y,
            final int width,
            final int height,
            final Operation<Void> original
    ) {
        if (!Util.isFluent()) {
            this.hotbarOffset = 0;
            original.call(context, renderLayerGetter, texture, x, y, width, height);
            return;
        }

        // Set offset to the full width of the hotbar (182 pixels)
        this.hotbarOffset = width;
        this.hotbarCenterX = x + width / 2; // Store the center position

        // Always draw normal hotbar on the left
        context.drawGuiTexture(renderLayerGetter, texture, x - this.hotbarOffset / 2, y, width, height);

        // Always draw extended hotbar on the right
        context.drawGuiTexture(renderLayerGetter, texture, x + this.hotbarOffset / 2, y, width, height);
    }

    @ModifyArg(
            method = "renderHotbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V",
                    ordinal = 1
            ),
            index = 2
    )
    private int drawHotbarSelection(final int x) {
        if (this.hotbarOffset == 0) {
            return x;
        }

        // REVERSED: Selection indicator position depends on which hotbar is active
        final Position position = Util.getRenderedFluentPosition();
        return switch (position) {
            case LEFT -> x + this.hotbarOffset / 2;  // Selection on right hotbar (normal hotbar) - REVERSED
            case RIGHT -> x - this.hotbarOffset / 2; // Selection on left hotbar (extended hotbar) - REVERSED
        };
    }

    @WrapOperation(
            method = "renderHotbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V",
                    ordinal = 0
            )
    )
    private void drawExtraHotbarItem(
            final InGameHud instance,
            final DrawContext context,
            final int x,
            final int y,
            final RenderTickCounter tickCounter,
            final PlayerEntity player,
            final ItemStack stack,
            final int seed,
            final Operation<Void> original
    ) {
        if (this.hotbarOffset == 0) {
            original.call(instance, context, x, y, tickCounter, player, stack, seed);
            return;
        }

        // Calculate slot index from x position
        final int baseX = this.hotbarCenterX - 90 + 2; // Base position for slot 0
        final int slotIndex = (x - baseX) / 20; // Each slot is 20 pixels apart

        // Validate slot index
        if (slotIndex < 0 || slotIndex >= 9) {
            original.call(instance, context, x, y, tickCounter, player, stack, seed);
            return;
        }

        // Calculate positions for left and right hotbars
        final int leftHotbarX = x - this.hotbarOffset / 2;
        final int rightHotbarX = x + this.hotbarOffset / 2;

        // Get the actual hotbar item for this slot
        final ItemStack hotbarItem = player.getInventory().main.get(slotIndex);

        // Get the inventory row item for this slot
        final int inventorySlot = slotIndex + Util.SLOT_OFFSET; // SLOT_OFFSET = 27
        final ItemStack inventoryRowSlotItem;
        if (inventorySlot >= 27 && inventorySlot <= 35) {
            inventoryRowSlotItem = player.getInventory().main.get(inventorySlot);
        } else {
            inventoryRowSlotItem = ItemStack.EMPTY;
        }

        // Get current position to determine which hotbar is active
        final Position currentPosition = Util.getRenderedFluentPosition();

        // REVERSED: Render based on current position:
        // When LEFT: normal hotbar on right (active), inventory row on left (inactive)
        // When RIGHT: inventory row on right (active), normal hotbar on left (inactive)
        if (currentPosition == Position.LEFT) {
            // Normal hotbar is active (right side) - REVERSED
            this.renderHotbarItem(context, leftHotbarX, y, tickCounter, player, inventoryRowSlotItem, seed + 100);
            this.renderHotbarItem(context, rightHotbarX, y, tickCounter, player, hotbarItem, seed);
        } else {
            // Inventory row is active (right side) - REVERSED
            this.renderHotbarItem(context, leftHotbarX, y, tickCounter, player, hotbarItem, seed);
            this.renderHotbarItem(context, rightHotbarX, y, tickCounter, player, inventoryRowSlotItem, seed + 100);
        }
    }

    @ModifyArg(
            method = "renderHotbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V",
                    ordinal = 2
            ),
            index = 2
    )
    private int drawOffhandItemBackgroundLeft(final int x) {
        if (this.hotbarOffset == 0) {
            return x;
        }
        // Move offhand further left to make room for the left hotbar
        return x - this.hotbarOffset / 2;
    }

    @ModifyArg(
            method = "renderHotbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V",
                    ordinal = 1
            ),
            index = 1
    )
    private int drawOffhandItemLeft(final int x) {
        if (this.hotbarOffset == 0) {
            return x;
        }
        // Move offhand further left to make room for the left hotbar
        return x - this.hotbarOffset / 2;
    }

    @ModifyArg(
            method = "renderHotbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V",
                    ordinal = 3
            ),
            index = 2
    )
    private int drawOffhandItemBackgroundRight(final int x) {
        if (this.hotbarOffset == 0) {
            return x;
        }
        // Move offhand further right to make room for the right hotbar
        return x + this.hotbarOffset / 2;
    }

    @ModifyArg(
            method = "renderHotbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V",
                    ordinal = 2
            ),
            index = 1
    )
    private int drawOffhandItemRight(final int x) {
        if (this.hotbarOffset == 0) {
            return x;
        }
        // Move offhand further right to make room for the right hotbar
        return x + this.hotbarOffset / 2;
    }
}
