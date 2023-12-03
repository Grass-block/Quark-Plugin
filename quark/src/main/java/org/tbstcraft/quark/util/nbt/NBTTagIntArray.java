package org.tbstcraft.quark.util.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagIntArray extends NBTBase {
	
    /** The array of saved integers */
    public int[] intArray;
    private static final String __OBFID = "CL_00001221";

    public NBTTagIntArray() {}

    public NBTTagIntArray(int[] p_i45132_1_)
    {
        this.intArray = p_i45132_1_;
    }
    
	@Override
    public void writeTagContents(DataOutput p_74734_1_) throws IOException
    {
        p_74734_1_.writeInt(this.intArray.length);

        for (int i : this.intArray) {
            p_74734_1_.writeInt(i);
        }
    }

	@Override
	public void readTagContents(DataInput  p_152446_1_) throws IOException {
        int var4 = p_152446_1_.readInt();
        this.intArray = new int[var4];

        for (int var5 = 0; var5 < var4; ++var5)
        {
            this.intArray[var5] = p_152446_1_.readInt();
        }
	}

	@Override
	public byte getType() {
		return 11;
	}

}
