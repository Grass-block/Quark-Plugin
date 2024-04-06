package org.tbstcraft.quark.service.web;

import org.bukkit.configuration.ConfigurationSection;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.service.Service;
import org.tbstcraft.quark.util.ObjectContainer;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public interface SMTPMailService extends Service {
    ObjectContainer<SMTPMailService> INSTANCE = new ObjectContainer<>();

    static void init() {
        INSTANCE.set(create(Quark.CONFIG.getConfig("mail_service")));
        INSTANCE.get().onEnable();
    }

    static void stop() {
        INSTANCE.get().onDisable();
    }

    static boolean sendMailTo(String recipient, String subject, String content) {
        return INSTANCE.get().sendMail(subject, content, recipient);
    }

    static SMTPMailService create(ConfigurationSection section) {
        return new ServiceImplementation(section);
    }

    boolean sendMail(String subject, String content, String... recipients);


    final class ServiceImplementation implements SMTPMailService {
        private final ConfigurationSection config;

        public ServiceImplementation(ConfigurationSection config) {
            this.config = config;
        }

        @Override
        public void onEnable() {
        }

        @Override
        public void onDisable() {
        }

        @Override
        public boolean sendMail(String subject, String content, String... recipients) {
            String from = this.config.getString("smtp_user_name");
            String host = this.config.getString("smtp_server");
            String password = this.config.getString("smtp_password");

            Properties properties = System.getProperties();
            if (host == null) {
                return false;
            }
            if (from == null) {
                return false;
            }

            Properties props = System.getProperties();
            properties.setProperty("mail.smtp.host", host.split(":")[0]);
            properties.setProperty("mail.smtp.port", host.split(":")[1]);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            Session session = Session.getDefaultInstance(props, SMTPAuth.get(from, password));
            session.setDebug(false);

            try {
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(from));

                for (String address : recipients) {
                    try {
                        message.addRecipient(Message.RecipientType.TO, new InternetAddress(address));
                    } catch (Exception e) {
                        Quark.LOGGER.warning("failed to add recipient: " + e.getMessage());
                    }
                }

                message.setSubject(subject);
                message.setContent(content, "text/html; charset=UTF-8");
                Transport.send(message);
                return true;
            } catch (MessagingException mex) {
                Quark.LOGGER.severe(mex.getMessage());
                return false;
            }
        }
    }

    final class SMTPAuth extends Authenticator {
        private final String user;
        private final String password;

        private SMTPAuth(String user, String password) {
            this.user = user;
            this.password = password;
        }

        static SMTPAuth get(String user, String password) {
            return new SMTPAuth(user, password);
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(this.user, this.password);
        }
    }
}
