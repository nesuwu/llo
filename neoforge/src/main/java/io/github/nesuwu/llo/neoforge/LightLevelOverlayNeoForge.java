package io.github.nesuwu.llo.neoforge;

import io.github.nesuwu.llo.ClientConfig;
import io.github.nesuwu.llo.ClothConfigScreens;
import io.github.nesuwu.llo.LightLevelOverlayClient;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod("lightleveloverlay")
public class LightLevelOverlayNeoForge {

    public LightLevelOverlayNeoForge() {
        ClientConfig.ensureLoaded();
        LightLevelOverlayClient.init();
        NeoForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerExtensionPoint(
                IConfigScreenFactory.class,
                () -> (mc, parent) -> ClothConfigScreens.create(parent));
    }

    @SubscribeEvent
    public void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)
            return;
        LightLevelOverlayClient.onRenderWorld(
                event.getPoseStack(),
                event.getPartialTick().getRealtimeDeltaTicks(),
                event.getCamera());
    }
}
