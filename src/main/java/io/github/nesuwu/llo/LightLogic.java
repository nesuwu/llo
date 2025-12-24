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

    public static void scanLightLevels(
            Level level,
            LightCache cache,
            BlockPos centerPos,
            int rangeH,
            int rangeV,
            int minY,
            int maxY) {
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

                    if (!level.isLoaded(pos) || !level.isLoaded(posAbove)) {
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

    /**
     * Scans for water blocks and records light levels for drowned spawn prevention.
     * 
     * @param displayMode      "Floor" to show on riverbed, "Surface" to show at
     *                         water
     *                         top, "Both" to auto-switch based on player position
     * @param playerUnderwater true if player is currently underwater (used for Both
     *                         mode)
     */
    public static void scanWaterLightLevels(
            Level level,
            LightCache cache,
            BlockPos centerPos,
            int rangeH,
            int rangeV,
            int minY,
            int maxY,
            String displayMode,
            boolean playerUnderwater) {
        if (level == null || centerPos == null) {
            return;
        }

        int minX = centerPos.getX() - rangeH;
        int maxX = centerPos.getX() + rangeH;
        int minZ = centerPos.getZ() - rangeH;
        int maxZ = centerPos.getZ() + rangeH;

        int topY = Math.min(maxY - 1, centerPos.getY() + rangeV);
        int bottomY = Math.max(minY, centerPos.getY() - rangeV);

        // Determine actual display mode
        boolean showOnFloor;
        if ("Both".equals(displayMode)) {
            // If player is underwater, show on floor (easier to see from below)
            // If player is above water, show on surface (easier to see from above)
            showOnFloor = playerUnderwater;
        } else {
            showOnFloor = "Floor".equals(displayMode);
        }

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                // For each column, scan for water
                for (int y = topY; y >= bottomY; y--) {
                    BlockPos pos = new BlockPos(x, y, z);

                    if (!level.isLoaded(pos)) {
                        continue;
                    }

                    FluidState fluidState = level.getFluidState(pos);
                    if (!fluidState.is(Fluids.WATER) && !fluidState.is(Fluids.FLOWING_WATER)) {
                        continue;
                    }

                    // Found water, get light level at this position
                    int lightLevel = level
                            .getLightEngine()
                            .getLayerListener(LightLayer.BLOCK)
                            .getLightValue(pos);

                    if (showOnFloor) {
                        // Find the floor beneath this water
                        BlockPos floorPos = findWaterFloor(level, pos, bottomY);
                        if (floorPos != null) {
                            cache.putUnderwater(BlockPos.asLong(floorPos.getX(), floorPos.getY(), floorPos.getZ()),
                                    lightLevel);
                        }
                    } else {
                        // Find the surface (top of water)
                        BlockPos surfacePos = findWaterSurface(level, pos, topY);
                        if (surfacePos != null) {
                            cache.putUnderwater(
                                    BlockPos.asLong(surfacePos.getX(), surfacePos.getY(), surfacePos.getZ()),
                                    lightLevel);
                        }
                    }
                    // Skip the rest of this column after processing first water block
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

            // If it's not water and is a solid surface, we found the floor
            if (!fluidState.is(Fluids.WATER) && !fluidState.is(Fluids.FLOWING_WATER)) {
                if (state.isFaceSturdy(level, mutable, Direction.UP)) {
                    return mutable.immutable();
                }
                return null; // Non-solid block below water, skip
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

    public static int getLightColor(
            int lightLevel,
            int colorZero,
            int colorLow,
            int colorSafe) {
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
            double maxDistSq) {
        double dx = (posX + 0.5) - camX;
        double dy = (posY + 0.5) - camY;
        double dz = (posZ + 0.5) - camZ;
        return (dx * dx + dy * dy + dz * dz) <= maxDistSq;
    }

    public static boolean shouldDisplay(
            int lightLevel,
            boolean showOnlySpawnable) {
        if (showOnlySpawnable && lightLevel >= 8) {
            return false;
        }
        return true;
    }
}
