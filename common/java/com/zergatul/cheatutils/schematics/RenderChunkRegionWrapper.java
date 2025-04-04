package com.zergatul.cheatutils.schematics;

import com.zergatul.cheatutils.modules.automation.Schematica;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class RenderChunkRegionWrapper implements BlockAndTintGetter {

    private final RenderChunkRegion region;

    public RenderChunkRegionWrapper(RenderChunkRegion region) {
        this.region = region;
    }

    @Override
    public float getShade(Direction direction, boolean p_45523_) {
        return region.getShade(direction, p_45523_);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return region.getLightEngine();
    }

    @Override
    public int getBlockTint(BlockPos p_45520_, ColorResolver p_45521_) {
        return region.getBlockTint(p_45520_, p_45521_);
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos p_45570_) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return Schematica.instance.getBlockState(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public FluidState getFluidState(BlockPos p_45569_) {
        return Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public int getHeight() {
        return region.getHeight();
    }

    @Override
    public int getMinY() {
        return region.getMinY();
    }
}