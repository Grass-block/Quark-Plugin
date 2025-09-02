package org.atcraftmc.quark.chat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import me.gb2022.commons.http.HttpMethod;
import me.gb2022.commons.http.HttpRequest;
import me.gb2022.commons.reflect.Inject;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.bukkit.command.CommandSender;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.SharedObjects;
import org.atcraftmc.starlight.api.PluginMessages;
import org.atcraftmc.starlight.api.PluginStorage;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.qlib.language.LanguageItem;
import org.atcraftmc.starlight.framework.module.CommandModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.core.TaskService;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

@QuarkCommand(name = "chatgpt", permission = "+quark.chatgpt")
@SLModule
public final class ChatGPT extends CommandModule {
    public static final String GPT35 = "api.alcex.cn/API/gpt-3.5/";
    public static final String GPT40 = "api.alcex.cn/API/gpt-4/";
    public static final String PROMPT = "(请使用尽量简短的文字回答,给出的文字中将用“$$”代表一个空格,请使用纯文本回答)";
    public static final String MESSAGE = "[{\"role\":\"user\",\"content\":\"%s\"}]";

    public final Set<String> cooldown = new HashSet<>();

    @Inject("tip")
    private LanguageItem tip;

    @Inject
    private Logger logger;

    @Inject
    private LanguageEntry language;

    public static void chatgpt(String model, String content, Consumer<String> out, Consumer<String> badLine) {
        var s = HttpRequest.https(HttpMethod.GET, model)
                .browserBehavior(false)
                .param("type", "stream")
                .param("messages", MESSAGE.formatted(content.replace(" ", "$$") + PROMPT))
                .build()
                .request();

        StringBuilder sb = new StringBuilder();
        for (String delta : s.split("\n")) {
            String line = delta.replace("data:", "").trim();

            if (line.equals("[DONE]")) {
                break;
            }

            try {
                JsonElement doc = SharedObjects.JSON_PARSER.parse(line);

                if (doc instanceof JsonNull) {
                    continue;
                }

                JsonObject root = doc.getAsJsonObject();

                JsonArray choices = root.get("choices").getAsJsonArray();
                JsonObject choice = choices.get(0).getAsJsonObject();
                JsonObject delta2 = choice.get("delta").getAsJsonObject();
                if (!delta2.has("content")) {
                    continue;
                }

                String s2 = delta2.get("content").getAsString();
                sb.append(s2);
            } catch (Exception ignored) {
                badLine.accept(line);
            }
        }

        out.accept(sb.toString());
    }

    @Override
    public void enable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tip));
        super.enable();
    }

    @Override
    public void disable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tip));
        super.disable();
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (this.cooldown.contains(sender.getName())) {
            MessageAccessor.send(this.language, sender, "cooldown", 10);
            return;
        }

        this.cooldown.add(sender.getName());
        TaskService.global().delay(200, () -> this.cooldown.remove(sender.getName()));

        TaskService.async().run(() -> {
            StringBuilder sb = new StringBuilder();

            String model = GPT35;

            for (String str : args) {
                if (Objects.equals(str, "-gpt4")) {
                    model = GPT40;
                    continue;
                }
                sb.append(str).append(" ");
            }

            String request = sb.toString().trim();

            if (model.equals(GPT40)) {
                MessageAccessor.send(this.language, sender, "request-gpt4", "{;}" + request);
            } else {
                MessageAccessor.send(this.language, sender, "request", "{;}" + request);
            }

            chatgpt(
                    model,
                    request,
                    (s) -> MessageAccessor.send(this.language, sender, "response", s),
                    (line) -> this.logger.warn("pared bad response flow: " + line)
                   );
        });
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        tabList.add("-gpt4");
    }
}
