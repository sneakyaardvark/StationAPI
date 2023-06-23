package net.modificationstation.stationapi.api.client.event.render.model;

import net.modificationstation.stationapi.api.client.registry.BlockModelPredicateProviderRegistry;
import net.modificationstation.stationapi.api.event.registry.RegistryEvent;

public final class BlockModelPredicateProviderRegistryEvent extends RegistryEvent<BlockModelPredicateProviderRegistry> {
    public BlockModelPredicateProviderRegistryEvent() {
        super(BlockModelPredicateProviderRegistry.INSTANCE);
    }
}
