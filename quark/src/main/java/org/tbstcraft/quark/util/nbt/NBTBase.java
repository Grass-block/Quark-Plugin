package org.tbstcraft.quark.util.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class NBTBase {
    public String key;

    public NBTBase() {
        super();
        this.key = null;
    }

    public static NBTBase createTagOfType(final byte byte1) {
        switch (byte1) {
            case 0 -> {
                return new NBTTagEnd();
            }
            case 1 -> {
                return new NBTTagByte();
            }
            case 2 -> {
                return new NBTTagShort();
            }
            case 3 -> {
                return new NBTTagInt();
            }
            case 4 -> {
                return new NBTTagLong();
            }
            case 5 -> {
                return new NBTTagFloat();
            }
            case 6 -> {
                return new NBTTagDouble();
            }
            case 7 -> {
                return new NBTTagByteArray();
            }
            case 8 -> {
                return new NBTTagString();
            }
            case 9 -> {
                return new NBTTagList();
            }
            case 10 -> {
                return new NBTTagCompound();
            }
            case 11 -> {
                return new NBTTagIntArray();
            }
            default -> {
                return null;
            }
        }
    }

    public static String getTagName(final byte byte1) {
        return switch (byte1) {
            case 0 -> "TAG_End";
            case 1 -> "TAG_Byte";
            case 2 -> "TAG_Short";
            case 3 -> "TAG_Int";
            case 4 -> "TAG_Long";
            case 5 -> "TAG_Float";
            case 6 -> "TAG_Double";
            case 7 -> "TAG_Byte_Array";
            case 8 -> "TAG_String";
            case 9 -> "TAG_List";
            case 10 -> "TAG_Compound";
            case 11 -> "TAG_Int_Array";
            default -> "UNKNOWN";
        };
    }

    public abstract void writeTagContents(final DataOutput dataOutput) throws IOException;

    public abstract void readTagContents(final DataInput dataInput) throws IOException;

    public abstract byte getType();

    public String getKey() {
        if (this.key == null) {
            return "";
        }
        return this.key;
    }

    public NBTBase setKey(final String string) {
        this.key = string;
        return this;
    }
}

