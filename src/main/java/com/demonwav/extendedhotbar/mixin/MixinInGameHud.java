package com.demonwav.extendedhotbar.mixin;

import com.demonwav.extendedhotbar.Util;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud extends DrawableHelper {

    @Shadow private int scaledWidth;
    @Shadow private int scaledHeight;

    @Shadow protected abstract void renderHotbarItem(final int i, final int j, final float f, final PlayerEntity playerEntity, final ItemStack itemStack);

    @Inject(
        method = "renderHotbar",
        at = @At(
            value = "INVOKE",
            shift = At.Shift.AFTER,
            ordinal = 0,
            target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"
        )
    )
    private void drawTopHotbarBackground(final float f, final MatrixStack matrixStack, final CallbackInfo ci) {
        if (Util.enabled) {
            final int i = this.scaledWidth / 2;
            this.drawTexture(matrixStack, i - 91, this.scaledHeight - 22 + Util.DISTANCE, 0, 0, 182, 22);
        }
    }

    @Inject(
        method = "renderHotbar",
        at = @At(
            value = "INVOKE",
            shift = At.Shift.AFTER,
            ordinal = 0,
            target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(IIFLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V"
        ),
        locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private void drawTopHotbarItems(final float partialTicks, final MatrixStack matrixStack, final CallbackInfo ci,
                                    // locals
                                    PlayerEntity player,
                                    ItemStack _0, Arm _1, int _2, int _3, // ignored locals
                                    int loopIndex, int x, int y) {
        if (Util.enabled) {
            this.renderHotbarItem(x, y + Util.DISTANCE, partialTicks, player, player.inventory.main.get(loopIndex + 27));
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
    private void moveHud(final MatrixStack matrixStack, final CallbackInfo ci) {
        if ("move:head".equals(ci.getId())) {
            Util.moveUp(matrixStack);
        } else {
            Util.reset(matrixStack);
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
    private void moveExpBar(final MatrixStack matrices, final int x, final CallbackInfo ci) {
        if ("move:head".equals(ci.getId())) {
            Util.moveUp(matrices);
        } else {
            Util.reset(matrices);
        }
    }

    @ModifyArg(method = "render", index = 3, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I"))
    private float moveActionBarText(final float y) {
        if (Util.enabled) {
            return y + Util.DISTANCE;
        } else {
            return y;
        }
    }
}
