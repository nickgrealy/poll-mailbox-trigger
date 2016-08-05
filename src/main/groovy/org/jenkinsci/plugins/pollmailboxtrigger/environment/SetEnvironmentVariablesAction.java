package org.jenkinsci.plugins.pollmailboxtrigger.environment;

import hudson.model.Action;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.StringParameterValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This wrapper is needed since SECURITY-170 is blocking undefined build parameters in the job.
 */
public class SetEnvironmentVariablesAction extends ParametersAction implements Action {

    private final Map<String, String> parameters;
    private final Map<String, ParameterValue> values;

    public SetEnvironmentVariablesAction(Map<String, String> parameters) {
        this.parameters = parameters;
        this.values = new HashMap<>();
        for (String key : parameters.keySet()) {
            this.values.put(key, new StringParameterValue(key, parameters.get(key)));
        }
    }

    @Override
    public List<ParameterValue> getParameters() {
        return new ArrayList<>(values.values());
    }

    @Override
    public ParameterValue getParameter(String name) {
        return values.get(name);
    }

    public Map<String, String> getParametersAsMap() {
        return this.parameters;
    }

}