package com.zergatul.cheatutils.schematics;

import com.zergatul.cheatutils.common.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Map;
import java.util.Optional;

public class BlockStateMapper {

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