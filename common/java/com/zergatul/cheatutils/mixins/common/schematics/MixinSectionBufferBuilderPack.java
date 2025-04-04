package com.zergatul.cheatutils.mixins.common.schematics;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.zergatul.cheatutils.schematics.extensions.SectionBufferBuilderPackExtension;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(SectionBufferBuilderPack.class)
public abstract class MixinSectionBufferBuilderPack implements SectionBufferBuilderPackExtension {

    @Shadow
    @Final
    private static List<RenderType> RENDER_TYPES;

    private Map<RenderType, ByteBufferBuilder> schematicaBuffers;

    public ByteBufferBuilder schematicaBuffer(RenderType renderType) {
        allocateSchematicaBufferIfRequired();
        return this.schematicaBuffers.get(renderType);
    }

    @Inject(at = @At("HEAD"), method = "clearAll")
    private void onClearAll(CallbackInfo info) {
        if (this.schematicaBuffers != null) {
            this.schematicaBuffers.values().forEach(ByteBufferBuilder::clear);
        }
    }

    @Inject(at = @At("HEAD"), method = "discardAll")
    private void onDiscardAll(CallbackInfo info) {
        if (this.schematicaBuffers != null) {
            this.schematicaBuffers.values().forEach(ByteBufferBuilder::discard);
        }
    }

    @Inject(at = @At("HEAD"), method = "discardAll")
    private void onClose(CallbackInfo info) {
        if (this.schematicaBuffers != null) {
            this.schematicaBuffers.values().forEach(ByteBufferBuilder::close);
        }
    }

    private void allocateSchematicaBufferIfRequired() {
        if (schematicaBuffers == null) {
            schematicaBuffers = Util.make(new Reference2ObjectArrayMap<>(RENDER_TYPES.size()), map -> {
                for (RenderType rendertype : RENDER_TYPES) {
                    map.put(rendertype, new ByteBufferBuilder(rendertype.bufferSize()));
                }
            });
        }
    }
}