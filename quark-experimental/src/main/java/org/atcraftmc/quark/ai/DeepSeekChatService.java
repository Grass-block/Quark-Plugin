package org.atcraftmc.quark.ai;

import me.gb2022.commons.http.HttpMethod;
import me.gb2022.commons.http.HttpRequest;
import org.atcraftmc.starlight.framework.service.Service;

public class DeepSeekChatService implements Service {


    public void rq(String token) {
        HttpRequest.http(HttpMethod.POST, "https://api.deepseek.com/chat/completions")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer %s".formatted(token))
                .build();
    }

}
