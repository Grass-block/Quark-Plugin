# 修改插件平台
文: GrassBlock2022

-----

## 前言

Starlight带来的全新架构中，部分API使用额外的Platform-pipeline架构进行实际实现的选择。
调用方的数据不再直接通过不同的平台API传递，而是流入管线并寻找最合适的实现进行处理。
而这也就提供了为Starlight的插件平台替换后端的可能性。

如果还不理解管线架构的话，尝试想象一下Netty的Channel Pipeline结构。
调用方的数据将会流过各个handler，直到有handler愿意捕捉它。

Starlight依赖的qlib-bukkit平台提供了默认的bukkit后端，
为不同API（bukkit/spigot/paper）提供了尽可能贴合的调用。
但同时，你也可以注入自己的管线处理器，甚至换掉原版的实现。

## 实现自定义PluginPlatform

PluginPlatform是一个抽象接口，其提供了多个方法定义用于隔离插件与平台间的交互。

您可以像这样创建一个PluginPlatform类:

```java
    public static class PAPIEvalPlatform extends ForwardingPluginPlatform {
        @Override
        public String globalFormatMessage(String source) {
            return super.globalFormatMessage(PlaceholderAPI.setPlaceholders(null, source));
        }

        @Override
        public void sendMessage(Object pointer, ComponentLike message) {
            super.sendMessage(pointer, message);
        }

        @Override
        public MinecraftLocale locale(Object sender) {
            return super.locale(sender);
        }

        @Override
        public ComponentLike examineComponent(ComponentLike component, Object pointer, MinecraftLocale locale) {
            return super.examineComponent(component, pointer, locale);
        }

        @Override
        public void broadcastLine(Function<MinecraftLocale, ComponentLike> component, boolean opOnly, boolean toConsole) {
            super.broadcastLine(component, opOnly, toConsole);
        }

        @Override
        public String pluginsFolder() {
            return super.pluginsFolder();
        }
    }
    
    //额……其实你可以选择不用重写多余的方法的 嘻嘻

```

super方法调用将会自动传递你的调用到下一个可用(更接近底层)的handler(默认实现).

当然也可以在数据流过handler的时候微微修改一下，比如这里注入的handler将会把文本丢给PlaceHolder处理，再向下传递。

## 注册你的平台

平台管线可以用`PluginPlatform.global()`静态函数获得。
你可以简单的把你的平台丢到任何位置，但首先你需要一个不重复的平台ID。
平台ID没有明确定义格式, 但是形如`<namespace>:<id>`的名字会更规范一些。
当然如果你写一个`锟斤拷烫烫烫`之类的名字貌似除了有点大病之外也不会引起bug(猜猜为啥我知道)

对于管线来说，越往后的平台越靠近'上方', 而调用传入的顺序是从上往下依次流动。

举个例子: 以下代码将会在starlight-core平台调用之前添加一个处理类目:

```java
//这里是你的伟大业务代码的一个小小片段
    @Override
    public void enable() {
        this.extension.register();

        PluginPlatform.global().addAfter("starlight:core", "starlight:papi-inject", new PAPIEvalPlatform());
    }
```

而用完了或者扩展卸载的时候千万别忘了把你的平台处理器卸掉:

```java
//这里是你的伟大业务代码的另一个小小片段
    @Override
    public void disable() {
        this.extension.unregister();

        PluginPlatform.global().remove("starlight:papi-inject");
    }
```

诚然，你大可以直接把默认管线也换掉（因为默认管线实际上也是PluginPlatform）。但这样做的意义是什么？

## 一个简单的示例

以下代码摘自starlight-core中使用ProtocolLib消息API替换掉默认消息来扩大文本兼容性的一个模块。

```java
package org.atcraftmc.starlight.internal;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.kyori.adventure.text.ComponentLike;
import org.atcraftmc.qlib.platform.ForwardingPluginPlatform;
import org.atcraftmc.qlib.platform.PluginPlatform;
import org.atcraftmc.starlight.foundation.ComponentSerializer;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.bukkit.entity.Player;

@SLModule(internal = true,description = "Create more compatible message sending via ProtocolLib.")
public final class ProtocolLibPlatformInjector extends PackageModule {

    @Override
    public void enable() {
        PluginPlatform.global().addAfter("starlight:core", "starlight:plib-inject", new ProtocolLibMessageImpl());
    }

    @Override
    public void disable() {
        PluginPlatform.global().remove("starlight:plib-inject");
    }

    public static class ProtocolLibMessageImpl extends ForwardingPluginPlatform {

        @Override
        public void sendMessage(Object pointer, ComponentLike message) {
            if (!(pointer instanceof Player player)) {
                super.sendMessage(pointer, message);
                return;
            }

            var packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.CHAT);
            var component = WrappedChatComponent.fromJson(ComponentSerializer.json(message));

            packet.getChatComponents().write(0, component);

            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

```