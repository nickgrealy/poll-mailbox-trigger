package org.jenkinsci.plugins.pollmailboxtrigger;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        strict = false,
        plugin = {"pretty"},
        tags = {"@wip"}
)
public class WipAcceptanceTests {
}
