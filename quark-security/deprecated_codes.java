@Command(name = "explosion-def", op = true)
public static final class CommandHandler extends ModuleCommand<ExplosionDefender> {
    public CommandHandler(ExplosionDefender module) {
        super(module);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        this.checkException(args.length == 2);
        String arg2 = args[1];
        switch (args[0]) {
            case "mode" -> {
                if ((!Objects.equals(arg2, "cancel") && !Objects.equals(arg2, "block"))) {
                    this.sendExceptionMessage(sender);
                    return;
                }
                if (Objects.equals(arg2, "block") && (BukkitUtil.getBukkitVersion() < 16)) {
                    this.getModule().sendMessageTo(sender, "api_unsupported");
                    return;
                }
                this.getModule().getConfig()..set("mode", arg2);
                this.getModule().sendMessageTo(sender, "command_mode_set", arg2);
                this.getModule().reloadConfig();
            }
            case "broadcast" -> {
                if ((!Objects.equals(arg2, "none") && !Objects.equals(arg2, "operator") && !Objects.equals(arg2, "all"))) {
                    this.sendExceptionMessage(sender);
                    return;
                }
                this.getModule().getConfig()..set("broadcast", arg2);
                this.getModule().sendMessageTo(sender, "command_broadcast_set", arg2);
                this.getModule().reloadConfig();
            }
            case "record" -> {
                this.checkException(isBooleanOption(arg2));
                this.getModule().getConfig()..set("record", arg2);
                this.getModule().sendMessageTo(sender, "command_record_set", arg2);
                this.getModule().reloadConfig();

                if (arg2.equals("true")) {
                    this.getModule().openRecordStream();
                }
                if (arg2.equals("false")) {
                    this.getModule().closeRecordStream();
                }
            }
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] args, List<String> tabList) {
        if (args.length == 1) {
            tabList.add("mode");
            tabList.add("record");
            tabList.add("broadcast");
            return;
        }

        switch (args[0]) {
            case "mode" -> {
                tabList.add("cancel");
                tabList.add("block");
            }
            case "record" -> {
                tabList.add("on");
                tabList.add("off");
            }
            case "broadcast" -> {
                tabList.add("none");
                tabList.add("operator");
                tabList.add("all");
            }
        }
    }

    @Override
    public String getUsage() {
        return """
                以下是各个子命令(蓝色填入整数, 黄色填入名称, 绿色填入tab指定的选项):
                {white} - 保护模式: /explosion-defender {green}mode [operation]
                {white} - 记录开关: /explosion-defender {green}record [operation]
                {white} - 广播开关: /explosion-defender {green}broadcast [operation]
                 """;
    }
}


@Command(name = "ip-defender", op = true)
public static final class CommandHandler extends ModuleCommand<IPDefender> {
    public CommandHandler(IPDefender module) {
        super(module);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        String arg2 = args[1];
        switch (args[0]) {
            case "auto-ban-time" -> {
                this.checkException(args.length == 5);
                this.getModule().getConfig().getConfigSection().set("auto_ban_day_time", args[1]);
                this.getModule().getConfig().getConfigSection().set("auto_ban_hour_time", args[2]);
                this.getModule().getConfig().getConfigSection().set("auto_ban_minute_time", args[3]);
                this.getModule().getConfig().getConfigSection().set("auto_ban_second_time", args[4]);
                this.getModule().sendMessageTo(sender, "command_auto_ban_time_set", args[1], args[2], args[3], args[4]);
                this.getModule().reloadConfig();
            }
            case "auto-ban" -> {
                this.checkException(args.length == 2);
                this.checkException(isBooleanOption(arg2));
                this.getModule().getConfig().getConfigSection().set("auto_ban", arg2);
                this.getModule().sendMessageTo(sender, "command_auto_ban_set", arg2);
                this.getModule().reloadConfig();
            }
            case "record" -> {
                this.checkException(args.length == 2);
                this.checkException(isBooleanOption(arg2));
                this.getModule().getConfig().getConfigSection().set("record", arg2);
                this.getModule().sendMessageTo(sender, "command_record_set", arg2);
                this.getModule().reloadConfig();

                if (arg2.equals("true")) {
                    this.getModule().openRecordStream();
                }
                if (arg2.equals("false")) {
                    this.getModule().closeRecordStream();
                }
            }
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] args, List<String> tabList) {
        if (args.length == 1) {
            tabList.add("auto-ban");
            tabList.add("auto-ban-time");
            tabList.add("record");
            return;
        }

        switch (args[0]) {
            case "auto-ban", "record" -> {
                if (args.length + 1 > 2) {
                    return;
                }
                tabList.add("true");
                tabList.add("false");
            }
            case "auto-ban-time" -> {
                if (args.length > 5) {
                    return;
                }
                tabList.add("0");
                tabList.add("1");
                tabList.add("2");
            }
        }
    }

    @Override
    public String getUsage() {
        return """
                以下是各个子命令(蓝色填入整数, 黄色填入名称, 绿色填入tab指定的选项):
                {white} - 自动封禁: /ip-defender {green}auto-ban [operation]
                {white} - 封禁时间: /ip-defender {green}auto-ban-time {aqua}[day] [hour] [minute] [second]
                {white} - 记录开关: /ip-defender {green}record [operation]
                 """;
    }

