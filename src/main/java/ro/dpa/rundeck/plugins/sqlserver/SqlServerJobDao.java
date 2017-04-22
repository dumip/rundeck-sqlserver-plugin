package ro.dpa.rundeck.plugins.sqlserver;

import ro.dpa.rundeck.plugins.DaoWithConnection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by dumitru.pascu on 3/31/2017.
 */
public interface SqlServerJobDao extends AutoCloseable {
    /**
     * Starts a SQL Server job using the sp_start_job stored procedure available in MS SQL Server.
     * Refer {@here https://docs.microsoft.com/en-us/sql/relational-databases/system-stored-procedures/sp-start-job-transact-sql} for
     * detailed information about this stored procedure
     *
     * @param jobName The job name
     * @param stepName The step name. If null or empty, job will start from the first step
     * @throws SQLException
     */
    public void startJob(String jobName, String stepName) throws SQLException;

    /**
     * Returns the <b>current_execution_status</b> of the selected SQL Server job
     *
     * @param jobName
     * @return The execution status (int value, as it is in DB)
     * @throws SQLException
     */
    public int getCurrentExecutionStatus(String jobName) throws SQLException;

    /**
     * Returns the <b>last_run_outcome</b> of the selected SQL Server job
     *
     * @param jobName
     * @return
     * @throws SQLException
     */
    public int getLastExecutionStatus(String jobName) throws SQLException;

}
