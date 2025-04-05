package com.zergatul.cheatutils.schematics;

import com.zergatul.cheatutils.common.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Map;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockStateMapper {

    private static final Pattern pattern = Pattern.compile("^(?<block>[a-z0-9_.-]+:[a-z0-9/._-]+)(?:\\[(?<properties>[a-z0-9_]+=[a-z0-9_]+(?:,[a-z0-9_]+=[a-z0-9_]+)*)\\])?$");

    public static BlockState map(String value) {
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            MatchResult result = matcher.toMatchResult();
            CompoundTag compound = new CompoundTag();
            compound.put("Name", StringTag.valueOf(result.group("block")));

            String propertiesStr = result.group("properties");
            if (propertiesStr != null && !propertiesStr.isEmpty()) {
                CompoundTag properties = new CompoundTag();
                for (String propertyStr : propertiesStr.split(",")) {
                    String[] parts = propertyStr.split("=");
                    if (parts.length != 2) {
                        return Blocks.AIR.defaultBlockState();
                    }
                    properties.put(parts[0], StringTag.valueOf(parts[1]));
                }
            }

            return map(compound);
        } else {
            return Blocks.AIR.defaultBlockState();
        }
    }

    public static BlockState map(CompoundTag compound) {
        return compound.getString("Name").map(name -> {
            if (name.contains("%%FILTER_ME%%")) {
                return Blocks.AIR.defaultBlockState();
            }

            Block block = Registries.BLOCKS.getValue(ResourceLocation.parse(name));
            return compound.getCompound("Properties").map(properties -> {
                return block.getStateDefinition()
                        .getPossibleStates()
                        .stream()
                        .filter(state -> {
                            Map<Property<?>, Comparable<?>> tags = state.getValues();
                            if (tags.size() != properties.size()) {
                                return false;
                            }
                            for (String propertyName : properties.keySet()) {
                                Optional<String> optional = properties.getString(propertyName);
                                if (optional.isEmpty()) {
                                    return false;
                                }
                                String propertyValue = optional.get();

                                Optional<Property<?>> property = tags.keySet().stream()
                                        .filter(p -> p.getName().equals(propertyName))
                                        .findFirst();
                                if (property.isEmpty()) {
                                    return false;
                                }
                                Comparable<?> value = tags.get(property.get());
                                if (!propertyValue.equals(value.toString())) {
                                    return false;
                                }
                            }
                            return true;
                        })
                        .findFirst()
                        .orElse(block.defaultBlockState());
            }).orElse(block.defaultBlockState());
        }).orElse(Blocks.AIR.defaultBlockState());
    }
}