package io.github.nesuwu.llo;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.AfterParticles;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

public class LightLevelOverlayClient {

    public static boolean overlayEnabled = false;
    private final Long2IntMap lightLevelCache = new Long2IntOpenHashMap();
    private long lastUpdateTime = 0;

    private static int RANGE_HORIZONTAL = 16;
    private static int RANGE_VERTICAL = 8;
    private static long UPDATE_INTERVAL_MS = 150;

    private static final KeyMapping toggleOverlayKey = new KeyMapping(
        "key.lightleveloverlay.toggle",
        KeyConflictContext.IN_GAME,
        KeyModifier.NONE,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_F9,
        "key.categories.lightleveloverlay"
    );

    private static final KeyMapping openConfigKey = new KeyMapping(
        "key.lightleveloverlay.open_config",
        KeyConflictContext.IN_GAME,
        KeyModifier.NONE,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_F10,
        "key.categories.lightleveloverlay"
    );

    public static KeyMapping getToggleOverlayKey() {
        return toggleOverlayKey;
    }

    public static KeyMapping getOpenConfigKey() {
        return openConfigKey;
    }

    // [FIX] Subscribe to the specific subclass 'AfterParticles'
    @SubscribeEvent
    public void onRenderLevel(RenderLevelStageEvent.AfterParticles event) {
        while (toggleOverlayKey.consumeClick()) {
            overlayEnabled = !overlayEnabled;
            if (!overlayEnabled) {
                lightLevelCache.clear();
            }
        }

        while (openConfigKey.consumeClick()) {
            Minecraft mc2 = Minecraft.getInstance();
            mc2.setScreen(ClothConfigScreens.create(mc2.screen));
        }

        if (!overlayEnabled) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        RANGE_HORIZONTAL = ClientConfigFile.getRangeHorizontal();
        RANGE_VERTICAL = ClientConfigFile.getRangeVertical();
        UPDATE_INTERVAL_MS = ClientConfigFile.getUpdateIntervalMs();

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime > UPDATE_INTERVAL_MS) {
            lastUpdateTime = currentTime;
            updateLightLevelCache(mc);
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc
            .renderBuffers()
            .bufferSource();
        Camera mainCamera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = mainCamera.getPosition();
        Frustum frustum = event.getFrustum();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        boolean showOnlySpawnable = ClientConfigFile.isShowOnlySpawnable();

        double maxDistSqr = 64.0 * 64.0;

        for (Long2IntMap.Entry entry : lightLevelCache.long2IntEntrySet()) {
            long packedPos = entry.getLongKey();
            int lightLevel = entry.getIntValue();
            BlockPos pos = BlockPos.of(packedPos);

            if (pos.distToCenterSqr(cameraPos) > maxDistSqr) {
                continue;
            }

            if (frustum != null && !frustum.isVisible(new AABB(pos))) {
                continue;
            }

            if (showOnlySpawnable && lightLevel >= 8) {
                continue;
            }

            String text = String.valueOf(lightLevel);
            int color;
            if (lightLevel == 0) {
                color = ClientConfigFile.getColorZero();
            } else if (lightLevel < 8) {
                color = ClientConfigFile.getColorLow();
            } else {
                color = ClientConfigFile.getColorSafe();
            }
            drawTextOnBlock(poseStack, buffer, text, pos, color);
        }

        poseStack.popPose();
        buffer.endBatch();
    }

    private void updateLightLevelCache(Minecraft mc) {
        lightLevelCache.clear();
        if (mc.player == null || mc.level == null) return;

        BlockPos playerPos = mc.player.blockPosition();
        int minX = playerPos.getX() - RANGE_HORIZONTAL;
        int maxX = playerPos.getX() + RANGE_HORIZONTAL;
        int minZ = playerPos.getZ() - RANGE_HORIZONTAL;
        int maxZ = playerPos.getZ() + RANGE_HORIZONTAL;

        int worldMinY = mc.level.dimensionType().minY();
        int worldMaxY = worldMinY + mc.level.dimensionType().height();

        int topY = Math.min(worldMaxY - 1, playerPos.getY() + RANGE_VERTICAL);
        int bottomY = Math.max(worldMinY, playerPos.getY() - RANGE_VERTICAL);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = topY; y >= bottomY; y--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockPos posAbove = pos.above();

                    if (
                        !mc.level.hasChunkAt(pos) ||
                        !mc.level.hasChunkAt(posAbove)
                    ) {
                        continue;
                    }

                    BlockState bottomState = mc.level.getBlockState(pos);
                    boolean isSurfaceSolid = bottomState.isFaceSturdy(
                        mc.level,
                        pos,
                        Direction.UP
                    );

                    if (!isSurfaceSolid) {
                        continue;
                    }

                    BlockState upState = mc.level.getBlockState(posAbove);
                    if (upState.isCollisionShapeFullBlock(mc.level, posAbove)) {
                        continue;
                    }

                    int lightLevel = mc.level
                        .getLightEngine()
                        .getLayerListener(LightLayer.BLOCK)
                        .getLightValue(posAbove);
                    lightLevelCache.put(BlockPos.asLong(x, y, z), lightLevel);
                }
            }
        }
    }

    private void drawTextOnBlock(
        PoseStack poseStack,
        MultiBufferSource buffer,
        String text,
        BlockPos pos,
        int color
    ) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();

        poseStack.pushPose();

        poseStack.translate(
            pos.getX() + 0.5,
            pos.getY() + 1.5,
            pos.getZ() + 0.5
        );

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
            0xF000F0
        );

        poseStack.popPose();
    }
}
