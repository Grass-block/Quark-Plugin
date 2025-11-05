import org.atcraftmc.starlight.core.objects.Region;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bukkit.Location;
import org.bukkit.World;
import org.joml.Vector3d;

import java.util.Set;
import java.util.UUID;

public class BuildArea extends Region {
    private Set<UUID> invited;
    private String buildStyle;

    public BuildArea(UUID owner, String name, World world, Location p0, Location p1) {
        super(owner, name, world, p0, p1);
    }

    public BuildArea(UUID owner, String name, String world, Vector3d p0, Vector3d p1) {
        super(owner, name, world, p0, p1);
    }

    public BuildArea(UUID uuid, UUID owner, String name, String world, Vector3d p0, Vector3d p1, BsonDocument extraMetadata) {
        super(uuid, owner, name, world, p0, p1, extraMetadata);
    }

    @Override
    public void serializeMetadata(BsonDocument metadata) {
        metadata.append("build_style", new BsonString(this.buildStyle));
    }
}
