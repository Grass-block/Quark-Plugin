package org.atcraftmc.starlight.core.data.flex;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.atcraftmc.starlight.core.data.FlexibleMapService;
import org.atcraftmc.starlight.core.data.JDBCBasedDataService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public abstract class TableColumn<I> {
    protected final String name;
    protected final I defaultValue;
    private final Map<UUID, DBInstance<I>> caches = new ConcurrentHashMap<>();

    protected TableColumn(String name, I defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    //integrated
    public static TableColumn<Integer> integer(String id, int defaultValue) {
        return new IntColumn(id, defaultValue);
    }

    public static TableColumn<String> string(String id, int maxLength, String defaultValue) {
        return new StringColumn(id, defaultValue, maxLength);
    }

    public static <A> TableColumn<A> custom(String id, int maxLength, A defaultValue, FlexibleMapService.Codec<A> codec) {
        return new CustomColumn<>(id, defaultValue, maxLength, codec);
    }


    //privates
    private DBInstance<I> getDBInstance(FlexibleMapService ds) {
        return caches.computeIfAbsent(ds.getSessionUUID(), (s) -> new DBInstance<>(this, ds, 10));
    }

    public abstract PreparedStatement createColumn(Connection conn) throws SQLException;

    public abstract I dispatchResult(ResultSet rs) throws SQLException;

    public abstract void encodeStatement(PreparedStatement ps, I value) throws SQLException;

    public final I get(FlexibleMapService ds, UUID uuid) {
        return getDBInstance(ds).get(uuid, this.defaultValue);
    }

    public final void set(FlexibleMapService ds, UUID uuid, I value) {
        getDBInstance(ds).set(uuid, value);
    }

    private static final class DBInstance<I> {
        private final TableColumn<I> owner;
        private final FlexibleMapService service;
        private final Cache<UUID, I> cache;
        private final JDBCBasedDataService.WrappedConnection connection;

        public DBInstance(TableColumn<I> owner, FlexibleMapService service, int cacheLife) {
            this.owner = owner;
            this.service = service;
            this.cache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(cacheLife)).build();
            this.connection = new JDBCBasedDataService.WrappedConnection(service.connection, this.owner.name, "_col_");
        }

        public void verifyColumn() throws SQLException {
            if (this.service.hasColumnRegistered(this.owner.name)) {
                return;
            }

            try {
                this.owner.createColumn(this.connection).executeUpdate();
            } catch (SQLException e) {
                if (!e.getMessage().contains("Duplicate") && !e.getMessage().contains("duplicate")) {
                    throw e;
                }
            }
            this.service.onColumnAdded(this.owner.name);
        }

        public I get(UUID uuid, I defaultValue) {
            try {
                return this.cache.get(uuid, () -> {
                    this.verifyColumn();
                    var p = this.connection.prepareStatement("SELECT _col_ FROM _table_ WHERE uuid = ?");
                    p.setString(1, uuid.toString());

                    try (var rs = p.executeQuery()) {
                        if (rs.next()) {
                            return this.owner.dispatchResult(rs);
                        }
                    }

                    return defaultValue;
                });
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        public void set(UUID uuid, I value) {
            this.cache.put(uuid, value);

            try {
                this.service.createRow(uuid);
                this.verifyColumn();
                var p = this.connection.prepareStatement("UPDATE _table_ SET _col_ = ? WHERE uuid = ?");

                this.owner.encodeStatement(p, value);

                p.setString(2, uuid.toString());
                p.executeUpdate();
            } catch (Exception e) {
                this.cache.invalidate(uuid);
                throw new RuntimeException(e);
            }
        }
    }

    private static final class IntColumn extends TableColumn<Integer> {
        public IntColumn(String name, Integer defaultValue) {
            super(name, defaultValue);
        }

        @Override
        public PreparedStatement createColumn(Connection conn) throws SQLException {
            return conn.prepareStatement("ALTER TABLE _table_ ADD COLUMN _col_ INT DEFAULT " + defaultValue);
        }

        @Override
        public Integer dispatchResult(ResultSet rs) throws SQLException {
            return rs.getInt(1);
        }

        @Override
        public void encodeStatement(PreparedStatement ps, Integer value) throws SQLException {
            ps.setInt(1, value);
        }
    }

    private static class StringColumn extends TableColumn<String> {
        private final int maxLength;

        public StringColumn(String name, String defaultValue, int maxLength) {
            super(name, defaultValue);
            this.maxLength = maxLength;
        }

        @Override
        public PreparedStatement createColumn(Connection conn) throws SQLException {
            return conn.prepareStatement("ALTER TABLE _table_ ADD COLUMN _col_ varchar(" + this.maxLength + ") DEFAULT '" + this.defaultValue + "'");
        }

        @Override
        public String dispatchResult(ResultSet rs) throws SQLException {
            return rs.getString(1);
        }

        @Override
        public void encodeStatement(PreparedStatement ps, String value) throws SQLException {
            ps.setString(1, value);
        }
    }

    private static final class CustomColumn<I> extends TableColumn<I> {
        private final FlexibleMapService.Codec<I> codec;
        private final int maxLength;

        public CustomColumn(String name, I defaultValue, int maxLength, FlexibleMapService.Codec<I> codec) {
            super(name, defaultValue);
            this.codec = codec;
            this.maxLength = maxLength;
        }

        @Override
        public PreparedStatement createColumn(Connection conn) throws SQLException {
            return conn.prepareStatement("ALTER TABLE _table_ ADD COLUMN _col_ varchar(" + this.maxLength + ") DEFAULT '" + this.defaultValue + "'");
        }

        @Override
        public I dispatchResult(ResultSet rs) throws SQLException {
            return this.codec.decode(rs.getString(1));
        }

        @Override
        public void encodeStatement(PreparedStatement ps, I value) throws SQLException {
            ps.setString(1, this.codec.encode(value));
        }
    }
}
