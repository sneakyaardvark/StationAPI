package net.modificationstation.stationapi.api.template.block;

import net.minecraft.block.Cake;
import net.modificationstation.stationapi.api.registry.Identifier;

public class TemplateCake extends Cake implements BlockTemplate {

    public TemplateCake(Identifier identifier, int texture) {
        this(BlockTemplate.getNextId(), texture);
        BlockTemplate.onConstructor(this, identifier);
    }

    public TemplateCake(int id, int texture) {
        super(id, texture);
    }
}
