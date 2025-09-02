package org.atcraftmc.starlight.console;

import me.gb2022.apm.local.ListedBroadcastEvent;
import me.gb2022.apm.local.PluginMessageHandler;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.appender.rewrite.RewriteAppender;
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy;
import org.apache.logging.log4j.core.config.*;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.MessageFormatMessage;
import org.apache.logging.log4j.util.StringMap;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.Registers;
import org.atcraftmc.starlight.core.GameTestService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SLModule(defaultEnable = false)
@AutoRegister(Registers.PLUGIN_MESSAGE)
public final class LogColorPatch extends PackageModule {
    private final Map<String, Log4jConfiguration> configurations = new HashMap<>();
    private final Set<String> injections = new HashSet<>();

    private Configuration config() {
        var ctx = (LoggerContext) LogManager.getContext(false);
        return ctx.getConfiguration();
    }

    public void active(Appender appender) {
        appender.initialize();
        appender.start();
    }

    @PluginMessageHandler("starlight:log:reconfigure")
    public void onMessageFetch(ListedBroadcastEvent event) {
        this.disable();
        this.enable();
    }

    private void reloadAppenders() {
        config().getAppenders().forEach((n, a) -> {
            if (a instanceof AbstractOutputStreamAppender) {
                return;
            }
            if (a.isStarted()) {
                a.stop();
            }
            a.initialize();
            a.start();
        });
    }


    @Override
    public void disable() {
        for (var id : this.injections) {
            for (var config : this.configurations.values()) {
                uninject(config, id);
            }
        }

        this.injections.clear();
        this.configurations.clear();

        this.reloadAppenders();

        GameTestService.unregister("log-color");
    }

    @Override
    public void enable() {
        GameTestService.register("log-color", () -> {
            for (var conf : this.configurations.keySet()) {
                test(conf, this.configurations.get(conf));
            }
        });

        var config = config();

        this.configurations.clear();
        this.configurations.put("_root", Log4jConfiguration.root(config));
        this.configurations.put("root-logger", Log4jConfiguration.logger(config.getRootLogger()));

        for (var lc : config().getLoggers().values()) {
            this.configurations.put("logger-config:" + lc.getName(), Log4jConfiguration.logger(lc));
        }

        redirect("TerminalConsole", new ColorCharRewritePolicy.AnsiColorPolicy());
        redirect("File", new ColorCharRewritePolicy());
        redirect("ServerGuiConsole", new ColorCharRewritePolicy());

        this.reloadAppenders();
    }


    public void redirect(String id, RewritePolicy policy) {
        this.injections.add(id);
        for (var config : this.configurations.values()) {
            inject(config, id, policy);
        }
    }

    private void test(String lid, Log4jConfiguration config) {
        for (var logger : config.getAppenders().keySet()) {
            var event = Log4jLogEvent.newBuilder()
                    .setLevel(Level.INFO)
                    .setMessage(new MessageFormatMessage("[%s:%s] §3Test".formatted(lid, logger)))
                    .setLoggerName("_Test_")
                    .build();

            config.getAppenders().get(logger).append(event);
        }
    }

    private void inject(Log4jConfiguration config, String id, RewritePolicy policy) {
        var rid = id + "_rewrite_backend";
        var appender = config.getAppender(id);

        if (appender == null) {
            return;
        }

        if (config.getAppenders().containsKey(rid)) {
            return;
        }

        config.addAppender(rid, appender);
        active(appender);

        var rewrite = RewriteAppender.createAppender(
                id,
                Boolean.toString(appender.ignoreExceptions()),
                new AppenderRef[]{AppenderRef.createAppenderRef(rid, Level.INFO, null)},
                config(),
                policy,
                null
        );

        config.removeAppender(id);
        config.addAppender(id, rewrite);
        active(rewrite);
    }

