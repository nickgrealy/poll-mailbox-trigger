package org.jenkinsci.plugins.pollmailboxtrigger;

import antlr.ANTLRException;
import hudson.Extension;
import hudson.Util;
import hudson.console.AnnotatedLargeText;
import hudson.model.*;
import org.apache.commons.jelly.XMLOutput;
import org.jenkinsci.lib.envinject.EnvInjectException;
import org.jenkinsci.lib.envinject.service.EnvVarsResolver;
import org.jenkinsci.lib.xtrigger.XTriggerDescriptor;
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
import javax.mail.search.SearchTerm;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import static org.jenkinsci.plugins.pollmailboxtrigger.mail.MailReader.MessagesWrapper;
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
        return checkIfModifiedWithScriptsEvaluation(executingNode, log);
    }

    private boolean checkIfModifiedWithScriptsEvaluation(Node executingNode, XTriggerLog log) throws ScriptTriggerException {

        EnvVarsResolver envVarsResolver = new EnvVarsResolver();
        Map<String, String> envVars;
        try {
            envVars = envVarsResolver.getPollingEnvVars((AbstractProject) job, executingNode);
        } catch (EnvInjectException e) {
            throw new ScriptTriggerException(e);
        }
        //int exitCode = executor.executeScriptPathAndGetExitCode(executingNode, scriptFilePath, envVars);

        if (script != null && !script.isEmpty()) {
            try {
                CustomProperties p = new CustomProperties(script);
                // check required properties exist
                String[] requiredProps = {"host", "storeName", "username", "password", "folder"};
                boolean allRequired = true;
                for (String prop : requiredProps) {
                    if (!p.has(prop)) {
                        log.error(String.format("Email property '%s' is required!", prop));
                        allRequired = false;
                    }
                    if (!allRequired) {
                        return false;
                    }
                }
                // connect to mailbox

                log.info("Connecting to the mailbox...");
                MailReader mailbox = new MailReader(new Logger.XTriggerLoggerWrapper(log),
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
                // if matches found, trigger the build
                log.info("Found matching email(s) : " + messages.getMessages().size());
                boolean hasMessages = !messages.getMessages().isEmpty();
                if (hasMessages) {
                    messages.markAsRead(messages.getMessages().get(0)); // only mark one (of many?) messages as read
                }
                mailbox.close();
                return hasMessages;
            } catch (Throwable e) {
                log.error(e.getLocalizedMessage());
                return false;
            }
        }

        return false;
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
