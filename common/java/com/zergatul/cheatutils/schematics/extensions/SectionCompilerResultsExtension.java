package com.zergatul.cheatutils.schematics.extensions;

import com.mojang.blaze3d.vertex.MeshData;
import net.minecraft.client.renderer.RenderType;

import java.util.Map;

public interface SectionCompilerResultsExtension {
    Map<RenderType, MeshData> getSchematicaRenderedLayers();
    void setSchematicaRenderedLayers(Map<RenderType, MeshData> renderedLayers);
}