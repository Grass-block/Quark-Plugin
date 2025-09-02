# 制作扩展包
文: GrassBlock2022

-----

> 注意：此部分需要读者拥有较强的Java和Bukkit功底。

## 引入依赖
事实上 quark.jar编译设为compileOnly即可
如何设置就不用我多说了对吧:)

另外项目还需要引入Bukkit支持库，环境方面可以支持AdventureAPI(Quark-Platform会带)

## 扩展包结构
其实就是个大号插件，主类继承特定的类用于注册内容，像这样：

```java
@QuarkPackageProvider
public final class QuarkBase extends MultiPackageProvider {

    @Override
    public Set<PackageInitializer> createInitializers() {
        return Set.of(
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark_security.json"),);
    }
}

```

MultiPackageProvider实现了JavaPlugin, 所以这是一个有效的插件主类。
记得重写方法后要加super，否则无法唤起模块注入。

## 注册内容
### 方法1: 编写Json模块描述符初始化包
```json
{
  "id": "quark-contents",
  "version": "0.10",
  "package_namespace": "org.atcraftmc.quark.contents",
  "modules": {
    "elevator": ".Elevator"
  },
  "languages": [
    "quark-contents:zh_cn"
  ],
  "configs": [
    "quark-contents"
  ]
}

```

### 方法2: 利用PackageBuilderInitializer初始化包
```java
class Example{
    PackageBuilderInitializer init(){
        return new PackageBuilderInitializer("quark-core", FeatureAvailability.BOTH)
                .service(TaskService.class)
                .module("modrinth-version-check", ModrinthVersionCheck.class)
                .language("quark-core", "zh_cn")
                .language("quark-core", "en_us")
                .config("quark-core");
    }
}
```

## 编写模块

### 基础的模块框架和约定

```java
package org.atcraftmc.quark.storage;

@QuarkModule(version="1.0")
public final class ExampleModule extends PackageModule {
    @Override
    public void enable(){
    }
    
    @Override
    public void disable(){
    }
}

```

### 检查平台兼容性
```java
@QuarkModule(version="1.0")
public final class ExampleModule extends PackageModule {
    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requireClass(() -> Class.forName("org.bukkit.inventory.SmithingInventory"));
        Compatibility.requirePDC(); //require persistent data container API
        Compatibility.requirePlugin("WorldEdit"); //require plugin
    }
}

```

### 模块服务
```java
@QuarkModule
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class ExampleModule extends PackageModule {
    //your impl
}
```
```java
package org.tbstcraft.quark.framework.module.services;

public interface ServiceType {
    String EVENT_LISTEN = "qb:el";
    String REMOTE_MESSAGE = "qb:rm";
    String PLUGIN_MESSAGE = "qb:pm";
    String CLIENT_MESSAGE = "qb:cm";
}
```

### 依赖注入
```java
@QuarkModule(id = "chat-component", version = "1.3.0")
public final class ChatComponent extends PackageModule {
    @Inject("tip")
    private LanguageItem tip;
    
    @Inject("-quark.example.permission")
    private Permission examplePermission;
    
    @Inject
    private LanguageEntry language;

}
```