package org.jenkinsci.plugins.pollmailboxtrigger.environment;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;
import hudson.model.TaskListener;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * If {@link SetEnvironmentVariablesAction} exists, set the environment variables.
 */
@Extension
public final class SetEnvironmentVariablesContributor extends EnvironmentContributor {

    @Override
    public void buildEnvironmentFor(@Nonnull final Run r, @Nonnull final EnvVars envVars, @Nonnull final TaskListener listener)
            throws IOException, InterruptedException {
        SetEnvironmentVariablesAction action = r.getAction(SetEnvironmentVariablesAction.class);
        if (action != null) {
            envVars.putAll(action.getParametersAsMap());
        }
    }
}
