package org.tbstcraft.quark.data.jdbc;

import java.sql.DriverManager;
import java.sql.SQLException;

public interface JDBCEnvironment {
    static void loadLib() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    static Database createDatabase(String URL, String user, String name) {
        try {
            var connection = DriverManager.getConnection(URL, user, name);
            return new Database(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
