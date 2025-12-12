package org.atcraftmc.starlight.core.data.region;

import me.gb2022.commons.nbt.NBT;
import org.atcraftmc.starlight.core.data.JDBCBasedDataService;
import org.atcraftmc.starlight.data.storage.StorageTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegionDBService extends JDBCBasedDataService<StorageTable> {


    public RegionDBService(String table) {
        super(table);
    }

    @Override
    public String getTableNamePlaceholder() {
        return "_rdb_";
    }

    @Override
    public PreparedStatement attemptCreateTable(Connection conn) throws SQLException {
        var sql = """
                CREATE TABLE _rdb_ (
                    uuid CHAR(36) PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    region LONG NOT NULL,
                    data BLOB NOT NULL
                );
                """;

        return conn.prepareStatement(sql);
    }

    public Map<String, StorageTable> loadData(int cx, int cz) {
        long key = RegionPos.encode(cx, cz);

        try (var ps = this.connection.prepareStatement("SELECT name,data from _rdb_ where region=?")) {
            ps.setLong(1, key);
            try (var rs = ps.executeQuery()) {
                var map = new HashMap<String, StorageTable>();
                while (rs.next()) {
                    map.put(rs.getString("uuid"), (StorageTable) NBT.read(rs.getBlob("data").getBinaryStream()));
                }

                return map;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updateRegionPosition(UUID uuid,long newPosition) {
        return false;//todo
    }

}
