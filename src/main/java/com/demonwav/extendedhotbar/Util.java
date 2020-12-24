package com.demonwav.extendedhotbar;

import net.minecraft.client.util.math.MatrixStack;

public final class Util {

    public static final int DISTANCE = -22;

    public static boolean enabled = true;

    private Util() {}

    public static void moveUp(MatrixStack matrixStack) {
        if (enabled) {
            matrixStack.push();
            matrixStack.translate(0, DISTANCE, 0);
        }
    }

    public static void reset(MatrixStack matrixStack) {
        if (enabled) {
            matrixStack.pop();
        }
    }
}