    private void uninject(Log4jConfiguration config, String id) {
        var rid = id + "_rewrite_backend";
        var appender = config.getAppender(rid);

        if (appender == null) {
            return;
        }

        config.removeAppender(id);
        config.removeAppender(rid);
        config.addAppender(id, appender);

        active(appender);
    }

    interface Log4jConfiguration {
        static Log4jConfiguration root(Configuration config) {
            return new Log4jConfiguration() {
                @Override
                public Map<String, Appender> getAppenders() {
                    return config.getAppenders();
                }

                @Override
                public void removeAppender(String id) {
                    config.getAppenders().remove(id);
                }

                @Override
                public void addAppender(String id, Appender appender) {
                    config.getAppenders().put(id, appender);
                }
            };
        }

        static Log4jConfiguration logger(LoggerConfig config) {
            return new LoggerConfigWrapper(config);
        }

        void addAppender(String id, Appender appender);

        void removeAppender(String id);

        Map<String, Appender> getAppenders();

        default Appender getAppender(String id) {
            return getAppenders().get(id);
        }

        class LoggerConfigWrapper implements Log4jConfiguration {
            private final AppenderControlArraySet controlSet;

            public LoggerConfigWrapper(LoggerConfig config) {
                try {
                    var f_controlSet = LoggerConfig.class.getDeclaredField("appenders");
                    f_controlSet.setAccessible(true);
                    this.controlSet = ((AppenderControlArraySet) f_controlSet.get(config));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Map<String, Appender> getAppenders() {
                return this.controlSet.asMap();
            }

            @Override
            public void removeAppender(String id) {
                this.controlSet.remove(id);
            }

            @Override
            public void addAppender(String id, Appender appender) {
                this.controlSet.add(redirectNamed(appender, id));
            }

            private AppenderControl redirectNamed(Appender appender, String name) {
                try {
                    var control = new AppenderControl(appender, Level.INFO, null);

                    var f_control = AppenderControl.class.getDeclaredField("appenderName");
                    f_control.setAccessible(true);
                    f_control.set(control, name);

                    return control;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }


        }
    }


    private static class ColorCharRewritePolicy implements RewritePolicy {
        @Override
        public LogEvent rewrite(LogEvent source) {
            var message = source.getMessage();

            if (message == null) {
                return source;
            }


            var newMessage = replace(message.getFormattedMessage());

            return Log4jLogEvent.newBuilder()
                    .setLoggerName(source.getLoggerName())
                    .setLoggerFqcn(source.getLoggerFqcn())
                    .setLevel(source.getLevel())
                    .setMessage(new MessageFormatMessage(newMessage))
                    .setThrown(source.getThrown())
                    .setContextData((StringMap) source.getContextData())
                    .setContextStack(source.getContextStack())
                    .setThreadName(source.getThreadName())
                    .setTimeMillis(source.getTimeMillis())
                    .setSource(source.getSource())
                    .build();
        }

        protected String replace(String origin) {
            return origin.replaceAll("§[a-z]", "").replaceAll("§[0-9]", "");
        }


        private static final class AnsiColorPolicy extends ColorCharRewritePolicy {
            @Override
            public String replace(String origin) {
                return super.replace(origin.replace("§0", "\033[30m")
                                             .replace("§1", "\034[34m")
                                             .replace("§2", "\033[32m")
                                             .replace("§3", "\033[36m")
                                             .replace("§4", "\033[31m")
                                             .replace("§5", "\033[35m")
                                             .replace("§6", "\033[33m")
                                             .replace("§7", "\033[37m")
                                             .replace("§8", "\033[90m")
                                             .replace("§9", "\033[94m")
                                             .replace("§a", "\033[92m")
                                             .replace("§b", "\033[96m")
                                             .replace("§c", "\033[91m")
                                             .replace("§d", "\033[95m")
                                             .replace("§e", "\033[93m")
                                             .replace("§f", "\033[39m")
                                             .replace("§r", "\033[39m"));
            }
        }
    }


}
