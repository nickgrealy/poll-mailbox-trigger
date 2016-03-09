[![Version](http://sebastian-badge.info/plugins/poll-mailbox-trigger-plugin.svg "Version")](https://wiki.jenkins-ci.org/display/JENKINS/Poll Mailbox Trigger Plugin) [![Build Status](https://jenkins.ci.cloudbees.com/buildStatus/icon?job=plugins/poll-mailbox-trigger-plugin&style=plastic "Build Status")](https://jenkins.ci.cloudbees.com/job/plugins/job/poll-mailbox-trigger-plugin/) [![Dependency Status](https://www.versioneye.com/user/projects/56d76c71d71695003886c352/badge.svg?style=plastic)](https://www.versioneye.com/user/projects/56d76c71d71695003886c352) [![Code Coverage](https://codecov.io/github/nickgrealy/poll-mailbox-trigger/coverage.svg?branch=master)](https://codecov.io/github/nickgrealy/poll-mailbox-trigger?branch=master) [![Join the chat at https://gitter.im/jenkinsci/poll-mailbox-trigger-plugin](https://badges.gitter.im/jenkinsci/poll-mailbox-trigger-plugin.svg)](https://gitter.im/jenkinsci/poll-mailbox-trigger-plugin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# Poll Mailbox Trigger Plugin

A Jenkins plugin, to poll an email inbox, and trigger jobs based on new emails.

Table of contents
---

1. [Overview](#overview)
1. [Building](#building)
1. [Screenshots](#screenshots)
1. [Configuration](#configuration)
    1. [GMAIL](#gmail)
    1. [HOTMAIL](#hotmail)
    1. [ZIMBRA](#zimbra)
    1. [Office 365](#office-365)
    1. [MS Exchange Server 2013](#ms-exchange-server-2013)
    1. [Want to add a server example](#want-to-add-a-server-example)
1. [Email Conventions](#email-conventions)
1. [Troubleshooting](#troubleshooting)
1. [Want to say thanks?](#want-to-say-thanks)

__Additional:__

1. [Changelog](#changelog)
1. [Backlog](#backlog)
1. [Wiki](https://wiki.jenkins-ci.org/display/JENKINS/poll-mailbox-trigger-plugin)
1. [Source Code](https://github.com/jenkinsci/poll-mailbox-trigger-plugin)
1. [Issue Tracking](https://issues.jenkins-ci.org/secure/IssueNavigator.jspa?jqlQuery=component+%3D+%27poll-mailbox-trigger-plugin%27)
1. [Authors](#authors)
1. [License](#license)

Overview
---

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

Building
---

Prerequisites:

- JDK 6 (or above)

To setup for use with Intellij IDEA

```Shell
./gradlew cleanIdea idea
```

To run Jenkins ([http://localhost:8080](http://localhost:8080)) locally with the plugin installed:

```Shell
./gradlew clean server
```

To build the Jenkins plugin (.jpi) file:

```Shell
./gradlew clean jpi
```

To publish/release the Jenkins plugin:

1. Update the `version` in `gradle.properties`, to remove "-SNAPSHOT" (increment and re-add after publishing)
2. Setup the `~/.jenkins-ci.org` credentials file as per the instructions here [https://wiki.jenkins-ci.org/display/JENKINS/Gradle+JPI+Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Gradle+JPI+Plugin)
3. Then run the command:

```Shell
./gradlew clean publish
```

Screenshots
---

_Screenshot - Version 1.018_

![Version 1.018](src/main/site/images/screenshot-version-1.018.png "Version 1.018")


Configuration
---

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

#### [Office 365](#office-365 "Thanks to Jason Swager for this contribution!")
    host=<your_mail_server>
    username=<your_email>
    password=<your_password>
    mail.imap.ssl.enable=true
    mail.imap.starttls.enable=true

#### [MS Exchange Server 2013](#ms-exchange-server-2013 "Thanks to Shaun Alexander for this contribution!")
    host=<your_imap_host_server>
    username=<your_email>
    password=<your_password>
    mail.imaps.port=993
    mail.imap.ssl.enable=true
    mail.imap.starttls.enable=true

#### Want to add a server example?
    If you've managed to successfully setup a connection, 
    please contact me, and I'll update this section.

Email Conventions
---

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

    `<a href="mailto:<jenkins_poll_mailbox>@gmail.com?subject=Jenkins > $JOB_NAME">Retry $JOB_NAME</a>`

That way, when the job fails, it will email you a nice "Retry My Job" link. Clicking on it, opens a new email, which
you can just send straight away (good for mobile phones).

### Custom Job Parameters

As of version 0.15, you can pass addition custom job parameters to the new build, by simply putting key=value property
notation into the content of your email. N.B. both `text/plain` and `text/html` content types are supported. Emails
with attachments have not yet been tested.

For example:

    Hi Jenkins,

    Please execute the job with these parameters:

    fruit=banana
    veg=carrot
    email=foobar@abc.com

    --
    Kind regards,

    Nick

Will inject the following job parameters into the new job instance:

|Name    | Value           |
|--------|-----------------|
|fruit   | banana          |
|veg     | carrot          |
|email   | foobar@abc.com  |

Troubleshooting
---

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

Want to say thanks?
---

Want to say thanks but can't find the words? [Coffee donations are VERY welcome](http://nickgrealy.github.io/)!

![http://wrldhq.com/2014/02/12/new-meaning-to-the-term-coffee-drip-coined/](src/main/site/images/star-wars-coffee.jpg "http://wrldhq.com/2014/02/12/new-meaning-to-the-term-coffee-drip-coined/")

ChangeLog
---

### 1.020
1. If the "script" property has spaces only (someone might have pressed some spaces by mistake), the CustomProperties class still should recognized it as empty (.equals(""))
1. The default values of the properties were being loaded AFTER loading the user-provided ones. For instance I want to get rid of the subject filter "jenkins >" and I cannot do that, because when I add the property with the empty value, this default takes over. Thus, I submit this PR to change the order in which these data is loaded: first we load the defaults, THEN we load the user-provided values

### 1.018
1. Fixed defect in Session.getDefaultInstance -> getInstance
1. Changed build process to use gradle

### 0.15
1. Fixed defect in parsing properties

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

Authors
---

Nick Grealy - <nickgrealy@gmail.com>

License
---

Licensed under the [MIT License (MIT)](LICENSE)
