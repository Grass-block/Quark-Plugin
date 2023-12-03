package org.tbstcraft.quark.security;

import org.tbstcraft.quark.pkg.PluginPackage;
import org.tbstcraft.quark.pkg.QuarkPackage;

@QuarkPackage
public final class PluginMain extends PluginPackage {
    @Override
    public String getPackageId() {
        return "quark_security";
    }
}
