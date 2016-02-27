package org.jenkinsci.plugins.pollmailboxtrigger;

import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.util.DescribableList;
import hudson.util.Secret;
import jenkins.model.Jenkins;

import java.util.Base64;

import static java.util.Objects.isNull;

/**
 * This class provides a way for us to invoke the Jenkins.getInstance() method, during testing, without it throwing NoSuchMethod errors.
 * Currently this method throws the following error: 'java.lang.NoSuchMethodError: hudson.util.XStream2.getConverterRegistry()Lcom/thoughtworks/xstream/converters/ConverterRegistry;'.
 * <br/><br/>
 * Unfortunately this method is invoked in many places, often nested with other static methods. This makes mocking very
 * difficult/impossible. So, I've fashioned the following class to address these issues.
 */
public class SafeJenkins {

    /**
     * Provides a way to override the static Jenkins instance for unit testing.
     */
    protected static DescribableList<NodeProperty<?>, NodePropertyDescriptor> globalNodeProperties;
    protected static DescribableList<NodeProperty<?>, NodePropertyDescriptor> localNodeProperties;

    protected static boolean useNativeInstance = true;

    public static void useNativeInstance(boolean useNativeInstance) {
        SafeJenkins.useNativeInstance = useNativeInstance;
    }

    public static Jenkins getJenkinsInstance(){
        if (useNativeInstance){
            Jenkins instance = Jenkins.getInstance();
            if (isNull(instance)) {
                throw new RuntimeException("Could not get Jenkins instance using Jenkins.getInstance() (returns null). " +
                        "This can happen if Jenkins has not been started, or was already shut down. " +
                        "Please see http://javadoc.jenkins-ci.org/jenkins/model/Jenkins.html#getInstance() for more details. " +
                        "If you believe this is an error, please raise an 'issue' under https://wiki.jenkins-ci.org/display/JENKINS/poll-mailbox-trigger-plugin.");
            }
            return instance;
        } else {
            throw new RuntimeException("This method is not supported!");
        }
    }

    public static DescribableList<NodeProperty<?>, NodePropertyDescriptor> getGlobalNodeProperties(){
        return useNativeInstance ? getJenkinsInstance().getGlobalNodeProperties() : globalNodeProperties;
    }

    public static DescribableList<NodeProperty<?>, NodePropertyDescriptor> getNodeProperties(){
        return useNativeInstance ? getJenkinsInstance().getNodeProperties() : localNodeProperties;
    }

    public static String encrypt(String message){
        return useNativeInstance
                ? Secret.fromString(message).getEncryptedValue()
                : new String(Base64.getEncoder().encode(message.getBytes()));
    }

    public static String decrypt(String encryptedMessage){
        return useNativeInstance
                ? Secret.decrypt(encryptedMessage).getPlainText()
                : new String(Base64.getDecoder().decode(encryptedMessage));
    }
}
