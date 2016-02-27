
Feature: Test Connection Errors

  Background: The tests are setup.
    Given the plugin is initialised


  Scenario: I want the plugin to tell me if the hostname is unknown, so that I can troubleshoot the root cause of any issues.
    When I set the configuration to
      | Host                | Username | Password |
      | wubba.lubba.dub.dub | morty    | chickens |
    And I test the connection
    Then the response should be ERROR with message 'java.net.UnknownHostException: wubba.lubba.dub.dub'


  Scenario: I want the plugin to tell me if it times out connecting to the server, so that I can troubleshoot the root cause of any issues.
    When I set the configuration to
      | Host     | Username | Password | Script                         |
      | mail.com | morty    | chickens | mail.imaps.connectiontimeout=1 |
    And I test the connection
    Then the response should be ERROR with message 'java.net.SocketTimeoutException'


#  Scenario: I want the plugin to tell me if the supplied folder doesn't exist, so that I can troubleshoot the root cause of any issues.
#    When I set the configuration to
#      | Host     | Username | Password |
#      | mail.com | rick     | rabbits  |
#    And script to
#    """
#    folder=XXX
#    storeName=imap
#    mail.imap.connectiontimeout=2000
#    """
#    And I test the connection
#    Then the response should be OK with message 'java.net.SocketTimeoutException'
