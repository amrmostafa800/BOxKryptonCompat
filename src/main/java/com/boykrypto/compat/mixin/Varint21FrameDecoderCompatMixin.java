package com.boykrypto.compat.mixin;

import com.PinkCats.bandwidthoptimizer.connection.ConnectionInternetProbeGuard;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Varint21FrameDecoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Runs BandwidthOptimizer's internet-probe guard before Krypton Reno's frame decoder.
 *
 * Both mods inject at HEAD of Varint21FrameDecoder.decode, and Krypton Reno's injection
 * cancels unconditionally, so BO's injection at the same point never executes. Same-point
 * HEAD injections execute in ascending mixin priority order, so a priority below Krypton
 * Reno's (1000) makes this injection run first. BO's guard closes the connection for HTTP
 * and SSH probes, and if it rejects, cancel so Krypton Reno's decoder does not also process
 * the buffer. When BO does not reject, no cancel happens and Krypton Reno's optimized
 * decoder proceeds normally.
 */
@Mixin(value = Varint21FrameDecoder.class, priority = 500)
public abstract class Varint21FrameDecoderCompatMixin {

    @Inject(method = "decode", at = @At("HEAD"), cancellable = true)
    private void boykryptoncompat$probeGuardFirst(
            ChannelHandlerContext context,
            ByteBuf input,
            List<Object> output,
            CallbackInfo ci
    ) {
        if (ConnectionInternetProbeGuard.tryReject(context, input)) {
            ci.cancel();
        }
    }
}
