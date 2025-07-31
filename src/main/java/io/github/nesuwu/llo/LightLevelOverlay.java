package io.github.nesuwu.llo;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@Mod(LightLevelOverlay.MODID)
public class LightLevelOverlay {
    public static final String MODID = "lightleveloverlay";

    public LightLevelOverlay(IEventBus modEventBus) {
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::onKeyRegister);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(new LightLevelOverlayClient());
    }

    private void onKeyRegister(final RegisterKeyMappingsEvent event) {
        event.register(LightLevelOverlayClient.getToggleOverlayKey());
    }
}