poll-mailbox-trigger
================

A Jenkins plugin, to poll an email inbox, and trigger jobs based on new emails.

[![Built on CloudBees](src/main/site/images/Button-Powered-by-CB.png "Built on CloudBees")](http://www.cloudbees.com)

---

## Table of contents

1. [Overview](#overview)
1. [Screenshots](#screenshots)
1. [Configuration](#configuration)
1. [Email Conventions](#email-conventions)
1. [Troubleshooting](#troubleshooting)

__Additional:__

1. [Changelog](#changelog)
1. [Backlog](#backlog)
1. [Wiki](https://wiki.jenkins-ci.org/display/JENKINS/poll-mailbox-trigger-plugin)
1. [Source Code](https://github.com/jenkinsci/poll-mailbox-trigger-plugin)
1. [Issue Tracking](https://issues.jenkins-ci.org/secure/IssueNavigator.jspa?jqlQuery=component+%3D+%27poll-mailbox-trigger%27)
1. [License](LICENSE)

---

## Overview

The _poll-mailbox-trigger_ allows a build to poll an email inbox using the imap protocol.
When an unread email is found matching the configured criteria, it:

1. marks the email as read, so that it is not reprocessed
1. triggers a new job

### Why?

Mainly, because I want to be able to (re)trigger a (failing?) build, from the comfort of my home/beach/pub.
I may not always have direct/sychronous access to the build server (due to firewalls, network access, etc).
I'm already being notified by email when a job fails, why can't I just send an email response saying "retry"?

If you're working in a corporate environment, and are lucky enough to have a build server
 there's probably a __very__ small chance that the build server is also exposed to the outside world
 (without using a VPN).

Email is:

1. prevalent - accessible pretty much anywhere
1. convenient - it is built into my mobile phone
1. asychronous - I can fire it now and let it get picked up later
1. adopted - it's already being used to notify me of failed builds

Also, some side notes:

1. I haven't met a Jenkins interface for mobile devices that I like.
1. Email To SMS Gateways exist, for those that don't have Email on their mobile phones.
1. Polling is not ideal

Alternatives to polling?

1. Utilising [Push-IMAP](http://en.wikipedia.org/wiki/Push-IMAP)
1. Installing a sendmail/postfix server, forward emails to it, and write a perl script to process incoming emails.

---

## Screenshots

_Screenshot - Version 0.12_

![Version 0.12](src/main/site/images/screenshot-0.12.png "Version 0.12")

---

## Configuration

The <i>Host</i> field, allows you to enter the DNS name/hostname/IP Address of the server, hosting the email account service.

The <i>Username</i> field, allows you to enter the username required to connect to this email account.

The <i>Password</i> field, allows you to enter the password (<b>N.B.</b> or application password) required to connect to this email account.

The _Advanced Email Properties_ field, allows you to configure the plugin, using standard key=value property notation.

You can override the following default property values:

    # Connection configuration
    storeName=imaps

    # Search configuration
    folder=INBOX
    subjectContains=jenkins >
    receivedXMinutesAgo=1440

    # JavaMail configuration
    mail.debug=true
    mail.debug.auth=true
    mail.imaps.host=<above_host>
    mail.imaps.port=993

You can also add [java imap properties](https://javamail.java.net/nonav/docs/api/com/sun/mail/imap/package-summary.html),
to further configure the connection.

### Sample Configurations

Below are some sample configurations for common web based email services:

#### GMAIL
For google passwords, go to "Google account > security > app passwords".

    host=imap.gmail.com
    username=<your_email>@gmail.com
    password=<your_application_password>

#### HOTMAIL
For hotmail passwords, go to "Account Settings > Security Info > Create a new app password".

    host=imap-mail.outlook.com
    username=<your_email>@hotmail.com
    password=<your_application_password>

#### ZIMBRA
    host=<your_mail_server>
    username=<your_email>
    password=<your_password>

#### MS Exchange Servers (2003, 2007, 2010, 2013)
    I haven't yet attempted this, if you've managed to successfully setup a connection, please contact me, and I'll
    update this section.

---

## Email Conventions

### Job Parameters

The following build parameters, are injected into the job (sample values provided):

#### From the Incoming Email

|Name             |Sample value                                                |
|-----------------|------------------------------------------------------------|
|pmt_content      |&lt;the email's content&gt;                                 |
|pmt_contentType  |multipart/ALTERNATIVE; boundary=1234                        |
|pmt_flags        |SEEN                                                        |
|pmt_folder       |INBOX                                                       |
|pmt_from         |Me &lt;Sender@gmail.com&gt;                                 |
|pmt_headers      |Delivered-To=Foobar@gmail.com etc                           |
|pmt_messageNumber|14                                                          |
|pmt_receivedDate |2014-10-14T12:19Z                                           |
|pmt_recipients   |Foobar@gmail.com                                            |
|pmt_replyTo      |Me &lt;Sender@gmail.com&gt;                                 |
|pmt_sentDate     |2014-10-14T12:19Z                                           |
|pmt_subject      |Jenkins &gt; My Job                                         |

#### From the poll-mailbox-trigger configuration

|Name                   |Sample value    |
|-----------------------|----------------|
|pmt_mail.debug         |TRUE            |
|pmt_mail.debug.auth    |TRUE            |
|pmt_mail.imaps.host    |imap.gmail.com  |
|pmt_mail.imaps.port    |993             |
|pmt_host               |imap.gmail.com  |
|pmt_username           |Foobar@gmail.com|
|pmt_storeName          |imaps           |
|pmt_receivedXMinutesAgo|1440            |
|pmt_subjectContains    |Jenkins &gt;    |

#### Calculated values (pmt_subject - pmt_subjectContains)

|Name                   |Sample value    |
|-----------------------|----------------|
|pmt_jobTrigger         |My Job          |

N.B. Please note, these variables are injected into the triggered build (only). No other actions (implied or assumed)
are taken. (e.g. No jenkins node filtering is performed, unless you've configured the job to do so. No additional jobs
are triggered, unless you've configured the job to do so. And so on.)

#### Helpful Tips

##### Simple "Retry" email link on failed builds

1. Setup a "Mail Trigger" job, which builds other jobs based on the "pmt_jobTrigger" value.
1. Setup an editable Email notification on the target job, so that it emails you when a job has failed.
1. Include the following html, in the editable Email job configuration.

    `&lt;a href="mailto:&lt;jenkins_poll_mailbox&gt;@gmail.com?subject=Jenkins &gt; $JOB_NAME"&gt;Retry $JOB_NAME&lt;/a&gt;`

That way, when the job fails, it will email you a nice "Retry My Job" link. Clicking on it, opens a new email, which
you can just send straight away (good for mobile phones).

### Custom Job Parameters

As of version 0.14, you can pass addition custom job parameters to the new build, by simply putting key=value property
notation into the content of your email. N.B. both `text/plain` and `text/html` content types are supported. Emails
with attachments have not yet been tested.

For example:

    Hi Jenkins,

    Please execute the job with these parameters:

    aaa1=bbb
    aaa2= This is a test.
    aaa3= This is a test. - foo !
    aaa4_aaa=bbb
    aaa5 aaa=ccc
    aaa6.aaa=ddd

    --
    Kind regards,

    Nick

Will inject the following job parameters into the new job instance:

|Name    | Value                  |
|--------|------------------------|
|aaa1    | bbb                    |
|aaa2    | This is a test.        |
|aaa3    | This is a test. - foo !|
|aaa4_aaa| bbb                    |
|aaa5    | aaa=ccc                |
|aaa6.aaa| ddd                    |

---

## Troubleshooting

###1. Error: javax.mail.AuthenticationFailedException: AUTHENTICATE failed.
__Solution:__ Check the credentials you're using are correct.

###2. Error: javax.mail.MessagingException: com.ibm.jsse2.util.j: PKIX path building failed: java.security.cert.CertPathBuilderException: PKIXCertPathBuilderImpl could not build a valid CertPath.; ...
__Solution:__ To ignore certificate verification errors, you can use the following config property:

    mail.imaps.ssl.trust=*

__Warning:__ it's not advisable to ignore certificate verification errors (unless perhaps in a test environment): this defeats the point of using SSL/TLS. Instead, if you know you trust that server certificate, import it in your trust store, and specify the location of the trust store using:

    javax.net.ssl.trustStrore=/path/to/cacerts.jks

###3. Error : javax.mail.MessagingException: Connection timed out: connect;
__Solution:__ Check the Jenkins server can access the email server and port, by running the command (from the Jenkins server):

    telnet <your_host> <your_port_143_or_993>

If you get a message similar to the following, then there is no way to create a direct connection to the mail server - probably
the network is down, or the connection has been blocked by a firewall. If so, please check your network settings with
your network administrator. You may need to specify SOCKS proxy details, in the <i>Advanced Email Properties</i>.

    Connecting To imap.gmail.com...Could not open connection to the host, on port 993: Connect failed

###4. Error : java.lang.NullPointerException at org.jenkinsci.plugins.pollmailboxtrigger.PollMailboxTrigger.initialiseDefaults(PollMailboxTrigger.java:98)
__Solution:__ I'm not quite sure what the cause is! If you're able to reproduce the issue, please contact me with instructions.
In the meantime, the error is caught and the following message is displayed.

    Could not get Jenkins instance using Jenkins.getInstance() (returns null). This can happen if Jenkins
    has not been started, or was already shut down. Please see
    http://javadoc.jenkins-ci.org/jenkins/model/Jenkins.html#getInstance() for more details. If you believe
    this is an error, please raise an 'issue' under
    https://wiki.jenkins-ci.org/display/JENKINS/poll-mailbox-trigger-plugin.

---

## ChangeLog

### 0.14
1. Added ability to inject 'custom variables' as job parameters
1. Added handling for multipart emails
1. Improved handling of node properties (thanks [Charlie Stott](https://github.com/ecolyx)!)

### 0.12
1. Changed deprecated code - Hudson.getInstance() to Jenkins.getInstance()
1. Added exception handling, to provide a more informative error when Jenkins.getInstance() returns null.

### 0.11
1. interpret email body directly as build parameters (see mailto links)
1. Test using variable replacement!
1. Add config as build parameters

### 0.9
1. Change package dependencies, so that there is no dependency on ScriptTrigger (for future cloudbees support)
1. Implemented encrypted passwords (test connections using SSH keys)

### 0.8
1. Added default properties (to minimise configuration)
1. Documented and tested (successfully) configurations for the common web based email services.

### 0.5
1. Added a "Test Connection" button

### 0.4
1. get this plugin published under jenkinsci!

### 0.2
1. Add email properties (e.g. to, from, cc, bcc, subject, body) as job parameters (pmt_*)

---

## Backlog
1. Add option to filter emails by other fields (e.g. "from")
1. test email connections using SSH keys
1. Implement Push-IMAP?

### To Document
1. Test and Document config examples for connecting to MS Exchange

### To Test
1. Test emails with attachments
1. Write UnitTests!!! (preferably using groovy+selenium+webdriver+junit+bdd(easyb/cucumber))
1. Test build options - node label, concurrent builds
1. Jenkins support - try and support as far back/forwards as possible
1. Java Support - test with Java 1.5

### Optional
1. Internationalise all fields (i18n)
1. Have default System Config > overridden by individual Build config?
1. Add service, which sends an email with mailtos for triggering all available jobs
1. Download email attachments - attach as link to job's build parameters?