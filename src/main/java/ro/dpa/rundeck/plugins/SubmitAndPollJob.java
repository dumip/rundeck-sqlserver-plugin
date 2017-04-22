package ro.dpa.rundeck.plugins;

import com.dtolabs.rundeck.core.plugins.configuration.ConfigurationException;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Abstract class describing job class which submits an external job for execution,
 * then periodically polls that job for updating the status.
 *
 * Created by dumitru.pascu on 4/9/2017.
 */
public abstract class SubmitAndPollJob<T extends AutoCloseable> {

    protected long sleepInterval;
    protected String serverName;
    protected int port;
    protected String userName;
    protected String password;

    private static final Logger logger = LoggerFactory.getLogger(SubmitAndPollJob.class);

    public static final long DEFAULT_SLEEP_INTERVAL_BETWEEN_JOB_CHECKS = 5000;


    public void execute() throws Exception, InterruptedException {
        logger.info("Executing job with the following details: {}", this.toString());
        try (T dao = this.buildDao()) {
            this.startJob(dao);
            this.waitForJobExecution(dao);
        } catch (Exception ex) {
            throw ex;
        }
    }

    protected abstract T buildDao() throws SQLException;

    protected abstract void startJob(T dao) throws Exception;

    protected abstract void waitForJobExecution(T dao) throws Exception, InterruptedException;

    public static abstract class Builder<U extends SubmitAndPollJob> {
        protected long nestedSleepInterval = DEFAULT_SLEEP_INTERVAL_BETWEEN_JOB_CHECKS;
        protected String nestedServerName;
        protected int nestedPort;
        protected String nestedUserName;
        protected String nestedPassword;

        public Builder<U> serverName(String serverName) {
            this.nestedServerName = serverName;
            return this;
        }

        public Builder<U> port(int port) {
            this.nestedPort = port;
            return this;
        }

        public Builder<U> userName(String userName) {
            this.nestedUserName = userName;
            return this;
        }

        public Builder<U> password(String password) {
            this.nestedPassword = password;
            return this;
        }

        public Builder<U> sleepInterval(long sleepInterval) {
            this.nestedSleepInterval = sleepInterval;
            return this;
        }

        public U build() throws ConfigurationException {
            //check for mandatory params
            if (Strings.isNullOrEmpty(this.nestedServerName) || Strings.isNullOrEmpty(this.nestedUserName)
                    || Strings.isNullOrEmpty(this.nestedPassword) || this.nestedPort == 0) {
                throw new ConfigurationException("Following parameters are mandatory: serverName, port, userName, password");
            }

            validate();

            return createInstance();
        }

        protected abstract void validate() throws ConfigurationException;

        protected abstract U createInstance();
    }
}
