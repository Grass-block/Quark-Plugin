package org.tbstcraft.quark.internal;

import org.bukkit.configuration.ConfigurationSection;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.service.Service;
import org.tbstcraft.quark.framework.service.*;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@QuarkService(id = "smtp-service")
public interface SMTPService extends Service {
    @ServiceInject
    ServiceHolder<SMTPService> INSTANCE = new ServiceHolder<>();

    @ServiceProvider
    static SMTPService create(ConfigurationSection section) {
        return new ServiceImplementation(section);
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
            String from = this.config.getString("username");
            String host = this.config.getString("server");
            String password = this.config.getString("password");

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
            props.put("mail.smtp.socketFactory.port", host.split(":")[1]);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            Session session = Session.getDefaultInstance(props, SMTPAuth.get(from, password));

            try {
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(from));
                for (String address : recipients) {
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(address));
                }

                message.setSubject(subject);
                message.setContent(content, "text/html; charset=UTF-8");
                Transport.send(message);

                return true;
            } catch (Throwable mex) {
                mex.printStackTrace();
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
