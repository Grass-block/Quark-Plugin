package org.tbstcraft.quark.data.jdbc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Blocking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Database implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger("DataBase");

    private final Map<String, PreparedStatement> commands = new HashMap<>();
    private final Connection connection;

    public Database(Connection connection) {
        this.connection = connection;
    }

    public void compileCommand(DBCommand command) {
        command.compile(this);
    }


    public PreparedStatement compile(String sql) {
        return this.commands.computeIfAbsent(sql,(k)-> {
            try {
                return this.connection.prepareStatement(k);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }


    public <I> I query(String sql, Object... params) {
        I result = null;

        var state = compile(sql);

        return result;
    }


    public Connection getHandle() {
        return this.connection;
    }

    @Override
    public void close() throws Exception {
        this.connection.close();
    }
}
