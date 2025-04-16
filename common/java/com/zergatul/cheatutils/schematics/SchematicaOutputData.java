package com.zergatul.cheatutils.schematics;

import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public record SchematicaOutputData(int width, int height, int length, List<BlockState> palette, int[] blocks) {}