package org.tbstcraft.quark.data.jdbc;

import org.jetbrains.annotations.Blocking;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@SuppressWarnings({"rawtypes","unchecked"})
public class DBCommand {
    private final String command;
    private ParamDispatcher.LambdaResolver[] resolvers;
    private PreparedStatement ps;

    public DBCommand(String command) {
        this.command = command;
    }

    public PreparedStatement getHandle() {
        return this.ps;
    }

    public void setData(Object... data) throws SQLException {
        if (data.length != this.resolvers.length) {
            throw new IllegalArgumentException("unexpected param count(expect %d, got %d)".formatted(data.length, resolvers.length));
        }

        for (var i = 0; i < data.length; i++) {
            var resolver = resolvers[i];

            resolver.exec(this.ps, i, data[i]);
        }
    }

    @Blocking
    public void compile(Database db) {
        this.ps = db.compile(this.command);
    }

    @Blocking
    public boolean execute() {
        try {
            return this.ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Blocking
    public ResultSet query() {
        try {
            return this.ps.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Blocking
    public int update() {
        try {
            return this.ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
