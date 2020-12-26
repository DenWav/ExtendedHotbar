package com.demonwav.extendedhotbar.mixin;

import com.demonwav.extendedhotbar.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Shadow public Screen currentScreen;
    @Shadow public HitResult crosshairTarget;
    @Shadow public ClientWorld world;
    @Shadow public ClientPlayerEntity player;

    @Inject(
        method = "doItemPick",
        at = @At("HEAD")
    )
    private void beforeDoItemPick(final CallbackInfo ci) {
        if (!Util.isEnabled()) {
            return;
        }

        if (this.currentScreen != null) {
            return;
        }

        final HitResult target = this.crosshairTarget;
        if (target == null || target.getType() != HitResult.Type.BLOCK) {
            return;
        }

        final BlockPos pos = ((BlockHitResult) target).getBlockPos();
        final BlockState blockState = this.world.getBlockState(pos);
        final Block block = blockState.getBlock();
        if (blockState.isAir()) {
            return;
        }

        // If the block is in the hotbar, we do nothing and let Minecraft do it's thing
        final PlayerInventory inventory = this.player.inventory;
        for (int i = 0; i < 9; i++) {
            // While LEFT_HOTBAR_SLOT_INDEX is the base index for hotbar slots in the inventory gui, in InventoryPlayer,
            // the hotbar starts at 0
            final Item item = inventory.getStack(i).getItem();
            final Block blockFromItem = Block.getBlockFromItem(item);

            if (block == blockFromItem) {
                return;
            }
        }

        // If the block is in the bottom row and not in the hotbar, we need to emulate Minecraft's default behavior
        // We do this by swapping the rows and then letting Minecraft go from there
        // It'll find the item in the hotbar and move the selection to that item accordingly
        for (int i = 0; i < 9; i++) {
            final Item item = inventory.getStack(i + Util.LEFT_BOTTOM_ROW_SLOT_INDEX).getItem();
            final Block blockFromItem = Block.getBlockFromItem(item);

            if (block != blockFromItem) {
                continue;
            }

            Util.performSwap((MinecraftClient) (Object) this, true);
            break;
        }
    }
}
