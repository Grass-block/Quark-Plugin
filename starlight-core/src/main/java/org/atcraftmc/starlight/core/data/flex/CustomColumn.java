package org.atcraftmc.starlight.core.data.flex;

import org.atcraftmc.starlight.core.data.FlexibleMapService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

final class CustomColumn<I> extends TableColumn<I> {
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
