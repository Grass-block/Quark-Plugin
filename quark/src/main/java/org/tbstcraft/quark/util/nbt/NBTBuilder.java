package org.tbstcraft.quark.util.nbt;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * common NBT access
 *
 * @author GrassBlock2022
 */
public class NBTBuilder {
    /**
     * read tag
     * @param dataInput input
     * @return tag
     * @throws IOException input exception
     */
    public static NBTBase read(final DataInput dataInput) throws IOException {
        final byte byte1 = dataInput.readByte();
        if (byte1 == 0) {
            return new NBTTagEnd();
        }
        final NBTBase tagOfType = NBTBase.createTagOfType(byte1);
        tagOfType.key = dataInput.readUTF();
        tagOfType.readTagContents(dataInput);
        return tagOfType;
    }

    /**
     * write tag to data output
     * @param hm tag
     * @param dataOutput output
     * @throws IOException output exception
     */
    public static void write(final NBTBase hm, final DataOutput dataOutput) throws IOException {
        dataOutput.writeByte(hm.getType());
        if (hm.getType() == 0) {
            return;
        }
        dataOutput.writeUTF(hm.getKey());
        hm.writeTagContents(dataOutput);
    }
}
