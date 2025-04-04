package com.zergatul.cheatutils.mixins.fabric.schematics;

import com.mojang.blaze3d.vertex.*;
import com.zergatul.cheatutils.modules.automation.Schematica;
import com.zergatul.cheatutils.schematics.RenderChunkRegionWrapper;
import com.zergatul.cheatutils.schematics.extensions.SectionBufferBuilderPackExtension;
import com.zergatul.cheatutils.schematics.extensions.SectionCompilerResultsExtension;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;

@Mixin(SectionCompiler.class)
public abstract class MixinSectionCompiler {

    @Shadow
    @Final
    private BlockRenderDispatcher blockRenderer;

    @Inject(
            method = "compile",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;clearCache()V"),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onCompile(
            SectionPos sectionPos,
            RenderChunkRegion region,
            VertexSorting sorting,
            SectionBufferBuilderPack sectionBufferBuilderPack,
            CallbackInfoReturnable<SectionCompiler.Results> info,
            SectionCompiler.Results results
    ) {
        if (Schematica.instance.isGhostRenderingEnabled()) {
            PoseStack posestack = new PoseStack();
            Map<RenderType, BufferBuilder> map = new Reference2ObjectArrayMap<>(RenderType.chunkBufferLayers().size());
            RandomSource randomsource = RandomSource.create();
            List<BlockModelPart> list = new ObjectArrayList<>();
            SectionBufferBuilderPackExtension bufferBuilderPackExtension = (SectionBufferBuilderPackExtension) sectionBufferBuilderPack;
            BlockAndTintGetter wrapper = new RenderChunkRegionWrapper(region);

            for (BlockPos blockpos2 : BlockPos.betweenClosed(sectionPos.origin(), sectionPos.origin().offset(15, 15, 15))) {
                BlockState blockstate = wrapper.getBlockState(blockpos2);

                FluidState fluidstate = blockstate.getFluidState();
                if (!fluidstate.isEmpty()) {
                    continue;
                }

                if (blockstate.getRenderShape() == RenderShape.MODEL) {
                    RenderType renderType = ItemBlockRenderTypes.getChunkRenderType(blockstate);
                    BufferBuilder bufferBuilder = this.getOrBeginSchematicaLayer(map, bufferBuilderPackExtension, renderType);
                    randomsource.setSeed(blockstate.getSeed(blockpos2));
                    this.blockRenderer.getBlockModel(blockstate).collectParts(randomsource, list);
                    posestack.pushPose();
                    posestack.translate(
                            (float)SectionPos.sectionRelative(blockpos2.getX()),
                            (float)SectionPos.sectionRelative(blockpos2.getY()),
                            (float)SectionPos.sectionRelative(blockpos2.getZ())
                    );
                    this.blockRenderer.renderBatched(blockstate, blockpos2, wrapper, posestack, bufferBuilder, true, list);
                    posestack.popPose();
                    list.clear();
                }
            }

            var results2 = (SectionCompilerResultsExtension) (Object) results;
            for (Map.Entry<RenderType, BufferBuilder> entry : map.entrySet()) {
                RenderType rendertype1 = entry.getKey();
                MeshData meshdata = entry.getValue().build();
                if (meshdata != null) {
                    /*if (rendertype1 == RenderType.translucent()) {
                        sectioncompiler$results.transparencyState = meshdata.sortQuads(p_343546_.buffer(RenderType.translucent()), p_342522_);
                    }*/

                    Map<RenderType, MeshData> layers = results2.getSchematicaRenderedLayers();
                    if (layers == null) {
                        results2.setSchematicaRenderedLayers(layers = new Reference2ObjectArrayMap<>());
                    }
                    layers.put(rendertype1, meshdata);
                }
            }
        }
    }

    private BufferBuilder getOrBeginSchematicaLayer(Map<RenderType, BufferBuilder> renderTypeMap, SectionBufferBuilderPackExtension sectionBufferBuilderPack, RenderType renderType) {
        BufferBuilder bufferbuilder = renderTypeMap.get(renderType);
        if (bufferbuilder == null) {
            ByteBufferBuilder bytebufferbuilder = sectionBufferBuilderPack.schematicaBuffer(renderType);
            bufferbuilder = new BufferBuilder(bytebufferbuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            renderTypeMap.put(renderType, bufferbuilder);
        }

        return bufferbuilder;
    }
}