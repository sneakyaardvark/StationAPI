package net.modificationstation.stationapi.api.template.block;

import net.minecraft.block.SoulSand;
import net.modificationstation.stationapi.api.registry.Identifier;

public class TemplateSoulSand extends SoulSand implements BlockTemplate {
    
    public TemplateSoulSand(Identifier identifier, int j) {
        this(BlockTemplate.getNextId(), j);
        BlockTemplate.onConstructor(this, identifier);
    }
    
    public TemplateSoulSand(int i, int j) {
        super(i, j);
    }
}
