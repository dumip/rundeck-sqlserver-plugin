package ro.dpa.rundeck.plugins.sqlserver;

import com.dtolabs.rundeck.core.plugins.configuration.ConfigurationException;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Created by dumitru.pascu on 3/26/2017.
 */
public class SqlServerJob {
    private static final Logger logger = LoggerFactory.getLogger(SqlServerJob.class);

    private static final long SLEEP_INTERVAL_BETWEEN_JOB_CHECKS = 30;

    private String serverName;
    private int port;
    private String userName;
    private String password;
    private String jobName;
    private String stepName;

    public void execute() throws SQLException, InterruptedException {
        logger.info("Executing SQL Server job with the following details: {}", this.toString());
        try (Connection conn = getConnection()) {
            this.startJob(conn);
            this.waitForJobExecution(conn);
        }
    }

    /**
     * Starts a SQL Server job using the sp_start_job stored procedure available in MS SQL Server.
     * Refer to https://docs.microsoft.com/en-us/sql/relational-databases/system-stored-procedures/sp-start-job-transact-sql for
     * detailed information about this stored procedure
     *
     * @param conn
     * @throws SQLException
     */
    private void startJob(Connection conn) throws SQLException {
        String startJobCommand = "exec msdb.dbo.sp_start_job @job_name = N'"+jobName+"'";
        if (!Strings.isNullOrEmpty(stepName)) {
            startJobCommand += ", @step_name = N'"+stepName+"'";
        }

        logger.debug("Calling sp_start_job: {}", startJobCommand);

        try (CallableStatement cstmt = conn.prepareCall(startJobCommand)) {
            cstmt.execute();
            logger.debug("Executed sp_start_job successfully");
        }
    }

    /**
     * Methods that uses sp_help_job SQL Server stored procedure to check the stored procedure status.
     *
     * @param conn
     * @throws SQLException If the job fails, SQLException is thrown
     */
    private void waitForJobExecution(Connection conn) throws SQLException, InterruptedException {
        String checkJobCommand = "exec msdb.dbo.sp_help_job @job_name = N'"+this.jobName+"'";
        boolean isFinished = false;
        while (!isFinished) {
            //normally sleep should be at the end of the loop, but apparently SQL Server doesn't
            //change the job status immediately, so we can risk not getting the correct status
            //immediately after we start the job
            Thread.sleep(SLEEP_INTERVAL_BETWEEN_JOB_CHECKS * 1000);
            logger.debug("Running checkJobCommand={}", checkJobCommand);
            try (CallableStatement cstmt = conn.prepareCall(checkJobCommand)) {
                ResultSet rs = cstmt.executeQuery();
                if (rs.next()) {
                    int currentExecutionStatus = rs.getInt("current_execution_status");
                    logger.debug("Job name='{}', current_execution_status={}", this.jobName, currentExecutionStatus);

                    ExecutionStatus currentStatus = ExecutionStatus.valueOf(currentExecutionStatus);
                    isFinished = ExecutionStatus.Idle == currentStatus;
                    logger.debug("isFinished={}", isFinished);
                    //job finished, check final status
                    if (isFinished) {
                        ExecutionStatus finalStatus = ExecutionStatus.valueOf(rs.getInt("last_run_outcome"));
                        if (finalStatus != ExecutionStatus.Succeeded) {
                            //the job failed, raise SQL Exception
                            logger.error("Job with name='{}' failed with status={}", this.jobName, finalStatus);
                            throw new SQLException("SQL Server Job with name='{"+this.jobName+"}' " +
                                    "failed with status={"+finalStatus+"}. Check SQL Server logs for more details.");
                        } else {
                            //Successful execution
                            logger.info("Job with name='{}' processed successfully");
                            return;
                        }
                    }

                    logger.info("Job with name={} still in progress. Waiting for {} seconds...", this.jobName, SLEEP_INTERVAL_BETWEEN_JOB_CHECKS);
                }
            }
        }
    }

    private Connection getConnection() throws SQLException {
        String connectionUrl = "jdbc:sqlserver://" + serverName + ":" + port + ";" + "username="
                + userName + ";password=" + password + ";";

        // Establish the connection.
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException ex) {
            //should never reach here, unless we have bad jdbc driver configuration in pom.xml
            throw new RuntimeException(ex);
        }

        Connection conn = DriverManager.getConnection(connectionUrl);
        logger.info("Connected successfully to DB for following URL={}", connectionUrl);
        return conn;
    }

    private SqlServerJob() {

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
