package com.boykrypto.compat;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Applies the compat mixin only when both BandwidthOptimizer and Krypton Reno are present.
 * The mixin references a BandwidthOptimizer class, so it must never load without BO installed.
 */
public final class BoyKryptonMixinPlugin implements IMixinConfigPlugin {

    private boolean shouldApply;

    @Override
    public void onLoad(String mixinPackage) {
        // ModList is not populated yet when Mixin configs load, so detect by class presence.
        // Never link these classes here: referring to one initializes it.
        boolean bo = classPresent("com.PinkCats.bandwidthoptimizer.connection.ConnectionInternetProbeGuard");
        boolean kr = classPresent("one.pkg.kreno.mixin.network.pipeline.Varint21FrameDecoderMixin");
        shouldApply = bo && kr;
        System.out.println("[BOxKryptonCompat] bandwidthoptimizer=" + bo + " kreno=" + kr
                + " -> compat mixin " + (shouldApply ? "ENABLED" : "disabled"));
    }

    private static boolean classPresent(String name) {
        try {
            Class.forName(name, false, BoyKryptonMixinPlugin.class.getClassLoader());
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return shouldApply;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
