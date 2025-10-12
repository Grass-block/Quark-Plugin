package cn.imgnews.regulation;

import com.google.gson.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

//Code provided by ATCraftMC - https://atcraftmc.cn
// - MIT Licence.
public final class IMGRegulationService {
    public static final UUID TEST_UUID = UUID.fromString("33550336-0000-0000-0000-000000000000");
    public static final String REQUEST_URL = "https://regulation.imgnews.cn/index.php?action=api_check";

    public static BanRecord queryQQ(String qq, String UA) {
        return deserialize(queryRaw("{\"qq\":\"%s\",\"uuid\":\"\"}".formatted(qq), UA));
    }

    public static BanRecord queryUUID(UUID uuid, String UA) {
        return deserialize(queryRaw("{\"qq\":\"\",\"uuid\":\"%s\"}".formatted(uuid.toString()), UA));
    }

    private static String queryRaw(String payload, String ua) {
        return HttpRequest.https(HttpMethod.POST, "regulation.imgnews.cn")
                .path("/index.php")
                .param("action", "api_check")
                .header("User-Agent", ua)
                .build()
                .requestWithPayload(payload);
    }

    private static BanRecord deserialize(String content) {
        return BanRecordParser.parseBlacklistResponse(content);
    }

    public static void main(String[] args) {
        System.out.println(BanRecordParser.toJson(queryUUID(TEST_UUID, "Test")));
    }

    private enum HttpMethod {
        GET("GET"),
        PUT("PUT"),
        POST("POST");

        final String method;

        HttpMethod(String method) {
            this.method = method;
        }

        @Override
        public String toString() {
            return this.method;
        }
    }

    public static final class BanRecord {
        private String status;
        private int count;
        private List<BanEntry> records;

        // 构造函数
        public BanRecord() {
        }

        public BanRecord(String status, int count, List<BanEntry> records) {
            this.status = status;
            this.count = count;
            this.records = records;
        }

        // Getter和Setter方法
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public List<BanEntry> getRecords() {
            return records;
        }

        public void setRecords(List<BanEntry> records) {
            this.records = records;
        }

        @Override
        public String toString() {
            return "BanRecord{" +
                    "status='" + status + '\'' +
                    ", count=" + count +
                    ", records=" + records +
                    '}';
        }
    }

    public static final class BanEntry {
        private UUID uuid;
        private String qqNumber;
        private String mcid;
        private String reason;
        private String punishment;
        private String server;
        private Date createdAt;

        // 构造函数
        public BanEntry() {
        }

        public BanEntry(
                UUID uuid, String qqNumber, String mcid, String reason, String punishment, String server, Date createdAt
        ) {
            this.uuid = uuid;
            this.qqNumber = qqNumber;
            this.mcid = mcid;
            this.reason = reason;
            this.punishment = punishment;
            this.server = server;
            this.createdAt = createdAt;
        }

        // Getter和Setter方法
        public UUID getUuid() {
            return uuid;
        }

        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }

        public String getQqNumber() {
            return qqNumber;
        }

        public void setQqNumber(String qqNumber) {
            this.qqNumber = qqNumber;
        }

        public String getMcid() {
            return mcid;
        }

        public void setMcid(String mcid) {
            this.mcid = mcid;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getPunishment() {
            return punishment;
        }

        public void setPunishment(String punishment) {
            this.punishment = punishment;
        }

        public String getServer() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }

