package org.tbstcraft.quark.packages.provider;

import org.tbstcraft.quark.packages.AbstractPackage;

import java.util.Set;

public interface PackageProvider {
    Set<AbstractPackage> createPackages();
}
