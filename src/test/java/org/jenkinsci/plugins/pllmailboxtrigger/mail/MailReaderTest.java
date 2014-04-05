package org.jenkinsci.plugins.pllmailboxtrigger.mail;

import org.jenkinsci.plugins.pollmailboxtrigger.mail.CustomProperties;
import org.jenkinsci.plugins.pollmailboxtrigger.mail.Logger;
import org.jenkinsci.plugins.pollmailboxtrigger.mail.MailReader;
import org.jenkinsci.plugins.pollmailboxtrigger.mail.MailWrapperUtils;
import org.junit.Ignore;
import org.junit.Test;

import javax.mail.Flags;
import javax.mail.Message;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

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
                "user@gmail.com",
                "password",
                cp.getProperties()).connect();
        MailWrapperUtils.MessagesWrapper messagesWrapper = reader.folder("inbox").search(
                not(flag(Flags.Flag.SEEN))
                , receivedSince(relativeDate(Calendar.DATE, -365))
                , subject("jenkins_rocks")
        );
        List<Message> messages = messagesWrapper.getMessages();
        assertTrue("size should be greater than zero!", messages.size() > 0);
        Map<String, String> map = messagesWrapper.getMessageProperties(messages.get(0));
        for (String key : map.keySet()) {
            System.out.println(key + "=" + map.get(key));
        }
    }


}
