package net.modificationstation.stationapi.mixin.flattening;

import net.minecraft.block.BlockBase;
import net.minecraft.level.chunk.Chunk;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.world.chunk.StationFlatteningChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Chunk.class)
public abstract class MixinChunk implements StationFlatteningChunk {

    @Shadow public abstract int getTileId(int i, int j, int k);

    @Shadow public abstract boolean method_860(int i, int j, int k, int l);

    @Override
    public BlockState getBlockState(int x, int y, int z) {
        return BlockBase.BY_ID[getTileId(x, y, z)].getDefaultState();
    }

    @Override
    public BlockState setBlockState(int x, int y, int z, BlockState blockState) {
        BlockState oldState = getBlockState(x, y, z);
        return method_860(x, y, z, blockState.getBlock().id) ? oldState : null;
    }
}
