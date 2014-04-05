package org.jenkinsci.plugins.pollmailboxtrigger.mail;

import com.sun.mail.imap.IMAPFolder;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SearchTerm;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.jenkinsci.plugins.pollmailboxtrigger.mail.SearchTermHelpers.*;

/**
 * @author nickgrealy@gmail.com
 */
@SuppressWarnings("unused")
public abstract class MailWrapperUtils {


    /* public inner classes */

    public abstract static class Stringify {

        public static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

        public static String toString(String message) {
            return message;
        }

        public static String toString(Object[] array) {
            return toString(Arrays.asList(array));
        }

        public static String toString(List<Object> list) {
            StringBuilder sb = new StringBuilder();
            for (Iterator<Object> iterator = list.iterator(); iterator.hasNext(); ) {
                try {
                    sb.append(toString(iterator.next()));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (iterator.hasNext()) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }

        public static String toString(Address[] addresses) {
            return InternetAddress.toString(addresses);
        }

        public static String toString(Date date) {
            return df.format(date);
        }

        public static String toString(Flags flags) {
            return toString(flags.getSystemFlags()) + ", " + toString(flags.getUserFlags());
        }

        public static String toString(Folder folder) {
            return folder.getFullName();
        }

        public static String toString(int messageNumber) {
            return String.valueOf(messageNumber);
        }

        public static String toString(Enumeration allHeaders) {
            List<Object> list = new ArrayList<Object>();
            while (allHeaders.hasMoreElements()) {
                list.add(allHeaders.nextElement());
            }
            return toString(list);
        }

        public static String toString(Header content) {
            return content.getName() + "=" + content.getValue();
        }

        public static String toString(MimeMultipart content) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                content.writeTo(baos);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return new String(baos.toByteArray());
        }


        public static String toString(Object content) {
            if (content instanceof MimeMultipart) {
                return toString((MimeMultipart) content);
            }
            if (content instanceof Header) {
                return toString((Header) content);
            }
            return content == null ? "null" : content.toString();
        }
    }

    /**
     * Utility class for providing extra Messaging related methods!
     */
    public static class MessagesWrapper extends Logger.HasLogger {

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

        public Map<String, String> getMessageProperties(Message message) throws IOException, MessagingException {
            return getMessageProperties(message, "");
        }

        public Map<String, String> getMessageProperties(Message message, String prefix) throws MessagingException, IOException {
            Map<String, String> envVars = new HashMap<String, String>();
            envVars.put(prefix + "subject", Stringify.toString(message.getSubject()));
            envVars.put(prefix + "from", Stringify.toString(message.getFrom()));
            envVars.put(prefix + "replyTo", Stringify.toString(message.getReplyTo()));
            envVars.put(prefix + "flags", Stringify.toString(message.getFlags()));
            envVars.put(prefix + "folder", Stringify.toString(message.getFolder()));
            envVars.put(prefix + "messageNumber", Stringify.toString(message.getMessageNumber()));
            envVars.put(prefix + "receivedDate", Stringify.toString(message.getReceivedDate()));
            envVars.put(prefix + "sentDate", Stringify.toString(message.getSentDate()));
            envVars.put(prefix + "headers", Stringify.toString(message.getAllHeaders()));
            envVars.put(prefix + "content", Stringify.toString(message.getContent()));
            envVars.put(prefix + "contentType", Stringify.toString(message.getContentType()));
            envVars.put(prefix + "recipients", Stringify.toString(message.getAllRecipients()));
            return envVars;
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
