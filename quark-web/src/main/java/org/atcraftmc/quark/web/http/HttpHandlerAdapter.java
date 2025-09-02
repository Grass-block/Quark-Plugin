package org.atcraftmc.quark.web.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.atcraftmc.starlight.framework.module.AbstractModule;
import org.atcraftmc.starlight.framework.module.ModuleManager;
import me.gb2022.commons.TriState;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

public final class HttpHandlerAdapter implements HttpHandler {
    private final Method method;
    private final Object handler;

    HttpHandlerAdapter(Method method, Object handler) {
        this.method = method;
        this.handler = handler;
    }

    @Override
    public void handle(HttpExchange exchange) {
        if(this.handler instanceof AbstractModule module) {
            if (ModuleManager.getInstance().getModuleStatus(module.getFullId()) != TriState.FALSE) {
                return;
            }
        }

        byte[] data;
        HttpHandlerContext ctx = new HttpHandlerContext(exchange);
        try {
            this.method.invoke(this.handler, ctx);
        } catch (Exception e) {
            e.printStackTrace();
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
            exchange.getResponseHeaders().add("Content-Type", ctx.contentType().toString());
            exchange.sendResponseHeaders(ctx.getResponseCode(), data.length);

            OutputStream os = exchange.getResponseBody();
            os.write(data);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
