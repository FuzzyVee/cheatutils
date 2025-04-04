package com.zergatul.cheatutils.mixins.common.schematics;

import com.mojang.blaze3d.vertex.MeshData;
import com.zergatul.cheatutils.schematics.extensions.SectionCompilerResultsExtension;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(SectionCompiler.Results.class)
public abstract class MixinSectionCompilerResults implements SectionCompilerResultsExtension {

    private Map<RenderType, MeshData> schematicaRenderedLayers;

    public Map<RenderType, MeshData> getSchematicaRenderedLayers() {
        return schematicaRenderedLayers;
    }

    public void setSchematicaRenderedLayers(Map<RenderType, MeshData> renderedLayers) {
        schematicaRenderedLayers = renderedLayers;
    }

    @Inject(at = @At("HEAD"), method = "release")
    private void onRelease(CallbackInfo info) {
        if (schematicaRenderedLayers != null) {
            schematicaRenderedLayers.values().forEach(MeshData::close);
        }
    }
}