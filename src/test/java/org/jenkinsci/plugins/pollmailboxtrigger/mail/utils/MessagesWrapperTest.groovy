package org.jenkinsci.plugins.pollmailboxtrigger.mail.utils

import org.jenkinsci.plugins.pollmailboxtrigger.mail.testingTools.NoopFolder
import org.jenkinsci.plugins.pollmailboxtrigger.mail.testingTools.NoopLogger
import org.jenkinsci.plugins.pollmailboxtrigger.mail.testingTools.NoopMessage
import org.junit.Before
import org.junit.Test

import javax.mail.Flags
import javax.mail.Folder
import javax.mail.Message
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMultipart

import static org.junit.Assert.assertEquals

/**
 * Created by nickgrealy@gmail.com on 13/10/14.
 */
class MessagesWrapperTest {

    public static final Date TEST_DATE = new Date(1413196250440)
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
        def message = buildMessage()
        def newProps = wrapper.getMessageProperties(message, 'a_', properties)
        def result = newProps.keySet().sort().collect { "${it}='${newProps[it]}'" }.join("\n")
        assertEquals """a_content='
aaa=bbb
foo=bar'
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
        def result = newProps.keySet().sort().collect { "${it}='${newProps[it]}'" }.join("\n")
        assertEquals """a_content='

aaa=bbb
foo=bar

--
Kind regards,

Nick Grealy
M: +61 4 1234 5678
<div dir="ltr">aaa=bbb<br clear="all">foo=bar<br clear="all"><div><br></div>-- <br>Kind regards,<br><br><div>Nick Grealy</div><div>M: +61 4 1234 5678</div>
</div>'
a_contentType='${message.contentType}'
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

    private Message buildMessage(subject) {
        (subject == null ? buildMessageCandidate() : buildMessageCandidate(subject)) as NoopMessage
    }

    private Map buildMessageCandidate(subject = 'foobar!!!') {
        [
                getSentDate     : { TEST_DATE },
                getFrom         : { ['foo1@bar.com', 'foo2@bar.com'].collect { new InternetAddress(it) } },
                getSubject      : { subject },
                getFlags        : {
                    def flags = new Flags()
                    flags.add(Flags.Flag.ANSWERED)
                    flags.add(Flags.Flag.DRAFT)
                    flags
                },
                getFolder       : { [getFullName: { 'Drafts/Foobar' }] as NoopFolder },
                getMessageNumber: { 1337 },
                getReceivedDate : { TEST_DATE },
                getAllHeaders   : { Collections.enumeration(['Foo', 'Bar']) },
                getContentType  : { 'text/html' },
                getAllRecipients: { ['foo3@bar.com', 'foo4@bar.com'].collect { new InternetAddress(it) } },
                getContent      : { 'aaa=bbb\nfoo=bar'.toString() },
                isMimeType      : { it.startsWith('text') }
        ]
    }


    Message buildMultipartMessage() {
        // build multipart message
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText("""
aaa=bbb
foo=bar

--
Kind regards,

Nick Grealy
M: +61 4 1234 5678""", "TEXT/PLAIN; charset=UTF-8");

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent("""<div dir="ltr">aaa=bbb<br clear="all">foo=bar<br clear="all"><div><br></div>-- <br>Kind regards,<br><br><div>Nick Grealy</div><div>M: +61 4 1234 5678</div>
</div>""", "TEXT/HTML; charset=UTF-8");
        def multiPart = new MimeMultipart("ALTERNATIVE")
        multiPart.addBodyPart(textPart)
        multiPart.addBodyPart(htmlPart)
        def candidate = buildMessageCandidate()
        candidate['getContent'] = { multiPart }
        candidate['isMimeType'] = { it.startsWith('multipart') }
        candidate['getContentType'] = { multiPart.getContentType() }
        candidate as NoopMessage
    }

}
