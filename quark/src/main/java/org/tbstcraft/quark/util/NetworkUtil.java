package org.tbstcraft.quark.util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public interface NetworkUtil {
    String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36";
    String ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7";

    static String httpGet(String url, boolean extraHeads) throws IOException {
        String str;
        HttpURLConnection con = getHttpURLConnection(url, extraHeads);

        var code = con.getResponseCode();
        if (code != 200) {
            InputStream error = con.getErrorStream();
            str = new String(error.readAllBytes(), StandardCharsets.UTF_8);
            error.close();
            con.disconnect();
            return str;
        }

        InputStream in = con.getInputStream();
        str = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        in.close();
        con.disconnect();
        return str;
    }

    static String httpGet(String url) throws IOException {
        return httpGet(url, true);
    }

    private static @NotNull HttpURLConnection getHttpURLConnection(String url, boolean extraHeads) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept", ACCEPT);
        if (extraHeads) {
            con.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
            con.setRequestProperty("Accept-Encoding", "gzip, deflate, br, zst");

            con.setRequestProperty("x-requested-with", "xmlhttprequest");
            con.setRequestProperty("Content-Type", "application/json");
        }
        return con;
    }

    static String httpPost(String url) {
        String str;
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);

            InputStream in = con.getInputStream();
            str = new String(in.readAllBytes());
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return str;
    }

    static RequestBuilder request(String url, boolean https) {
        return new RequestBuilder(url, https);
    }

    class RequestBuilder {
        private final StringBuilder builder = new StringBuilder();

        public RequestBuilder(String url, boolean https) {
            this.builder.append(https ? "https://" : "http://").append(url).append("?");
        }

        public RequestBuilder param(String key, String value) {
            this.builder.append(key).append("=").append(value).append("&");
            return this;
        }

        public String build() {
            return this.builder.deleteCharAt(builder.lastIndexOf("&")).toString();
        }

        public void get(Consumer<String> result, boolean extraHeads) {
            try {
                result.accept(httpGet(build(), extraHeads));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void get(Consumer<String> result) {
            get(result, true);
        }

        public RequestBuilder copy(Consumer<RequestBuilder> copied) {
            copied.accept(this);
            return this;
        }
    }
}
