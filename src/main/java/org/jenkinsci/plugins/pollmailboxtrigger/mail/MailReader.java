package org.jenkinsci.plugins.pollmailboxtrigger.mail;

import com.sun.mail.imap.IMAPFolder;

import javax.mail.*;
import javax.mail.search.SearchTerm;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static org.jenkinsci.plugins.pollmailboxtrigger.mail.Logger.HasLogger;
import static org.jenkinsci.plugins.pollmailboxtrigger.mail.SearchTermHelpers.*;

/**
 * Allows us to read mail using the imap protocol.
 *
 * @author nickgrealy@gmail.com
 */
@SuppressWarnings("unused")
public class MailReader extends HasLogger {

    private String host, storeName, username, password;
    private Properties properties;
    private IMAPFolder currentFolder;
    private Store store;

    public MailReader(Logger logger, String host, String storeName, String username, String password, Properties properties) {
        super(logger);
        this.host = host;
        this.storeName = storeName;
        this.username = username;
        this.password = password;
        this.properties = properties;
    }

    public MailReader connect() throws MessagingException {
        Session session = Session.getDefaultInstance(properties, null);
        if ("true".equals(properties.get("mail.debug"))
                || "true".equals(properties.get("mail.debug.auth"))) {
            logger.info("[Poll Mailbox Trigger] - Enabling debug output.");
            session.setDebugOut(logger.getPrintStream());
        } else {
            logger.info("[Poll Mailbox Trigger] - Disabling debug output.");
        }
        store = session.getStore(storeName);
        store.connect(host, username, password);
        logger.info("Mail Store Connected!");
        return this;
    }

    public FolderWrapper folder(String folderName) throws MessagingException {
        if (store == null) {
            throw new RuntimeException("Session is not connected!");
        }
        currentFolder = (IMAPFolder) store.getFolder(folderName);
        return new FolderWrapper(logger, currentFolder);
    }

    public void close() {
        if (currentFolder != null && currentFolder.isOpen()) {
            try {
                currentFolder.close(true);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
        if (store != null) {
            try {
                store.close();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }

    /* public inner classes */

    /**
     * Utility class for providing extra Messaging related methods!
     */
    public static class MessagesWrapper extends HasLogger {

        private List<Message> messages;
        private IMAPFolder folder;

        public MessagesWrapper(Logger logger, List<Message> messages, IMAPFolder folder) {
            super(logger);
            this.messages = messages;
            this.folder = folder;
        }

        public List<Message> getMessages() {
            return messages;
        }

        public IMAPFolder getFolder() {
            return folder;
        }

        public MessagesWrapper print() throws MessagingException {
            logger.info("Found message(s) : " + messages.size());
            for (Message message : messages) {
                logger.info(">>>>>>");
                logger.info("Date    : " + message.getSentDate().toString());
                logger.info("From    : " + message.getFrom()[0].toString());
                logger.info("Subject : " + message.getSubject());
                logger.info("<<<<<<");
            }
            return this;
        }

        public MessagesWrapper markAsRead() throws IOException, MessagingException {
            if (folder.isOpen()) {
                folder.close(true);
            }
            folder.open(Folder.READ_WRITE);
            for (Message message : messages) {
                message.setFlag(Flags.Flag.SEEN, true);
            }
            logger.info("Marked email(s) as read : " + messages.size());
            return this;
        }

        public MessagesWrapper markAsRead(Message message) throws IOException, MessagingException {
            if (folder.isOpen()) {
                folder.close(true);
            }
            folder.open(Folder.READ_WRITE);
            message.setFlag(Flags.Flag.SEEN, true);
            logger.info("Marked email(s) as read : 1");
            return this;
        }

        public void close() throws MessagingException {
            folder.close(true);
        }

    }


    /**
     * Utility class for providing extra Folder related methods!
     */
    public static class FolderWrapper extends HasLogger {

        public static final SearchTerm UNREAD_FLAG = not(flag(Flags.Flag.SEEN));

        private IMAPFolder folder;

        public FolderWrapper(Logger logger, IMAPFolder folder) {
            super(logger);
            this.folder = folder;
        }

        public IMAPFolder getFolder() {
            return folder;
        }

        public MessagesWrapper getUnreadMessagesSince(Date fromDate) throws MessagingException {
            return search(UNREAD_FLAG, receivedSince(fromDate));
        }

        public MessagesWrapper getUnreadMessages() throws MessagingException {
            return search(UNREAD_FLAG);
        }

        public MessagesWrapper search(SearchTerm... term) throws MessagingException {
            return search(Arrays.asList(term));
        }

        public MessagesWrapper search(List<SearchTerm> term) throws MessagingException {
            if (!folder.isOpen()) {
                folder.open(Folder.READ_ONLY);
            }
            Message[] messages = folder.search(and(term));
            return new MessagesWrapper(logger, Arrays.asList(messages), folder);
        }

        public void close() throws MessagingException {
            folder.close(true);
        }

    }
}
