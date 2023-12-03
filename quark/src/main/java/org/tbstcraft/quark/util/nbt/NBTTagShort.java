package org.tbstcraft.quark.util.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagShort extends NBTBase
{
    public short shortValue;
    
    public NBTTagShort() {
        super();
    }
    
    public NBTTagShort(final short short1) {
        super();
        this.shortValue = short1;
    }
    
    @Override
    public void writeTagContents(final DataOutput dataOutput) throws IOException {
        dataOutput.writeShort(this.shortValue);
    }
    
    @Override
    public void readTagContents(final DataInput dataInput) throws IOException {
        this.shortValue = dataInput.readShort();
    }
    
    @Override
    public byte getType() {
        return 2;
    }
    
    @Override
    public String toString() {
        return "" + this.shortValue;
    }
}
