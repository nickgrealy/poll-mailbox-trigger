Feature: Runtime Configuration

  Background: The tests are setup.
    Given the plugin is initialised
    Given a mailbox with domain mail.com and username rick and emails
      | Subject                   | SentXMinutesAgo | IsSeenFlag |
      | Hudson > Build Plugin #1  | 5               | false      |
      | Jenkins > Build Plugin #2 | 1445            | false      |
      | Jenkins > Build Plugin #3 | 5               | true       |
      | Jenkins > Build Plugin #4 | 5               | false      |
    And script to
    """
    storeName=imap
    mail.imap.connectiontimeout=1000
    """

  # required fields validation

  Scenario: If 'host' isn't set, then the plugin should return a validation error.
    When I set the configuration to
      | Host | Username | Password | Script |
      |      | rick     | chickens |        |
    When I test the connection
    Then the response should be ERROR with message 'Error : Email property &#039;host&#039; is required!'


  Scenario: If 'username' isn't set, then the plugin should return a validation error.
    When I set the configuration to
      | Host     | Username | Password | Script |
      | mail.com |          | chickens |        |
    When I test the connection
    Then the response should be ERROR with message 'Error : Email property &#039;username&#039; is required!'


  Scenario: If 'password' isn't set, then the plugin should return a validation error.
    When I set the configuration to
      | Host     | Username | Password | Script |
      | mail.com | rick     |          |        |
    When I test the connection
    Then the response should be ERROR with message 'Error : Email property &#039;password&#039; is required!'


  Scenario: If 'host', 'username' and 'password' properties aren't set, then the plugin should return a validation error.
    When I set the configuration to
      | Host | Username | Password | Script |
    When I test the connection
    Then the response should be ERROR with message 'Error : Email property &#039;host&#039; is required!, Email property &#039;username&#039; is required!, Email property &#039;password&#039; is required!'

  # happy path

  Scenario: I want to be able to set the basic config values.
    When I set the configuration to
      | Host     | Username | Password |
      | mail.com | rick     | chickens |
    When I test the connection
    Then the response should be OK with message 'Found matching email(s) : 1<br><br>- Jenkins > Build Plugin #4'

  # overriding filters

  Scenario: I can override the subjectContains filter.
    When I set the configuration to
      | Host     | Username | Password | Script                 |
      | mail.com | rick     | chickens | subjectContains=Hudson |
    When I test the connection
    Then the response should be OK with message 'Found matching email(s) : 1<br><br>- Hudson > Build Plugin #1'


  Scenario: I can override the receivedXMinutesAgo filter.
    When I set the configuration to
      | Host     | Username | Password | Script                   |
      | mail.com | rick     | chickens | receivedXMinutesAgo=1450 |
    When I test the connection
    Then the response should be OK with message 'Found matching email(s) : 2<br><br>- Jenkins > Build Plugin #2'


  Scenario: I can override the folder filter - [mock-javamail ignores folders].
    When I set the configuration to
      | Host     | Username | Password | Script        |
      | mail.com | rick     | chickens | folder=Hudson |
    When I test the connection
    Then the response should be OK with message 'Found matching email(s) : 1<br><br>- Jenkins > Build Plugin #4'


  # blanking values - what happens?


  Scenario: I can clear the subjectContains filter.
    When I set the configuration to
      | Host     | Username | Password | Script           |
      | mail.com | rick     | chickens | subjectContains= |
    When I test the connection
    Then the response should be OK with message 'Found matching email(s) : 2<br><br>- Hudson > Build Plugin #1'


  Scenario: I can clear the receivedXMinutesAgo filter.
    When I set the configuration to
      | Host     | Username | Password | Script               |
      | mail.com | rick     | chickens | receivedXMinutesAgo= |
    When I test the connection
    Then the response should be OK with message 'Found matching email(s) : 2<br><br>- Jenkins > Build Plugin #2'


  Scenario: I can clear the folder filter - [mock-javamail ignores folders].
    When I set the configuration to
      | Host     | Username | Password | Script  |
      | mail.com | rick     | chickens | folder= |
    When I test the connection
    Then the response should be ERROR with message 'Please set the &#039;folder=XXX&#039; parameter to one of the following values: <br>Folders: '
