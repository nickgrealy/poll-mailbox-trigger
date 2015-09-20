package org.jenkinsci.plugins.pollmailboxtrigger.mail

import org.jenkinsci.plugins.pollmailboxtrigger.mail.testingTools.MessageBuilder
import org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.SearchTermHelpers
import org.junit.Test
import org.jvnet.mock_javamail.Mailbox

import javax.mail.Folder
import javax.mail.Session
import javax.mail.Store

import static org.junit.Assert.assertEquals

/**
 * Created by nickgrealy@gmail.com on 17/10/2014.
 */
class QuickTest {

    /**
     * Wow! Mock-JavaMail is beautiful!
     */
    @Test
    def void testMock() {
        // Setup test: add mail to inbox
        Mailbox tmp = Mailbox.get("foo@bar.com");
        tmp.add(MessageBuilder.buildMultipartMessage())
        assertEquals 1, tmp.size()

        // Connect to the inmemory mailbox using "imap"
        Session session = Session.getInstance(System.getProperties(), null);
        Store store = session.getStore('imap');
        store.connect("bar.com", "foo", "anything");

        // Check the mail exists!
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);
        assertEquals 1, inbox.getMessageCount()
        assertEquals 1, inbox.search(SearchTermHelpers.subject('foobar')).size()
        assertEquals 0, inbox.search(SearchTermHelpers.subject('chickens')).size()
        store.close();
    }
}
