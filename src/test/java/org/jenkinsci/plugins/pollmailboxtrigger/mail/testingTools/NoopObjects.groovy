package org.jenkinsci.plugins.pollmailboxtrigger.mail.testingTools

import org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.Logger

import javax.activation.DataHandler
import javax.mail.Address
import javax.mail.Flags
import javax.mail.Folder
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Multipart
import javax.mail.Store

/**
 * Created by nickpersonal on 13/10/14.
 */
class NoopObjects {
}

class NoopMessage extends Message {

    @Override
    Address[] getFrom() throws MessagingException {
        return new Address[0]
    }

    @Override
    void setFrom() throws MessagingException {

    }

    @Override
    void setFrom(Address address) throws MessagingException {

    }

    @Override
    void addFrom(Address[] addresses) throws MessagingException {

    }

    @Override
    Address[] getRecipients(javax.mail.Message.RecipientType recipientType) throws MessagingException {
        return new Address[0]
    }

    @Override
    void setRecipients(javax.mail.Message.RecipientType recipientType, Address[] addresses) throws MessagingException {

    }

    @Override
    void addRecipients(javax.mail.Message.RecipientType recipientType, Address[] addresses) throws MessagingException {

    }

    @Override
    String getSubject() throws MessagingException {
        return null
    }

    @Override
    void setSubject(String s) throws MessagingException {

    }

    @Override
    Date getSentDate() throws MessagingException {
        return null
    }

    @Override
    void setSentDate(Date date) throws MessagingException {

    }

    @Override
    Date getReceivedDate() throws MessagingException {
        return null
    }

    @Override
    Flags getFlags() throws MessagingException {
        return null
    }

    @Override
    void setFlags(Flags flags, boolean b) throws MessagingException {

    }

    @Override
    Message reply(boolean b) throws MessagingException {
        return null
    }

    @Override
    void saveChanges() throws MessagingException {

    }

    @Override
    int getSize() throws MessagingException {
        return 0
    }

    @Override
    int getLineCount() throws MessagingException {
        return 0
    }

    @Override
    String getContentType() throws MessagingException {
        return null
    }

    @Override
    boolean isMimeType(String s) throws MessagingException {
        return false
    }

    @Override
    String getDisposition() throws MessagingException {
        return null
    }

    @Override
    void setDisposition(String s) throws MessagingException {

    }

    @Override
    String getDescription() throws MessagingException {
        return null
    }

    @Override
    void setDescription(String s) throws MessagingException {

    }

    @Override
    String getFileName() throws MessagingException {
        return null
    }

    @Override
    void setFileName(String s) throws MessagingException {

    }

    @Override
    InputStream getInputStream() throws IOException, MessagingException {
        return null
    }

    @Override
    DataHandler getDataHandler() throws MessagingException {
        return null
    }

    @Override
    Object getContent() throws IOException, MessagingException {
        return null
    }

    @Override
    void setDataHandler(DataHandler dataHandler) throws MessagingException {

    }

    @Override
    void setContent(Object o, String s) throws MessagingException {

    }

    @Override
    void setText(String s) throws MessagingException {

    }

    @Override
    void setContent(Multipart multipart) throws MessagingException {

    }

    @Override
    void writeTo(OutputStream outputStream) throws IOException, MessagingException {

    }

    @Override
    String[] getHeader(String s) throws MessagingException {
        return new String[0]
    }

    @Override
    void setHeader(String s, String s2) throws MessagingException {

    }

    @Override
    void addHeader(String s, String s2) throws MessagingException {

    }

    @Override
    void removeHeader(String s) throws MessagingException {

    }

    @Override
    Enumeration getAllHeaders() throws MessagingException {
        return null
    }

    @Override
    Enumeration getMatchingHeaders(String[] strings) throws MessagingException {
        return null
    }

    @Override
    Enumeration getNonMatchingHeaders(String[] strings) throws MessagingException {
        return null
    }
}

class NoopLogger extends Logger {

    @Override
    void info(String message) {

    }

    @Override
    void error(String message) {

    }
}


class NoopFolder extends Folder {

    protected NoopFolder() {
        super((Store)null)
    }

    @Override
    String getName() {
        return null
    }

    @Override
    String getFullName() {
        return null
    }

    @Override
    Folder getParent() throws MessagingException {
        return null
    }

    @Override
    boolean exists() throws MessagingException {
        return false
    }

    @Override
    Folder[] list(String s) throws MessagingException {
        return new Folder[0]
    }

    @Override
    char getSeparator() throws MessagingException {
        return 0
    }

    @Override
    int getType() throws MessagingException {
        return 0
    }

    @Override
    boolean create(int i) throws MessagingException {
        return false
    }

    @Override
    boolean hasNewMessages() throws MessagingException {
        return false
    }

    @Override
    Folder getFolder(String s) throws MessagingException {
        return null
    }

    @Override
    boolean delete(boolean b) throws MessagingException {
        return false
    }

    @Override
    boolean renameTo(Folder folder) throws MessagingException {
        return false
    }

    @Override
    void open(int i) throws MessagingException {

    }

    @Override
    void close(boolean b) throws MessagingException {

    }

    @Override
    boolean isOpen() {
        return false
    }

    @Override
    Flags getPermanentFlags() {
        return null
    }

    @Override
    int getMessageCount() throws MessagingException {
        return 0
    }

    @Override
    Message getMessage(int i) throws MessagingException {
        return null
    }

    @Override
    void appendMessages(Message[] messages) throws MessagingException {

    }

    @Override
    Message[] expunge() throws MessagingException {
        return new Message[0]
    }
}