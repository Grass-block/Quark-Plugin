package org.atcraftmc.starlight.core.data;

import org.atcraftmc.starlight.core.JDBCService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class TagMapService extends JDBCBasedDataService<String> {
    private WrappedConnection payloadHandlerA;
    private WrappedConnection payloadHandlerB;
    private WrappedConnection payloadHandlerC;
    private WrappedConnection payloadHandlerD;

    public TagMapService(String table) {
        super(table);
    }

    @Override
    public void init(JDBCService.JDBCDatabase db) throws SQLException {
        super.init(db);
        this.payloadHandlerA = new WrappedConnection(this.connection, "payload_a", "_payload_");
        this.payloadHandlerB = new WrappedConnection(this.connection, "payload_b", "_payload_");
        this.payloadHandlerC = new WrappedConnection(this.connection, "payload_c", "_payload_");
        this.payloadHandlerD = new WrappedConnection(this.connection, "payload_d", "_payload_");
    }

    @Override
    public String getTableNamePlaceholder() {
        return "_tagmap_";
    }

    @Override
    public PreparedStatement attemptCreateTable(Connection conn) throws SQLException {
        var sql = """
                CREATE TABLE IF NOT EXISTS _tagmap_ (
                 uuid varchar(36) PRIMARY KEY UNIQUE,
                 payload_a varchar(4096) default ';',
                 payload_b varchar(4096) default ';',
                 payload_c varchar(4096) default ';',
                 payload_d varchar(4096) default ';'
                )
                """;

        return conn.prepareStatement(sql);
    }

    public int getSlot(String tag) {
        if (tag.contains(";")) {
            throw new IllegalArgumentException("Invalid tag format: " + tag);
        }

        return Math.abs(tag.hashCode()) % 4;
    }

    public WrappedConnection getPayloadHandlerFor(String data) {
        return switch (getSlot(data)) {
            case 0 -> this.payloadHandlerA;
            case 1 -> this.payloadHandlerB;
            case 2 -> this.payloadHandlerC;
            case 3 -> this.payloadHandlerD;
            default -> throw new IllegalArgumentException("Invalid slot: " + getSlot(data));
        };
    }


    public Set<String> get(UUID uuid) throws SQLException {
        var result = new HashSet<String>();

        try (var p = this.connection.prepareStatement("SELECT * FROM _tagmap_ WHERE uuid = ?")) {
            p.setString(1, uuid.toString());
            try (var set = p.executeQuery()) {
                if (!set.next()) {
                    return Set.of();
                }

                result.addAll(Arrays.asList(set.getString("payload_a").split(";")));
                result.addAll(Arrays.asList(set.getString("payload_b").split(";")));
                result.addAll(Arrays.asList(set.getString("payload_c").split(";")));
                result.addAll(Arrays.asList(set.getString("payload_d").split(";")));
            }
        }
        result.removeIf(String::isEmpty);
        return result;
    }

    public boolean has(UUID uuid, String data) throws SQLException {
        var sql = "SELECT uuid FROM _tagmap_ WHERE uuid = ? AND _payload_ LIKE ?";

        try (var p = this.getPayloadHandlerFor(data).prepareStatement(sql)) {
            p.setString(1, uuid.toString());
            p.setString(2, ";" + data + ";");

            try (var rs = p.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean hasEntry(UUID uuid) throws SQLException {
        var sql = "SELECT uuid FROM _tagmap_ WHERE uuid = ?";

        try (var p = this.connection.prepareStatement(sql)) {
            p.setString(1, uuid.toString());

            try (var rs = p.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void add(UUID uuid, String data) throws SQLException {
        if (!hasEntry(uuid)) {
            var sql = "INSERT INTO _tagmap_ (uuid,payload_a,payload_b,payload_c,payload_d) values ( ?,';_init_;',';_init_;',';_init_;',';_init_;' )";
            try (var p = this.connection.prepareStatement(sql)) {
                p.setString(1, uuid.toString());
                p.executeUpdate();
            }
        }

        var sql = """
                UPDATE _tagmap_
                SET _payload_ = CONCAT(_payload_, ?)
                WHERE uuid = ?;
                """;

        try (var p = this.getPayloadHandlerFor(data).prepareStatement(sql)) {
            p.setString(1, data + ";");
            //p.setString(2, ";" + data + ";");
            p.setString(2, uuid.toString());
            p.executeUpdate();
        }
    }

    public void delete(UUID uuid, String data) throws SQLException {
        var sql = """
                UPDATE _tagmap_
                SET _payload_ = REPLACE(_payload_, ?, '')
                WHERE _payload_ LIKE ? AND uuid = ?;
                """;

        try (var p = this.getPayloadHandlerFor(data).prepareStatement(sql)) {
            p.setString(1, data);
            p.setString(2, data + ";");
            p.setString(3, uuid.toString());
            p.executeUpdate();
        }
    }
}
