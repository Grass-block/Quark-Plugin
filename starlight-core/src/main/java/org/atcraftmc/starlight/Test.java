package org.atcraftmc.starlight;

import me.gb2022.commons.http.HttpMethod;
import me.gb2022.commons.http.HttpRequest;
import org.atcraftmc.starlight.data.storage.backend.LDBDataStorage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public interface Test {
    static void main(String[] args) {
        System.out.println(HttpRequest.http(HttpMethod.POST, "regulation.imgnews.cn/index.php")
                                   .param("action", "api_check")
                                   .param("mcid", "123")
                                   .browserBehavior(false)
                                   .header("Content-Type", "application/json")
                                   .header("User-Agent", ProductInfo.CORE_UA)
                                   .header("X-API-Source", "atcraftmc.cn/install#" + "a")
                                   .build()
                                   .request());
    }

}
