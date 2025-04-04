package com.zergatul.cheatutils.mixins.common.schematics;

import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunkSection.class)
public abstract class MixinLevelChunkSection {

    /*@Inject(at = @At("RETURN"), method = "hasOnlyAir", cancellable = true)
    private void onGettingHasOnlyAir(CallbackInfoReturnable<Boolean> info) {
        info.setReturnValue(false);
    }*/
}