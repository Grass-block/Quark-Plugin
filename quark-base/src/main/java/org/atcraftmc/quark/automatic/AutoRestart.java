package org.atcraftmc.quark.automatic;

import org.bukkit.Bukkit;
import org.atcraftmc.starlight.framework.module.PackageModule;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AutoRestart extends PackageModule implements Runnable {
    @Override
    public void enable() {

    }

    @Override
    public void disable() {

    }


    @Override
    public void run() {
        String s = this.getConfig().value("restart_command").string();
        if (s == null) {
            return;
        }
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), s);
    }

    public long generateMillSeconds(int hour, int minute) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayTwoAM = LocalDateTime.of(now.toLocalDate(), LocalTime.of(hour, minute));
        if (now.isAfter(todayTwoAM)) {
            todayTwoAM = todayTwoAM.plusDays(1);
        }
        Duration duration = Duration.between(now, todayTwoAM);
        return duration.toMillis();
    }
}
