package org.jenkinsci.plugins.pollmailboxtrigger;

import antlr.ANTLRException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Util;
import hudson.console.AnnotatedLargeText;
import hudson.model.*;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.util.DescribableList;
import hudson.util.FormValidation;
import hudson.util.Secret;
import hudson.util.StreamTaskListener;
import org.apache.commons.jelly.XMLOutput;
import org.jenkinsci.lib.xtrigger.XTriggerCause;
import org.jenkinsci.lib.xtrigger.XTriggerDescriptor;
import org.jenkinsci.lib.xtrigger.XTriggerException;
import org.jenkinsci.lib.xtrigger.XTriggerLog;
import org.jenkinsci.plugins.pollmailboxtrigger.mail.MailReader;
import org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.CustomProperties;
import org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.Logger;
import org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.MailWrapperUtils;
import org.jenkinsci.plugins.scripttrigger.AbstractTrigger;
import org.jenkinsci.plugins.scripttrigger.LabelRestrictionClass;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.mail.Flags;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.search.SearchTerm;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.jenkinsci.plugins.pollmailboxtrigger.PollMailboxTrigger.Properties.*;
import static org.jenkinsci.plugins.pollmailboxtrigger.SafeJenkins.*;
import static org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.MailWrapperUtils.MessagesWrapper;
import static org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.SearchTermHelpers.*;
import static org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.Stringify.stringify;

/**
 * @author Nick Grealy
 */
@SuppressWarnings("unused")
public class PollMailboxTrigger extends AbstractTrigger {
    
    public static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss a");

    private String host;
    private String username;
    private Secret password;
    private String script;
    private String attachments;

    enum AttachmentOptions {
        IGNORE, AUTO
    }

    @DataBoundConstructor
    public PollMailboxTrigger(
            String cronTabSpec,
            LabelRestrictionClass labelRestriction,
            boolean enableConcurrentBuild,
            String host,
            String username,
            Secret password,
            String script,
            String attachments
    ) throws ANTLRException {
        super(cronTabSpec, labelRestriction != null, (labelRestriction == null) ? null : labelRestriction.getTriggerLabel(), enableConcurrentBuild);
        this.host = Util.fixEmpty(host);
        this.username = Util.fixEmpty(username);
        this.password = password;
        this.script = Util.fixEmpty(script);
        this.script = Util.fixEmpty(script);
        this.attachments = Util.fixEmpty(attachments);
    }

    public static CustomProperties initialiseDefaults(String host, String username, Secret password, String script, String attachments) {

        // extracts global node properties from environment, add them to new empty local list
        DescribableList<NodeProperty<?>, NodePropertyDescriptor> properties = getGlobalNodeProperties();
        EnvVars envVars = new EnvVars();
		if (null != properties) {
			final EnvironmentVariablesNodeProperty envClass = properties
					.get(EnvironmentVariablesNodeProperty.class);
			if (null != envClass) {
				envVars.putAll(envClass.getEnvVars());
			}
		}
		
        // extracts specific node properties from environment, merge them with local copy of global list
        DescribableList<NodeProperty<?>, NodePropertyDescriptor> propsNode = getNodeProperties();
		if (null != propsNode) {
			final EnvironmentVariablesNodeProperty envClass = propsNode
					.get(EnvironmentVariablesNodeProperty.class);
			if (null != envClass) {
				envVars.putAll(envClass.getEnvVars());
			}
		}
		
		// perform variable substitution
		host = Util.replaceMacro(host, envVars);
		username = Util.replaceMacro(username, envVars);
        String passwordVariableReplaced = nonNull(password)
                ? Util.replaceMacro(password.getPlainText(), envVars)
                : "";
		script = Util.replaceMacro(script, envVars);

		// build properties
        CustomProperties userConfig = CustomProperties.read(script);
        CustomProperties p = new CustomProperties();
        p.putAll(userConfig);
        p.put(Properties.host, host);
        p.put(Properties.username, username);
        p.put(Properties.password, encrypt(passwordVariableReplaced));
        p.put(Properties.attachments, attachments);
        // setup default values
        p.putIfBlank(storeName, "imaps");
        p.putIfBlank(folder, "INBOX");
        p.putIfBlank(subjectContains, "jenkins >");
        p.putIfBlank(receivedXMinutesAgo, Integer.toString(60 * 24)); // 60mins * 24hrs = 1 day
        String cnfHost = p.get(Properties.host);
        String cnfStoreName = p.get(storeName);
        p.putIfBlank("mail." + cnfStoreName + ".host", cnfHost);
        p.putIfBlank("mail." + cnfStoreName + ".port", cnfStoreName.toLowerCase().endsWith("s") ? "993" : "143");
        p.putIfBlank("mail.debug", "false");
        p.putIfBlank("mail.debug.auth", "false");
        // re-override the user properties, to ensure they're set.
        p.putAll(userConfig);
        p.removeBlanks();
        return p;
    }

