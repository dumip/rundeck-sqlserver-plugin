package ro.dpa.rundeck.plugins;

import com.dtolabs.rundeck.core.plugins.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Abstract class describing job class which submits an external job for execution,
 * then periodically polls that job for updating the status.
 *
 * Created by dumitru.pascu on 4/9/2017.
 */
public abstract class SubmitAndPollJob {
    private static final Logger logger = LoggerFactory.getLogger(SubmitAndPollJob.class);

    public static final long DEFAULT_SLEEP_INTERVAL_BETWEEN_JOB_CHECKS = 5000;


    public void execute() throws Exception, InterruptedException {
        logger.info("Executing job with the following details: {}", this.toString());
        try (JobDao dao = this.buildDao()) {
            this.startJob(dao);
            this.waitForJobExecution(dao);
        } catch (Exception ex) {
            throw ex;
        }
    }

    protected abstract JobDao buildDao() throws SQLException;

    protected abstract void startJob(JobDao dao) throws Exception;

    protected abstract void waitForJobExecution(JobDao dao) throws Exception, InterruptedException;

    public static abstract class Builder<T extends SubmitAndPollJob> {
        protected long nestedSleepInterval = DEFAULT_SLEEP_INTERVAL_BETWEEN_JOB_CHECKS;

        public Builder<T> sleepInterval(long sleepInterval) {
            this.nestedSleepInterval = sleepInterval;
            return this;
        }

        public abstract T build() throws ConfigurationException;
    }
}
