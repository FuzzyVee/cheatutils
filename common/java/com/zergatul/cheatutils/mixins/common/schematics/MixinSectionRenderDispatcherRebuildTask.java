package com.zergatul.cheatutils.mixins.common.schematics;

import com.mojang.blaze3d.vertex.MeshData;
import com.zergatul.cheatutils.schematics.extensions.SectionCompilerResultsExtension;
import com.zergatul.mixin.ModifyMethodReturnValue;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Mixin(targets = "net.minecraft.client.renderer.chunk.SectionRenderDispatcher$RenderSection$RebuildTask")
public abstract class MixinSectionRenderDispatcherRebuildTask {

    @Shadow
    @Final
    SectionRenderDispatcher.RenderSection field_20839;

    private SectionCompiler.Results results;

    @ModifyMethodReturnValue(
            method = "doTask",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/SectionCompiler;compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;"))
    private SectionCompiler.Results onReturnCompiledChunkResult(SectionCompiler.Results results) {
        return this.results = results;
    }

    @Inject(at = @At("RETURN"), method = "doTask")
    private void onAfterDoTask(SectionBufferBuilderPack sectionBufferBuilderPack, CallbackInfoReturnable<?> info) {
        this.results = null;
    }

    @ModifyArg(method = "doTask", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;sequenceFailFast(Ljava/util/List;)Ljava/util/concurrent/CompletableFuture;"))
    private List<?> onModifyList(List<CompletableFuture<Void>> list) {
        assert this.results != null;

        SectionCompilerResultsExtension resultsExtension = (SectionCompilerResultsExtension) (Object) this.results;
        Map<RenderType, MeshData> layers = resultsExtension.getSchematicaRenderedLayers();
        if (layers != null) {
            layers.forEach((renderType, meshData) -> {
                list.add(this.field_20839.uploadSectionLayer(renderType, meshData));
                //compiledSection.hasBlocks.add(renderType);
            });
        }
        return list;
    }
}