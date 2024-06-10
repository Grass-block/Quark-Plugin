package org.tbstcraft.quark.framework.data.assets;

public final class AssetNotFoundException extends RuntimeException{
    public AssetNotFoundException(String name) {
        super(name);
    }
}