    public static FormValidation checkForEmails(CustomProperties properties, XTriggerLog log, boolean testConnection, PollMailboxTrigger pmt) {
        MailReader mailbox = null;
        List<String> testing = new ArrayList<String>();
        try {
            Enum[] requiredProps = {Properties.host, Properties.storeName, Properties.username, Properties.password};
            List<String> errors = new ArrayList<String>();
            boolean allRequired = true;
            for (Enum prop : requiredProps) {
                if (!properties.has(prop)) {
                    String err = String.format("Email property '%s' is required!", prop);
                    log.error(err);
                    errors.add(err);
                    allRequired = false;
                }
            }
            if (!allRequired) {
                return FormValidation.error("Error : " + stringify(errors));
            }

            // connect to mailbox
            log.info("Connecting to the mailbox...");
            String encryptedPassword = properties.get(Properties.password);
            String decryptedPassword = decrypt(encryptedPassword);
            mailbox = new MailReader(
                    properties.get(Properties.host),
                    properties.get(Properties.username),
                    decryptedPassword,
                    properties.get(storeName),
                    new Logger.XTriggerLoggerWrapper(log),
                    properties
            ).connect();
            final String connected = "Connected to mailbox. ";
            log.info(connected + "Searching for messages where:");
            testing.add(connected);

            // search for messages
            List<SearchTerm> searchTerms = new ArrayList<SearchTerm>();
            // unread
            searchTerms.add(not(flag(Flags.Flag.SEEN)));
            log.info("- [flag is unread]");
            // containing subject
            if (properties.has(subjectContains)) {
                searchTerms.add(subject(properties.get(subjectContains)));
                log.info("- [subject contains '" + properties.get(subjectContains) + "']");
            }
            // received since X minutes ago
            if (properties.has(receivedXMinutesAgo)) {
                final int minsAgo = Integer.parseInt(properties.get(receivedXMinutesAgo)) * -1;
                Date date = relativeDate(Calendar.MINUTE, minsAgo);
                searchTerms.add(receivedSince(date));
                log.info("- [received date is greater than '" + formatter.format(date) + "']");
            }
            log.info("...");
            if (!properties.has(folder)) {
                throw new FolderNotFoundException();
            } else {
                // look for mail...
                final MailWrapperUtils.FolderWrapper mbFolder = mailbox.folder(properties.get(folder));
                testing.add("Searching folder...");
                String downloadAttachments = properties.get(Properties.attachments);
                MessagesWrapper messagesTool = mbFolder.search(searchTerms);
                List<Message> messageList = messagesTool.getMessages();
                StringBuilder subjects = new StringBuilder();
                for (Message message : messageList) {
                    subjects.append("\n\n- ").append(message.getSubject()).append(" (").append(formatter.format(message.getReceivedDate())).append(")");
                }
                final String foundEmails = "Found matching email(s) : " + messageList.size() + subjects.toString();
                log.info(foundEmails);
                testing.add(foundEmails);
                if (!testConnection) {
                    // trigger jobs...
                    for (Message message : messageList) {
                        final String prefix = "pmt_";
                        CustomProperties buildParams = messagesTool.getMessageProperties(message, prefix, properties);
                        // download attachments if set to AUTO...
                        log.info("Download attachments? " + downloadAttachments);
                        if (AttachmentOptions.AUTO.name().equals(downloadAttachments)){
                            File attachmentsDir = messagesTool.saveAttachments(message);
                            if (nonNull(attachmentsDir)){
                                buildParams.put("pmt_attachmentsDirectory", attachmentsDir.getAbsolutePath());
                            }
                        }
                        properties.remove(Properties.password);
                        buildParams.putAll(properties, prefix);
                        // build a "retry" link...
                        buildParams.put("pmt_retryEmailLink", buildEmailRetryLink(properties));

                        String jobCause = "Job was triggered by email sent from " + stringify(message.getFrom());
                        // start a jenkins job...
                        pmt.startJob(log, jobCause, buildParams.getMap());
                        // finally mark the message as read so that we don't reprocess it...
                        messagesTool.markAsRead(message);
                    }
                }
            }
            // return success
            if (testConnection) {
                testing.add("\nResult: Success!");
                return FormValidation.ok(stringify(testing, "\n"));
            }
        } catch (FolderNotFoundException e) {
            // list any folders we can find...
            try {
                testing.add("Please set the 'folder=XXX' parameter to one of the following values: ");
                final String folders = stringify(mailbox.getFolders());
                testing.add("Folders: " + folders);
                log.info(folders);
                return FormValidation.error(stringify(testing, "\n"));
            } catch (Throwable t) {
                return handleError(log, testing, t);
            }
        } catch (Throwable t) {
            return handleError(log, testing, t);
        } finally {
            // cleanup connections
            if (mailbox != null) {
                mailbox.close();
            }
        }
        return FormValidation.ok("Success");
    }

    public static String buildEmailRetryLink(CustomProperties properties){
        return String.format("<a href=\"mailto:%s?subject=%s&body=%s\">Click to Retry Job</a>",
                properties.get("pmt_recipients"),
                properties.get("pmt_subject"),
                properties.get("pmt_body")
        );
    }