    @Override
    public String getDescription() {
        return "对IP防护进行指定的操作。";
    }
}





    public Module _getModule(String moduleID) {
        return this.modules.get(moduleID);
    }

    public Map<String, Module> getModules() {
        return this.modules;
    }

    public void _reload(String id) {
        this.disable(id);
        this.enable(id);
    }

    public void _allReload() {
        SharedContext.PLUGIN_INSTANCE.reloadConfig();
        for (String id : this.modules.keySet()) {
            if (this.isModuleEnabled(id)) {
                this.reload(id);
            }
        }
    }

    public boolean _enable(String id) {
        if (!this.modules.containsKey(id)) {
            this.logger.warning("核心: 启用模块 %s 时出现错误: 无法找到中对应的模块实例".formatted(id));
            return false;
        }
        try {
            this.modules.get(id).loadConfig();
            this.modules.get(id).onEnable();
            setModuleEnabled(id, true);
            this.logger.info("核心: 启用模块 %s".formatted(id));
            return true;
        } catch (Exception e) {
            this.logger.warning("核心: 启用模块 %s 时出现错误".formatted(id));
            e.printStackTrace();
            return false;
        }
    }

    public boolean _disable(String id) {
        if (!this.modules.containsKey(id)) {
            this.logger.warning("核心: 停用模块 %s 时出现错误: 无法找到中对应的模块实例".formatted(id));
            return false;
        }
        try {
            this.logger.info("核心: 停用模块 %s".formatted(id));
            this.modules.get(id).onDisable();
            this.modules.get(id).saveConfig();
            this.setModuleEnabled(id, false);
            return true;
        } catch (Exception e) {
            this.logger.warning("核心: 停用模块 %s 时出现错误".formatted(id));
            e.printStackTrace();
            return false;
        }
    }

    public void _allEnable() {
        for (String id : this.modules.keySet()) {
            if (!this.isModuleEnabled(id)) {
                this.enable(id);
            }
        }
        SharedContext.reloadConfig();
    }

    public void _allDisable() {
        for (String id : this.modules.keySet()) {
            if (this.isModuleEnabled(id)) {
                this.disable(id);
            }
        }
        SharedContext.reloadConfig();
    }

    public void _setModuleEnabled(String id, boolean enable) {
        ConfigurationSection section = SharedContext.PLUGIN_CONFIG.getModuleSection();
        section.set(id, enable);
    }

    public boolean _isModuleEnabled(String s) {
        return SharedContext.PLUGIN_CONFIG.getModuleSection().getBoolean(s);
    }

    public void _registerModule(Module m) {
        ConfigurationSection section = SharedContext.PLUGIN_CONFIG.getModuleSection();
        boolean defaultStatement = SharedContext.PLUGIN_CONFIG.getConfigSection().getBoolean("default_module_status");
        this.modules.put(m.getModuleID(), m);

        if (!section.contains(m.getModuleID())) {
            this.setModuleEnabled(m.getModuleID(), defaultStatement);
        }
        if (this.isModuleEnabled(m.getModuleID())) {
            m.loadConfig();
            m.onEnable();
        }

        SharedContext.reloadConfig();
    }

    public void _unregisterModule(Module m) {
        m.onDisable();
        m.saveConfig();
    }

    public void _registerModule(String packageID, Class<? extends Module> clazz) {
        String moduleID = clazz.getAnnotation(TypeItem.class).value();
        try {
            Module m = clazz.getDeclaredConstructor().newInstance();
            this.registerModule(m);
            this.logger.info("核心: 从包组 %s 中成功导入模块 %s".formatted(packageID, m.getModuleID()));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            this.logger.warning("核心: 从包组 %s 导入模块 %s 时出现错误: %s".formatted(packageID, moduleID, e.getMessage()));
        }
    }

    public void unregisterModule(String packageID, Class<? extends Module> clazz) {
        String id = clazz.getAnnotation(TypeItem.class).value();
        this.unregisterModule(this.modules.get(id));
        this.logger.info("核心: 从包组 %s 中成功卸载模块 %s".formatted(packageID, id));
    }
@QuarkCommand(name = "we-size-def", op = true)
private static class CommandHandler extends ModuleCommand<WorldEditSectionSizeDefender> {
    protected CommandHandler(WorldEditSectionSizeDefender module) {
        super(module);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        String arg2 = args[1];
        switch (args[0]) {
            case "set" -> {
                this.getModule().getConfig().getRootSection().set("size", arg2);
                this.getModule().getConfig().reload();
                PluginModule module = this.getModule();
                this.getModule().getLanguage().sendMessageTo(sender, module.getId(), "command_size_set", arg2);
            }
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] args, List<String> tabList) {
        if (args.length == 1) {
            tabList.add("set");
            tabList.add("record");
            return;
        }
        if ("record".equals(args[0])) {
            if (args.length + 1 > 2) {
                return;
            }
            tabList.add("true");
            tabList.add("false");
        }
    }

    @Override
    public @NotNull String getUsage() {
        return """
                    以下是各个子命令(蓝色填入整数, 黄色填入名称, 绿色填入tab指定的选项):
                    {white} - 设置大小: /we-size-def {green}set {aqua}[size]
                    {white} - 记录开关: /we-size-def {green}record [operation]
                     """;
    }

    @Override
    public @NotNull String getDescription() {
        return "对we选区大小进行指定的操作。";
    }
}