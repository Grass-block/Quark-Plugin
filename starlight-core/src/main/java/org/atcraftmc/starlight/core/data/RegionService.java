package org.atcraftmc.starlight.core.data;

import org.atcraftmc.starlight.core.objects.Region;
import org.bson.BsonDocument;
import org.joml.Vector3d;

import java.util.UUID;

public class RegionService extends AbstractRegionService<Region> {
    public RegionService(String table) {
        super(table);
    }

    @Override
    public Region create(UUID id, UUID owner, String name, String world, Vector3d p1, Vector3d p2, BsonDocument meta) {
        return new Region(id, owner, name, world, p1, p2, meta);
    }
}
