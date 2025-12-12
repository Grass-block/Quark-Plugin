package org.atcraftmc.starlight.chat.mail;

import java.time.Instant;
import java.util.UUID;

public final class MailMessage {
    private final long id;
    private final UUID sender;
    private final UUID recipient;
    private final boolean isAll;
    private final String title;
    private final String content;
    private final Instant sendTime;
    private Instant expireTime;
    private boolean isFavorite;
    private boolean isRead;

    public MailMessage(long id, UUID sender, UUID recipient, boolean isAll, String title, String content, Instant sendTime) {
        this.id = id;
        this.sender = sender;
        this.recipient = recipient;
        this.isAll = isAll;
        this.title = title;
        this.content = content;
        this.sendTime = sendTime;
    }

    public MailMessage(Builder mail, long id) {
        this.id = id;
        this.sender = mail.sender;
        this.recipient = mail.recipient;
        this.isAll = mail.isAll;
        this.title = mail.title;
        this.content = mail.content;
        this.sendTime = mail.sendTime;
    }

    public static MailMessage.Builder create(UUID sender, UUID recipient, String title, String content) {
        return new Builder(sender, recipient, false, title, content, Instant.now());
    }

    public static MailMessage.Builder createAll(UUID sender, String title, String content) {
        return new Builder(sender, null, true, title, content, Instant.now());
    }

    public boolean isExpired() {
        return expireTime != null && Instant.now().isAfter(expireTime) && !isFavorite && !isAll;
    }

    // --- Getters & Setters ---
    public long getId() {
        return id;
    }


    public UUID getSender() {
        return sender;
    }


    public UUID getRecipient() {
        return recipient;
    }

    public boolean isAll() {
        return isAll;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Instant getSendTime() {
        return sendTime;
    }

    public Instant getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Instant expireTime) {
        this.expireTime = expireTime;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static final class Builder {
        private final UUID sender;
        private final UUID recipient;
        private final boolean isAll;
        private final String title;
        private final String content;
        private final Instant sendTime;

        public Builder(UUID sender, UUID recipient, boolean isAll, String title, String content, Instant sendTime) {
            this.sender = sender;
            this.recipient = recipient;
            this.isAll = isAll;
            this.title = title;
            this.content = content;
            this.sendTime = sendTime;
        }

        public UUID getSender() {
            return sender;
        }

        public UUID getRecipient() {
            return recipient;
        }

        public boolean isAll() {
            return isAll;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public Instant getSendTime() {
            return sendTime;
        }
    }
}