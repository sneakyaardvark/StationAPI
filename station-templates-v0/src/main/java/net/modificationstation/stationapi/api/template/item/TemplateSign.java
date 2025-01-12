package net.modificationstation.stationapi.api.template.item;

import net.minecraft.item.Sign;
import net.modificationstation.stationapi.api.registry.Identifier;

public class TemplateSign extends Sign implements ItemTemplate {
    
    public TemplateSign(Identifier identifier) {
        this(ItemTemplate.getNextId());
        ItemTemplate.onConstructor(this, identifier);
    }
    
    public TemplateSign(int i) {
        super(i);
    }
}
