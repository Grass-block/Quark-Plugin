package org.atcraftmc.starlight.core.data.flex;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class JsonColumn extends TableColumn<JsonObject> {
    public JsonColumn(String name) {
        super(name, new JsonObject());
    }

    @Override
    public PreparedStatement createColumn(Connection conn) throws SQLException {
        return conn.prepareStatement("ALTER TABLE _table_ ADD COLUMN _col_ varchar(8192) DEFAULT '{}'");
    }

    @Override
    public JsonObject dispatchResult(ResultSet rs) throws SQLException {
        return (JsonObject) JsonParser.parseString(rs.getString(1));
    }

    @Override
    public void encodeStatement(PreparedStatement ps, JsonObject value) throws SQLException {
        ps.setString(1, value.toString());
    }
}
