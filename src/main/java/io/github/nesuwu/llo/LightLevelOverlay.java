package io.github.nesuwu.llo;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(LightLevelOverlay.MODID)
public class LightLevelOverlay {

    public static final String MODID = "lightleveloverlay";

    public LightLevelOverlay(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::onKeyRegister);

        ClientConfigFile.ensureLoaded();
        modContainer.registerExtensionPoint(
            IConfigScreenFactory.class,
            (mc, parent) -> ClothConfigScreens.create(parent)
        );
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(new LightLevelOverlayClient());
    }

    private void onKeyRegister(final RegisterKeyMappingsEvent event) {
        event.register(LightLevelOverlayClient.getToggleOverlayKey());
        event.register(LightLevelOverlayClient.getOpenConfigKey());
    }
}
