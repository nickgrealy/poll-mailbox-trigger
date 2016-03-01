package org.jenkinsci.plugins.pollmailboxtrigger.bdd

class EmailRow {

    String subject
    int sentXMinutesAgo
    boolean isSeenFlag
    String from
    String body
    String attachments

}
