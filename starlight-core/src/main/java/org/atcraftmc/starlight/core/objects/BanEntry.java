package org.atcraftmc.starlight.core.objects;

import java.util.UUID;

public class BanEntry {
    private final UUID banId;
    private final UUID target;
    private final long expires;
    private final String reason;
    private final String operator;

    public BanEntry(UUID banId, UUID target, long expires, String reason, String operator) {
        this.banId = banId;
        this.target = target;
        this.reason = reason;
        this.expires = expires;
        this.operator = operator;
    }

    public UUID getBanId() {
        return banId;
    }

    public UUID getTarget() {
        return target;
    }

    public String getReason() {
        return reason;
    }

    public long getExpires() {
        return expires;
    }

    public String getOperator() {
        return operator;
    }

    public boolean isPermanent() {
        return this.expires == -1;
    }

    public boolean isExpired() {
        return this.expires <= System.currentTimeMillis();
    }
}
