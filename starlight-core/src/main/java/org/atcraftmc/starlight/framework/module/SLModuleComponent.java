package org.atcraftmc.starlight.framework.module;

import me.gb2022.modular.subcomponent.SubComponent;
import org.bukkit.event.Listener;

public abstract class SLModuleComponent<E extends SLModule> extends SubComponent<E> implements Listener {
}
