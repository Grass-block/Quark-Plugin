package org.atcraftmc.starlight.core.data;

import org.atcraftmc.starlight.core.JDBCService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public final class FlexibleMapService extends JDBCBasedDataService<String> {
    private JDBCService.JDBCDatabase database;

    public FlexibleMapService(String table) {
        super(table);
    }

    public void createRow(UUID uuid) throws SQLException {
        try (var p = this.connection.prepareStatement("SELECT uuid from _table_ WHERE uuid = ?")) {
            p.setString(1, uuid.toString());
            try (var s = p.executeQuery()) {
                if (!s.next()) {
                    try (var p2 = this.connection.prepareStatement("INSERT INTO _table_ (uuid) values (?)")) {
                        p2.setString(1, uuid.toString());
                        p2.executeUpdate();
                    }
                }
            }
        }
    }

    public void onColumnAdded(String col) throws SQLException {
        this.database.recordColumnRegisterFor(this.table, col);
    }

    public boolean hasColumnRegistered(String col) {
        return this.database.isColumnRegistered(this.table, col);
    }

    @Override
    public void init(JDBCService.JDBCDatabase db) throws SQLException {
        super.init(db);
        this.database = db;
    }

    @Override
    public PreparedStatement attemptCreateTable(Connection conn) throws SQLException {
        var sql = """
                CREATE TABLE IF NOT EXISTS _table_ (
                    uuid VARCHAR(36) PRIMARY KEY
                );
                """;

        return conn.prepareStatement(sql);
    }

    public interface Codec<I> {
        Codec<String> STRING = new Codec<>() {
            @Override
            public String decode(String data) {
                return data;
            }

            @Override
            public String encode(String data) {
                return data;
            }
        };

        String encode(I data);

        I decode(String data);
    }

}
