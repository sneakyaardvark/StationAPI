package net.modificationstation.stationapi.mixin.tools;

import net.minecraft.entity.animal.Sheep;
import net.minecraft.item.ItemBase;
import net.minecraft.item.ItemInstance;
import net.modificationstation.stationapi.api.StationAPI;
import net.modificationstation.stationapi.impl.item.ShearsOverrideEvent;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Sheep.class)
public class MixinSheep {

    @Redirect(method = "interact(Lnet/minecraft/entity/player/PlayerBase;)Z", at = @At(value = "FIELD", target = "Lnet/minecraft/item/ItemInstance;itemId:I", opcode = Opcodes.GETFIELD))
    private int hijackSheepShearing(ItemInstance itemInstance) {
        return StationAPI.EVENT_BUS.post(
                ShearsOverrideEvent.builder()
                        .itemStack(itemInstance)
                        .overrideShears(false)
                        .build()
        ).overrideShears ? ItemBase.shears.id : itemInstance.itemId;
    }
}
