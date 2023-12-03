package org.tbstcraft.quark.util.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NBTTagList extends NBTBase {
    private List<NBTBase> tagList;
    private byte tagType;
    
    public NBTTagList() {
        super();
        this.tagList = new ArrayList<>();
    }
    
    @Override
    public void writeTagContents(final DataOutput dataOutput) throws IOException {
        if (this.tagList.size() > 0) {
            this.tagType = this.tagList.get(0).getType();
        }
        else {
            this.tagType = 1;
        }
        dataOutput.writeByte(this.tagType);
        dataOutput.writeInt(this.tagList.size());
        for (NBTBase nbtBase : this.tagList) {
            nbtBase.writeTagContents(dataOutput);
        }
    }
    
    @Override
    public void readTagContents(final DataInput dataInput) throws IOException {
        this.tagType = dataInput.readByte();
        final int int1 = dataInput.readInt();
        this.tagList = new ArrayList<>();
        for (int i = 0; i < int1; ++i) {
            final NBTBase tagOfType = createTagOfType(this.tagType);
            tagOfType.readTagContents(dataInput);
            this.tagList.add(tagOfType);
        }
    }
    
    @Override
    public byte getType() {
        return 9;
    }
    
    @Override
    public String toString() {
        return "" + this.tagList.size() + " entries of type " + getTagName(this.tagType);
    }
    
    public void setTag(final NBTBase hm) {
        this.tagType = hm.getType();
        this.tagList.add(hm);
    }

}
