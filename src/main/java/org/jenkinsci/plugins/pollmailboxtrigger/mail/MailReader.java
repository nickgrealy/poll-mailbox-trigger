package org.jenkinsci.plugins.pollmailboxtrigger.mail;

import javax.mail.*;
import java.util.Properties;

import static org.jenkinsci.plugins.pollmailboxtrigger.mail.Logger.HasLogger;
import static org.jenkinsci.plugins.pollmailboxtrigger.mail.MailWrapperUtils.FolderWrapper;

/**
 * Allows us to read mail using the imap protocol.
 *
 * @author Nick Grealy
 */
@SuppressWarnings("unused")
public class MailReader extends HasLogger {

    private String host, storeName, username, password;
    private Properties properties;
    private Folder currentFolder;
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
        ExchangeAuthenticator authenticator = new ExchangeAuthenticator(username, password);
        Session session = Session.getDefaultInstance(properties, null);//authenticator);
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
        currentFolder = store.getFolder(folderName);
        return new FolderWrapper(logger, currentFolder);
    }

    public MailReader listFolders() throws MessagingException {
//        Folder[] folders = store.getPersonalNamespaces();
        System.out.println("FolderImpl:" + store.getDefaultFolder().getClass());
        javax.mail.Folder[] folders = store.getDefaultFolder().list();//.list("*");
        for (javax.mail.Folder folder : folders) {
//            if ((folder.getType() & javax.mail.Folder.HOLDS_MESSAGES) != 0) {
            System.out.println(folder.getFullName() + ": " + folder.getMessageCount());
//            }
        }
        return this;
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
}

class ExchangeAuthenticator extends Authenticator {

    String username;
    String password;

    public ExchangeAuthenticator(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }

    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
    }
}