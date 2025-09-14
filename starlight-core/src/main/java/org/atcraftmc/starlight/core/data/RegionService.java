package org.atcraftmc.starlight.core.data;

import org.atcraftmc.starlight.core.objects.Region;
import org.atcraftmc.starlight.util.BsonCodec;
import org.bukkit.Location;
import org.joml.Vector3d;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class RegionService extends JDBCBasedDataService<Region> {
    public RegionService(String table) {
        super(table);
    }

    @Override
    public PreparedStatement attemptCreateTable(Connection conn) throws SQLException {
        var createTableSQL = """
                CREATE TABLE IF NOT EXISTS _region_ (
                    uuid VARCHAR(36) PRIMARY KEY,
                    name VARCHAR(128) NOT NULL UNIQUE,
                    world VARCHAR(128) NOT NULL,
                    x0 DOUBLE NOT NULL,
                    y0 DOUBLE NOT NULL,
                    z0 DOUBLE NOT NULL,
                    x1 DOUBLE NOT NULL,
                    y1 DOUBLE NOT NULL,
                    z1 DOUBLE NOT NULL,
                    meta VARCHAR(16384) NOT NULL
                );
                """;

        return conn.prepareStatement(createTableSQL);
    }

    @Override
    public void encode(PreparedStatement ps, Region data) throws SQLException {
        var p0 = data.getMinPoint();
        var p1 = data.getMaxPoint();

        ps.setString(1, data.getUuid().toString());
        ps.setString(2, data.getName());
        ps.setString(3, data.getWorldId());
        ps.setDouble(4, p0.getX());
        ps.setDouble(5, p0.getY());
        ps.setDouble(6, p0.getZ());
        ps.setDouble(7, p1.getX());
        ps.setDouble(8, p1.getY());
        ps.setDouble(9, p1.getZ());

        ps.setString(10, BsonCodec.string(data.getExtraMetadata()));
    }

    @Override
    public Region decode(ResultSet rs) throws SQLException {
        return new Region(
                UUID.fromString(rs.getString("uuid")),
                rs.getString("name"),
                rs.getString("world"),
                new Vector3d(rs.getDouble("x0"), rs.getDouble("y0"), rs.getDouble("z0")),
                new Vector3d(rs.getDouble("x1"), rs.getDouble("y1"), rs.getDouble("z1")),
                BsonCodec.string(rs.getString("meta"))
        );
    }

    public boolean add(Region region) throws SQLException {
        if (existName(region.getName())) {
            throw new SQLException("名称已存在: " + region.getName());
        }

        try (var p = this.connection.prepareStatement(
                "INSERT INTO _region_ (uuid, name, world, x0, y0, z0, x1, y1, z1, meta) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            encode(p, region);
            return p.executeUpdate() > 0;
        }
    }

    public Set<String> listNames() throws SQLException {
        try (var ps = this.connection.prepareStatement("SELECT name FROM _region_")) {
            return queryNames(ps);
        }
    }

    public boolean rename(String origin, String dest) throws SQLException {
        try (var p = this.connection.prepareStatement("UPDATE _region_ SET name = ? WHERE name = ?")) {
            p.setString(1, dest);
            p.setString(2, origin);
            return p.executeUpdate() > 0;
        }
    }

    public boolean delete(String name) throws SQLException {
        try (var p = connection.prepareStatement("DELETE FROM _region_ WHERE name = ?")) {
            p.setString(1, name);
            return p.executeUpdate() > 0;
        }
    }

    public Set<String> queryNames(PreparedStatement ps) throws SQLException {
        var result = new HashSet<String>();
        try (var rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(rs.getString("name"));
            }
        }

        return result;
    }

    public boolean existName(String name) throws SQLException {
        try (var p = connection.prepareStatement("SELECT uuid FROM _region_ WHERE name = ?")) {
            p.setString(1, name);
            try (var rs = p.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Set<Region> queryWaypoints(PreparedStatement ps) throws SQLException {
        var result = new HashSet<Region>();

        try (var rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(decode(rs));
            }
        }

        return result;
    }

    public boolean update(Region data) throws SQLException {
        var sql = "UPDATE _region_ SET name = ?, world = ?, x0 = ?, y0 = ?, z0 = ?, x1 = ?, y1 = ?, z1 = ?, meta = ? WHERE uuid = ?";

        var p0 = data.getMinPoint();
        var p1 = data.getMaxPoint();

        try (var ps = connection.prepareStatement(sql)) {
            ps.setString(1, data.getName());
            ps.setString(2, data.getWorldId());
            ps.setDouble(3, p0.getX());
            ps.setDouble(4, p0.getY());
            ps.setDouble(5, p0.getZ());
            ps.setDouble(6, p1.getX());
            ps.setDouble(7, p1.getY());
            ps.setDouble(8, p1.getZ());
            ps.setString(9, BsonCodec.string(data.getExtraMetadata()));
            return ps.executeUpdate() > 0;
        }
    }

    public Optional<Region> byName(String name) throws SQLException {
        try (var p = connection.prepareStatement("SELECT * FROM _region_ WHERE name = ?")) {
            p.setString(1, name);

            try (var rs = p.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(decode(rs));
                }
                return Optional.empty();
            }
        }
    }

    public Set<Region> queryInbound(Location location) throws SQLException {
        var world = location.getWorld().getName();
        var x = location.getX();
        var y = location.getY();
        var z = location.getZ();

        var sql = "SELECT * FROM _region_ WHERE ? >= x0 AND ? <= x1 AND ? >= y0 AND ? <= y1 AND ? >= z0 AND ? <= z1 AND world = ?";

        try (var ps = this.connection.prepareStatement(sql)) {
            renderBoundCall(world, x, y, z, ps);

            var result = new HashSet<Region>();

            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(decode(rs));
                }
            }

            return result;
        }
    }

    public boolean isAnyHit(Location location) throws SQLException {
        var world = location.getWorld().getName();
        var x = location.getX();
        var y = location.getY();
        var z = location.getZ();

        var sql = "SELECT uuid FROM _region_ WHERE ? >= x0 AND ? <= x1 AND ? >= y0 AND ? <= y1 AND ? >= z0 AND ? <= z1 AND world = ? LIMIT 1";

        try (var ps = this.connection.prepareStatement(sql)) {
            renderBoundCall(world, x, y, z, ps);

            try (var rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void renderBoundCall(String world, double x, double y, double z, PreparedStatement ps) throws SQLException {
        ps.setDouble(1, x);
        ps.setDouble(2, x);
        ps.setDouble(3, y);
        ps.setDouble(4, y);
        ps.setDouble(5, z);
        ps.setDouble(6, z);
        ps.setString(7, world);
    }


    @Override
    public String getTableNamePlaceholder() {
        return "_region_";
    }
}
