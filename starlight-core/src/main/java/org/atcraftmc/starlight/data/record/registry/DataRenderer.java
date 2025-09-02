package org.atcraftmc.starlight.data.record.registry;

import me.gb2022.commons.nbt.NBTTagCompound;
import org.bukkit.entity.Player;

public interface DataRenderer<I> {
    DataRenderer<Number> NUMBER = new DataRenderer<>() {

        @Override
        public String render(Number o) {
            return o.toString();
        }

        @Override
        public void render(NBTTagCompound tag, String name, Number o) {
            if (o instanceof Byte n) {
                tag.setByte(name, n);
            }
            if (o instanceof Short n) {
                tag.setShort(name, n);
            }
            if (o instanceof Integer n) {
                tag.setInteger(name, n);
            }
            if (o instanceof Long n) {
                tag.setLong(name, n);
            }
            if (o instanceof Float n) {
                tag.setFloat(name, n);
            }
            if (o instanceof Double n) {
                tag.setDouble(name, n);
            }
        }
    };

    DataRenderer<String> STRING = new DataRenderer<>() {
        @Override
        public String render(String o) {
            return o;
        }

        @Override
        public void render(NBTTagCompound tag, String name, String o) {
            tag.setString(name, o);
        }
    };

    DataRenderer<Boolean> BOOLEAN = new DataRenderer<>() {
        @Override
        public void render(NBTTagCompound tag, String name, Boolean o) {
            tag.setBoolean(name, o);
        }

        @Override
        public String render(Boolean o) {
            return o.toString();
        }
    };

    DataRenderer<Player> PLAYER = new DataRenderer<>() {

        @Override
        public String render(Player o) {
            return o.getName();
        }

        @Override
        public void render(NBTTagCompound tag, String name, Player o) {
            tag.setString(name, o.getName());
        }
    };

    String render(I o);

    void render(NBTTagCompound tag, String name, I o);
}
