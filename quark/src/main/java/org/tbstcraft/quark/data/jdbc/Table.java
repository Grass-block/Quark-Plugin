package org.tbstcraft.quark.data.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Table {
    private final Connection connection;
    private final String id;

    public Table(Connection connection, String id) {
        this.connection = connection;
        this.id = id;
    }


    static final class StatementBuilder {
        private final PreparedStatement statement;

        static StatementBuilder of(PreparedStatement ps) {
            return new StatementBuilder(ps);
        }

        StatementBuilder(PreparedStatement statement) {
            this.statement = statement;
        }

        public StatementBuilder arg(int id, int data) {
            try {
                this.statement.setInt(id, data);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return this;
        }

        public StatementBuilder arg(int id, String value) {
            try {
                this.statement.setString(id, value);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return this;
        }

        public PreparedStatement build() {
            return this.statement;
        }
    }
}
