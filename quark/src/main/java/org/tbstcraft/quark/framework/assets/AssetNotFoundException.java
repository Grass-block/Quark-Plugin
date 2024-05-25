package org.tbstcraft.quark.framework.assets;

public class AssetNotFoundException extends RuntimeException{
    public AssetNotFoundException(String name) {
        super(name);
    }
}
