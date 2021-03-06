package ro.dpa.rundeck.plugins.sqlserver;

import com.dtolabs.rundeck.core.plugins.configuration.ConfigurationException;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * The class that implements the logic for launching and monitoring SQL Server jobs.
 * Jobs are being launched and monitored using standard SQL Agent Stored procedures (sp_start_job and sp_help_job),
 * as described ${@https://docs.microsoft.com/en-us/sql/relational-databases/system-stored-procedures/sql-server-agent-stored-procedures-transact-sql here}
 *
 * Created by dumitru.pascu on 3/26/2017.
 */
public class SqlServerJob {
    private static final Logger logger = LoggerFactory.getLogger(SqlServerJob.class);

    private static final long SLEEP_INTERVAL_BETWEEN_JOB_CHECKS = 5;

    private String serverName;
    private int port;
    private String userName;
    private String password;
    private String jobName;
    private String stepName;


    //constructor is private, it can only be instantiated through the builder
    private SqlServerJob() {

    }

    public void execute() throws SQLException, InterruptedException {
        logger.info("Executing SQL Server job with the following details: {}", this.toString());
        try (SqlServerJobDao dao = this.getSqlServerJobDao()) {
            this.startJob(dao);
            this.waitForJobExecution(dao);
        } catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    private void startJob(SqlServerJobDao dao) throws SQLException {
        dao.startJob(this.jobName, this.stepName);
    }

    public SqlServerJobDao getSqlServerJobDao() throws SQLException {
        SqlServerJobDao dao = new SqlServerJobDaoImpl(this.serverName, this.port, this.userName, this.password);

        return dao;
    }

    /**
     * Methods that uses sp_help_job SQL Server stored procedure to check the stored procedure status.
     *
     * @param dao
     * @throws SQLException If the job fails, SQLException is thrown
     */
    private void waitForJobExecution(SqlServerJobDao dao) throws SQLException, InterruptedException {
        boolean isFinished = false;
        while (!isFinished) {
            //normally sleep should be at the end of the loop, but apparently SQL Server doesn't
            //change the job status immediately, so we can risk not getting the correct status
            //immediately after we start the job
            logger.debug("Job with name={} in progress. Waiting for {} seconds until next check...", this.jobName, SLEEP_INTERVAL_BETWEEN_JOB_CHECKS);
            Thread.sleep(SLEEP_INTERVAL_BETWEEN_JOB_CHECKS * 1000);

            int currentExecutionStatus = dao.getCurrentExecutionStatus(this.jobName);
            logger.debug("Job name='{}', current_execution_status={}", this.jobName, currentExecutionStatus);

            ExecutionStatus currentStatus = ExecutionStatus.valueOf(currentExecutionStatus);
            isFinished = ExecutionStatus.Idle == currentStatus;
            logger.debug("isFinished={}", isFinished);
            //job finished, check final status
            if (isFinished) {
                ExecutionStatus finalStatus = ExecutionStatus.valueOf(dao.getLastExecutionStatus(this.jobName));
                if (finalStatus != ExecutionStatus.Succeeded) {
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

    public static class SqlServerJobBuilder {
        private String nestedServerName;
        private int nestedPort;
        private String nestedUserName;
        private String nestedPassword;
        private String nestedJobName;
        private String nestedStepName;

        public SqlServerJobBuilder() {

        }

        public SqlServerJobBuilder serverName(String serverName) {
            this.nestedServerName = serverName;
            return this;
        }

        public SqlServerJobBuilder port(int port) {
            this.nestedPort = port;
            return this;
        }

        public SqlServerJobBuilder userName(String userName) {
            this.nestedUserName = userName;
            return this;
        }

        public SqlServerJobBuilder password(String password) {
            this.nestedPassword = password;
            return this;
        }

        public SqlServerJobBuilder jobName(String jobName) {
            this.nestedJobName = jobName;
            return this;
        }

        public SqlServerJobBuilder stepName(String stepName) {
            this.nestedStepName = stepName;
            return this;
        }

        public SqlServerJob build() throws ConfigurationException {
            SqlServerJob job = new SqlServerJob();
            //check for mandatory params
            if (Strings.isNullOrEmpty(this.nestedServerName) || Strings.isNullOrEmpty(this.nestedUserName)
                    || Strings.isNullOrEmpty(this.nestedPassword) || Strings.isNullOrEmpty(this.nestedJobName) ||
                    this.nestedPort == 0) {
                throw new ConfigurationException("Following parameters are mandatory: serverName, port, userName, password, " +
                        "jobName");
            }

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
