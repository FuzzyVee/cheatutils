package com.zergatul.cheatutils.schematics;

import com.zergatul.cheatutils.utils.NbtUtils;
import net.minecraft.nbt.*;
import net.minecraft.world.level.block.state.BlockState;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LitematicFile implements SchemaFile {

    private static final String VERSION_TAG = "Version";
    private static final String DATA_VERSION_TAG = "MinecraftDataVersion";
    private static final String REGIONS_TAG = "Regions";

    private final CompoundTag compound;
    private final int version;
    private final int dataVersion;
    private final Region[] regions;

    public LitematicFile(byte[] data) throws IOException, InvalidFormatException {
        this(NbtIo.readCompressed(new ByteArrayInputStream(data), NbtAccounter.create(104857600L)));
    }

    private LitematicFile(CompoundTag compound) throws InvalidFormatException {
        validateRequiredTags(compound);
        this.compound = compound;

        version = compound.getInt(VERSION_TAG).orElseThrow();
        dataVersion = compound.getInt(DATA_VERSION_TAG).orElseThrow();

        CompoundTag regionCompounds = compound.getCompound(REGIONS_TAG).orElseThrow();
        regions = new Region[regionCompounds.size()];
        int index = 0;
        for (String key : regionCompounds.keySet()) {
            regions[index++] = new Region(key, regionCompounds.getCompound(key).orElseThrow());
        }

        if (regions.length == 0) {
            throw new InvalidFormatException("Zero regions.");
        }

        if (regions.length > 1) {
            throw new InvalidFormatException("More than 1 regions. Not supported.");
        }
    }

    private void validateRequiredTags(CompoundTag compound) throws InvalidFormatException {
        if (!NbtUtils.hasInt(compound, VERSION_TAG)) {
            throw new InvalidFormatException("Invalid NBT structure. [" + VERSION_TAG + "] IntTag is required.");
        }
        if (!NbtUtils.hasInt(compound, DATA_VERSION_TAG)) {
            throw new InvalidFormatException("Invalid NBT structure. [" + DATA_VERSION_TAG + "] IntTag is required.");
        }
        if (!NbtUtils.hasCompound(compound, "Metadata")) {
            throw new InvalidFormatException("Invalid NBT structure. [Metadata] CompoundTag is required.");
        }
        if (!NbtUtils.hasCompound(compound, REGIONS_TAG)) {
            throw new InvalidFormatException("Invalid NBT structure. [" + REGIONS_TAG + "] CompoundTag is required.");
        }

        CompoundTag regions = compound.getCompound("Regions").orElseThrow();
        for (String key : regions.keySet()) {
            validateRegion(regions.getCompoundOrEmpty(key), String.format("Invalid NBT structure in %s region", key));
        }
    }

    private void validateRegion(CompoundTag compound, String errorPrefix) throws InvalidFormatException {
        if (!NbtUtils.hasLongs(compound, "BlockStates")) {
            throw new InvalidFormatException(String.format("%s. [BlockStates] LongArrayTag is required.", errorPrefix));
        }
        if (!NbtUtils.hasCompound(compound, "Position")) {
            throw new InvalidFormatException(String.format("%s. [Position] CompoundTag is required.", errorPrefix));
        }
        if (!NbtUtils.hasList(compound, "BlockStatePalette")) {
            throw new InvalidFormatException(String.format("%s. [BlockStatePalette] ListTag is required.", errorPrefix));
        }
        if (!NbtUtils.hasCompound(compound, "Size")) {
            throw new InvalidFormatException(String.format("%s. [Size] CompoundTag is required.", errorPrefix));
        }
        if (!NbtUtils.hasList(compound, "TileEntities")) {
            throw new InvalidFormatException(String.format("%s. [TileEntities] ListTag is required.", errorPrefix));
        }

        validateVector(compound.getCompound("Position").orElseThrow(), errorPrefix + ", [Position] tag");
        validateVector(compound.getCompound("Size").orElseThrow(), errorPrefix + ", [Size] tag");
    }

    private void validateVector(CompoundTag compound, String errorPrefix) throws InvalidFormatException {
        if (!NbtUtils.hasInt(compound, "x")) {
            throw new InvalidFormatException(String.format("%s. [x] IntTag is required.", errorPrefix));
        }
        if (!NbtUtils.hasInt(compound, "y")) {
            throw new InvalidFormatException(String.format("%s. [x] IntTag is required.", errorPrefix));
        }
        if (!NbtUtils.hasInt(compound, "z")) {
            throw new InvalidFormatException(String.format("%s. [x] IntTag is required.", errorPrefix));
        }
    }

    @Override
    public int getWidth() {
        return regions[0].width;
    }

    @Override
    public int getHeight() {
        return regions[0].height;
    }

    @Override
    public int getLength() {
        return regions[0].length;
    }

    @Override
    public BlockState getBlockState(int x, int y, int z) {
        return regions[0].getBlockState(x, y, z);
    }

    @Override
    public int[] getSummary() {
        return regions[0].getSummary();
    }

    @Override
    public BlockState[] getPalette() {
        return regions[0].palette;
    }

    @Override
    public void write(OutputStream output) {

    }

    private static class Region {

        public final String name;
        private final int width;
        private final int height;
        private final int length;
        private final BlockState[] palette;
        private final long[] blocks;
        private final int bitSize;
        private final long bitMask;
        private final int[] summary;

        public Region(String name, CompoundTag compound) throws InvalidFormatException {
            this.name = name;

            CompoundTag sizeTag = compound.getCompound("Size").orElseThrow();
            width = Math.abs(sizeTag.getInt("x").orElseThrow());
            height = Math.abs(sizeTag.getInt("y").orElseThrow());
            length = Math.abs(sizeTag.getInt("z").orElseThrow());

            palette = parsePalette((ListTag) compound.get("BlockStatePalette"));
            blocks = compound.getLongArray("BlockStates").orElseThrow();
            bitSize = 32 - Integer.numberOfLeadingZeros(palette.length);
            bitMask = (1L << bitSize) - 1L;
            summary = createSummary();
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getLength() {
            return length;
        }

        public BlockState getBlockState(int x, int y, int z) {
            return palette[getPaletteIndex(x, y, z)];
        }

        public int[] getSummary() {
            return summary;
        }

        private BlockState[] parsePalette(ListTag list) throws InvalidFormatException {
            BlockState[] palette = new BlockState[list.size()];
            for (int i = 0; i < list.size(); i++) {
                palette[i] = BlockStateMapper.map((CompoundTag) list.get(i));
            }
            return palette;
        }

        private int[] createSummary() {
            int[] summary = new int[palette.length];
            int size = width * height * length;
            for (int i = 0; i < size; i++) {
                summary[getPaletteIndex(i)]++;
            }
            return summary;
        }

        private int getPaletteIndex(int x, int y, int z) {
            return getPaletteIndex(((long) y * length + z) * width + x);
        }

        private int getPaletteIndex(long index) {
            long startOffset = index * bitSize;
            int startArrIndex = (int) (startOffset >> 6); // startOffset / 64
            int endArrIndex = (int) (((index + 1L) * (long) bitSize - 1L) >> 6);
            int startBitOffset = (int) (startOffset & 0x3F); // startOffset % 64

            if (startArrIndex == endArrIndex)
            {
                return (int) (blocks[startArrIndex] >>> startBitOffset & bitMask);
            }
            else
            {
                int endOffset = 64 - startBitOffset;
                return (int) ((blocks[startArrIndex] >>> startBitOffset | blocks[endArrIndex] << endOffset) & bitMask);
            }
        }
    }
}