        public Date getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Date createdAt) {
            this.createdAt = createdAt;
        }

        @Override
        public String toString() {
            return "BanEntry{" + "uuid=" + uuid + ", qqNumber='" + qqNumber + '\'' + ", mcid='" + mcid + '\'' + ", reason='" + reason + '\'' + ", punishment='" + punishment + '\'' + ", server='" + server + '\'' + ", createdAt=" + createdAt + '}';
        }
    }

    private static final class BanRecordParser {

        private static final Gson gson;

        static {
            // 创建Gson构建器，配置自定义适配器
            GsonBuilder gsonBuilder = new GsonBuilder();

            // 注册UUID反序列化器
            gsonBuilder.registerTypeAdapter(UUID.class, new UuidDeserializer());

            // 注册日期反序列化器
            gsonBuilder.registerTypeAdapter(Date.class, new DateDeserializer());

            gson = gsonBuilder.create();
        }

        /**
         * 从JSON字符串解析黑名单响应
         *
         * @param jsonString JSON字符串
         * @return BlacklistResponse对象
         */
        public static BanRecord parseBlacklistResponse(String jsonString) {
            return gson.fromJson(jsonString, BanRecord.class);
        }

        /**
         * 将对象转换为JSON字符串
         *
         * @param obj 要转换的对象
         * @return JSON字符串
         */
        public static String toJson(Object obj) {
            return gson.toJson(obj);
        }

        // UUID反序列化器
        private static class UuidDeserializer implements JsonDeserializer<UUID> {
            @Override
            public UUID deserialize(
                    JsonElement json, Type typeOfT,
                    JsonDeserializationContext context
            ) throws JsonParseException {
                try {
                    return UUID.fromString(json.getAsString());
                } catch (IllegalArgumentException e) {
                    throw new JsonParseException("Invalid UUID format: " + json.getAsString(), e);
                }
            }
        }

        // 日期反序列化器
        private static class DateDeserializer implements JsonDeserializer<Date> {
            private final SimpleDateFormat dateFormat =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            @Override
            public Date deserialize(
                    JsonElement json, Type typeOfT,
                    JsonDeserializationContext context
            ) throws JsonParseException {
                try {
                    return dateFormat.parse(json.getAsString());
                } catch (ParseException e) {
                    throw new JsonParseException("Invalid date format: " + json.getAsString(), e);
                }
            }
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static final class HttpRequest {
        private final Map<String, String> headers;
        private final String url;
        private final HttpMethod method;

        public HttpRequest(Map<String, String> headers, String url, HttpMethod method) {
            this.headers = headers;
            this.url = url;
            this.method = method;
        }

        public static Builder https(HttpMethod method, String url) {
            return new Builder(true, method, url);
        }

        public static Builder http(HttpMethod method, String url) {
            return new Builder(false, method, url);
        }

        public HttpMethod getMethod() {
            return method;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public String getUrl() {
            return url;
        }

        public HttpURLConnection createConnection() throws IOException {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

            con.setRequestMethod(this.method.toString());
            con.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
            );

            for (String k : this.headers.keySet()) {
                con.setRequestProperty(k, this.headers.get(k));
            }

            return con;
        }

        public String request() {
            return requestWithPayload((Consumer<OutputStream>) null);
        }

        public String requestWithPayload(Consumer<OutputStream> stream) {
            try {
                String str;
                HttpURLConnection con = createConnection();

                if (stream != null) {
                    con.setDoOutput(true);
                    var s = con.getOutputStream();
                    stream.accept(s);
                    s.flush();
                }

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
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public String requestWithPayload(String payload) {
            return requestWithPayload((s) -> {
                try {
                    s.write(payload.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        public static final class Builder {
            private final Map<String, String> headers = new HashMap<>();
            private final StringBuilder args = new StringBuilder();

            private final StringBuilder url = new StringBuilder();

            private final HttpMethod method;

            public Builder(boolean https, HttpMethod method, String url) {
                this.url.append(https ? "https://" : "http://");
                this.method = method;
                this.url.append(url);
            }

            public Builder path(String path) {
                url.append(path);
                return this;
            }

            public Builder param(String key, String value) {
                this.args.append(key).append("=").append(value).append("&");
                return this;
            }

            public Builder header(String key, String value) {
                this.headers.put(key, value);
                return this;
            }

            public Builder browserBehavior(boolean extra) {
                header(
                        "Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"
                );

                if (extra) {
                    header("Accept-Encoding", "gzip, deflate, br, zst").header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
                    header("x-requested-with", "xmlhttprequest");
                    header("Content-Type", "application/json");
                }

                return this;
            }

            public HttpRequest build() {
                return new HttpRequest(
                        this.headers,
                        this.url + "?" + (this.args.isEmpty() ? this.args : this.args.deleteCharAt(this.args.length() - 1)),
                        this.method
                );
            }
        }
    }
}
