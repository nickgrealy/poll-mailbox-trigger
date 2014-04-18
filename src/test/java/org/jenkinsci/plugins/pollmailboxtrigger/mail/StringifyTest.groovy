package org.jenkinsci.plugins.pollmailboxtrigger.mail

import javax.mail.Flags
import javax.mail.internet.InternetAddress

import static org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.Stringify.stringify

/**
 * @author Nick Grealy
 */
class StringifyTest extends groovy.util.GroovyTestCase {

    public static final String delim = '|'
    public static final String expected1 = '1, two, false'
    public static final String expected2 = '1|two|false'
    def list = [1, 'two', false]
    def map = ["one": 1, "two": "two", "three": false]
    def strList = ['1', 'two', 'false']

    void testList() {
        assert expected1 == stringify((List) list)
        assert expected2 == stringify((List) list, delim)
    }

    void testArray() {
        assert expected1 == stringify((Object[]) list)
        assert expected2 == stringify((Object[]) list, delim)
    }

    void testSet() {
        assert '1, false, two' == stringify(new TreeSet<Object>(strList))
        assert '1|false|two' == stringify(new TreeSet<Object>(strList), delim)
    }

    void testMap() {
        assertEquals "one=1, two=two, three=false", stringify(map)
        assert "one_1|two_two|three_false" == stringify(map, "_", delim)
    }

    void testThrowable() {
        assert stringify(new RuntimeException('foobar')).startsWith('java.lang.RuntimeException: foobar')
    }

    void testString() {
        assert 'foobar' == stringify('foobar')
    }

    void testInt() {
        assert '1' == stringify(1)
        assert '2' == stringify(new Integer(2))
    }

    void testDate() {
        def date = Calendar.instance
        date.set(2001, 2, 3, 4, 5, 6)
        assert '2001-03-03T04:05Z' == stringify(date.time)
    }

    void testFlags() {
        def flags = new Flags()
        flags.add(Flags.Flag.ANSWERED)
        flags.add(Flags.Flag.SEEN)
        assert 'ANSWERED, SEEN' == stringify(flags)
    }

    void testAddress() {
        def expected = 'Sup <foo@bar.com.au>'
        assert expected == stringify(new InternetAddress(expected))
    }
}
