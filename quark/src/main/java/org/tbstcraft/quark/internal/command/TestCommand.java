package org.tbstcraft.quark.internal.command;

import me.gb2022.commons.nbt.NBT;
import me.gb2022.commons.nbt.NBTTagCompound;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.tbstcraft.quark.data.storage.StorageTable;
import org.tbstcraft.quark.foundation.command.CoreCommand;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

@QuarkCommand(name = "test", permission = "-quark.test")
public final class TestCommand extends CoreCommand {
    @Override
    public void suggest(CommandSuggestion suggestion) {

    }

    @Override
    public void execute(CommandExecution context) {
        StorageTable table = new StorageTable();

        table.setBoolean("boolean", true);
        table.setByte("byte", (byte) 1);
        table.setShort("short", (short) 2);
        table.setInteger("integer", 3);
        table.setString("string", "test");

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        NBT.write(table, new DataOutputStream(os));

        NBTTagCompound tag = (NBTTagCompound) NBT.read(new ByteArrayInputStream(os.toByteArray()));

        System.out.println("boolean->" + tag.getBoolean("boolean"));
        System.out.println("byte->" + tag.getByte("byte"));
        System.out.println("short->" + tag.getShort("short"));
        System.out.println("integer->" + tag.getInteger("integer"));
        System.out.println("string->" + tag.getString("string"));
    }
}
