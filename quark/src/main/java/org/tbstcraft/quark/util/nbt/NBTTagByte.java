package org.tbstcraft.quark.util.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagByte extends NBTBase
{
    public byte byteValue;
    
    public NBTTagByte() {
        super();
    }
    
    public NBTTagByte(final byte byte1) {
        super();
        this.byteValue = byte1;
    }
    
    @Override
    public void writeTagContents(final DataOutput dataOutput) throws IOException {
        dataOutput.writeByte(this.byteValue);
    }
    
    @Override
    public void readTagContents(final DataInput dataInput) throws IOException {
        this.byteValue = dataInput.readByte();
    }
    
    @Override
    public byte getType() {
        return 1;
    }
    
    @Override
    public String toString() {
        return "" + this.byteValue;
    }
}
