package io.github.nesuwu.llo;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

public class LightLevelOverlayClient {

    public static boolean overlayEnabled = false;
    private final LightCache lightCache = new LightCache();

    private static final double MAX_RENDER_DIST_SQ = 64.0 * 64.0;

    private static final KeyMapping toggleOverlayKey = new KeyMapping(
            "key.lightleveloverlay.toggle",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F9,
            "key.categories.lightleveloverlay");

    private static final KeyMapping openConfigKey = new KeyMapping(
            "key.lightleveloverlay.open_config",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F10,
            "key.categories.lightleveloverlay");

    public static KeyMapping getToggleOverlayKey() {
        return toggleOverlayKey;
    }

    public static KeyMapping getOpenConfigKey() {
        return openConfigKey;
    }

    @SubscribeEvent
    public void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        handleKeyInput();

        if (!overlayEnabled) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        updateCacheIfNeeded(mc);

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc
                .renderBuffers()
                .bufferSource();
        Camera mainCamera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = mainCamera.getPosition();

        renderOverlay(mc, poseStack, buffer, mainCamera, cameraPos);

        buffer.endBatch();
    }

    private void handleKeyInput() {
        while (toggleOverlayKey.consumeClick()) {
            overlayEnabled = !overlayEnabled;
            if (!overlayEnabled) {
                lightCache.clear();
            }
        }

        while (openConfigKey.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(ClothConfigScreens.create(mc.screen));
        }
    }

    private void updateCacheIfNeeded(Minecraft mc) {
        long currentTime = System.currentTimeMillis();
        long updateInterval = ClientConfigFile.getUpdateIntervalMs();

        if (lightCache.shouldUpdate(currentTime, updateInterval)) {
            lightCache.setLastUpdateTimeMs(currentTime);

            LightLogic.scanLightLevels(
                    mc.level,
                    lightCache,
                    mc.player.blockPosition(),
                    ClientConfigFile.getRangeHorizontal(),
                    ClientConfigFile.getRangeVertical(),
                    mc.level.getMinBuildHeight(),
                    mc.level.getMaxBuildHeight());

            // Also scan water if underwater mode is enabled
            if (ClientConfigFile.isUnderwaterModeEnabled()) {
                boolean playerUnderwater = mc.player.isUnderWater();
                LightLogic.scanWaterLightLevels(
                        mc.level,
                        lightCache,
                        mc.player.blockPosition(),
                        ClientConfigFile.getRangeHorizontal(),
                        ClientConfigFile.getRangeVertical(),
                        mc.level.getMinBuildHeight(),
                        mc.level.getMaxBuildHeight(),
                        ClientConfigFile.getUnderwaterDisplayMode(),
                        playerUnderwater);
            }
        }
    }

    private boolean isBlockVisibleFromCamera(
            Minecraft mc,
            Vec3 cameraPos,
            BlockPos pos) {
        Vec3 blockCenter = Vec3.atCenterOf(pos).add(0, 0.5, 0);
        Vec3 blockBottom = Vec3.atBottomCenterOf(pos).add(0, 1.0, 0);

        ClipContext contextCenter = new ClipContext(
                cameraPos,
                blockCenter,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                mc.player);

        ClipContext contextBottom = new ClipContext(
                cameraPos,
                blockBottom,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                mc.player);

        BlockHitResult hitCenter = mc.level.clip(contextCenter);
        BlockHitResult hitBottom = mc.level.clip(contextBottom);

        BlockPos hitCenterPos = hitCenter.getBlockPos();
        BlockPos hitBottomPos = hitBottom.getBlockPos();

        boolean centerVisible = hitCenterPos.equals(pos) ||
                hitCenterPos.equals(pos.above()) ||
                !mc.level.getBlockState(hitCenterPos).canOcclude();

        boolean bottomVisible = hitBottomPos.equals(pos) ||
                hitBottomPos.equals(pos.above()) ||
                !mc.level.getBlockState(hitBottomPos).canOcclude();

        return centerVisible || bottomVisible;
    }

    private void renderOverlay(
            Minecraft mc,
            PoseStack poseStack,
            MultiBufferSource buffer,
            Camera camera,
            Vec3 cameraPos) {
        boolean showOnlySpawnable = ClientConfigFile.isShowOnlySpawnable();
        int colorZero = ClientConfigFile.getColorZero();
        int colorLow = ClientConfigFile.getColorLow();
        int colorSafe = ClientConfigFile.getColorSafe();
        int colorUnderwater = ClientConfigFile.getColorUnderwater();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        // Render land entries
        for (Long2IntMap.Entry entry : lightCache
                .getCache()
                .long2IntEntrySet()) {
            long packedPos = entry.getLongKey();
            int lightLevel = entry.getIntValue();
            BlockPos pos = BlockPos.of(packedPos);

            if (!LightLogic.isWithinRenderDistance(
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    cameraPos.x,
                    cameraPos.y,
                    cameraPos.z,
                    MAX_RENDER_DIST_SQ)) {
                continue;
            }

            if (!isBlockVisibleFromCamera(mc, cameraPos, pos)) {
                continue;
            }

            if (!LightLogic.shouldDisplay(lightLevel, showOnlySpawnable)) {
                continue;
            }

            int color = LightLogic.getLightColor(
                    lightLevel,
                    colorZero,
                    colorLow,
                    colorSafe);

            drawTextOnBlock(
                    poseStack,
                    buffer,
                    camera,
                    String.valueOf(lightLevel),
                    pos,
                    color);
        }

        // Render underwater entries
        for (Long2IntMap.Entry entry : lightCache
                .getUnderwaterCache()
                .long2IntEntrySet()) {
            long packedPos = entry.getLongKey();
            int lightLevel = entry.getIntValue();
            BlockPos pos = BlockPos.of(packedPos);

            if (!LightLogic.isWithinRenderDistance(
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    cameraPos.x,
                    cameraPos.y,
                    cameraPos.z,
                    MAX_RENDER_DIST_SQ)) {
                continue;
            }

            if (!isBlockVisibleFromCamera(mc, cameraPos, pos)) {
                continue;
            }

            if (!LightLogic.shouldDisplay(lightLevel, showOnlySpawnable)) {
                continue;
            }

            drawTextOnBlock(
                    poseStack,
                    buffer,
                    camera,
                    String.valueOf(lightLevel),
                    pos,
                    colorUnderwater);
        }

        poseStack.popPose();
    }

    private void drawTextOnBlock(
            PoseStack poseStack,
            MultiBufferSource buffer,
            Camera camera,
            String text,
            BlockPos pos,
            int color) {
        Minecraft mc = Minecraft.getInstance();

        poseStack.pushPose();

        poseStack.translate(
                pos.getX() + 0.5,
                pos.getY() + 1.5,
                pos.getZ() + 0.5);

        poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));

        float scale = (float) ClientConfigFile.getTextScale();
        poseStack.scale(-scale, -scale, scale);

        Matrix4f matrix4f = poseStack.last().pose();
        float textWidth = -mc.font.width(text) / 2.0f;
        int finalColor = 0xFF000000 | color;

        mc.font.drawInBatch(
                text,
                textWidth,
                0F,
                finalColor,
                false,
                matrix4f,
                buffer,
                Font.DisplayMode.NORMAL,
                0,
                0xF000F0);

        poseStack.popPose();
    }
}
