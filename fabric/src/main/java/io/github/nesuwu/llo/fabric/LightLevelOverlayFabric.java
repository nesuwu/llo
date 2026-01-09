package io.github.nesuwu.llo.fabric;

import io.github.nesuwu.llo.ClientConfig;
import io.github.nesuwu.llo.LightLevelOverlayClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class LightLevelOverlayFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientConfig.ensureLoaded();
        LightLevelOverlayClient.init();

        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> LightLevelOverlayClient.onRenderWorld(
                context.matrixStack(),
                context.tickCounter().getRealtimeDeltaTicks(),
                context.camera()));
    }
}
