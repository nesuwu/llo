package io.github.nesuwu.llo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;

public final class LightLogic {

    private LightLogic() {}

    public static void scanLightLevels(
        Level level,
        LightCache cache,
        BlockPos centerPos,
        int rangeH,
        int rangeV,
        int minY,
        int maxY
    ) {
        cache.clear();

        if (level == null || centerPos == null) {
            return;
        }

        int minX = centerPos.getX() - rangeH;
        int maxX = centerPos.getX() + rangeH;
        int minZ = centerPos.getZ() - rangeH;
        int maxZ = centerPos.getZ() + rangeH;

        int topY = Math.min(maxY - 1, centerPos.getY() + rangeV);
        int bottomY = Math.max(minY, centerPos.getY() - rangeV);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = topY; y >= bottomY; y--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockPos posAbove = pos.above();

                    if (!level.hasChunkAt(pos) || !level.hasChunkAt(posAbove)) {
                        continue;
                    }

                    BlockState bottomState = level.getBlockState(pos);
                    if (!bottomState.isFaceSturdy(level, pos, Direction.UP)) {
                        continue;
                    }

                    BlockState upState = level.getBlockState(posAbove);
                    if (upState.isCollisionShapeFullBlock(level, posAbove)) {
                        continue;
                    }

                    int lightLevel = level
                        .getLightEngine()
                        .getLayerListener(LightLayer.BLOCK)
                        .getLightValue(posAbove);

                    cache.put(BlockPos.asLong(x, y, z), lightLevel);
                }
            }
        }
    }

    public static int getLightColor(
        int lightLevel,
        int colorZero,
        int colorLow,
        int colorSafe
    ) {
        if (lightLevel == 0) {
            return colorZero;
        } else if (lightLevel < 8) {
            return colorLow;
        } else {
            return colorSafe;
        }
    }

    public static boolean isWithinRenderDistance(
        int posX,
        int posY,
        int posZ,
        double camX,
        double camY,
        double camZ,
        double maxDistSq
    ) {
        double dx = (posX + 0.5) - camX;
        double dy = (posY + 0.5) - camY;
        double dz = (posZ + 0.5) - camZ;
        return (dx * dx + dy * dy + dz * dz) <= maxDistSq;
    }

    public static boolean shouldDisplay(
        int lightLevel,
        boolean showOnlySpawnable
    ) {
        if (showOnlySpawnable && lightLevel >= 8) {
            return false;
        }
        return true;
    }
}
