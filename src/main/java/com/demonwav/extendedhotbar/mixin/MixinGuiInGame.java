package com.demonwav.extendedhotbar.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GuiIngame.class)
public class MixinGuiInGame extends Gui {

    @Overwrite
    protected void renderHotbar(ScaledResolution sr, float partialTicks) {
    }
}
