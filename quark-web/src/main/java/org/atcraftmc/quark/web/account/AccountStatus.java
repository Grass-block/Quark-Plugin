package org.atcraftmc.quark.web.account;

//integrated classes
enum AccountStatus {
    UNVERIFIED("unverified"),
    VERIFIED("verified"),
    UNLINKED("unlinked");

    final String id;

    AccountStatus(String id) {
        this.id = id;
    }

    static AccountStatus fromId(String id) {
        return switch (id) {
            case "unverified" -> UNVERIFIED;
            case "unlinked" -> UNLINKED;
            case "verified" -> VERIFIED;
            default -> throw new IllegalStateException("Unexpected value: " + id);
        };
    }

    boolean shouldAllowPlayerAction() {
        return switch (this) {
            case UNLINKED, UNVERIFIED -> false;
            case VERIFIED -> true;
        };
    }

    public String getId() {
        return id;
    }
}
