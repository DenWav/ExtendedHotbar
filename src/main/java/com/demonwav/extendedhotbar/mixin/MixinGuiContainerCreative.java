package com.demonwav.extendedhotbar.mixin;

import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.creativetab.CreativeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiContainerCreative.class)
public interface MixinGuiContainerCreative {

    @Invoker
    void callSetCurrentCreativeTab(CreativeTabs tab);
}
