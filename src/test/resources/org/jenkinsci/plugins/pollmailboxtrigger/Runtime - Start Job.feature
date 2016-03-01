@wip
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


  Scenario: I want the email body to pass parameters to the Jenkins job,
  So that I can configure the Job instance.
    Given the emails
      | Subject                   | SentXMinutesAgo | IsSeenFlag | From           | Body                    |
      | Jenkins > Build Plugin #4 | 5               | false      | nick@email.com | aaa=bbb\nfoo=<b>bar</b> |
    When the Plugin's polling is triggered
    Then a Jenkins job is scheduled with quietPeriod 0 and cause '[PollMailboxTrigger] Job was triggered by email sent from nick@email.com (<a href="triggerCauseAction">log</a>)' and parameters
      | aaa                             | bbb                        |
      | foo                             | bar                        |
      | pmt_content                     | \naaa=bbb\nfoo=<b>bar</b>  |
      | pmt_contentType                 | text/html                  |
      | pmt_flags                       | RECENT                     |
      | pmt_folder                      | INBOX                      |
      | pmt_from                        | nick@email.com             |
      | pmt_headers                     | Foo, Bar                   |
      | pmt_host                        | mail.com                   |
      | pmt_jobTrigger                  | Build Plugin #4            |
      | pmt_mail.debug                  | false                      |
      | pmt_mail.debug.auth             | false                      |
      | pmt_mail.imap.connectiontimeout | 1000                       |
      | pmt_mail.imap.host              | mail.com                   |
      | pmt_mail.imap.port              | 143                        |
      | pmt_messageNumber               | 1337                       |
      | pmt_receivedXMinutesAgo         | 1440                       |
      | pmt_recipients                  | foo3@bar.com, foo4@bar.com |
      | pmt_replyTo                     | nick@email.com             |
      | pmt_storeName                   | imap                       |
      | pmt_subject                     | Jenkins > Build Plugin #4  |
      | pmt_subjectContains             | jenkins >                  |
      | pmt_username                    | rick                       |


  Scenario: I want the email to save any attachments to disk, and pass in file pointers in the environment variables,
  So that I have access to the email attachments from the Job.
    Given the emails
      | Subject                   | SentXMinutesAgo | IsSeenFlag | From           | Body                    | Attachments |
      | Jenkins > Build Plugin #4 | 5               | false      | nick@email.com | aaa=bbb\nfoo=<b>bar</b> | lighter.jpg |
    When the Plugin's polling is triggered
    Then a Jenkins job is scheduled with quietPeriod 0 and cause '[PollMailboxTrigger] Job was triggered by email sent from nick@email.com (<a href="triggerCauseAction">log</a>)' and parameters
      | aaa                             | bbb                        |
      | foo                             | bar                        |
      | pmt_content                     | \naaa=bbb\nfoo=<b>bar</b>  |
      | pmt_contentType                 | text/html                  |
      | pmt_flags                       | RECENT                     |
      | pmt_folder                      | INBOX                      |
      | pmt_from                        | nick@email.com             |
      | pmt_headers                     | Foo, Bar                   |
      | pmt_host                        | mail.com                   |
      | pmt_jobTrigger                  | Build Plugin #4            |
      | pmt_mail.debug                  | false                      |
      | pmt_mail.debug.auth             | false                      |
      | pmt_mail.imap.connectiontimeout | 1000                       |
      | pmt_mail.imap.host              | mail.com                   |
      | pmt_mail.imap.port              | 143                        |
      | pmt_messageNumber               | 1337                       |
      | pmt_receivedXMinutesAgo         | 1440                       |
      | pmt_recipients                  | foo3@bar.com, foo4@bar.com |
      | pmt_replyTo                     | nick@email.com             |
      | pmt_storeName                   | imap                       |
      | pmt_subject                     | Jenkins > Build Plugin #4  |
      | pmt_subjectContains             | jenkins >                  |
      | pmt_username                    | rick                       |
