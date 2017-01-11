package com.demonwav.extendedhotbar.mixin;

import com.mumfrey.liteloader.core.LiteLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.mumfrey.liteloader.gl.GL.*;

import com.demonwav.extendedhotbar.LiteModExtendedHotbar;

@Mixin(GuiIngame.class)
public abstract class MixinGuiInGame extends Gui {

    private static final int distance = -22;

    @Shadow @Final private static ResourceLocation WIDGETS_TEX_PATH;

    @Shadow @Final private Minecraft mc;
    @Shadow protected abstract void renderHotbarItem(int p_184044_1_, int p_184044_2_, float p_184044_3_, EntityPlayer player, ItemStack stack);

    @Inject(method = "renderHotbar", at = @At("RETURN"))
    public void drawTopHotbar(ScaledResolution sr, float partialTicks, CallbackInfo info) {
        if (!LiteLoader.getInstance().getMod(LiteModExtendedHotbar.class).isEnabled()) {
            return;
        }

        final EntityPlayer entityplayer = (EntityPlayer) this.mc.getRenderViewEntity();
        if (entityplayer == null) {
            return;
        }

        final int i = sr.getScaledWidth() / 2;

        glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(WIDGETS_TEX_PATH);
        glEnableBlend();
        drawTexturedModalRect(i - 91, sr.getScaledHeight() - 22 + distance, 0, 0, 182, 22);

        glEnableRescaleNormal();
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        RenderHelper.enableGUIStandardItemLighting();

        for (int l = 0; l < 9; ++l) {
            int i1 = i - 90 + l * 20 + 2;
            int j1 = sr.getScaledHeight() - 16 - 3 + distance;
            this.renderHotbarItem(i1, j1, partialTicks, entityplayer, entityplayer.inventory.mainInventory.get(l + 27));
        }

        RenderHelper.disableStandardItemLighting();
        glDisableRescaleNormal();
        glDisableBlend();
    }

    private void moveUp() {
        if (LiteLoader.getInstance().getMod(LiteModExtendedHotbar.class).isEnabled()) {
            glPushMatrix();
            glTranslated(0, distance, 0);
        }
    }

    private void reset() {
        if (LiteLoader.getInstance().getMod(LiteModExtendedHotbar.class).isEnabled()) {
            glPopMatrix();
        }
    }

    @Inject(method = "renderPlayerStats", at = @At("HEAD"))
    public void movePlayerStatsUp(ScaledResolution scaledRes, CallbackInfo info) {
        moveUp();
    }

    @Inject(method = "renderPlayerStats", at = @At("RETURN"))
    public void resetPlayerStats(ScaledResolution scaledRes, CallbackInfo info) {
        reset();
    }

    @Inject(method = "renderExpBar", at = @At("HEAD"))
    public void moveExpBarUp(ScaledResolution scaledRes, int x, CallbackInfo info) {
        moveUp();
    }

    @Inject(method = "renderExpBar", at = @At("RETURN"))
    public void resetExpBar(ScaledResolution scaledRes, int x, CallbackInfo info) {
        reset();
    }

    @Inject(method = "renderSelectedItem", at = @At("HEAD"))
    public void moveItemTextUp(ScaledResolution scaledRes, CallbackInfo info) {
        moveUp();
    }

    @Inject(method = "renderSelectedItem", at = @At("RETURN"))
    public void resetItemTExt(ScaledResolution scaledRes, CallbackInfo info) {
        reset();
    }

    @ModifyArg(method = "renderGameOverlay", index = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawString(Ljava/lang/String;III)I"))
    protected int moveActionBarText(int y) {
        if (LiteLoader.getInstance().getMod(LiteModExtendedHotbar.class).isEnabled()) {
            return y + distance;
        } else {
            return y;
        }
    }
}
