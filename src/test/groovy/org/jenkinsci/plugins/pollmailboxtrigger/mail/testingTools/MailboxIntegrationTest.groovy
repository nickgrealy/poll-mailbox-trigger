package org.jenkinsci.plugins.pollmailboxtrigger.mail.testingTools

import hudson.util.Secret
import org.jenkinsci.lib.xtrigger.XTriggerLog
import org.jenkinsci.plugins.pollmailboxtrigger.PollMailboxTrigger
import org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.CustomProperties
import org.jenkinsci.plugins.scripttrigger.LabelRestrictionClass
import org.junit.After
import org.junit.Before
import org.jvnet.mock_javamail.Mailbox

/**
 * Created by nickgrealy@gmail.com on 17/10/2014.
 */
abstract class MailboxIntegrationTest {

    Mailbox inmemoryMailbox
    PollMailboxTrigger trigger
    XTriggerLog logger
    CustomProperties config

    @Before
    def void setup() {
        // setup mocking
        final Secret secret = new Secret('anything')

        // Setup Jenkins Secret (keep it secret, keep it safe!)
        inmemoryMailbox = Mailbox.get("foo@bar.com");
        trigger = new PollMailboxTrigger("5 * * * *", new LabelRestrictionClass("master"), false,
                "bar.com", "foo", secret, '')
        trigger.metaClass.startJob = { XTriggerLog log, Map<String, String> envVars ->
            println "startJob invoked."
        }
        logger = new XTriggerLog()

        config = new CustomProperties([host: 'bar.com', storeName: 'imap', username: 'foo', password: secret.getPlainText()])
    }

    @After
    def void teardown() {
        inmemoryMailbox.clear()
    }

}
