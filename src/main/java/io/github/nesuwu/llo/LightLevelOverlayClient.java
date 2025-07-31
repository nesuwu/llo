package io.github.nesuwu.llo;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LightLevelOverlayClient {

    public static boolean overlayEnabled = false;
    private final Map<BlockPos, Integer> lightLevelCache = new ConcurrentHashMap<>();
    private long lastUpdateTime = 0;

    private static final KeyMapping toggleOverlayKey = new KeyMapping(
            "key.lightleveloverlay.toggle",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F9,
            "key.categories.lightleveloverlay"
    );

    public static KeyMapping getToggleOverlayKey() {
        return toggleOverlayKey;
    }

    @SubscribeEvent
    public void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        while (toggleOverlayKey.consumeClick()) {
            overlayEnabled = !overlayEnabled;
            if (!overlayEnabled) {
                lightLevelCache.clear();
            }
        }

        if (!overlayEnabled) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime > 100) {
            lastUpdateTime = currentTime;
            updateLightLevelCache(mc);
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        lightLevelCache.forEach((pos, lightLevel) -> {
            String text = String.valueOf(lightLevel);
            int color;
            if (lightLevel == 0) {
                color = 0xFF4040; // Red
            } else if (lightLevel < 8) {
                color = 0xFFFF40; // Yellow
            } else {
                color = 0x40FF40; // Green
            }
            drawTextOnBlock(poseStack, buffer, text, pos, color);
        });

        poseStack.popPose();
        buffer.endBatch();
    }

    private void updateLightLevelCache(Minecraft mc) {
        mc.submit(() -> {
            lightLevelCache.clear();
            if (mc.player == null || mc.level == null) return;

            int range = 16;
            BlockPos playerPos = mc.player.blockPosition();

            for (BlockPos pos : BlockPos.betweenClosed(playerPos.offset(-range, -range, -range), playerPos.offset(range, range, range))) {
                BlockState bottomState = mc.level.getBlockState(pos);
                BlockPos posAbove = pos.above();
                BlockState topState = mc.level.getBlockState(posAbove);

                boolean isSurfaceSolid = bottomState.isFaceSturdy(mc.level, pos, Direction.UP);
                boolean isSpaceAboveEmpty = !topState.isSuffocating(mc.level, posAbove);

                if (isSurfaceSolid && isSpaceAboveEmpty) {
                    int lightLevel = mc.level.getLightEngine().getLayerListener(LightLayer.BLOCK).getLightValue(posAbove);
                    lightLevelCache.put(pos.immutable(), lightLevel);
                }
            }
        });
    }

    private void drawTextOnBlock(PoseStack poseStack, MultiBufferSource buffer, String text, BlockPos pos, int color) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();

        poseStack.pushPose();

        poseStack.translate(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5);

        poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));

        poseStack.scale(-0.025f, -0.025f, 0.025f);

        Matrix4f matrix4f = poseStack.last().pose();
        float textWidth = -mc.font.width(text) / 2.0f;

        int finalColor = 0xFF000000 | color;

        mc.font.drawInBatch(text, textWidth, 0F, finalColor, false, matrix4f, buffer, Font.DisplayMode.SEE_THROUGH, 0, 0xF000F0);

        poseStack.popPose();
    }
}