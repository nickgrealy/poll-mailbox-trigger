package org.jenkinsci.plugins.pollmailboxtrigger.mail.utils;

import org.apache.commons.lang.StringUtils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static javax.mail.Flags.Flag.*;

/**
 * @author Nick Grealy
 */
public abstract class Stringify {

    public static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

    public static String stringify(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    public static String stringify(String message) {
        return message;
    }

    public static <A extends Object> String stringify(A[] array) {
        return stringify(array, null);
    }

    public static <A extends Object> String stringify(A[] array, String delimiter) {
        return stringify(Arrays.asList(array), delimiter);
    }

    public static String stringify(List list) {
        return stringify(list.iterator(), null);
    }

    public static String stringify(List list, String delimiter) {
        return stringify(list.iterator(), delimiter);
    }

    public static String stringify(Set list) {
        return stringify(list.iterator(), null);
    }

    public static String stringify(Set list, String delimiter) {
        return stringify(list.iterator(), delimiter);
    }

    public static String stringify(Iterator iterator, String delimiter) {
        if (delimiter == null) {
            delimiter = ", ";
        }
        StringBuilder sb = new StringBuilder();
        while (iterator.hasNext()) {
            try {
                sb.append(stringify(iterator.next()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (iterator.hasNext()) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    public static String stringify(Iterator<Map.Entry> iterator, String keyValueDelimiter, String delimiter) {
        if (keyValueDelimiter == null) {
            keyValueDelimiter = "=";
        }
        if (delimiter == null) {
            delimiter = ", ";
        }
        StringBuilder sb = new StringBuilder();
        while (iterator.hasNext()) {
            try {
                sb.append(stringify(iterator.next(), keyValueDelimiter));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (iterator.hasNext()) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    public static String stringify(Date date) {
        if (date == null){
            return "null";
        }
        return df.format(date);
    }

    public static String stringify(int messageNumber) {
        return String.valueOf(messageNumber);
    }

    public static String stringify(Enumeration allHeaders) {
        if (allHeaders == null){
            return "null";
        }
        List<Object> list = new ArrayList<Object>();
        while (allHeaders.hasMoreElements()) {
            list.add(allHeaders.nextElement());
        }
        return stringify(list);
    }

    public static String stringify(Map content) {
        return stringify(content, null, null);
    }

    public static String stringify(Map content, String keyValueDelimiter, String delimiter) {
        return stringify(content.entrySet().iterator(), keyValueDelimiter, delimiter);
    }

    public static String stringify(Map.Entry content, String keyValueDelimiter) {
        return content.getKey() + keyValueDelimiter + content.getValue();
    }

    public static String stringify(Object content) {
        if (content instanceof Header) {
            return stringify((Header) content);
        } else if (content instanceof Part) {
            return stringify((Part) content);
        } else if (content instanceof Map) {
            return stringify((Map) content);
        } else if (content instanceof Flags.Flag) {
            return stringify((Flags.Flag) content);
        }
        return content == null ? "null" : content.toString();
    }


    /* javax.mail */

    public static final String TEXT_WILDCARD = "text/*", TEXT_HTML = "text/html", TEXT_PLAIN = "text/plain",
            MULTIPART_WILDCARD = "multipart/*", MULTIPART_ALTERNATIVE = "multipart/alternative", NEWLINE = "\n",
            BLANK = "";


    public static String stringify(Header content) {
        return content.getName() + "=" + content.getValue();
    }

    public static String stringify(Address[] addresses) {
        List<String> parsed = new ArrayList<String>();
        for (Address addr : addresses){
            if (addr instanceof InternetAddress){
                parsed.add(((InternetAddress)addr).getAddress());
            } else {
                parsed.add(addr.toString());
            }
        }
        return StringUtils.join(parsed, ",");
    }

    /**
     * Get text from all parts of the email.
     * Loosely based on <a href="http://www.oracle.com/technetwork/java/javamail/faq/index.html#mainbody">oracle.com</a>.
     * @param part - the mail part to convert to string.
     * @return String
     */
    public static String stringify(Part part) {
        try {
            return stringify(new StringBuilder(), part);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String stringify(StringBuilder builder, Part part) throws MessagingException, IOException {
        final Object content = part.getContent();
        if (content == null){
            return "null";
        }
        if (part.isMimeType(TEXT_WILDCARD) && content instanceof String) {
            builder.append(NEWLINE + content);
        }
        if (part.isMimeType(MULTIPART_WILDCARD) && content instanceof Multipart) {
            Multipart mp = (Multipart) content;

            // iterate backwards through the parts... should be html first (muddled with html tags), plain text (pure) last
            for (int i = mp.getCount()-1; i >= 0; i--) {
                stringify(builder, mp.getBodyPart(i));
            }
        }
        return builder.toString();
    }

    public static String stringify(Flags flags) {
        if (flags == null){
            return "null";
        }
        List tmp = new ArrayList();
        tmp.addAll(Arrays.asList(flags.getSystemFlags()));
        tmp.addAll(Arrays.asList(flags.getUserFlags()));
        return stringify(tmp);
    }

    public static String stringify(Flags.Flag flag) {
        if (flag == SEEN) {
            return "SEEN";
        } else if (flag == ANSWERED) {
            return "ANSWERED";
        } else if (flag == DELETED) {
            return "DELETED";
        } else if (flag == DRAFT) {
            return "DRAFT";
        } else if (flag == FLAGGED) {
            return "FLAGGED";
        } else if (flag == RECENT) {
            return "RECENT";
        } else if (flag == USER) {
            return "USER";
        } else {
            return "";
        }
    }

    public static String stringify(Folder folder) {
        if (folder == null){
            return "null";
        }
        return folder.getFullName();
    }

//    public static String stringify(MimeMultipart content) {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        try {
//            content.writeTo(baos);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        return new String(baos.toByteArray());
//    }
}