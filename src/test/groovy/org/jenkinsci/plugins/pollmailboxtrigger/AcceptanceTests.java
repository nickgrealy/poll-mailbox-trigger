package org.jenkinsci.plugins.pollmailboxtrigger;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {"pretty"},
        tags = {"~@ignore"}
)
public class AcceptanceTests {
}
