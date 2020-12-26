package com.demonwav.extendedhotbar;

import me.sargunvohra.mcmods.autoconfig1u.ConfigHolder;
import net.minecraft.client.util.math.MatrixStack;

public final class Util {

    public static final int DISTANCE = -22;

    public static ConfigHolder<ModConfig> configHolder = null;

    private Util() {}

    public static boolean isEnabled() {
        return configHolder != null && configHolder.getConfig().enabled;
    }

    public static void moveUp(MatrixStack matrixStack) {
        if (isEnabled()) {
            matrixStack.push();
            matrixStack.translate(0, DISTANCE, 0);
        }
    }

    public static void reset(MatrixStack matrixStack) {
        if (isEnabled()) {
            matrixStack.pop();
        }
    }
}
