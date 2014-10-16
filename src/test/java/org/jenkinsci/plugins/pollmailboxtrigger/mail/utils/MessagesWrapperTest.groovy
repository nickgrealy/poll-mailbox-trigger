package org.jenkinsci.plugins.pollmailboxtrigger.mail.utils

import org.jenkinsci.plugins.pollmailboxtrigger.mail.testingTools.MessageBuilder
import org.jenkinsci.plugins.pollmailboxtrigger.mail.testingTools.NoopFolder
import org.jenkinsci.plugins.pollmailboxtrigger.mail.testingTools.NoopLogger
import org.jenkinsci.plugins.pollmailboxtrigger.mail.testingTools.NoopMessage
import org.junit.Before
import org.junit.Test

import javax.mail.Folder
import javax.mail.Message

import static org.jenkinsci.plugins.pollmailboxtrigger.mail.testingTools.MessageBuilder.*
import static org.junit.Assert.assertEquals

/**
 * Created by nickgrealy@gmail.com on 13/10/14.
 */
class MessagesWrapperTest {

    StringBuilder sb
    Logger logger
    Folder folder = new NoopFolder()
    List<Message> messages = []
    MailWrapperUtils.MessagesWrapper wrapper
    CustomProperties properties

    @Before
    void setup() {
        sb = new StringBuilder()
        logger = [info: { sb.append(it).append("\n") }] as NoopLogger
        wrapper = new MailWrapperUtils.MessagesWrapper(logger, messages, folder)
        properties = new CustomProperties()
    }

    @Test
    void testPrintingNoMessages() {
        wrapper.print()
        assertEquals """Found message(s) : 0
""".toString(), sb.toString()
    }

    @Test
    void testPrintingOneMessage() {
        messages.add(buildMessage())
        wrapper.print()
        assertEquals """Found message(s) : 1
>>>>>>
Date    : Mon Oct 13 21:30:50 EST 2014
From    : foo1@bar.com
Subject : foobar!!!
<<<<<<
""".toString(), sb.toString()
    }

    @Test
    void testPrintingOneMessageWithNullValues() {
        def date = new Date()
        messages.add(new NoopMessage())
        wrapper.print()
        assertEquals """Found message(s) : 1
>>>>>>
Date    : null
From    : null
Subject : null
<<<<<<
""".toString(), sb.toString()
    }

    @Test
    void testPrintingMultipleMessages() {
        (1..3).each {
            messages.add buildMessage("foobar$it")
        }
        wrapper.print()
        assertEquals """Found message(s) : 3
>>>>>>
Date    : Mon Oct 13 21:30:50 EST 2014
From    : foo1@bar.com
Subject : foobar1
<<<<<<
>>>>>>
Date    : Mon Oct 13 21:30:50 EST 2014
From    : foo1@bar.com
Subject : foobar2
<<<<<<
>>>>>>
Date    : Mon Oct 13 21:30:50 EST 2014
From    : foo1@bar.com
Subject : foobar3
<<<<<<
""".toString(), sb.toString()
    }

    @Test
    void getMessagePropertiesWithNullValues() {
        def newProps = wrapper.getMessageProperties(new NoopMessage(), 'a_', properties)
        StringBuilder sb = new StringBuilder()
        newProps.keySet().sort().each { sb.append(it).append("=").append(newProps.get(it)).append("\n") }
        assertEquals """a_content=null
a_contentType=null
a_flags=null
a_folder=null
a_from=null
a_headers=null
a_messageNumber=0
a_receivedDate=null
a_recipients=null
a_replyTo=null
a_sentDate=null
a_subject=null
""".toString(), sb.toString()
    }

    @Test
    void getMessagePropertiesWithValidValues() {
        def message = MessageBuilder.buildMessage()
        def newProps = wrapper.getMessageProperties(message, 'a_', properties)
        def result = newProps.keySet().sort().collect { "${it}='${newProps[it]}'" }.join("\n")
        assertEquals """a_content='
aaa=bbb
foo=<b>bar</b>'
a_contentType='text/html'
a_flags='ANSWERED, DRAFT'
a_folder='Drafts/Foobar'
a_from='foo1@bar.com, foo2@bar.com'
a_headers='Foo, Bar'
a_messageNumber='1337'
a_receivedDate='2014-10-13T21:30Z'
a_recipients='foo3@bar.com, foo4@bar.com'
a_replyTo='foo1@bar.com, foo2@bar.com'
a_sentDate='2014-10-13T21:30Z'
a_subject='foobar!!!'
aaa='bbb'
foo='bar'""".toString(), result.toString()
    }

    @Test
    void getMessagePropertiesFromMultipartMessage() {
        def message = buildMultipartMessage()
        def newProps = wrapper.getMessageProperties(message, 'a_', properties)
        newProps.remove 'a_content'
        def result = newProps.keySet().sort().collect { "${it}='${newProps[it]}'" }.join("\n")
        assertEquals """a_contentType='${message.contentType}'
a_flags='ANSWERED, DRAFT'
a_folder='Drafts/Foobar'
a_from='foo1@bar.com, foo2@bar.com'
a_headers='Foo, Bar'
a_messageNumber='1337'
a_receivedDate='2014-10-13T21:30Z'
a_recipients='foo3@bar.com, foo4@bar.com'
a_replyTo='foo1@bar.com, foo2@bar.com'
a_sentDate='2014-10-13T21:30Z'
a_subject='foobar!!!'
abc='0123456789'
def='!@#\$%^&*()'
email='foobar@abc.com'
email2='foobar@abc.com'
fruit='banana'
fruit2='banana'
ghi='[];',./{}|:"?'
veg='carrot'
veg2='carrot'""".toString(), result.toString()
    }

    /* utils */

}
