package org.atcraftmc.quark.web;

import com.google.gson.JsonObject;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import org.atcraftmc.starlight.framework.service.SLService;
import org.atcraftmc.starlight.framework.service.Service;

import java.util.function.Consumer;

@SLService(id = "http-server")
public class VertxHttpService implements Service {
    private final HttpServer server = VertxContextService.vertx().createHttpServer();
    private final Router router = Router.router(VertxContextService.vertx());
    private final int port;

    public VertxHttpService(int port) {
        this.port = port;
    }

    public static String json(Consumer<JsonObject> builder) {
        var json = new JsonObject();
        builder.accept(json);
        return json.toString();
    }

    public static Router router() {
        return Router.router(VertxContextService.vertx());
    }

    @Override
    public void onEnable() {
        this.server.listen(this.port);
    }

    @Override
    public void onDisable() {
        this.server.close();
    }

    public void createListener(Object handle) {
        for (var method : handle.getClass().getMethods()) {
            if(!method.isAnnotationPresent(VertxRouter.class)){
                continue;
            }

            var meta = method.getAnnotation(VertxRouter.class);
            var route =  meta.value();

        }



        this.router.route("/").handler(routingContext -> {});

        router.route().handler(ctx -> {

            // 所有的请求都会调用这个处理器处理
            HttpServerResponse response = ctx.response();
            response.putHeader("content-type", "text/plain");

            // 写入响应并结束处理
            response.end("Hello World from Vert.x-Web!");
        });
    }
}
