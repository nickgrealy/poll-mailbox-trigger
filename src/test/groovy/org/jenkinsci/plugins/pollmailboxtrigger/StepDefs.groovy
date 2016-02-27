package org.jenkinsci.plugins.pollmailboxtrigger

import cucumber.api.DataTable
import cucumber.api.java.After
import cucumber.api.java.Before
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import hudson.util.FormValidation
import hudson.util.Secret
import org.jenkinsci.plugins.pollmailboxtrigger.bdd.ConfigurationRow
import org.jenkinsci.plugins.pollmailboxtrigger.bdd.EmailRow
import org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.CustomProperties
import org.jenkinsci.plugins.scripttrigger.LabelRestrictionClass
import org.jvnet.mock_javamail.Mailbox

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.core.Is.is
import static org.hamcrest.core.StringContains.containsString
import static org.jenkinsci.plugins.pollmailboxtrigger.SafeJenkins.isNull
import static org.jenkinsci.plugins.pollmailboxtrigger.SafeJenkins.useNativeInstance
import static org.jenkinsci.plugins.pollmailboxtrigger.mail.testingTools.MessageBuilder.buildMessage
import static org.junit.Assert.fail
import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.spy

public class StepDefs {

    Mailbox inmemoryMailbox

    PollMailboxTrigger plugin

    FormValidation validation

    PollMailboxTrigger.PollMailboxTriggerDescriptor descriptor

    ConfigurationRow config
    CustomProperties effectiveConfig

    @Before
    void setup() {
//        MockitoAnnotations.initMocks(this)
        useNativeInstance(false)
        descriptor = new PollMailboxTrigger.PollMailboxTriggerDescriptor()
//        plugin.metaClass.startJob = { XTriggerLog log, Map<String, String> envVars ->
//            println "startJob invoked."
//        }
//        logger = new XTriggerLog()
//
//        config = new CustomProperties([host: 'bar.com', storeName: 'imap', username: 'foo', password: secret.getPlainText()])
    }

    @After
    void teardown() {
        inmemoryMailbox?.clear()
    }

    @Given('the plugin is initialised')
    public void setup_plugin(){
        plugin = spy(new PollMailboxTrigger("*/5 * * * *", new LabelRestrictionClass("master"), false,
                '', '', new Secret(''), ''))

        // mocking...
        doReturn(descriptor).when(plugin).getDescriptor()
    }

    @Given('a mailbox with domain (.+) and username (.+) and emails')
    public void setup_mailbox(String domain, String username, List<EmailRow> emails) {
        inmemoryMailbox = Mailbox.get("$username@$domain")
        emails.each {
            inmemoryMailbox.add(buildMessage(it.subject, it.sentXMinutesAgo, it.isSeenFlag))
        }
    }

    @When('I set the configuration to')
    public void set_config(List<ConfigurationRow> rows) throws Throwable {
        if (isNull(config)){
            config = new ConfigurationRow();
        }
        if (!rows.isEmpty()){
            def newConf = rows.get(0)
            config.host = newConf.host
            config.username = newConf.username
            config.password = newConf.password
            config.appendScript(newConf.script)
        }
        effectiveConfig = PollMailboxTrigger.initialiseDefaults(config?.host, config?.username, config?.buildPasswordSecret(), config?.script)
    }

    @When('script to')
    public void set_config(String script) throws Throwable {
        if (isNull(config)){
            config = new ConfigurationRow();
        }
        config.appendScript(script)
        effectiveConfig = PollMailboxTrigger.initialiseDefaults(config?.host, config?.username, config?.buildPasswordSecret(), config?.script)
    }

    @When('I test the connection')
    public void i_test_the_connection() throws Throwable {
        validation = plugin.getDescriptor().doTestConnection(config.host, config.username, config?.buildPasswordSecret(), config.script)
    }

    @Then('the effective configuration should be')
    public void the_config_should_be(DataTable expectedDataTable){

        List<List<String>> actualDataTable = effectiveConfig.getMap().collect { [it.key, it.value ?: ''] }.sort { a,b -> a[0] <=> b[0] }

        expectedDataTable.diff(actualDataTable)
    }

    @Then('the response should be (OK|ERROR|WARNING) with message \'(.+)\'')
    public void i_should_see_the_following_text(String expectedValidationKind, String expectedMessage) throws Throwable {
        if (isNull(validation)){
            fail("Cannot validate response if you haven't invoked TestConnection first!")
        }
        assertThat(validation.kind, is(FormValidation.Kind.valueOf(expectedValidationKind)))
        assertThat(validation.message, containsString(expectedMessage))
    }
}
