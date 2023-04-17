package net.modificationstation.stationapi.mixin.tools;

import net.minecraft.block.BlockBase;
import net.minecraft.item.ItemInstance;
import net.minecraft.item.tool.Shears;
import net.minecraft.item.tool.ToolMaterial;
import net.modificationstation.stationapi.api.item.tool.StationShearsItem;
import net.modificationstation.stationapi.api.registry.BlockRegistry;
import net.modificationstation.stationapi.api.registry.Identifier;
import net.modificationstation.stationapi.api.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Shears.class)
public class MixinShears implements StationShearsItem {

    @Unique
    private ToolMaterial stationapi_toolMaterial;
    @Unique
    private TagKey<BlockBase> stationapi_effectiveBlocks;

    @Inject(method = "<init>(I)V", at = @At("RETURN"))
    private void captureToolMaterial(int i, CallbackInfo ci) {
        stationapi_toolMaterial = ToolMaterial.field_1690;
        setEffectiveBlocks(TagKey.of(BlockRegistry.INSTANCE.getKey(), Identifier.of("mineable/shears")));
    }

    @Override
    public void setEffectiveBlocks(TagKey<BlockBase> effectiveBlocks) {
        stationapi_effectiveBlocks = effectiveBlocks;
    }

    @Override
    public TagKey<BlockBase> getEffectiveBlocks(ItemInstance itemInstance) {
        return stationapi_effectiveBlocks;
    }

    @Override
    public ToolMaterial getMaterial(ItemInstance itemInstance) {
        return stationapi_toolMaterial;
    }
}
