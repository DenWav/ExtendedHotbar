package com.demonwav.extendedhotbar.mixin;

import static com.demonwav.extendedhotbar.Util.moveUp;
import static com.demonwav.extendedhotbar.Util.reset;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.minecraftforge.client.GuiIngameForge")
public abstract class MixinGuiInGameForge extends GuiIngame {

    public MixinGuiInGameForge(final Minecraft mcIn) {
        super(mcIn);
    }

    @Inject(
        method = "Lnet/minecraftforge/client/GuiIngameForge;renderGameOverlay(F)V",
        at = {
            @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;renderBossHealth()V", shift = At.Shift.BY, by = 2),
            @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;renderSleepFade(II)V", shift = At.Shift.AFTER)
        }
    )
    private void moveGuiUp(float partialTicks, final CallbackInfo info) {
        moveUp();
    }

    @Inject(
        method = "Lnet/minecraftforge/client/GuiIngameForge;renderGameOverlay(F)V", at = {
            @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;renderSleepFade(II)V"),
            @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;renderFPSGraph()V")
        }
    )
    private void moveGuiDown(float partialTicks, final CallbackInfo info) {
        reset();
    }
}
