package org.tbstcraft.quark.event;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HTTPQueryEvent {
    private final HttpExchange context;
    private final Map<String, String> params = new HashMap<>();
    private byte[] response = "NULL".getBytes(StandardCharsets.UTF_8);

    public HTTPQueryEvent(HttpExchange context) {
        this.context = context;

    }

    public String getQueryPath() {
        return this.context.getHttpContext().getPath();
    }

    public HttpExchange getContext() {
        return context;
    }


    public byte[] getResponse() {
        return response;
    }

    public void setResponse(byte[] response) {
        this.response = response;
    }

    public String getParam(String id) {
        return this.params.get(id);
    }

    public void makeStatusReturn(String status, String msg) {
        JsonObject object = new JsonObject();
        object.addProperty("status", status);
        object.addProperty("msg", msg);
        this.makeReturn(object);
    }

    public void makeExceptionReturn() {
        this.makeStatusReturn("error", "internal_exception");
    }

    public void makeReturn(JsonObject object) {
        this.context.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
        this.setResponse(object.toString().getBytes(StandardCharsets.UTF_8));
    }
}
