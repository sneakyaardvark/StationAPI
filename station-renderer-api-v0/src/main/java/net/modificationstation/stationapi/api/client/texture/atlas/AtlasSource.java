package net.modificationstation.stationapi.api.client.texture.atlas;

import net.modificationstation.stationapi.api.client.texture.SpriteContents;
import net.modificationstation.stationapi.api.client.texture.SpriteLoader;
import net.modificationstation.stationapi.api.registry.Identifier;
import net.modificationstation.stationapi.api.resource.Resource;
import net.modificationstation.stationapi.api.resource.ResourceFinder;
import net.modificationstation.stationapi.api.resource.ResourceManager;

import java.util.function.Predicate;
import java.util.function.Supplier;

import static net.modificationstation.stationapi.api.StationAPI.MODID;

public interface AtlasSource {
    ResourceFinder RESOURCE_FINDER = new ResourceFinder(MODID + "/textures", ".png");

    void load(ResourceManager var1, SpriteRegions var2);

    AtlasSourceType getType();

    interface SpriteRegion extends Supplier<SpriteContents> {
        default void close() {}
    }

    interface SpriteRegions {
        default void add(Identifier id, Resource resource) {
            this.add(id, () -> SpriteLoader.load(id, resource));
        }

        void add(Identifier var1, SpriteRegion var2);

        void removeIf(Predicate<Identifier> var1);
    }
}

