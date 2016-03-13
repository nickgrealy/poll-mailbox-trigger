
Feature: Runtime - Start Job

  Background: The tests are setup.
    Given the plugin is initialised
    Given a mailbox with domain mail.com and username rick
    And the script
    """
    storeName=imap
    mail.imap.connectiontimeout=1000
    """
    When I set the configuration to
      | Host     | Username | Password |
      | mail.com | rick     | chickens |

  # happy path


  Scenario: I want the Job's description to tell me who started the job,
  So that I can have accountability.
    Given the emails
      | Subject                   | SentXMinutesAgo | IsSeenFlag | From           | Body                    |
      | Jenkins > Build Plugin #4 | 5               | false      | fred@email.com | aaa=bbb\nfoo=<b>bar</b> |
    When the Plugin's polling is triggered
    Then a Jenkins job is scheduled with quietPeriod 0 and cause '[PollMailboxTrigger] Job was triggered by email sent from fred@email.com (<a href="triggerCauseAction">log</a>)'


  Scenario: I the job's logging to give me useful information,
  So that I know exactly what happened.
    Given the emails
      | Subject                   | SentXMinutesAgo | IsSeenFlag |
      | Jenkins > Build Plugin #3 | 5               | false      |
      | Jenkins > Build Plugin #4 | 10              | false      |
    When the Plugin's polling is triggered
    Then the log is
    """
    Connecting to the mailbox...
    [Poll Mailbox Trigger] - Connected!
    Connected to mailbox. Searching for messages where:
    - [flag is unread]
    - [subject contains 'jenkins >']
    - [received date is greater than '<date>']
    ...
    Found matching email(s) : 2

    - Jenkins > Build Plugin #3 (<date>)

    - Jenkins > Build Plugin #4 (<date>)
    Download attachments? IGNORE
    Changes found. Scheduling a build.
    Marked email(s) as read : 1
    Download attachments? IGNORE
    Changes found. Scheduling a build.
    Marked email(s) as read : 1

    """


  Scenario: I want the email body to pass parameters to the Jenkins job,
  So that I can configure the Job instance.
    Given the emails
      | Subject                   | SentXMinutesAgo | IsSeenFlag | From           | Body                    |
      | Jenkins > Build Plugin #4 | 5               | false      | nick@email.com | aaa=bbb\nfoo=<b>bar</b> |
    When the Plugin's polling is triggered
    Then a Jenkins job is scheduled with quietPeriod 0 and cause '[PollMailboxTrigger] Job was triggered by email sent from nick@email.com (<a href="triggerCauseAction">log</a>)'
    And the Job parameters were - excluding pmt_receivedDate,pmt_sentDate
      | aaa                             | bbb                                                                 |
      | foo                             | bar                                                                 |
      | pmt_attachments                 | IGNORE                                                              |
      | pmt_content                     | \naaa=bbb\nfoo=<b>bar</b>                                           |
      | pmt_contentType                 | text/html                                                           |
      | pmt_flags                       | RECENT                                                              |
      | pmt_folder                      | INBOX                                                               |
      | pmt_from                        | nick@email.com                                                      |
      | pmt_headers                     | Foo, Bar                                                            |
      | pmt_host                        | mail.com                                                            |
      | pmt_jobTrigger                  | Build Plugin #4                                                     |
      | pmt_mail.debug                  | false                                                               |
      | pmt_mail.debug.auth             | false                                                               |
      | pmt_mail.imap.connectiontimeout | 1000                                                                |
      | pmt_mail.imap.host              | mail.com                                                            |
      | pmt_mail.imap.port              | 143                                                                 |
      | pmt_messageNumber               | 1337                                                                |
      | pmt_receivedXMinutesAgo         | 1440                                                                |
      | pmt_recipients                  | foo3@bar.com,foo4@bar.com                                           |
      | pmt_replyTo                     | nick@email.com                                                      |
      | pmt_retryEmailLink              | <a href="mailto:null?subject=null&body=null">Click to Retry Job</a> |
      | pmt_storeName                   | imap                                                                |
      | pmt_subject                     | Jenkins > Build Plugin #4                                           |
      | pmt_subjectContains             | jenkins >                                                           |
      | pmt_username                    | rick                                                                |


    # todo: Finish writing acceptance test
  @ignore
  Scenario: I want the email to save any attachments to disk, and pass in file pointers in the environment variables,
  So that I have access to the email attachments from the Job.
    Given the emails
      | Subject                   | SentXMinutesAgo | IsSeenFlag | From           | Body    | Attachments            |
      | Jenkins > Build Plugin #4 | 5               | false      | nick@email.com | nothing | lighter.jpg,aurora.jpg |
    When the Plugin's polling is triggered
    Then the log is
    """
    foobar
    """
    Then a Jenkins job is scheduled with quietPeriod 0 and cause '[PollMailboxTrigger] Job was triggered by email sent from nick@email.com (<a href="triggerCauseAction">log</a>)'
    And the Job parameters were - excluding pmt_receivedDate,pmt_sentDate
      | pmt_attachment1                 | lighter.jpg               |
      | pmt_attachment2                 | aurora.jpg                |
      | pmt_content                     | \nnothing                 |
      | pmt_contentType                 | text/html                 |
      | pmt_flags                       | RECENT                    |
      | pmt_folder                      | INBOX                     |
      | pmt_from                        | nick@email.com            |
      | pmt_headers                     | Foo, Bar                  |
      | pmt_host                        | mail.com                  |
      | pmt_jobTrigger                  | Build Plugin #4           |
      | pmt_mail.debug                  | false                     |
      | pmt_mail.debug.auth             | false                     |
      | pmt_mail.imap.connectiontimeout | 1000                      |
      | pmt_mail.imap.host              | mail.com                  |
      | pmt_mail.imap.port              | 143                       |
      | pmt_messageNumber               | 1337                      |
      | pmt_receivedXMinutesAgo         | 1440                      |
      | pmt_recipients                  | foo3@bar.com,foo4@bar.com |
      | pmt_replyTo                     | nick@email.com            |
      | pmt_storeName                   | imap                      |
      | pmt_subject                     | Jenkins > Build Plugin #4 |
      | pmt_subjectContains             | jenkins >                 |
      | pmt_username                    | rick                      |

  # todo: Two emails triggers two job instances.
  # todo: Error occurs, shown in the logs.