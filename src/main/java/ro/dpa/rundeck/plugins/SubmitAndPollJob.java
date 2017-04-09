package ro.dpa.rundeck.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class describing job class which submits an external job for execution,
 * then periodically polls that job for updating the status.
 *
 * Created by dumitru.pascu on 4/9/2017.
 */
public abstract class SubmitAndPollJob {
    private static final Logger logger = LoggerFactory.getLogger(SubmitAndPollJob.class);

    protected static final long SLEEP_INTERVAL_BETWEEN_JOB_CHECKS = 5;


    public void execute() throws Exception, InterruptedException {
        logger.info("Executing job with the following details: {}", this.toString());
        try (JobDao dao = this.buildDao()) {
            this.startJob(dao);
            this.waitForJobExecution(dao);
        } catch (Exception ex) {
            throw ex;
        }
    }

    protected abstract JobDao buildDao();

    protected abstract void startJob(JobDao dao) throws Exception;

    protected abstract void waitForJobExecution(JobDao dao) throws Exception, InterruptedException;

    public static abstract class Builder<T extends SubmitAndPollJob> {
        protected long nestedSleepInterval = SLEEP_INTERVAL_BETWEEN_JOB_CHECKS;

        public Builder<T> sleepInterval(long sleepInterval) {
            this.nestedSleepInterval = sleepInterval;
            return this;
        }

        public abstract T build();
    }
}
