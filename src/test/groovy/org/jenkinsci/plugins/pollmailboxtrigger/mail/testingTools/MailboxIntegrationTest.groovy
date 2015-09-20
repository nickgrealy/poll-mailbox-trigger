package org.jenkinsci.plugins.pollmailboxtrigger.mail.testingTools

import hudson.util.FormValidation
import hudson.util.Secret
import org.jenkinsci.lib.xtrigger.XTriggerLog
import org.jenkinsci.plugins.pollmailboxtrigger.PollMailboxTrigger
import org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.CustomProperties
import org.jenkinsci.plugins.scripttrigger.LabelRestrictionClass
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.jvnet.mock_javamail.Mailbox

import static org.jenkinsci.plugins.pollmailboxtrigger.PollMailboxTrigger.Properties.folder
import static org.jenkinsci.plugins.pollmailboxtrigger.PollMailboxTrigger.checkForEmails
import static org.jenkinsci.plugins.pollmailboxtrigger.mail.testingTools.MessageBuilder.buildMessage
import static org.junit.Assert.assertEquals

/**
 * Created by nickgrealy@gmail.com on 17/10/2014.
 */
abstract class MailboxIntegrationTest {

    Mailbox inmemoryMailbox
    PollMailboxTrigger trigger
    XTriggerLog logger
    def config

    @Before
    def void setup() {
        // Setup Jenkins Secret (keep it secret, keep it safe!)
        Secret.SECRET = 'foo'
        inmemoryMailbox = Mailbox.get("foo@bar.com");
        trigger = new PollMailboxTrigger("5 * * * *", new LabelRestrictionClass("master"), false,
                "bar.com", "foo", new Secret("anything"), '')
        trigger.metaClass['startJob'] = { XTriggerLog log, Map<String, String> envVars ->
            println "startJob invoked."
        }
        logger = new XTriggerLog()
        config = new CustomProperties([host: 'bar.com', storeName: 'imap', username: 'foo', password: new Secret('anything').getEncryptedValue()])
    }

    @After
    def void teardown() {
        inmemoryMailbox.clear()
    }

}
