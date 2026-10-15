# BOxKryptonCompat

Keeps [BandwidthOptimizer](https://github.com/duckgun13476/BandwidthOptimizer) and
[Krypton Reno](https://github.com/404Setup/KryptonReno) working together on Minecraft 1.20.1 Forge.

## The problem

Both mods optimize the same network pipeline, and one silently disables part of the other:

- **Krypton Reno** injects into `Varint21FrameDecoder.decode` at `HEAD` and cancels unconditionally.
- **BandwidthOptimizer** injects at the same point (its `InternetProbeFrameDecoderMixin` calls
  `ConnectionInternetProbeGuard.tryReject`).

Because Krypton Reno's injection always cancels, BandwidthOptimizer's injection at the same point
**never executes**, so BO's internet-probe guard is silently disabled. BandwidthOptimizer's own
documentation lists Krypton Reno as incompatible.

## The fix

A single injection at `HEAD` of `Varint21FrameDecoder.decode` with mixin priority **500** (lower
than Krypton Reno's default `1000`). Same-point `HEAD` injections run in ascending priority order,
so the resulting order is:

```
compat (500)  ->  Krypton Reno (1000)  ->  BandwidthOptimizer (1000)
```

The compat injection runs BandwidthOptimizer's `ConnectionInternetProbeGuard.tryReject(...)` first.
If it rejects an HTTP/SSH probe it cancels; otherwise no cancel happens and Krypton Reno's optimized
decoder runs normally. **No code from either mod is copied or replaced** - Krypton Reno keeps all of
its framing, compression and micro-optimizations, and BandwidthOptimizer keeps its transport.

A mixin config plugin keeps the compat mixin inert unless both `bandwidthoptimizer` and `kreno` are
loaded, so installing this mod by itself is harmless.

## Install

Drop the jar in `mods/` on **both the server and every client**:

```
mods/
  BandwidthOptimizer-5.10.32.139-forge-1.20.1.jar
  Krypton Reno-forge-1.20.1-26.1.1-1.20.1.jar
  boykryptoncompat-1.0.0.jar
```

No config and no JVM flags are needed.

> Note: BandwidthOptimizer's transport is client-required. Every player needs BO and this compat
> mod, otherwise their client may not receive the world.

## Download

Grab the jar from the [latest release](../../releases/latest), or build it yourself:

```
./gradlew build
# output: build/libs/boykryptoncompat-<version>.jar
```

Building needs JDK 17. `libs/BO-139.jar` is only needed for compilation (`compileOnly`); it is not
bundled and not redistributed.

## Verifying it works

Check the log on the server and client:

```
[BOxKryptonCompat] bandwidthoptimizer=true kreno=true -> compat mixin ENABLED
```

When only one of the two mods is present, it reads `compat mixin disabled`.

## Tested

Server-side, Forge 1.20.1-47.4.28:

| Configuration | Result |
|---|---|
| BO + KR, no compat | BO's probe guard is dead (HTTP probe hangs, 8s timeout) |
| BO + KR + compat | BO's probe guard restored (HTTP probe closed in 0.23s) |
| KR only | boots, no crash |
| BO only | probe guard native |
| compat only / compat + one mod | boots, compat mixin auto-disabled |

Bytecode was exported with `-Dmixin.debug.export=true` and the merged
`Varint21FrameDecoder` decompiled to confirm the injection order above.

## Not yet verified

A real Forge **client** run. Validation here used a protocol client, server-side only. On the client,
confirm the world loads (not empty) and that `latest.log` shows the compat line above.

## Requirements

- Minecraft 1.20.1, Forge 47.x
- BandwidthOptimizer 5.10.x
- Krypton Reno 26.x

## License

LGPL-3.0-or-later. See [LICENSE](LICENSE).
