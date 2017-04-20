package ro.dpa.rundeck.plugins.sqlserver;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by dumitru.pascu on 3/31/2017.
 */
public class FailedSqlServerJobDao implements SqlServerJobDao {
    @Override
    public void startJob(String jobName, String stepName) throws SQLException {

    }

    @Override
    public int getCurrentExecutionStatus(String jobName) throws SQLException {
        return SqlExecutionStatus.Idle.value();
    }

    @Override
    public int getLastExecutionStatus(String jobName) throws SQLException {
        return SqlExecutionStatus.Failed.value();
    }

    @Override
    public Connection getConnection(String serverName, int port, String userName, String password) throws SQLException {
        return new FakeConnection();
    }

    @Override
    public void close() throws Exception {

    }
}
