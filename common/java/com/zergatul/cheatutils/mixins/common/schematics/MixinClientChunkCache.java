package com.zergatul.cheatutils.mixins.common.schematics;

import com.zergatul.mixin.ModifyMethodReturnValue;
import net.minecraft.client.multiplayer.ClientChunkCache;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientChunkCache.class)
public abstract class MixinClientChunkCache {

    //@ModifyMethodReturnValue()
}