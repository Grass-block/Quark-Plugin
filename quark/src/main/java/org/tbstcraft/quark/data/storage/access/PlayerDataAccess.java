package org.tbstcraft.quark.data.storage.access;

import org.bukkit.entity.Player;
import org.tbstcraft.quark.data.PlayerDataService;

public interface PlayerDataAccess<I> extends StorageAccess<I, Player> {
    static PlayerDataAccess<Byte> byteElement(String id) {
        return new PlayerElementAccess<>(id) {
            @Override
            public Byte get(Player holder) {
                return PlayerDataService.get(holder).getByte(id);
            }
        };
    }

    static PlayerDataAccess<Short> shortElement(String id) {
        return new PlayerElementAccess<>(id) {

            @Override
            public Short get(Player value) {
                return PlayerDataService.get(value).getShort(id);
            }
        };
    }

    static PlayerDataAccess<Integer> integerElement(String id) {
        return new PlayerElementAccess<>(id) {
            @Override
            public Integer get(Player value) {
                return PlayerDataService.get(value).getInteger(id);
            }
        };
    }

    abstract class PlayerElementAccess<I> extends ElementAccess<I, Player> implements PlayerDataAccess<I> {
        protected PlayerElementAccess(String name) {
            super(name);
        }

        @Override
        public void save() {
            PlayerDataService.save(this.name);
        }

        @Override
        public void set(Player holder, I value) {
            PlayerDataService.get(holder).set(this.name, value);
        }

        @Override
        public boolean contains(Player holder) {
            return PlayerDataService.get(holder).hasKey(this.name);
        }
    }
}
