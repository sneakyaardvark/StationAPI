package net.modificationstation.stationapi.api.template.block;

import net.minecraft.block.Ore;
import net.modificationstation.stationapi.api.registry.Identifier;

public class TemplateOre extends Ore implements BlockTemplate {

    public TemplateOre(Identifier identifier, int j) {
        this(BlockTemplate.getNextId(), j);
        BlockTemplate.onConstructor(this, identifier);
    }

    public TemplateOre(int i, int j) {
        super(i, j);
    }
}
