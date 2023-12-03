package org.tbstcraft.quark.command;

/*
@Command(name = "module", op = true)
public final class ModuleManagerCommand extends AbstractCommand {

    @Override
    public boolean shouldReturn() {
        return false;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        String mode = args[0];

        if (this.switchArgumentL1(sender, mode)) {
            return;
        }
        this.checkException(args.length == 2);
        this.switchArgumentL2(sender, mode, args[1]);
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] args, List<String> tabList) {
        if (args.length == 1) {
            tabList.add("enable");
            tabList.add("disable");
            tabList.add("reload");
            tabList.add("reload-config");
            tabList.add("reload-all-config");
            tabList.add("reload-all");
            tabList.add("enable-all");
            tabList.add("disable-all");
            tabList.add("import");
            tabList.add("list");
            tabList.add("info");
        }

        if (args.length == 2 && (!args[0].contains("_all") || !Objects.equals(args[0], "list"))) {
            tabList.addAll(ModuleManager.MODULES.keySet());
        }
    }







    @Override
    public void sendExceptionMessage(CommandSender sender) {
        SharedContext.PLUGIN_CONFIG.sendMessageTo(sender, "module_command_exception");
    }

    @Override
    public void sendPermissionMessage(CommandSender sender) {
        SharedContext.PLUGIN_CONFIG.sendMessageTo(sender, "module_command_exception");
    }

    private void switchArgumentL2(CommandSender sender, String mode, String moduleID) {
        switch (mode) {
            case "enable" -> {
                if (!this.moduleManager.enable(moduleID)) {
                    SharedContext.PLUGIN_CONFIG.sendMessageTo(sender, "module_enable_failed", moduleID);
                    return;
                }
                if (SharedContext.PLUGIN_CONFIG.isEnabled("module_status_lock")) {
                    SharedContext.PLUGIN_CONFIG.sendMessageTo(sender, "module_status_locked");
                    return;
                }
                SharedContext.reloadConfig();
                SharedContext.PLUGIN_CONFIG.sendMessageTo(sender, "module_enable_success", moduleID);
            }
            case "disable" -> {
                if (!this.moduleManager.disable(moduleID)) {
                    SharedContext.PLUGIN_CONFIG.sendMessageTo(sender, "module_disable_failed", moduleID);
                    return;
                }
                if (SharedContext.PLUGIN_CONFIG.isEnabled("module_status_lock")) {
                    SharedContext.PLUGIN_CONFIG.sendMessageTo(sender, "module_status_locked");
                    return;
                }
                SharedContext.reloadConfig();
                SharedContext.PLUGIN_CONFIG.sendMessageTo(sender, "module_disable_success", moduleID);
            }
            case "reload" -> {
                this.moduleManager.reload(moduleID);
                SharedContext.PLUGIN_CONFIG.sendMessageTo(sender, "module_reload_success", moduleID);
            }
            case "reload-config" -> {
                this.moduleManager.getModule(moduleID).loadConfig();
                SharedContext.PLUGIN_CONFIG.sendMessageTo(sender, "module_reload_config_success", moduleID);
            }
            case "info" -> {
                SharedContext.PLUGIN_CONFIG.sendMessageTo(sender, "module_info", moduleID);
                this.moduleManager.getModule(moduleID).displayInfo(sender);
            }
            default -> this.sendExceptionMessage(sender);
        }
    }

    public boolean switchArgumentL1(CommandSender sender, String mode) {
        switch (mode) {
            case "enable-all" -> {
                if (SharedContext.PLUGIN_CONFIG.isEnabled("module_status_lock")) {
                    SharedContext.PLUGIN_CONFIG.sendMessageTo(sender, "module_status_locked");
                    return true;
                }
                this.moduleManager.allEnable();
                SharedContext.PLUGIN_CONFIG.sendMessageTo(sender, "module_enable_all_success");
                return true;
            }
            case "disable-all" -> {
                if (SharedContext.PLUGIN_CONFIG.isEnabled("module_status_lock")) {
                    SharedContext.PLUGIN_CONFIG.sendMessageTo(sender, "module_status_locked");
                    return true;
                }
                this.moduleManager.allDisable();
                SharedContext.PLUGIN_CONFIG.sendMessageTo(sender, "module_disable_all_success");
                return true;
            }
            case "reload-all" -> {
                SharedContext.SHARED_THREAD_POOL.submit(() -> {
                    this.moduleManager.allReload();
                    SharedContext.PLUGIN_CONFIG.sendMessageTo(sender, "module_reload_all_success");
                });
                return true;
            }
            case "import" -> {
                PackageManager.scan(sender);
                SharedContext.PLUGIN_CONFIG.sendMessageTo(sender, "package_import_success");
                return true;
            }
            case "reload-all-config" -> {
                SharedContext.SHARED_THREAD_POOL.submit(() -> {
                    for (Module m : this.moduleManager.getModules().values()) {
                        m.loadConfig();
                    }
                    SharedContext.PLUGIN_CONFIG.sendMessageTo(sender, "module_reload_all_config_success");
                });
                return true;
            }
            case "list" -> {
                SharedContext.SHARED_THREAD_POOL.submit(() -> {
                    StringBuilder sb = new StringBuilder();
                    Set<String> keySet = this.moduleManager.getModules().keySet();
                    for (String s : keySet) {
                        sb.append(this.moduleManager.isModuleEnabled(s) ? "{green}" : "{gray}").append(s).append("\n");
                    }
                    SharedContext.PLUGIN_CONFIG.sendMessageTo(sender, "module_list_all", sb);
                });
                return true;
            }
            default -> {
                return false;
            }
        }
    }
}


 */