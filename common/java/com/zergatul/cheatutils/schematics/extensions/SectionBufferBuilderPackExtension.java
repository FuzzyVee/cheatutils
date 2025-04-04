package com.zergatul.cheatutils.schematics.extensions;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import net.minecraft.client.renderer.RenderType;

public interface SectionBufferBuilderPackExtension {
    ByteBufferBuilder schematicaBuffer(RenderType renderType);
}