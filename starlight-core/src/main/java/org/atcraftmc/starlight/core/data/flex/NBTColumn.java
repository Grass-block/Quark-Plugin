package org.atcraftmc.starlight.core.data.flex;

import me.gb2022.commons.nbt.NBT;
import me.gb2022.commons.nbt.NBTTagCompound;
import org.atcraftmc.starlight.core.data.FlexibleMapService;
import org.atcraftmc.starlight.data.storage.DataEntry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.UUID;

public final class NBTColumn extends TableColumn<DataEntry> {
    public static final String DEFAULT_EMPTY_B64_NBT = "H4sIAAAAAAAA/+NiYGAAAHg/+U4EAAAA";


    public NBTColumn(String name) {
        super(name, null);
    }

    @Override
    public PreparedStatement createColumn(Connection conn) throws SQLException {
        return conn.prepareStatement("ALTER TABLE _table_ ADD COLUMN _col_ varchar(8192) DEFAULT '" + DEFAULT_EMPTY_B64_NBT + "'");
    }


    @Override
    public DataEntry dispatchResult(ResultSet rs) throws SQLException {
        var bb = Base64.getDecoder().decode(rs.getString(1));
        var b = new ByteArrayInputStream(bb);

        return new DataEntry.DBDataEntry((NBTTagCompound) NBT.readZipped(b), this, null, null);
    }

    @Override
    public void encodeStatement(PreparedStatement ps, DataEntry value) throws SQLException {
        var b = new ByteArrayOutputStream();
        NBT.writeZipped(value, b);
        ps.setString(1, Base64.getEncoder().encodeToString(b.toByteArray()));
    }

    @Override
    public DataEntry getDefaultValue(FlexibleMapService ds, UUID uuid) {
        return new DataEntry.DBDataEntry(new NBTTagCompound(), this, ds, uuid);
    }

    @Override
    public DataEntry processValue(DataEntry value, FlexibleMapService ds, UUID uuid) {
        ((DataEntry.DBDataEntry) value).setContext(this, ds, uuid);
        return value;
    }
}
