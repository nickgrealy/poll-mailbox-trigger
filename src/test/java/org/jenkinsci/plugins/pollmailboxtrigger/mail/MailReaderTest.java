package org.jenkinsci.plugins.pollmailboxtrigger.mail;

/**
 * @author Nick Grealy
 */
public class MailReaderTest {

    public static void main(String[] args) throws Throwable {
        new MailReaderTest().testRetrieveMail();
    }

    /*

    domainF123456@btfin.com
     */
//    @Test
    //@Ignore
    public void testRetrieveMail() throws Throwable {
        String host = "btmail.btfin.com";//"sp.btfinancialgroup.com");
        String user = "nicholas.grealy@btfinancialgroup.com";
//        String user = "btfin.com\\L065295\\nicholas.grealy";
        String password = "P@ssword02";
        String storeName = "imaps";
        boolean ssl = true;
        CustomProperties cp = new CustomProperties();
        cp.put("mail." + storeName + ".host", host);
        cp.put("mail." + storeName + ".port", ssl ? "993" : "143");
        if (ssl) {
            cp.put("mail." + storeName + ".ssl.trust", "*");
//            cp.put("mail."+storeName+".starttls.enable", "true");
//            cp.put("mail."+storeName+".ssl.enable", "false");
        }
        // login settings
        cp.put("mail." + storeName + ".auth.ntlm.domain", "L065295@btfin.com");
//        cp.put("mail."+storeName+".auth.ntlm.disable", "true");
        cp.put("mail." + storeName + ".auth.plain.disable", "true");
        cp.put("mail." + storeName + ".auth.gssapi.disable", "true");
        cp.put("mail.debug", "true");
        cp.put("mail.debug.auth", "true");
//        cp.put("mail.user", user);
//        cp.put("mail.host", host);
        MailReader reader = new MailReader(Logger.DEFAULT,
                host,
                storeName,
                user,
                password,
                cp.getProperties()).connect();
        reader.listFolders();
//        MailWrapperUtils.MessagesWrapper messagesWrapper = reader
//                .folder("INBOX").search(
//                not(flag(Flags.Flag.SEEN))
//                , receivedSince(relativeDate(Calendar.DATE, -10))
////                , subject("jenkins_rocks")
//        ).print();
//        Assert.assertTrue(true);
//        List<Message> messages = messagesWrapper.print().getMessages();
//        assertTrue("size should be greater than zero!", messages.size() > 0);
//        Map<String, String> map = messagesWrapper.getMessageProperties(messages.get(0));
//        for (String key : map.keySet()) {
//            System.out.println(key + "=" + map.get(key));
//        }
    }


}
