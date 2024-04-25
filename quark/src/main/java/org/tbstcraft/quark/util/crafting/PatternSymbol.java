package org.tbstcraft.quark.util.crafting;

import org.bukkit.Material;

@SuppressWarnings("ClassCanBeRecord")
public class PatternSymbol {
    private final char id;
    private final Material require;

    public PatternSymbol(char id, Material require) {
        this.id = id;
        this.require = require;
    }

    public char getId() {
        return id;
    }

    public Material getRequire() {
        return require;
    }
}
