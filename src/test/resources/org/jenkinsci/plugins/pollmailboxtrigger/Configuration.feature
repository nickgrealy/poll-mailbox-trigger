
Feature: Configuration

  Background: The tests are setup.
    Given the plugin is initialised


  Scenario: I want to have sensible default config values.
    When I set the configuration to
      | Host | Username | Password | Script |
    Then the effective configuration should be
      | folder              | INBOX     |
      | mail.debug          | false     |
      | mail.debug.auth     | false     |
      | mail.imaps.port     | 993       |
      | receivedXMinutesAgo | 1440      |
      | storeName           | imaps     |
      | subjectContains     | jenkins > |


  Scenario: I want to be able to set the basic config values.
    When I set the configuration to
      | Host     | Username | Password |
      | mail.com | morty    | chickens |
    Then the effective configuration should be
      | folder              | INBOX        |
      | host                | mail.com     |
      | mail.debug          | false        |
      | mail.debug.auth     | false        |
      | mail.imaps.host     | mail.com     |
      | mail.imaps.port     | 993          |
      | password            | Y2hpY2tlbnM= |
      | receivedXMinutesAgo | 1440         |
      | storeName           | imaps        |
      | subjectContains     | jenkins >    |
      | username            | morty        |


  Scenario: I want to be able to override the default config values.
    When I set the configuration to
      | Host | Username | Password |
      | aaa  | bbb      | ccc      |
    And script to
    """
    mail.debug=true
    mail.debug.auth=true
    mail.fff.host=xxx
    mail.fff.port=111
    receivedXMinutesAgo=222
    subjectContains=ddd
    storeName=fff
    folder=eee
    mail.fff.timeout=333
    mail.fff.connectiontimeout=444
    """
    Then the effective configuration should be
      | folder                     | eee  |
      | host                       | aaa  |
      | mail.debug                 | true |
      | mail.debug.auth            | true |
      | mail.fff.connectiontimeout | 444  |
      | mail.fff.host              | xxx  |
      | mail.fff.port              | 111  |
      | mail.fff.timeout           | 333  |
      | password                   | Y2Nj |
      | receivedXMinutesAgo        | 222  |
      | storeName                  | fff  |
      | subjectContains            | ddd  |
      | username                   | bbb  |


  Scenario: I want to be able to clear the default config values.
    When I set the configuration to
      | Username | Password |
      | bbb      | ccc      |
    And script to
    """
    folder=
    mail.debug=
    mail.debug.auth=
    mail.imaps.port=
    receivedXMinutesAgo=
    storeName=
    subjectContains=
    """
    Then the effective configuration should be
      | password | Y2Nj |
      | username | bbb  |


  Scenario: I want the imap port and host varaibles to be automatically updated, if I switch to IMAP [no 'S'].
    When I set the configuration to
      | Host     | Username | Password |
      | mail.com | morty    | chickens |
    And script to
    """
    storeName=imap
    """
    Then the effective configuration should be
      | folder              | INBOX        |
      | host                | mail.com     |
      | mail.debug          | false        |
      | mail.debug.auth     | false        |
      | mail.imap.host      | mail.com     |
      | mail.imap.port      | 143          |
      | password            | Y2hpY2tlbnM= |
      | receivedXMinutesAgo | 1440         |
      | storeName           | imap         |
      | subjectContains     | jenkins >    |
      | username            | morty        |