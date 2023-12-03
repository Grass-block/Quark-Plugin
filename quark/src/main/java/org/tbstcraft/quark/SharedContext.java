package org.tbstcraft.quark;

import org.tbstcraft.quark.web.HTTPServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SharedContext {
    public static ExecutorService SHARED_THREAD_POOL= Executors.newFixedThreadPool(9);
    public static HTTPServer HTTP_SERVER;
}
