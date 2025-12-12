package org.atcraftmc.starlight.chat;

import org.atcraftmc.starlight.chat.mail.MailMessage;
import org.atcraftmc.starlight.core.data.JDBCBasedDataService;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class JDBCMailDataService extends JDBCBasedDataService<MailMessage> {
    public JDBCMailDataService() {
        super("__null__");
    }

    public MailMessage insert(MailMessage.Builder mail) throws SQLException {
        String sql = """
                INSERT INTO sl_mail_message
                (sender, recipient, global, title, content, send_time, expire_time,
                 favorite, read)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (var ps = this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, mail.getSender().toString());

            if (mail.isAll() || mail.getRecipient() == null) {
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(2, mail.getRecipient().toString());
            }

            ps.setBoolean(3, mail.isAll());
            ps.setString(4, mail.getTitle());
            ps.setString(5, mail.getContent());
            ps.setTimestamp(6, Timestamp.from(mail.getSendTime()));
            ps.setTimestamp(7, null);
            ps.setBoolean(8, false);
            ps.setBoolean(9, false);
            ps.executeUpdate();

            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new MailMessage(mail, rs.getLong(1));
                }
            }
        }
        return null;
    }

    public void markFavorite(long id, boolean favorite) throws SQLException {
        try (var ps = this.connection.prepareStatement("UPDATE sl_mail_message SET favorite=? WHERE id=? AND GLOBAL=false")) {
            ps.setBoolean(1, favorite);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public void markRead(long id, boolean read) throws SQLException {
        try (var ps = this.connection.prepareStatement("UPDATE sl_mail_message SET READ=? WHERE id=? AND GLOBAL=false")) {
            ps.setBoolean(1, read);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public int purgeExpired() throws SQLException {
        var sql = """
                   DELETE FROM sl_mail_message
                   WHERE GLOBAL = FALSE
                     AND favorite = FALSE
                     AND expire_time IS NOT NULL
                     AND expire_time <= ?
                """;

        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.from(Instant.now()));
            return ps.executeUpdate();
        }
    }

    public Optional<MailMessage> findById(long id) throws SQLException {
        try (PreparedStatement ps = this.connection.prepareStatement("SELECT * FROM sl_mail_message WHERE id=?")) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(map(rs));
            }
            return Optional.empty();
        }
    }

    public List<MailMessage> listUnread(UUID recipientId) throws SQLException {
        var sql = """
                SELECT * FROM SL_MAIL_MESSAGE
                WHERE (GLOBAL = TRUE OR recipient = ?)
                  AND READ = FALSE
                ORDER BY send_time DESC
                """;

        return dispatchMailList(recipientId, this.connection.prepareStatement(sql));
    }

    public List<MailMessage> listFavorite(UUID recipientId) throws SQLException {
        var sql = """
                SELECT * FROM SL_MAIL_MESSAGE
                WHERE (global = TRUE OR recipient = ?)
                  AND FAVORITE = true
                ORDER BY send_time DESC
                """;

        return dispatchMailList(recipientId, this.connection.prepareStatement(sql));
    }

    public boolean owns(long id, UUID user) {
        try {
            return findById(id).map((m) -> m.getRecipient().equals(user) && !m.isAll()).orElse(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @NotNull
    private List<MailMessage> dispatchMailList(UUID recipientId, PreparedStatement ps) throws SQLException {
        var result = new ArrayList<MailMessage>();

        ps.setString(1, recipientId.toString());
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
        }

        ps.close();
        return result;
    }

    private MailMessage map(ResultSet rs) throws SQLException {
        var id = rs.getLong("id");
        var sender = UUID.fromString(rs.getString("sender"));
        var rec = rs.getString("recipient");
        var recipient = rec == null ? null : UUID.fromString(rec);
        var all = rs.getBoolean("global");
        var title = rs.getString("title");
        var content = rs.getString("content");
        var sendTime = rs.getTimestamp("send_time").toInstant();
        var m = new MailMessage(id, sender, recipient, all, title, content, sendTime);

        var exp = rs.getTimestamp("expire_time");
        m.setExpireTime(exp == null ? null : exp.toInstant());
        m.setFavorite(rs.getBoolean("favorite"));
        m.setRead(rs.getBoolean("read"));

        return m;
    }

    @Override
    public PreparedStatement attemptCreateTable(Connection conn) throws SQLException {
        var sql = """
                CREATE TABLE sl_mail_message
                (
                    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                    sender      VARCHAR(36)  NOT NULL,
                    recipient   VARCHAR(36),
                    global      BOOLEAN      NOT NULL DEFAULT FALSE,
                    title       VARCHAR(255) NOT NULL,
                    content     TEXT         NOT NULL,
                    send_time   TIMESTAMP    NOT NULL,
                    expire_time TIMESTAMP    NULL,
                    favorite    BOOLEAN      NOT NULL DEFAULT FALSE,
                    read        BOOLEAN      NOT NULL DEFAULT FALSE
                );
                """;
        return conn.prepareStatement(sql);
    }
}
