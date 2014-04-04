package org.jenkinsci.plugins.pllmailboxtrigger.mail;

import org.jenkinsci.plugins.pollmailboxtrigger.mail.CustomProperties;
import org.jenkinsci.plugins.pollmailboxtrigger.mail.Logger;
import org.jenkinsci.plugins.pollmailboxtrigger.mail.MailReader;
import org.junit.Ignore;
import org.junit.Test;

import javax.mail.Flags;
import java.util.Calendar;

import static org.jenkinsci.plugins.pollmailboxtrigger.mail.SearchTermHelpers.*;
import static org.junit.Assert.assertTrue;

/**
 * @author nickgrealy@gmail.com
 */
public class MailReaderTest {

    @Test
    @Ignore
    public void testRetrieveMail() throws Throwable {
        CustomProperties cp = new CustomProperties();
        cp.put("mail.imaps.host", "imap.gmail.com");
        cp.put("mail.imaps.port", "993");
        MailReader reader = new MailReader(Logger.DEFAULT,
                "imap.gmail.com",
                "imaps",
                "charlie@gmail.com",
                "milksteak",
                cp.getProperties()).connect();
        int size = reader.folder("inbox").search(
                not(flag(Flags.Flag.SEEN))
                , receivedSince(relativeDate(Calendar.DATE, -365))
                , subject("raw jelly beans")
        ).print().getMessages().size();
        assertTrue("size should be greater than zero!", size > 0);
    }
}
