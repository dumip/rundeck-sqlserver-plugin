package ro.dpa.rundeck.plugins.sqlserver;

import com.dtolabs.rundeck.core.plugins.configuration.ConfigurationException;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.dpa.rundeck.plugins.SubmitAndPollJob;

import java.sql.*;

/**
 * The class that implements the logic for launching and monitoring SQL Server jobs.
 * Jobs are being launched and monitored using standard SQL Agent Stored procedures (sp_start_job and sp_help_job),
 * as described ${@https://docs.microsoft.com/en-us/sql/relational-databases/system-stored-procedures/sql-server-agent-stored-procedures-transact-sql here}
 *
 * Created by dumitru.pascu on 3/26/2017.
 */
public class SqlServerJob extends SubmitAndPollJob<SqlServerJobDao> {
    private static final Logger logger = LoggerFactory.getLogger(SqlServerJob.class);

    private String jobName;
    private String stepName;


    //constructor is private, it can only be instantiated through the builder
    private SqlServerJob() {

    }

    @Override
    protected SqlServerJobDao buildDao() throws SQLException {
        SqlServerJobDao dao = new SqlServerJobDaoImpl(this.serverName, this.port, this.userName, this.password);

        return dao;
    }

    @Override
    protected void startJob(SqlServerJobDao dao) throws Exception {
        dao.startJob(this.jobName, this.stepName);
    }

    /**
     * Methods that uses sp_help_job SQL Server stored procedure to check the stored procedure status.
     *
     * @param dao
     * @throws SQLException If the job fails, SQLException is thrown
     */
    @Override
    protected void waitForJobExecution(SqlServerJobDao dao) throws SQLException, InterruptedException {
        boolean isFinished = false;
        while (!isFinished) {
            //normally sleep should be at the end of the loop, but apparently SQL Server doesn't
            //change the job status immediately, so we can risk not getting the correct status
            //immediately after we start the job
            logger.debug("Job with name={} in progress. Waiting for {} seconds until next check...", this.jobName, this.sleepInterval / 1000);
            Thread.sleep(this.sleepInterval);

            int currentExecutionStatus = dao.getCurrentExecutionStatus(this.jobName);
            logger.debug("Job name='{}', current_execution_status={}", this.jobName, currentExecutionStatus);

            SqlExecutionStatus currentStatus = SqlExecutionStatus.valueOf(currentExecutionStatus);
            isFinished = SqlExecutionStatus.Idle == currentStatus;
            logger.debug("isFinished={}", isFinished);
            //job finished, check final status
            if (isFinished) {
                SqlExecutionStatus finalStatus = SqlExecutionStatus.valueOf(dao.getLastExecutionStatus(this.jobName));
                if (finalStatus != SqlExecutionStatus.Succeeded) {
                    //the job failed, raise SQL Exception
                    logger.error("Job with name='{}' failed with status={}", this.jobName, finalStatus);
                    throw new SQLException("SQL Server Job with name='{"+this.jobName+"}' " +
                            "failed with status={"+finalStatus+"}. Check SQL Server logs for more details.");
                } else {
                    //Successful execution
                    logger.info("Job with name='{}' processed successfully", this.jobName);
                    return;
                }
            }
        }
    }

    public static class SqlServerJobBuilder extends SubmitAndPollJob.Builder<SqlServerJob> {

        private String nestedJobName;
        private String nestedStepName;

        public SqlServerJobBuilder() {

        }

        public SqlServerJobBuilder jobName(String jobName) {
            this.nestedJobName = jobName;
            return this;
        }

        public SqlServerJobBuilder stepName(String stepName) {
            this.nestedStepName = stepName;
            return this;
        }

        @Override
        protected void validate() throws ConfigurationException {
            if (Strings.isNullOrEmpty(this.nestedJobName)) {
                throw new ConfigurationException("Following parameters are mandatory: serverName, port, userName, password, " +
                        "jobName");
            }
        }

        @Override
        protected SqlServerJob createInstance() {
            SqlServerJob job = new SqlServerJob();

            job.sleepInterval = this.nestedSleepInterval;
            job.serverName = this.nestedServerName;
            job.port = this.nestedPort;
            job.userName = this.nestedUserName;
            job.password = this.nestedPassword;
            job.jobName = this.nestedJobName;
            job.stepName = this.nestedStepName;

            return job;
        }
    }

    @Override
    public String toString() {
        return "SqlServerJob{" +
                "serverName='" + serverName + '\'' +
                ", port=" + port +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", jobName='" + jobName + '\'' +
                ", stepName='" + stepName + '\'' +
                '}';
    }
}
