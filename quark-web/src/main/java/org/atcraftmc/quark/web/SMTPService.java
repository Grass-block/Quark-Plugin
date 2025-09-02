package org.atcraftmc.quark.web;

import org.atcraftmc.qlib.config.ConfigEntry;
import org.atcraftmc.starlight.framework.service.SLService;
import org.atcraftmc.starlight.framework.service.ServiceHolder;
import org.atcraftmc.starlight.framework.service.ServiceProvider;
import org.bukkit.configuration.ConfigurationSection;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.Configurations;
import org.atcraftmc.starlight.framework.service.Service;
import org.atcraftmc.starlight.framework.service.ServiceInject;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@SLService(id = "smtp-service")
public interface SMTPService extends Service {
    @ServiceInject
    ServiceHolder<SMTPService> INSTANCE = new ServiceHolder<>();

    @ServiceProvider
    static SMTPService create(ConfigEntry section) {
        return new ServiceImplementation(Configurations.secret("smtp"));
    }


    static boolean sendMailTo(String recipient, String subject, String content) {
        return INSTANCE.get().sendMail(subject, content, recipient);
    }

    boolean sendMail(String subject, String content, String... recipients);


    final class ServiceImplementation implements SMTPService {
        private final ConfigurationSection config;

        public ServiceImplementation(ConfigurationSection config) {
            this.config = config;
        }

        @Override
        public boolean sendMail(String subject, String content, String... recipients) {
            var host = this.config.getString("server-host");
            var port = this.config.getInt("server-port");
            var username = this.config.getString("username");
            var password = this.config.getString("password");

            var properties = System.getProperties();
            if (host == null) {
                return false;
            }
            if (username == null) {
                return false;
            }

            var auth = this.config.getBoolean("smtp-auth");
            var starttls = this.config.getBoolean("smtp-starttls");

            var props = System.getProperties();
            properties.setProperty("mail.smtp.host", host);
            properties.setProperty("mail.smtp.port", String.valueOf(port));
            props.put("mail.smtp.socketFactory.port", port);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", String.valueOf(auth));
            props.put("mail.smtp.starttls.enable", String.valueOf(starttls));

            var session = Session.getDefaultInstance(props, SMTPAuth.get(username, password));

            try {
                var message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                for (String address : recipients) {
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(address));
                }

                message.setSubject(subject);
                message.setContent(content, "text/html; charset=UTF-8");

                Transport.send(message);

                return true;
            } catch (Throwable mex) {
                mex.printStackTrace();
                Starlight.instance().getLogger().severe(mex.getMessage());
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
