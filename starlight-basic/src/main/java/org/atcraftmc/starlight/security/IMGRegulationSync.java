package org.atcraftmc.starlight.security;

import cn.imgnews.regulation.IMGRegulationService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import me.gb2022.modular.Registrations;
import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.qlib.language.MinecraftLocale;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.api.event.PlayerExtraBanCheckEvent;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.bukkit.event.EventHandler;

import java.time.Duration;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@ApplicationModule(id = "img-regulation-sync")
@AutoRegister(Registrations.SERVER_EVENT)
public class IMGRegulationSync extends SLPackageModule {
    private final Cache<UUID, IMGRegulationService.BanRecord> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(5))
            .build();

    @Inject
    private LanguageEntry lang;

    @EventHandler
    public void onBanCheck(PlayerExtraBanCheckEvent event) throws ExecutionException {
        var rec = this.cache.get(event.getUuid(), () -> getBanRecord(event.getUuid()));

        if (Objects.equals(rec.getStatus(), "clean")) {
            return;
        }

        var rel = rec.getRecords();

        if (rel.isEmpty()) {
            return;
        }

        var r = rel.get(0);
        var op = this.lang.item("ban-operator").message(MinecraftLocale.locale(Locale.getDefault()));

        var calender = Calendar.getInstance();
        calender.set(9999, Calendar.DECEMBER, 31, 23, 59, 59);

        event.setBan(r.getReason() + "\n(" + r.getServer() + ")", op, calender.getTime());
    }

    private IMGRegulationService.BanRecord getBanRecord(UUID uuid) {
        var ua = "starlight://%s/IMGRegulationSync".formatted(Starlight.instance().getInstanceUUID());

        return IMGRegulationService.queryUUID(uuid, ua);
    }
}
