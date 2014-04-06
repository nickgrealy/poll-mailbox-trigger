poll-mailbox-trigger
================

A Jenkins plugin, to poll an email inbox, and trigger jobs based on new emails.

---

## Table of contents

1. [Overview](#overview)
1. [Screenshot](#screenshot)
1. [Configuration](#configuration)
1. [Changelog](#changelog)
1. [Backlog](#backlog)
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

---

## Screenshot

_Screenshot - Version 0.2_

![Version 0.2](screenshot-0.2.png "Version 0.2")

---

## Configuration

The Email Properties, allows you to configure the plugin, using standard key=value property notation.
__N.B.__ I've used gmail connection details for all example values.

### Plugin Configuration

You'll need to supply all the following properties:

    host=imap.gmail.com
    username=<your_email>@gmail.com
    password=<your_password>
    storeName=imaps
    folder=inbox

For google passwords, go to "Google account > security > app passwords".
__Warning:__ passwords are not currently encrypted.

### Filter Configuration

The following optional properties allow you to filter which unread emails are read (on the server side):

    subjectContains=jenkins
    receivedXMinutesAgo=60

### Java Imap(/s) Configuration

You can also include [java imap properties](https://javamail.java.net/nonav/docs/api/com/sun/mail/imap/package-summary.html):

    mail.imap.host=imap.gmail.com
    mail.imap.port=993
    ... etc ...

### Debugging

Don't even try to connect to an Exchange server, without setting these:

    mail.debug=true
    mail.debug.auth=true

---

## ChangeLog

### 0.2
1. Add email properties (e.g. to, from, cc, bcc, subject, body) as job parameters

The following build parameters, are now injected into the job (taken from the email trigger):

    pmt_content=--001a11c1370a85d90904f64302f5\nContent-Type: text/plain; charset=UTF-8 etc...
    pmt_headers=MIME-Version=1.0, Received=by 10.140.24.231 with HTTP; Fri, 4 Apr 2014 20:12:52 -0700 (PDT), etc...
    pmt_folder=inbox
    pmt_flags=,
    pmt_replyTo=someone@gmail.com
    pmt_recipients=someone@gmail.com
    pmt_subject=jenkins_rocks
    pmt_sentDate=2014-04-05T14:12Z
    pmt_messageNumber=28917
    pmt_contentType=multipart/ALTERNATIVE; boundary=111111
    pmt_from=someone@gmail.com
    pmt_receivedDate=2014-04-05T14:12Z

---

## Backlog
1. get this plugin published under jenkinsci!
1. interpret email body directly as build parameters (see mailto links)
1. Encrypt credentials
1. Setup a standard, whereby any Jenkins job is triggered, by the subject name.
1. Add config as build parameters?


### To Document
1. Give config examples for connecting to Gmail, MS Exchange, etc?

### To Test
1. Test using variable replacement!
1. Test build options - node label, concurrent builds
1. Jenkins support - try and support as far back/forwards as possible
1. Java Support - test with Java 1.5

### Optional
1. Internationalise all fields (i18n)
1. Add option to filter emails by other fields (e.g. "from")
1. Have default System Config > overridden by individual Build config
1. Add examples for using mailtos (e.g. in failed build job emails)
1. Add service, which sends an email with mailtos for triggering all available jobs
1. Download email attachments - attach as link to job's build parameters?


This is just my list, please feel free to email me with any suggestions you might have! (Until I setup bug tracking)