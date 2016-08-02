
Feature: Test Connection - Happy Paths

  Background: The tests are setup
    Given the plugin is initialised
    And a mailbox with domain mail.com and username rick
    And I set the configuration to
      | Host     | Username | Password |
      | mail.com | rick     | rabbits  |
    And the script
    """
    storeName=imap
    mail.imap.connectiontimeout=2000
    """

  Scenario: Zero Emails
    Given the emails
      | Subject | SentXMinutesAgo | IsSeenFlag |
    When I test the connection
    Then the response should be OK with message 'Connected to mailbox.'
    Then the response should be OK with message 'Found matching email(s) : 0<br>'

    # One Email

  Scenario: One Email - Subject doesn't match
    Given the emails
      | Subject                  | SentXMinutesAgo | IsSeenFlag |
      | Hudson > Build Plugin #1 | 5               | false      |
    When I test the connection
    Then the response should be OK with message 'Connected to mailbox.'
    Then the response should be OK with message 'Found matching email(s) : 0<br>'


  Scenario: One Email - SentXMinutesAgo is outside of range
    Given the emails
      | Subject                   | SentXMinutesAgo | IsSeenFlag |
      | Jenkins > Build Plugin #1 | 1445            | false      |
    When I test the connection
    Then the response should be OK with message 'Connected to mailbox.'
    Then the response should be OK with message 'Found matching email(s) : 0<br>'


  Scenario: One Email - Email has already been read
    Given the emails
      | Subject                   | SentXMinutesAgo | IsSeenFlag |
      | Jenkins > Build Plugin #1 | 5               | true       |
    When I test the connection
    Then the response should be OK with message 'Connected to mailbox.'
    Then the response should be OK with message 'Found matching email(s) : 0<br>'


  Scenario: One Email - subject, sentDateTime and isSeen flag match
    Given the emails
      | Subject                   | SentXMinutesAgo | IsSeenFlag |
      | Jenkins > Build Plugin #1 | 5               | false      |
    When I test the connection
    Then the response should be OK with message 'Connected to mailbox.'
    Then the response should be OK with message 'Found matching email(s) : 1<br>'
    Then the response should be OK with message '- Jenkins > Build Plugin #1'

    # Multiple Emails

  Scenario: Multiple Emails - all match
    Given the emails
      | Subject                   | SentXMinutesAgo | IsSeenFlag |
      | Jenkins > Build Plugin #1 | 5               | false      |
      | Jenkins > Build Plugin #2 | 10              | false      |
      | Jenkins > Build Plugin #3 | 15              | false      |
    When I test the connection
    Then the response should be OK with message 'Connected to mailbox.'
    Then the response should be OK with message 'Found matching email(s) : 3<br>'
    Then the response should be OK with message '- Jenkins > Build Plugin #1'


#  Scenario: I want the plugin to tell me if authentication failed, so that I can troubleshoot the root cause of any issues.

