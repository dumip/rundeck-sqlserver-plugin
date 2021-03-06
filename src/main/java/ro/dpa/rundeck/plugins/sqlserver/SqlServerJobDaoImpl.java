package ro.dpa.rundeck.plugins.sqlserver;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * DAO that manages the queries for creating / viewing
 * SQL Server jobs
 *
 * Created by dumitru.pascu on 3/29/2017.
 */
public class SqlServerJobDaoImpl implements SqlServerJobDao {

    private static final Logger logger = LoggerFactory.getLogger(SqlServerJobDaoImpl.class);

    private Connection conn;

    public SqlServerJobDaoImpl() {

    }

    public SqlServerJobDaoImpl(String server, int port, String user, String password) throws SQLException {
        this.conn = this.getConnection(server, port, user, password);
    }

    @Override
    public void startJob(String jobName, String stepName) throws SQLException {
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

    @Override
    public int getCurrentExecutionStatus(String jobName) throws SQLException {
        return getStatusForParam(jobName, "current_execution_status");
    }

    @Override
    public int getLastExecutionStatus(String jobName) throws SQLException {
        return getStatusForParam(jobName, "last_run_outcome");
    }

    /**
     * Retrieves the job status. See {@https://docs.microsoft.com/en-us/sql/relational-databases/system-stored-procedures/sp-help-job-transact-sql here}
     * for detailed info regarding the <b>sp_help_job</b> stored procedure
     *
     * @param jobName
     * @param fieldName
     * @return
     * @throws SQLException
     */
    private int getStatusForParam(String jobName, String fieldName) throws SQLException {
        int currentExecutionStatus = -1;//unknown
        String checkJobCommand = "exec msdb.dbo.sp_help_job @job_name = N'"+jobName+"'";
        try (CallableStatement cstmt = conn.prepareCall(checkJobCommand)) {
            ResultSet rs = cstmt.executeQuery();
            if (rs.next()) {
                currentExecutionStatus = rs.getInt(fieldName);
            }
        }

        return currentExecutionStatus;
    }

    @Override
    public Connection getConnection(String serverName, int port, String userName, String password) throws SQLException {
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

    @Override
    public void close() throws SQLException {
        conn.close();
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }
}
