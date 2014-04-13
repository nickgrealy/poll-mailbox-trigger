package org.jenkinsci.plugins.pollmailboxtrigger.mail;

import com.sun.mail.imap.IMAPFolder;

import javax.mail.*;
import java.util.ArrayList;
import java.util.List;
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
    private CustomProperties properties;
    private Folder currentFolder;
    private Store store;

    public MailReader(Logger logger, String host, String storeName, String username, String password, Properties properties) {
        super(logger);
        this.host = host;
        this.storeName = storeName;
        this.username = username;
        this.password = password;
        this.properties = new CustomProperties(properties);
    }

    public MailReader connect() throws MessagingException {
        ExchangeAuthenticator authenticator = new ExchangeAuthenticator(username, password);
        Session session = Session.getDefaultInstance(properties.getProperties(), null);//authenticator);
        session.setDebugOut(logger.getPrintStream());
        store = session.getStore(storeName);
        store.connect(host, username, password);
        logger.info("[Poll Mailbox Trigger] - Connected!");
        return this;
    }

    public FolderWrapper folder(String folderName) throws MessagingException {
        if (store == null) {
            throw new RuntimeException("Session is not connected!");
        }
        currentFolder = store.getFolder(folderName);
        return new FolderWrapper(logger, currentFolder);
    }

    public List<String> getFolders() throws MessagingException {
        javax.mail.Folder[] folders = store.getDefaultFolder().list("*");
        List<String> folderNames = new ArrayList<String>();
        for (javax.mail.Folder folder : folders) {
            if ((folder.getType() & javax.mail.Folder.HOLDS_MESSAGES) != 0) {
                folderNames.add(folder.getFullName());
            }
        }
        return folderNames;
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