package org.jenkinsci.plugins.pollmailboxtrigger.mail.utils;

import javax.mail.*;
import javax.mail.search.SearchTerm;
import java.io.IOException;
import java.util.*;

import static org.jenkinsci.plugins.pollmailboxtrigger.PollMailboxTrigger.Properties.subjectContains;
import static org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.SearchTermHelpers.*;
import static org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.Stringify.NEWLINE;
import static org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.Stringify.stringify;

/**
 * @author Nick Grealy
 */
@SuppressWarnings("unused")
public abstract class MailWrapperUtils {

    /**
     * Utility class for providing extra Messaging related methods!
     */
    public static class MessagesWrapper extends Logger.HasLogger {

        private List<Message> messages;
        private Folder folder;

        public MessagesWrapper(Logger logger, List<Message> messages, Folder folder) {
            super(logger);
            this.messages = messages;
            this.folder = folder;
        }

        public List<Message> getMessages() {
            return messages;
        }

        public Folder getFolder() {
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

        public MessagesWrapper markAsRead(Message... messagez) throws IOException, MessagingException {
            return markAsRead(Arrays.asList(messagez));
        }

        public MessagesWrapper markAsRead(List<Message> messagez) throws IOException, MessagingException {
            if (folder.isOpen() && folder.getMode() != Folder.READ_WRITE) {
                folder.close(true);
            }
            if (!folder.isOpen()) {
                folder.open(Folder.READ_WRITE);
            }
            for (Message message : messagez) {
                message.setFlag(Flags.Flag.SEEN, true);
            }
            logger.info("Marked email(s) as read : " + messagez.size());
            return this;
        }

        public MessagesWrapper markAsRead() throws IOException, MessagingException {
            return markAsRead(messages);
        }

        public CustomProperties getMessageProperties(Message message, String prefix, CustomProperties p) throws MessagingException, IOException {
            CustomProperties envVars = new CustomProperties();
            String msgSubject = stringify(message.getSubject());
            envVars.put(prefix + "subject", msgSubject);
            envVars.put(prefix + "from", stringify(message.getFrom()));
            envVars.put(prefix + "replyTo", stringify(message.getReplyTo()));
            envVars.put(prefix + "flags", stringify(message.getFlags()));
            envVars.put(prefix + "folder", stringify(message.getFolder()));
            envVars.put(prefix + "messageNumber", stringify(message.getMessageNumber()));
            envVars.put(prefix + "receivedDate", stringify(message.getReceivedDate()));
            envVars.put(prefix + "sentDate", stringify(message.getSentDate()));
            envVars.put(prefix + "headers", stringify(message.getAllHeaders()));
            envVars.put(prefix + "content", stringify(message));
            envVars.put(prefix + "contentType", stringify(message.getContentType()));
            envVars.put(prefix + "recipients", stringify(message.getAllRecipients()));
            // add parameters from email content
            final CustomProperties properties = CustomProperties.read(getText(message));
            envVars.putAll(properties);
            envVars.put(prefix + "emailParams", stringify(properties.getMap(), "=", "&"));
            // add "jobTrigger"
            if (p.has(subjectContains)) {
                String subject = p.get(subjectContains);
                // normalise strings, find index, then get text after it
                int idx = msgSubject.toLowerCase().indexOf(subject.toLowerCase());
                int beginIndex = idx + subject.length();
                if (idx > -1 && beginIndex < msgSubject.length()) {
                    envVars.put(prefix + "jobTrigger", msgSubject.substring(beginIndex));
                }
            }
            return envVars;
        }

        public static String getText(Part p) {
            try {
                // get all text from the email body...
                String text = stringify(p);
                // strip out html...
                text = stringify(text.split("(\\<[^\\>]*\\>)+"), NEWLINE).trim();
                // only keep lines with a "=" sign...
                final List<String> content = filterProperties(text.split(NEWLINE), "=");
                // join lines back up into a single string...
                text = stringify(content, NEWLINE).trim();
                return text;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Only keep text containing the given string.
         */
        public static List<String> filterProperties(String[] props, String containing) {
            final ArrayList<String> list = new ArrayList<String>(Arrays.asList(props));
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                String foo = it.next();
                if (!foo.contains(containing)) it.remove();
            }
            return list;
        }

        public void close() throws MessagingException {
            folder.close(true);
        }
    }


    /**
     * Utility class for providing extra Folder related methods!
     */
    public static class FolderWrapper extends Logger.HasLogger {

        public static final SearchTerm UNREAD_FLAG = not(flag(Flags.Flag.SEEN));

        private Folder folder;

        public FolderWrapper(Logger logger, Folder folder) {
            super(logger);
            this.folder = folder;
        }

        public Folder getFolder() {
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
