package net.modificationstation.stationapi.api.template.item.tool;

import net.minecraft.item.tool.Pickaxe;
import net.minecraft.item.tool.ToolMaterial;
import net.modificationstation.stationapi.api.registry.Identifier;
import net.modificationstation.stationapi.api.template.item.ItemTemplate;

public class TemplatePickaxe extends Pickaxe implements ItemTemplate {
    
    public TemplatePickaxe(Identifier identifier, ToolMaterial material) {
        this(ItemTemplate.getNextId(), material);
        ItemTemplate.onConstructor(this, identifier);
    }
    
    public TemplatePickaxe(int id, ToolMaterial material) {
        super(id, material);
    }
}
