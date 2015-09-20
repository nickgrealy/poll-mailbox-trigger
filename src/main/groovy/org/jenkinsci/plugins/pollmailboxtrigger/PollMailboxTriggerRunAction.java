package org.jenkinsci.plugins.pollmailboxtrigger;

import hudson.model.InvisibleAction;

public class PollMailboxTriggerRunAction extends InvisibleAction {

    private String description;

    public PollMailboxTriggerRunAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
