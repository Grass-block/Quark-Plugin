package org.tbstcraft.quark.data.jdbc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Blocking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Database implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger("DataBase");
    private final Connection connection;

    public Database(Connection connection) {
        this.connection = connection;
    }

    public void compileCommand(DBCommand command){
        command.compile(this);
    }

    @Blocking
    public PreparedStatement compile(String command){
        try {
            return this.connection.prepareStatement(command);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getHandle(){
        return this.connection;
    }

    @Override
    public void close() throws Exception {
        this.connection.close();
    }

    @Override
    protected void finalize() throws Throwable {
        if(this.connection.isClosed()){
            return;
        }
        LOGGER.warn("unexpected unclosed DB object!");
        close();
    }
}
