package net.modificationstation.stationapi.api.registry;

import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.item.ItemBase;

import static net.modificationstation.stationapi.api.StationAPI.MODID;

public final class ItemRegistry extends SimpleRegistry<ItemBase> {

    public static final RegistryKey<Registry<ItemBase>> KEY = RegistryKey.ofRegistry(MODID.id("items"));
    public static final ItemRegistry INSTANCE = Registries.create(KEY, new ItemRegistry(), registry -> ItemBase.ironShovel, Lifecycle.experimental());
    public static final int ID_SHIFT = 256;
    public static final Int2IntFunction SHIFTED_ID = id -> id - ID_SHIFT;
    public static final int AUTO_ID = SHIFTED_ID.get(Registry.AUTO_ID);

    private ItemRegistry() {
        super(KEY, Lifecycle.experimental(), true);
    }
}
