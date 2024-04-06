package org.tbstcraft.quarkservice.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ink.flybird.jflogger.ILogger;
import ink.flybird.jflogger.LogManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class ProxyHandler implements HttpHandler {
    ILogger LOGGER= LogManager.getLogger("Proxy");

    private final String requestHost;
    private final String requestPrefix;

    public ProxyHandler(String requestHost, String requestPrefix) {
        this.requestHost = requestHost;
        this.requestPrefix = requestPrefix;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            HttpURLConnection con = getConnection(exchange);

            InputStream in = con.getInputStream();
            byte[] data = this.fixRequestOrigin(in.readAllBytes());

            exchange.sendResponseHeaders(con.getResponseCode(), data.length);
            exchange.getResponseBody().write(data);
            in.close();
            con.disconnect();

            exchange.close();
        } catch (Exception e) {
            if(e instanceof IOException){
                return;
            }
            LOGGER.exception(e);
        }
    }

    private byte[] fixRequestOrigin(byte[] data){
        String s = new String(data, StandardCharsets.UTF_8);
        if (s.contains("<head>")) {
            return s.replace("<head>", "<head><base href=\""+this.requestPrefix+"/\" />").getBytes(StandardCharsets.UTF_8);
        }else{
            return data;
        }
    }

    private HttpURLConnection getConnection(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();

        String path = uri.getPath().replaceFirst(this.requestPrefix, "");
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        String targetUrl = this.requestHost + path;
        //System.out.printf("%s -> %s%n", exchange.getRequestURI(), targetUrl);

        HttpURLConnection con = (HttpURLConnection) new URL(targetUrl).openConnection();
        con.setRequestMethod(exchange.getRequestMethod());
        return con;
    }
}
