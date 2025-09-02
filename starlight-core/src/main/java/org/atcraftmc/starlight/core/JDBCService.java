package org.atcraftmc.starlight.core;

import org.atcraftmc.starlight.Configurations;
import org.atcraftmc.starlight.core.data.TagMapService;
import org.atcraftmc.starlight.framework.service.SLService;
import org.atcraftmc.starlight.framework.service.ServiceInject;
import org.atcraftmc.starlight.framework.service.ServiceLayer;
import org.atcraftmc.starlight.util.FilePath;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

@SLService(id = "jdbc", layer = ServiceLayer.FOUNDATION)
public interface JDBCService {
    Map<String, JDBCDatabase> REGISTRY = new HashMap<>();

    String SL_SHARED = "starlight:shared";
    String SL_LOCAL = "starlight:default";

    @ServiceInject
    static void start() {
        Configurations.groupedYML("database", Set.of("database/sl-default.yml", "database/sl-shared.yml")).forEach((k, d) -> {
            var id = d.getString("id");

            if (d.contains("link")) {
                REGISTRY.put(id, new InlineDatabase(d.getString("link")));
                return;
            }

            var driver = Driver.valueOf(d.getString("driver"));
            var url = d.getString("url");
            var user = d.getString("user");
            var password = d.getString("password");

            var db = new SimpleDatabase(driver, url, user, password);
            REGISTRY.put(id, db);
        });

        REGISTRY.values().forEach(JDBCDatabase::open);
    }

    @ServiceInject
    static void stop() {
        REGISTRY.values().forEach(JDBCDatabase::close);
    }

    static Optional<JDBCDatabase> getDB(String id) {
        return Optional.ofNullable(REGISTRY.get(id));
    }

    static Connection getConnection(String id) {
        return getDB(id).orElseThrow().getConnection();
    }

    enum Driver {
        MYSQL("mysql", "com.mysql.cj.jdbc.Driver"),
        H2("h2", "org.h2.Driver");

        final String id;
        final String className;

        Driver(String id, String driver) {
            this.id = id;
            this.className = driver;
        }

        String id() {
            return id;
        }

        String className() {
            return className;
        }
    }


    interface JDBCDatabase {

        default void open() {
        }

        default void close() {
        }

        default TagMapService getFlexibleMetaMap() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        void recordColumnRegisterFor(String table, String column) throws SQLException;

        boolean isColumnRegistered(String table, String column);

        Connection getConnection();
    }

    class SimpleDatabase implements JDBCDatabase {
        private final Driver driver;
        private final String url;
        private final String user;
        private final String password;
        private final TagMapService flexibleMetaMap = new TagMapService("_sl_flexible_meta");
        private final Map<String, Set<String>> columns = new HashMap<>();
        private Connection conn;

        public SimpleDatabase(Driver driver, String url, String user, String password) {
            this.driver = driver;
            this.url = url;
            this.user = user;
            this.password = password;
        }

        @Override
        public void open() {
            if (this.conn != null) {
                throw new IllegalStateException("Already opened!");
            }

            this.conn = createConnection();
            try {
                this.flexibleMetaMap.init(this);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close() {
            if (this.conn == null) {
                throw new IllegalStateException("Already Closed!");
            }
            try {
                this.conn.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            this.conn = null;
        }

        @Override
        public TagMapService getFlexibleMetaMap() {
            return this.flexibleMetaMap;
        }

        private Set<String> getColumnMetaFor(String table) {
            var uuid = UUID.nameUUIDFromBytes(table.getBytes(StandardCharsets.UTF_8));
            return this.columns.computeIfAbsent(table,(s)-> {
                try {
                    return new HashSet<>(this.flexibleMetaMap.get(uuid));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        @Override
        public void recordColumnRegisterFor(String table, String column) throws SQLException {
            var uuid = UUID.nameUUIDFromBytes(table.getBytes(StandardCharsets.UTF_8));
            getColumnMetaFor(table).add(column);
            this.flexibleMetaMap.add(uuid, column);
        }

        @Override
        public boolean isColumnRegistered(String table, String column) {
            return getColumnMetaFor(table).contains(column);
        }

        @Override
        public Connection getConnection() {
            return this.conn;
        }

        private Connection createConnection() {
            try {
                Class.forName(this.driver.className());
                return DriverManager.getConnection(
                        "jdbc:" + this.driver.id() + ":" + this.url.replace("{folder}", FilePath.slDataFolder()),
                        this.user,
                        this.password
                );
            } catch (SQLException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    final class InlineDatabase implements JDBCDatabase {
        private final String id;

        public InlineDatabase(String id) {
            this.id = id;
        }

        @Override
        public Connection getConnection() {
            return JDBCService.getConnection(id);
        }

        @Override
        public TagMapService getFlexibleMetaMap() {
            return JDBCService.getDB(this.id).orElseThrow().getFlexibleMetaMap();
        }

        @Override
        public void recordColumnRegisterFor(String table, String column) throws SQLException {
            JDBCService.getDB(this.id).orElseThrow().recordColumnRegisterFor(table, column);
        }

        @Override
        public boolean isColumnRegistered(String table, String column) {
            return JDBCService.getDB(this.id).orElseThrow().isColumnRegistered(table, column);
        }
    }
}
