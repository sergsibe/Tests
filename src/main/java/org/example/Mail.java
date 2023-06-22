package org.example;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Mail {
    public static final String AUSTIN_POWERS = "Austin Powers";
    public static final String WEAPONS = "weapons";
    public static final String BANNED_SUBSTANCE = "banned substance";


    public static void main(String[] args) {

        Package box = new Package("1234 1234 1234  1234", 20);

        Sendable mail = new MailPackage("Moskow", "Kazan", box);

        MailService inspector = new Inspector();
        inspector.processMail(mail);
    }

    public static interface Sendable {
        String getFrom();
        String getTo();
    }

    public static abstract class AbstractSendable implements Sendable {

        protected final String from;
        protected final String to;

        public AbstractSendable(String from, String to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public String getFrom() {
            return from;
        }

        @Override
        public String getTo() {
            return to;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AbstractSendable that = (AbstractSendable) o;

            if (!from.equals(that.from)) return false;
            if (!to.equals(that.to)) return false;

            return true;
        }

    }

    public static class MailMessage extends AbstractSendable {

        private final String message;

        public MailMessage(String from, String to, String message) {
            super(from, to);
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            MailMessage that = (MailMessage) o;

            if (message != null ? !message.equals(that.message) : that.message != null) return false;

            return true;
        }

    }

    public static class MailPackage extends AbstractSendable {
        private final Package content;

        public MailPackage(String from, String to, Package content) {
            super(from, to);
            this.content = content;
        }

        public Package getContent() {
            return content;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            MailPackage that = (MailPackage) o;

            if (!content.equals(that.content)) return false;

            return true;
        }

    }

    public static class Package {
        private final String content;
        private final int price;

        public Package(String content, int price) {
            this.content = content;
            this.price = price;
        }

        public String getContent() {
            return content;
        }

        public int getPrice() {
            return price;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Package aPackage = (Package) o;

            if (price != aPackage.price) return false;
            if (!content.equals(aPackage.content)) return false;

            return true;
        }
    }

    public static interface MailService {
        Sendable processMail(Sendable mail);
    }

    public static class RealMailService implements MailService {

        @Override
        public Sendable processMail(Sendable mail) {
            // Здесь описан код настоящей системы отправки почты.
            return mail;
        }
    }

    public static class UntrustworthyMailWorker implements MailService {

        private final MailService realMailService = new RealMailService();

        private MailService[] mailServices;

        public UntrustworthyMailWorker(MailService[] service) {

            mailServices = service;
        }

        public MailService getRealMailService () {

            return realMailService;
        }

        public Sendable processMail(Sendable mail) {

            Sendable processed = mail;

            for (int i = 0; i < mailServices.length; i++) {

                processed = mailServices[i].processMail(processed);
            }
            return realMailService.processMail(processed);
        }

    }

    public static class Spy implements MailService {
        private Logger LOGGER;

        public Spy(Logger logger) {
            LOGGER = logger;
        }

        @Override
        public Sendable processMail(Sendable mail) {

            if (mail.getClass() == MailMessage.class) {

                MailMessage mailMessage = (MailMessage) mail;

                String from = mailMessage.getFrom();
                String to = mailMessage.getTo();

                if (from.equals(AUSTIN_POWERS) || to.equals(AUSTIN_POWERS)) {
                    LOGGER.log(Level.WARNING,
                            "Detected target mail correspondence: from {0} to {1} {2}",
                            new Object[]{mailMessage.getFrom(), mailMessage.getTo(), mailMessage.getMessage()});
                } else {
                    LOGGER.log(Level.INFO,
                            "Usual correspondence: from {from} to {to}",
                            new Object[]{mailMessage.getFrom(), mailMessage.getTo()});
                }
            }
            return mail;
        }
    }

    public static class Tief implements MailService {
        private static int value = 0;
        private int price;

        public Tief(int price) {
            this.price = price;
        }

        public int getValue() {
            return value;
        }

        public Sendable processMail(Sendable mail) {
            Logger logger = Logger.getLogger(Tief.class.getName());

            if (mail.getClass() == MailPackage.class) {
                MailPackage mailPackage = (MailPackage) mail;
                if(mailPackage.getContent().getPrice() >= price) {
                    Package returnPackage = new Package("stones instead of "+mailPackage.getContent().getContent(), 0);
                    Sendable returnSendable = new MailPackage(mailPackage.from, mailPackage.to, returnPackage );
                    value += mailPackage.getContent().getPrice();
                    MailPackage returnMail = (MailPackage) returnSendable;
                    logger.log(Level.INFO, "price = {0}, value = {1}, content = {2}",
                            new Object[] {price, value, returnMail.getContent().getContent()});
                    return returnSendable;
                }

            }

            MailPackage returnMail = (MailPackage) mail;
            logger.log(Level.INFO, "price = {0}, value = {1}, content = {2}",
                    new Object[] {price, value, returnMail.getContent().getContent()});
            return mail;
        }
    }

    public static class IllegalPackageException extends RuntimeException {
        public IllegalPackageException(String message) {
            super(message);
        }

        public IllegalPackageException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class StolenPackageException extends RuntimeException {
        public StolenPackageException(String message) {
            super(message);
        }

        public StolenPackageException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class Inspector implements MailService {

        public Sendable processMail(Sendable mail) throws IllegalPackageException {
            if (mail.getClass() == MailPackage.class) {
                MailPackage mailPackage = (MailPackage) mail;
                if(mailPackage.getContent().getContent().contains(WEAPONS) ||
                        mailPackage.getContent().getContent().contains(BANNED_SUBSTANCE)) {
                    throw new IllegalPackageException("IllegalPackage");
                }

                if(mailPackage.getContent().getContent().contains("stones")) {
                    throw new StolenPackageException("asdfq");
                }
            }
            return null;
        }
    }
}