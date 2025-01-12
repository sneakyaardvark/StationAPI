package net.modificationstation.stationapi.api.template.block;

import net.minecraft.block.Glowstone;
import net.minecraft.block.material.Material;
import net.modificationstation.stationapi.api.registry.Identifier;

public class TemplateGlowstone extends Glowstone implements BlockTemplate {
    
    public TemplateGlowstone(Identifier identifier, int j, Material arg) {
        this(BlockTemplate.getNextId(), j, arg);
        BlockTemplate.onConstructor(this, identifier);
    }
    
    public TemplateGlowstone(int i, int j, Material arg) {
        super(i, j, arg);
    }
}
