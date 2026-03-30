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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(InGameHud.class)
public abstract class MixinInGameHudFluent {

    @Shadow protected abstract void renderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed);

    @Unique private int hotbarWidth;
    @Unique private int totalHotbars;
    @Unique private int hotbarCenterX;
    @Unique private int[] hotbarPositions;

    @WrapOperation(
            method = "renderHotbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V",
                    ordinal = 0
            )
    )
    private void drawExtraHotbarBackground(
            DrawContext instance, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, Operation<Void> original, @Local(argsOnly = true) DrawContext context)
    {
        if (!Util.isFluent()) {
            this.hotbarWidth = 0;
            this.totalHotbars = 1;
            original.call(instance, pipeline, sprite, x, y, width, height);
            return;
        }

        this.totalHotbars = Math.min(4, Math.max(2, Util.configHolder.getConfig().numberOfHotbars));
        this.hotbarWidth = width; // 182 pixels
        this.hotbarCenterX = x + width / 2;

        // Calculate positions for all hotbars - spread them horizontally
        this.hotbarPositions = new int[this.totalHotbars];
        int totalWidth = this.hotbarWidth * this.totalHotbars;
        int startX = x - (totalWidth - this.hotbarWidth) / 2;

        for (int i = 0; i < this.totalHotbars; i++) {
            this.hotbarPositions[i] = startX + (i * this.hotbarWidth);
            context.drawGuiTexture(pipeline, sprite, this.hotbarPositions[i], y, width, height);
        }
    }

    @ModifyArg(
            method = "renderHotbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V",
                    ordinal = 1
            ),
            index = 3
    )
    private int drawHotbarSelection(final int y) {
        if (this.hotbarWidth == 0 || this.hotbarPositions == null) {
            return y;
        }

        // Get current hotbar index (0-based)
        int currentHotbarIndex = Util.getCurrentHotbarIndex();
        if (currentHotbarIndex < 0 || currentHotbarIndex >= this.totalHotbars) {
            currentHotbarIndex = 0;
        }

        // The selection indicator stays in the same vertical position
        return y;
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
            InGameHud instance, DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed, Operation<Void> original)
    {
        if (this.hotbarWidth == 0 || this.hotbarPositions == null || !Util.isFluent()) {
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

        // Get current hotbar for highlighting
        int currentHotbarIndex = Util.getCurrentHotbarIndex();

        // Render the same slot across all hotbars
        for (int hotbarIndex = 0; hotbarIndex < this.totalHotbars; hotbarIndex++) {
            // Calculate the x position for this hotbar
            int hotbarBaseX = this.hotbarPositions[hotbarIndex];
            int slotX = hotbarBaseX + (x - (this.hotbarCenterX - this.hotbarWidth / 2));

            // Get the item for this hotbar and slot
            final ItemStack itemToRender = getItemForHotbar(player, hotbarIndex, slotIndex);

            // Add visual distinction for the active hotbar
            int renderSeed = seed + (hotbarIndex * 100);
            if (hotbarIndex == currentHotbarIndex) {
                renderSeed += 1000; // Different seed for active hotbar to make it visually distinct
            }

            // Render the item
            this.renderHotbarItem(context, slotX, y, tickCounter, player, itemToRender, renderSeed);
        }

        // Mark that we've processed the hotbar change
        if (Util.hasHotbarJustChanged()) {
            Util.markHotbarChangeProcessed();
        }
    }

    @Unique
    private ItemStack getItemForHotbar(PlayerEntity player, int hotbarIndex, int slotIndex) {
        // Ensure we don't go out of bounds
        if (slotIndex < 0 || slotIndex >= 9) {
            return ItemStack.EMPTY;
        }

        int currentHotbarIndex = Util.getCurrentHotbarIndex();
        int inventorySlot;

        // The key insight: we need to show what SHOULD be in each hotbar position
        // based on their logical inventory positions, not their current swapped positions

        if (hotbarIndex == currentHotbarIndex) {
            // For the currently active hotbar, show what's actually in the hotbar slots
            // because that's what the player is using
            inventorySlot = slotIndex; // slots 0-8
        } else {
            // For inactive hotbars, we need to show what would be there if we switched to them
            // This means showing their "home" inventory positions

            // If the current hotbar is 0, then other hotbars show their natural positions
            if (currentHotbarIndex == 0) {
                inventorySlot = Util.getInventorySlotForHotbar(hotbarIndex, slotIndex);
            } else {
                // If current hotbar is not 0, we need to account for the fact that
                // the current hotbar's items are now in slots 0-8, and hotbar 0's items
                // are in the current hotbar's natural position
                if (hotbarIndex == 0) {
                    // Hotbar 0 should show what's currently in the active hotbar's natural position
                    inventorySlot = Util.getInventorySlotForHotbar(currentHotbarIndex, slotIndex);
                } else {
                    // Other inactive hotbars show their natural positions
                    inventorySlot = Util.getInventorySlotForHotbar(hotbarIndex, slotIndex);
                }
            }
        }

        if (inventorySlot == -1 || inventorySlot >= 36) {
            return ItemStack.EMPTY;
        }

        return player.getInventory().getStack(inventorySlot);
    }

    @ModifyArg(
            method = "renderHotbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V",
                    ordinal = 2
            ),
            index = 2
    )
    private int drawOffhandItemBackgroundLeft(final int x) {
        if (this.hotbarWidth == 0 || this.totalHotbars <= 1) {
            return x;
        }
        // Move offhand further left to make room for multiple hotbars
        int totalOffset = (this.hotbarWidth * (this.totalHotbars - 1)) / 2;
        return x - totalOffset;
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
        if (this.hotbarWidth == 0 || this.totalHotbars <= 1) {
            return x;
        }
        // Move offhand further left to make room for multiple hotbars
        int totalOffset = (this.hotbarWidth * (this.totalHotbars - 1)) / 2;
        return x - totalOffset;
    }

    @ModifyArg(
            method = "renderHotbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V",
                    ordinal = 3
            ),
            index = 2
    )
    private int drawOffhandItemBackgroundRight(final int x) {
        if (this.hotbarWidth == 0 || this.totalHotbars <= 1) {
            return x;
        }
        // Move offhand further right to make room for multiple hotbars
        int totalOffset = (this.hotbarWidth * (this.totalHotbars - 1)) / 2;
        return x + totalOffset;
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
        if (this.hotbarWidth == 0 || this.totalHotbars <= 1) {
            return x;
        }
        // Move offhand further right to make room for multiple hotbars
        int totalOffset = (this.hotbarWidth * (this.totalHotbars - 1)) / 2;
        return x + totalOffset;
    }
}
