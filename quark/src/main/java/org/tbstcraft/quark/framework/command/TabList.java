package org.tbstcraft.quark.framework.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class TabList extends ArrayList<String> {
    @Override
    public boolean add(String s) {
        if (this.contains(s)) {
            return false;
        }
        return super.add(s);
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        List<String> lst = new ArrayList<>();
        for (String s : c) {
            if (this.contains(s)) {
                continue;
            }
            lst.add(s);
        }
        return super.addAll(lst);
    }
}
