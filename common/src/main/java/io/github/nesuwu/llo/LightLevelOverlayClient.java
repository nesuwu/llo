package io.github.nesuwu.llo;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

public class LightLevelOverlayClient {

    public static boolean overlayEnabled = false;
    private static final LightCache lightCache = new LightCache();
    private static final double MAX_RENDER_DIST_SQ = 64.0 * 64.0;

    private static final KeyMapping TOGGLE_KEY = new KeyMapping(
            "key.lightleveloverlay.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F9,
            "key.categories.lightleveloverlay");

    private static final KeyMapping CONFIG_KEY = new KeyMapping(
            "key.lightleveloverlay.open_config",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F10,
            "key.categories.lightleveloverlay");

    public static void init() {
        KeyMappingRegistry.register(TOGGLE_KEY);
        KeyMappingRegistry.register(CONFIG_KEY);
        ClientTickEvent.CLIENT_POST.register(LightLevelOverlayClient::onClientTick);
    }

    public static KeyMapping getToggleKey() {
        return TOGGLE_KEY;
    }

    public static KeyMapping getConfigKey() {
        return CONFIG_KEY;
    }

    private static void onClientTick(Minecraft mc) {
        while (TOGGLE_KEY.consumeClick()) {
            overlayEnabled = !overlayEnabled;
            if (!overlayEnabled)
                lightCache.clear();
        }
        while (CONFIG_KEY.consumeClick()) {
            mc.setScreen(ClothConfigScreens.create(mc.screen));
        }
    }

    public static void onRenderWorld(PoseStack poseStack, float partialTick, Camera camera) {
        if (!overlayEnabled)
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null)
            return;

        updateCacheIfNeeded(mc);

        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        Vec3 cameraPos = camera.getPosition();

        renderOverlay(mc, poseStack, buffer, camera, cameraPos);
        buffer.endBatch();
    }

    private static void updateCacheIfNeeded(Minecraft mc) {
        long currentTime = System.currentTimeMillis();
        if (!lightCache.shouldUpdate(currentTime, ClientConfig.getUpdateIntervalMs()))
            return;

        lightCache.setLastUpdateTimeMs(currentTime);
        LevelHeightAccessor heightAccessor = (LevelHeightAccessor) mc.level;
        LightLogic.scanLightLevels(mc.level, lightCache, mc.player.blockPosition(),
                ClientConfig.getRangeHorizontal(), ClientConfig.getRangeVertical(),
                heightAccessor.getMinY(), heightAccessor.getMaxY());

        if (ClientConfig.isUnderwaterModeEnabled()) {
            LightLogic.scanWaterLightLevels(mc.level, lightCache, mc.player.blockPosition(),
                    ClientConfig.getRangeHorizontal(), ClientConfig.getRangeVertical(),
                    heightAccessor.getMinY(), heightAccessor.getMaxY(),
                    ClientConfig.getUnderwaterDisplayMode(), mc.player.isUnderWater());
        }
    }

    private static boolean isBlockVisibleFromCamera(Minecraft mc, Vec3 cameraPos, BlockPos pos) {
        Vec3 blockCenter = Vec3.atCenterOf(pos).add(0, 0.5, 0);
        Vec3 blockBottom = Vec3.atBottomCenterOf(pos).add(0, 1.0, 0);

        BlockHitResult hitCenter = mc.level.clip(new ClipContext(cameraPos, blockCenter,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        BlockHitResult hitBottom = mc.level.clip(new ClipContext(cameraPos, blockBottom,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));

        BlockPos hitCenterPos = hitCenter.getBlockPos();
        BlockPos hitBottomPos = hitBottom.getBlockPos();

        boolean centerVisible = hitCenterPos.equals(pos) || hitCenterPos.equals(pos.above())
                || !mc.level.getBlockState(hitCenterPos).canOcclude();
        boolean bottomVisible = hitBottomPos.equals(pos) || hitBottomPos.equals(pos.above())
                || !mc.level.getBlockState(hitBottomPos).canOcclude();

        return centerVisible || bottomVisible;
    }

    private static void renderOverlay(Minecraft mc, PoseStack poseStack, MultiBufferSource buffer,
            Camera camera, Vec3 cameraPos) {
        boolean showOnlySpawnable = ClientConfig.isShowOnlySpawnable();
        int colorZero = ClientConfig.getColorZero();
        int colorLow = ClientConfig.getColorLow();
        int colorSafe = ClientConfig.getColorSafe();
        int colorUnderwater = ClientConfig.getColorUnderwater();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        renderEntries(mc, poseStack, buffer, camera, cameraPos, lightCache.getCache(),
                showOnlySpawnable, colorZero, colorLow, colorSafe, false);
        renderEntries(mc, poseStack, buffer, camera, cameraPos, lightCache.getUnderwaterCache(),
                showOnlySpawnable, colorUnderwater, colorUnderwater, colorUnderwater, true);

        poseStack.popPose();
    }

    private static void renderEntries(Minecraft mc, PoseStack poseStack, MultiBufferSource buffer,
            Camera camera, Vec3 cameraPos, Long2IntMap entries, boolean showOnlySpawnable,
            int colorZero, int colorLow, int colorSafe, boolean isUnderwater) {
        for (Long2IntMap.Entry entry : entries.long2IntEntrySet()) {
            long packedPos = entry.getLongKey();
            int lightLevel = entry.getIntValue();
            BlockPos pos = BlockPos.of(packedPos);

            if (!LightLogic.isWithinRenderDistance(pos.getX(), pos.getY(), pos.getZ(),
                    cameraPos.x, cameraPos.y, cameraPos.z, MAX_RENDER_DIST_SQ))
                continue;
            if (!isBlockVisibleFromCamera(mc, cameraPos, pos))
                continue;
            if (!LightLogic.shouldDisplay(lightLevel, showOnlySpawnable))
                continue;

            int color = isUnderwater ? colorZero : LightLogic.getLightColor(lightLevel, colorZero, colorLow, colorSafe);
            drawTextOnBlock(poseStack, buffer, camera, String.valueOf(lightLevel), pos, color);
        }
    }

    private static void drawTextOnBlock(PoseStack poseStack, MultiBufferSource buffer,
            Camera camera, String text, BlockPos pos, int color) {
        Minecraft mc = Minecraft.getInstance();
        poseStack.pushPose();
        poseStack.translate(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));

        float scale = (float) ClientConfig.getTextScale();
        poseStack.scale(-scale, -scale, scale);

        Matrix4f matrix4f = poseStack.last().pose();
        float textWidth = -mc.font.width(text) / 2.0f;
        mc.font.drawInBatch(text, textWidth, 0F, 0xFF000000 | color, false, matrix4f,
                buffer, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        poseStack.popPose();
    }
}
