@ignore
Feature: Test Live Connection
  This is just for testing a connection to an email server.

  Scenario: Test connecting to GMAIL via IMAPS
    Given the plugin is initialised
    And I set the configuration to
      | Host           | Username        | Password |
      | imap.gmail.com | morty@gmail.com | foobar   |

#    When I test the connection
#    Then the response should be OK with message 'Connected to mailbox. <br>Searching folder...<br>Found matching email(s) : 1'

#    When the Plugin's polling is triggered
#    Then a Jenkins job is scheduled with quietPeriod 0 and cause '[PollMailboxTrigger] Job was triggered by email sent from morty@gmail.com (<a href="triggerCauseAction">log</a>)'


  @wip
  Scenario: Test connecting to GMAIL via POP3
    Given the plugin is initialised
    And I set the configuration to
      | Host          | Username        | Password |
      | pop.gmail.com | morty@gmail.com | foobar   |
    And the script
    """
    storeName=pop3
    """
    When I test the connection
    Then the response should be OK with message 'Connected to mailbox. <br>Searching folder...<br>Found matching email(s) : 1'
