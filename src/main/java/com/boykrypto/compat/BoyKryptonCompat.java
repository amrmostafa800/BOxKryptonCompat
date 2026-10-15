package com.boykrypto.compat;

import net.minecraftforge.fml.common.Mod;

/**
 * Entrypoint. Forge 1.20.1 requires an @Mod class for the mod to be constructed;
 * all behaviour lives in the mixin, so there is nothing to do here.
 */
@Mod(BoyKryptonCompat.MOD_ID)
public final class BoyKryptonCompat {

    public static final String MOD_ID = "boykryptoncompat";

    public BoyKryptonCompat() {
    }
}
