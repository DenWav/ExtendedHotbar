package com.demonwav.extendedhotbar;

import static com.mumfrey.liteloader.gl.GL.glPopMatrix;
import static com.mumfrey.liteloader.gl.GL.glPushMatrix;
import static com.mumfrey.liteloader.gl.GL.glTranslated;

import com.mumfrey.liteloader.core.LiteLoader;

public final class Util {

    private static LiteModExtendedHotbar mod = null;

    public static final int DISTANCE = -22;

    private Util() {}

    /*
     * This likely doesn't need to be thread-safe.
     */
    public static LiteModExtendedHotbar getMod() {
        LiteModExtendedHotbar m = Util.mod;
        if (m != null) {
            return m;
        }

        synchronized (Util.class) {
            m = Util.mod;
            if (m != null) {
                return m;
            }

            m = LiteLoader.getInstance().getMod(LiteModExtendedHotbar.class);
            Util.mod = m;
        }

        return m;
    }

    public static void moveUp() {
        if (getMod().isEnabled()) {
            glPushMatrix();
            glTranslated(0, DISTANCE, 0);
        }
    }

    public static void reset() {
        if (getMod().isEnabled()) {
            glPopMatrix();
        }
    }
}
