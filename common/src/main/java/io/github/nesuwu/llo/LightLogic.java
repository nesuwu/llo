package io.github.nesuwu.llo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class LightLogic {

    private LightLogic() {
    }

    public static void scanLightLevels(Level level, LightCache cache, BlockPos centerPos,
            int rangeH, int rangeV, int minY, int maxY) {
        cache.clear();
        if (level == null || centerPos == null)
            return;

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

                    if (!level.isLoaded(pos) || !level.isLoaded(posAbove))
                        continue;

                    BlockState bottomState = level.getBlockState(pos);
                    if (!bottomState.isFaceSturdy(level, pos, Direction.UP))
                        continue;

                    BlockState upState = level.getBlockState(posAbove);
                    if (upState.isCollisionShapeFullBlock(level, posAbove))
                        continue;

                    FluidState fluidAbove = level.getFluidState(posAbove);
                    if (fluidAbove.is(Fluids.WATER) || fluidAbove.is(Fluids.FLOWING_WATER))
                        continue;

                    int lightLevel = level.getLightEngine().getLayerListener(LightLayer.BLOCK).getLightValue(posAbove);
                    cache.put(BlockPos.asLong(x, y, z), lightLevel);
                }
            }
        }
    }

    public static void scanWaterLightLevels(Level level, LightCache cache, BlockPos centerPos,
            int rangeH, int rangeV, int minY, int maxY, String displayMode, boolean playerUnderwater) {
        if (level == null || centerPos == null)
            return;

        int minX = centerPos.getX() - rangeH;
        int maxX = centerPos.getX() + rangeH;
        int minZ = centerPos.getZ() - rangeH;
        int maxZ = centerPos.getZ() + rangeH;
        int topY = Math.min(maxY - 1, centerPos.getY() + rangeV);
        int bottomY = Math.max(minY, centerPos.getY() - rangeV);

        boolean showOnFloor = "Both".equals(displayMode) ? playerUnderwater : "Floor".equals(displayMode);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = topY; y >= bottomY; y--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!level.isLoaded(pos))
                        continue;

                    FluidState fluidState = level.getFluidState(pos);
                    if (!fluidState.is(Fluids.WATER) && !fluidState.is(Fluids.FLOWING_WATER))
                        continue;

                    int lightLevel = level.getLightEngine().getLayerListener(LightLayer.BLOCK).getLightValue(pos);

                    if (showOnFloor) {
                        BlockPos floorPos = findWaterFloor(level, pos, bottomY);
                        if (floorPos != null) {
                            cache.putUnderwater(BlockPos.asLong(floorPos.getX(), floorPos.getY(), floorPos.getZ()),
                                    lightLevel);
                        }
                    } else {
                        BlockPos surfacePos = findWaterSurface(level, pos, topY);
                        if (surfacePos != null) {
                            cache.putUnderwater(
                                    BlockPos.asLong(surfacePos.getX(), surfacePos.getY(), surfacePos.getZ()),
                                    lightLevel);
                        }
                    }
                    break;
                }
            }
        }
    }

    private static BlockPos findWaterFloor(Level level, BlockPos waterPos, int minY) {
        BlockPos.MutableBlockPos mutable = waterPos.mutable();
        while (mutable.getY() >= minY) {
            mutable.move(Direction.DOWN);
            BlockState state = level.getBlockState(mutable);
            FluidState fluidState = level.getFluidState(mutable);

            if (!fluidState.is(Fluids.WATER) && !fluidState.is(Fluids.FLOWING_WATER)) {
                if (state.isFaceSturdy(level, mutable, Direction.UP))
                    return mutable.immutable();
                return null;
            }
        }
        return null;
    }

    private static BlockPos findWaterSurface(Level level, BlockPos waterPos, int maxY) {
        BlockPos.MutableBlockPos mutable = waterPos.mutable();
        BlockPos lastWaterPos = waterPos;

        while (mutable.getY() <= maxY) {
            FluidState fluidState = level.getFluidState(mutable);
            if (fluidState.is(Fluids.WATER) || fluidState.is(Fluids.FLOWING_WATER)) {
                lastWaterPos = mutable.immutable();
                mutable.move(Direction.UP);
            } else {
                break;
            }
        }
        return lastWaterPos;
    }

    public static int getLightColor(int lightLevel, int colorZero, int colorLow, int colorSafe) {
        if (lightLevel == 0)
            return colorZero;
        if (lightLevel < 8)
            return colorLow;
        return colorSafe;
    }

    public static boolean isWithinRenderDistance(int posX, int posY, int posZ,
            double camX, double camY, double camZ, double maxDistSq) {
        double dx = (posX + 0.5) - camX;
        double dy = (posY + 0.5) - camY;
        double dz = (posZ + 0.5) - camZ;
        return (dx * dx + dy * dy + dz * dz) <= maxDistSq;
    }

    public static boolean shouldDisplay(int lightLevel, boolean showOnlySpawnable) {
        return !showOnlySpawnable || lightLevel < 8;
    }
}
