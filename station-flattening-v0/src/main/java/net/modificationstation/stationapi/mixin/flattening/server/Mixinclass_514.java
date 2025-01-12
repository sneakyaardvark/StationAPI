package net.modificationstation.stationapi.mixin.flattening.server;

import net.minecraft.packet.AbstractPacket;
import net.minecraft.server.ServerPlayerView;
import net.minecraft.tileentity.TileEntityBase;
import net.modificationstation.stationapi.impl.server.network.ChunkSectionTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayerView.class_514.class)
public abstract class Mixinclass_514 {

    @Shadow
    @Final
    ServerPlayerView field_2136;

    @Shadow
    private int field_2142;

    @Shadow
    private int field_2143;

    @Shadow
    private int field_2144;

    @Shadow
    private int field_2145;

    @Shadow
    private int field_2146;

    @Shadow
    private int field_2147;

    @Shadow
    private int field_2148;

    @Shadow
    private int field_2138;
    @Shadow
    private int field_2139;

    @Shadow
    public abstract void method_1755(AbstractPacket arg);

    @Shadow
    protected abstract void method_1756(TileEntityBase arg);

    private final ChunkSectionTracker[] sectionTrackers = new ChunkSectionTracker[field_2136.method_1741().countVerticalSections()];

    /**
     * @author mine_diver
     * @reason early version
     */
    @Overwrite
    public void method_1753(int x, int y, int z) {
        int sectionIndex = field_2136.method_1741().getSectionIndex(y);
        if (sectionTrackers[sectionIndex] == null)
            //noinspection DataFlowIssue
            sectionTrackers[sectionIndex] = new ChunkSectionTracker(field_2136, (ServerPlayerView.class_514) (Object) this, field_2138, field_2139, sectionIndex);
        sectionTrackers[sectionIndex].queueUpdate(x, y & 15, z);
    }

    /**
     * @author mine_diver
     * @reason early version
     */
    @Overwrite
    public void method_1752() {
        for (ChunkSectionTracker sectionTracker : sectionTrackers)
            if (sectionTracker != null)
                sectionTracker.sendQueue();
    }
}
