package org.tbstcraft.quark.service.network.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.tbstcraft.quark.framework.module.ModuleManager;
import org.tbstcraft.quark.util.ObjectStatus;
import org.tbstcraft.quark.framework.module.PackageModule;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

public final class HttpHandlerAdapter implements HttpHandler {
    private final Method method;
    private final PackageModule handler;

    HttpHandlerAdapter(Method method, PackageModule handler) {
        this.method = method;
        this.handler = handler;
    }

    @Override
    public void handle(HttpExchange exchange) {
        if (ModuleManager.getModuleStatus(this.handler.getFullId()) != ObjectStatus.ENABLED) {
            return;
        }

        byte[] data;
        HttpHandlerContext ctx = new HttpHandlerContext(exchange);
        try {
            this.method.invoke(this.handler, ctx);
        } catch (Exception e) {
            this.handler.getLogger().severe(e.getMessage());
        }
        data = ctx.getData();

        try {
            if (data == null) {
                exchange.sendResponseHeaders(502, 0);
                OutputStream os = exchange.getResponseBody();
                os.write("ERR_EMPTY_RESPONSE".getBytes(StandardCharsets.UTF_8));
                os.close();
                return;
            }
            exchange.sendResponseHeaders(ctx.getResponseCode(), data.length);
            OutputStream os = exchange.getResponseBody();
            os.write(data);
            os.close();
        } catch (IOException e) {
            this.handler.getLogger().severe(e.getMessage());
        }
    }
}
