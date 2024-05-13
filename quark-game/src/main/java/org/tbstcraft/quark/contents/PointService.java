package org.tbstcraft.quark.contents;

import org.tbstcraft.quark.framework.service.QuarkService;
import org.tbstcraft.quark.framework.service.Service;

@QuarkService(id = "points")
public interface PointService extends Service {


    void add(String account, int amount);

    void remove(String account, int amount);

    int get(String account);

    default boolean cost(String account, int amount) {
        if (get(account) < amount) {
            return false;
        }
        remove(account, amount);
        return true;
    }

    final class Impl implements PointService {

        @Override
        public void add(String account, int amount) {

        }

        @Override
        public void remove(String account, int amount) {

        }

        @Override
        public int get(String account) {
            return 0;
        }
    }
}
