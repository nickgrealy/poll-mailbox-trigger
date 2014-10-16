package org.jenkinsci.plugins.pollmailboxtrigger.mail.utils

import org.junit.Ignore
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

/**
 * Created by nickgrealy@gmail.com on 16/10/2014.
 */
class CustomPropertiesTest {

    def static final TEXT = '''
fruit=banana
veg=carrot
email=gaurav.chhabra@abc.com'''
    def static final HTML = '<div dir="ltr"><div style="font-family:arial,sans-serif;font-size:13px"><font color="#990000" face="verdana, sans-serif">fruit=banana<br></font></div><div style="font-family:arial,sans-serif;font-size:13px"><div><font color="#990000" face="verdana, sans-serif">veg=carrot</font></div><div><font color="#990000" face="verdana, sans-serif">email=<a href="mailto:gaurav.chhabra@abc.com" target="_blank">gaurav.chhabra@abc.com</a></font></div></div><div><br></div><div><div>fruit2=banana</div><div>veg2=carrot</div><div>email2=<a href="mailto:gaurav.chhabra@abc.com">gaurav.chhabra@abc.com</a></div></div><div><br></div><div>abc=0123456789</div><div>def=!@#$%^&amp;*()</div><div>ghi=[]\\;&#39;,./{}|:&quot;&lt;&gt;?</div><div><br></div>-- <br>Kind regards,<br><br><div>Nick Grealy</div><div>M: +61 4 0775 6895</div><div>E: <a href="mailto:nickgrealy@gmail.com" target="_blank">nickgrealy@gmail.com</a></div></div>'

    @Test
    public void testReadText() {
        def read = CustomProperties.read(TEXT)
        [
                'fruit': 'banana',
                'veg'  : 'carrot',
                'email': 'gaurav.chhabra@abc.com'
        ].each {
            assertEntry(it, read)
        }
    }

    @Ignore('This is a rubbish test!')
    @Test
    public void testReadHtml() {
        def read = CustomProperties.read(HTML)
        [
                'fruit': 'banana',
                'veg'  : 'carrot',
                'email': 'gaurav.chhabra@abc.com'
        ].each {
            assertEntry(it, read)
        }
    }

    def assertEntry(Map.Entry entry, CustomProperties actual) {
        assertTrue "map doesn't contain key '$entry.key' - map='$actual'", actual.containsKey(entry.key)
        assertEquals "key='$entry.key' - actual value doesn't match expected value", actual[entry.key], entry.value
    }
}
