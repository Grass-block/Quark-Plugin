package org.atcraftmc.starlight.chat;

import org.atcraftmc.starlight.core.data.JDBCBasedDataService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

public class Mail {
    private final MailDataService mailDataService = new MailDataService("sl_mail");


    public void viewMessage(Message msg) {

    }

    public static final class Message {
        private final UUID uuid;
        private final long date;
        private final String sender;
        private final String title;
        private final String content;

        private Message(UUID uuid, long date, String sender, String title, String content) {
            this.uuid = uuid;
            this.date = date;
            this.sender = sender;
            this.title = title;
            this.content = content;
        }

        public static Message send(String sender, String title, String content) {
            var date = System.currentTimeMillis();
            var uuid = UUID.randomUUID();
            return new Message(uuid, date, sender, title, content);
        }

        public UUID getUuid() {
            return uuid;
        }

        public long getDate() {
            return date;
        }

        public String getContent() {
            return content;
        }

        public String getSender() {
            return sender;
        }

        public String getTitle() {
            return title;
        }
    }

    //private static final class MailMarkupService extends JDBCBasedDataService {}

    private static final class MailDataService extends JDBCBasedDataService<Message> {
        public static final String NOTIFY_ALL = "__all__";

        public MailDataService(String table) {
            super(table);
        }

        @Override
        public PreparedStatement attemptCreateTable(Connection conn) {
            return null;
        }

        public void send(Message msg, String recipient) {

        }

        public void markAsRead(UUID messageId) {

        }
    }
}
