package org.jenkinsci.plugins.pollmailboxtrigger;

import antlr.ANTLRException;
import hudson.Extension;
import hudson.Util;
import hudson.console.AnnotatedLargeText;
import hudson.model.*;
import org.apache.commons.jelly.XMLOutput;
import org.jenkinsci.lib.xtrigger.XTriggerCause;
import org.jenkinsci.lib.xtrigger.XTriggerDescriptor;
import org.jenkinsci.lib.xtrigger.XTriggerException;
import org.jenkinsci.lib.xtrigger.XTriggerLog;
import org.jenkinsci.plugins.pollmailboxtrigger.mail.CustomProperties;
import org.jenkinsci.plugins.pollmailboxtrigger.mail.Logger;
import org.jenkinsci.plugins.pollmailboxtrigger.mail.MailReader;
import org.jenkinsci.plugins.scripttrigger.AbstractTrigger;
import org.jenkinsci.plugins.scripttrigger.LabelRestrictionClass;
import org.jenkinsci.plugins.scripttrigger.ScriptTriggerAction;
import org.jenkinsci.plugins.scripttrigger.ScriptTriggerException;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.search.SearchTerm;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import static org.jenkinsci.plugins.pollmailboxtrigger.mail.MailWrapperUtils.MessagesWrapper;
import static org.jenkinsci.plugins.pollmailboxtrigger.mail.SearchTermHelpers.*;

/**
 * @author nickgrealy@gmail.com
 */
@SuppressWarnings("unused")
public class PollMailboxTrigger extends AbstractTrigger {


    private String script;

    @DataBoundConstructor
    public PollMailboxTrigger(String cronTabSpec, LabelRestrictionClass labelRestriction, boolean enableConcurrentBuild, String script) throws ANTLRException {
        super(cronTabSpec, labelRestriction != null, (labelRestriction == null) ? null : labelRestriction.getTriggerLabel(), enableConcurrentBuild);
        this.script = Util.fixEmpty(script);
    }

    @SuppressWarnings("unused")
    public String getScript() {
        return script;
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        ScriptTriggerAction action = new InternalScriptTriggerAction(getDescriptor().getDisplayName());
        return Collections.singleton(action);
    }

    @Override
    protected String getName() {
        return "PollMailboxTrigger";
    }

    @Override
    public PollMailboxTrigger.ScriptTriggerDescriptor getDescriptor() {
        return (PollMailboxTrigger.ScriptTriggerDescriptor) Hudson.getInstance().getDescriptorOrDie(getClass());
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
    protected boolean checkIfModified(Node executingNode, XTriggerLog log) throws ScriptTriggerException {
        checkForEmails(executingNode, log);
        return false; // Don't use XTrigger for invoking a (single) job, we may want to invoke multiple jobs!
    }

    private void startJob(XTriggerLog log, Map<String, String> envVars) throws XTriggerException {
        log.info("Changes found. Scheduling a build.");
        AbstractProject project = (AbstractProject) job;
        List<Action> actions = new ArrayList<Action>();
        actions.addAll(Arrays.asList(getScheduledXTriggerActions(null, log)));
        List<ParameterValue> buildParams = new ArrayList<ParameterValue>();
        for (String key : envVars.keySet()) {
            String value = envVars.get(key);
            if (value != null) {
                buildParams.add(new StringParameterValue(key, value));
            }
        }
        actions.add(new ParametersAction(buildParams));
        project.scheduleBuild(0, new NewEmailCause(getName(), getCause(), true), actions.toArray(new Action[actions.size()]));
    }

    private void checkForEmails(Node node, XTriggerLog log) throws ScriptTriggerException {

        if (script != null && !script.isEmpty()) {
            MailReader mailbox = null;
            try {
                // check required properties exist
                CustomProperties p = new CustomProperties(script);
                String[] requiredProps = {"host", "storeName", "username", "password", "folder"};
                boolean allRequired = true;
                for (String prop : requiredProps) {
                    if (!p.has(prop)) {
                        log.error(String.format("Email property '%s' is required!", prop));
                        allRequired = false;
                    }
                    if (!allRequired) {
                        return;
                    }
                }

                // connect to mailbox
                log.info("Connecting to the mailbox...");
                mailbox = new MailReader(new Logger.XTriggerLoggerWrapper(log),
                        p.get("host"),
                        p.get("storeName"),
                        p.get("username"),
                        p.get("password"),
                        p.getProperties()
                ).connect();
                log.info("Connected to the mailbox. Searching for messages where:");

                // search for messages
                List<SearchTerm> searchTerms = new ArrayList<SearchTerm>();
                String subjectContains = "subjectContains", recvXMinutesAgo = "receivedXMinutesAgo";
                searchTerms.add(not(flag(Flags.Flag.SEEN)));    // unread
                log.info("- [flag is unread]");
                if (p.has(subjectContains)) {                    // containing subject
                    searchTerms.add(subject(p.get(subjectContains)));
                    log.info("- [subject contains " + p.get(subjectContains) + "]");
                }
                if (p.has(recvXMinutesAgo)) {                         // received since X minutes ago
                    Date date = relativeDate(Calendar.MINUTE, Integer.parseInt(p.get(recvXMinutesAgo)) * -1);
                    searchTerms.add(receivedSince(date));
                    log.info("- [received date is greater than " + date + "]");
                }
                log.info("...");
                MessagesWrapper messages = mailbox.folder(p.get("folder")).search(searchTerms);
                List<Message> messageList = messages.getMessages();
                log.info("Found matching email(s) : " + messageList.size());
                String prefix = "pollmailboxtrigger_";

                // for each message, trigger a new job, then mark the message as read.
                for (Message message : messageList) {
                    Map<String, String> envVars = messages.getMessageProperties(message, "pmt_");
                    startJob(log, envVars);
                    messages.markAsRead(message);
                }
            } catch (Throwable e) {
                log.error(e.getLocalizedMessage());
            } finally {
                // close up after everthing is done.
                if (mailbox != null) {
                    mailbox.close();
                }
            }
        }
    }

    @Extension
    @SuppressWarnings("unused")
    public static class ScriptTriggerDescriptor extends XTriggerDescriptor {

        @Override
        public boolean isApplicable(Item item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "[Poll Mailbox Trigger] - Poll an email inbox";
        }

        @Override
        public String getHelpFile() {
            return "/plugin/poll-mailbox-trigger/help-PollMailboxTrigger.html";
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

    public final class InternalScriptTriggerAction extends ScriptTriggerAction {

        private transient String actionTitle;

        public InternalScriptTriggerAction(String actionTitle) {
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
            new AnnotatedLargeText<InternalScriptTriggerAction>(getLogFile(), Charset.defaultCharset(), true, this).writeHtmlTo(0, out.asWriter());
        }
    }

}