    private static FormValidation handleError(XTriggerLog log, List<String> testing, Throwable t) {
        // return error
        final String error = stringify(t);
        log.error(error);
        testing.add("Error : " + error);
        return FormValidation.error(stringify(testing, "\n"));
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Secret getPassword() {
        return password;
    }

    public void setPassword(Secret password) {
        this.password = password;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        PollMailboxTriggerAction action = new InternalPollMailboxTriggerAction(getDescriptor().getDisplayName());
        return Collections.singleton(action);
    }

    @Override
    protected String getName() {
        return "PollMailboxTrigger";
    }

    @Override
    public PollMailboxTriggerDescriptor getDescriptor() {
        return (PollMailboxTriggerDescriptor) Hudson.getInstance().getDescriptorOrDie(getClass());
    }

    @Override
    protected File getLogFile() {
        return new File(job.getRootDir(), "pollMailboxTrigger-polling.log");
    }

    @Override
    protected String getDefaultMessageCause() {
        return "An email matching the filter criteria was found.";
    }

    @Override
    protected boolean checkIfModified(Node executingNode, XTriggerLog log) {
        CustomProperties properties = initialiseDefaults(host, username, password, script, attachments);
        checkForEmails(properties, log, false, this);// use executingNode, ???
        return false; // Don't use XTrigger for invoking a (single) job, we may want to invoke multiple jobs!
    }

    protected void startJob(XTriggerLog log, String jobTriggerCause, Map<String, String> envVars) throws Throwable {
        try {
            log.info("Changes found. Scheduling a build.");
            AbstractProject project = getJob();
            List<Action> actions = new ArrayList<Action>();
            actions.addAll(getScheduledXTriggerActions(log));
            actions.add(new ParametersAction(convertToBuildParams(envVars)));

            // build parameters for schedule job...
            Cause cause = new NewEmailCause(getName(), jobTriggerCause, true);
            Action[] actionsArray = actions.toArray(new Action[actions.size()]);
            project.scheduleBuild(0, cause, actionsArray);
        } catch (Throwable t){
            log.error("Error occurred starting job - " + t.getMessage());
            throw t;
        }
    }

    protected AbstractProject getJob(){
        return (AbstractProject) job;
    }

    protected List<Action> getScheduledXTriggerActions(XTriggerLog log) throws XTriggerException {
        return Arrays.asList(getScheduledXTriggerActions(null, log));
    }

    /**
     * Converts a Map of String values, to Build Parameters.
     */
    private List<ParameterValue> convertToBuildParams(Map<String, String> envVars) {
        List<ParameterValue> buildParams = new ArrayList<ParameterValue>();
        for (String key : envVars.keySet()) {
            String value = envVars.get(key);
            if (value != null) {
                buildParams.add(new StringParameterValue(key, value));
            }
        }
        return buildParams;
    }

    public enum Properties {
        storeName, host, username, password, folder, subjectContains, receivedXMinutesAgo, attachments
    }

    @Extension
    @SuppressWarnings("unused")
    public static class PollMailboxTriggerDescriptor extends XTriggerDescriptor {

        @Override
        public boolean isApplicable(Item item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Poll Mailbox Trigger";
        }

        @Override
        public String getHelpFile() {
            return "/plugin/poll-mailbox-trigger/help-PollMailboxTrigger.html";
        }

        public FormValidation doTestConnection(
                @QueryParameter("host") final String host,
                @QueryParameter("username") final String username,
                @QueryParameter("password") final Secret password,
                @QueryParameter("script") final String script,
                @QueryParameter("attachments") final String attachments
        ) {
            try {
                CustomProperties properties = initialiseDefaults(host, username, password, script, attachments);
                return checkForEmails(properties, new XTriggerLog(new StreamTaskListener(Logger.DEFAULT.getOutputStream())), true, null);
            } catch (Throwable t) {
                return FormValidation.error("Error : " + stringify(t));
            }
        }
    }

    /**
     * Because the XTriggerCause constructors are protected. (Why?)
     */
    class NewEmailCause extends XTriggerCause {

        protected NewEmailCause(String triggerName, String causeFrom, boolean logEnabled) {
            super(triggerName, causeFrom, logEnabled);
        }
    }

    public final class InternalPollMailboxTriggerAction extends PollMailboxTriggerAction {

        private transient String actionTitle;

        public InternalPollMailboxTriggerAction(String actionTitle) {
            this.actionTitle = actionTitle;
        }

        @SuppressWarnings("unused")
        public AbstractProject<?, ?> getOwner() {
            return (AbstractProject) job;
        }

        public String getDisplayName() {
            return "Poll Mailbox Trigger Log";
        }

        public String getUrlName() {
            return "pollMailboxTriggerLog";
        }

        public String getIconFileName() {
            return "clipboard.gif";
        }

        @SuppressWarnings("unused")
        public String getLabel() {
            return actionTitle;
        }

        @SuppressWarnings("unused")
        public String getLog() throws IOException {
            return Util.loadFile(getLogFile());
        }

        @SuppressWarnings("unused")
        public void writeLogTo(XMLOutput out) throws IOException {
            new AnnotatedLargeText<InternalPollMailboxTriggerAction>(getLogFile(), Charset.defaultCharset(), true, this).writeHtmlTo(0, out.asWriter());
        }
    }

}
