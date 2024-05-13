package org.tbstcraft.quark.service.network.http;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpHandlerContext {
    private final HttpExchange exchange;
    private int responseCode = -1;
    private JsonObject json = null;
    private byte[] data = null;

    private final Map<String, String> params = new HashMap<>();

    public HttpHandlerContext(HttpExchange exchange) {
        this.exchange = exchange;
        if (exchange.getRequestURI().getQuery() == null) {
            return;
        }
        for (String s : exchange.getRequestURI().getQuery().split("&")) {
            this.params.put(s.split("=")[0], s.split("=")[1]);
        }
    }

    public HttpExchange getExchange() {
        return this.exchange;
    }

    public byte[] getData() {
        if (this.data == null) {
            return this.json.toString().getBytes(StandardCharsets.UTF_8);
        }
        return this.data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public JsonObject createJsonReturn() {
        this.json = new JsonObject();
        return this.json;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public void error(int code, String err) {
        this.setResponseCode(code);
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getParam(String id){
        return this.params.get(id);
    }

    public JsonObject getJson() {
        return json;
    }
}
