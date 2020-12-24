package com.demonwav.extendedhotbar.mixin;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CreativeInventoryScreen.class)
public interface MixinCreativeInventoryScreen {

    @Invoker
    void callSetSelectedTab(ItemGroup group);
}
