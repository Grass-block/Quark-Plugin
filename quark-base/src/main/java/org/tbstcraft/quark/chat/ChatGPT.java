package org.tbstcraft.quark.chat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import me.gb2022.commons.reflect.Inject;
import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.api.PluginMessages;
import org.tbstcraft.quark.data.PlaceHolderStorage;
import org.tbstcraft.quark.data.language.LanguageItem;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.internal.task.TaskService;
import org.tbstcraft.quark.util.NetworkUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

@QuarkCommand(name = "chatgpt", permission = "-quark.chatgpt")
@QuarkModule
public final class ChatGPT extends CommandModule {
    public static final String GPT35 = "api.alcex.cn/API/gpt-3.5/";
    public static final String GPT40 = "api.alcex.cn/API/gpt-4/";
    public static final String PROMPT = "(请使用尽量简短的文字回答,给出的文字中将用“$$”代表一个空格,请使用纯文本回答)";

    public final Set<String> cooldown = new HashSet<>();

    @Inject("tip")
    private LanguageItem tip;

    public static void chatgpt(String model, String content, Consumer<String> out, Consumer<String> badLine) {
        NetworkUtil.request(model, true)
                .param("type", "stream")
                .param("messages", "[{\"role\":\"user\",\"content\":\"%s\"}]".formatted(content.replace(" ", "$$") + PROMPT))
                .get((s) -> {
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
                });
    }

    @Override
    public void enable() {
        PlaceHolderStorage.get(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, HashSet.class, (s) -> s.add(this.tip));
        super.enable();
    }

    @Override
    public void disable() {
        PlaceHolderStorage.get(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, HashSet.class, (s) -> s.remove(this.tip));
        super.disable();
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (this.cooldown.contains(sender.getName())) {
            getLanguage().sendMessage(sender, "cooldown", 10);
            return;
        }

        this.cooldown.add(sender.getName());
        TaskService.laterTask(200, () -> this.cooldown.remove(sender.getName()));

        TaskService.asyncTask(() -> {
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
                getLanguage().sendMessage(sender, "request-gpt4", "{;}" + request);
            } else {
                getLanguage().sendMessage(sender, "request", "{;}" + request);
            }

            chatgpt(model, request,
                    (s) -> getLanguage().sendMessage(sender, "response", s),
                    (line) -> this.getLogger().warning("pared bad response flow: " + line)
            );
        });
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        tabList.add("-gpt4");
    }
}
