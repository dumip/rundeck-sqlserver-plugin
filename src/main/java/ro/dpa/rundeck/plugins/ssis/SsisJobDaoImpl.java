package ro.dpa.rundeck.plugins.ssis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.dpa.rundeck.plugins.DaoWithConnection;
import ro.dpa.rundeck.plugins.sqlserver.SqlServerJobDaoImpl;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by dumitru.pascu on 4/22/2017.
 */
public class SsisJobDaoImpl extends DaoWithConnection implements SsisJobDao {

    private static final Logger logger = LoggerFactory.getLogger(SqlServerJobDaoImpl.class);

    private Connection conn;

    @Override
    public long createExecution(String packageName, String folderName, String projectName) throws SQLException {
        return 0;
    }

    @Override
    public void setExecutionParameterValue(long executionId, ObjectType objectType, String parameterName, String parameterValue) throws SQLException {

    }

    @Override
    public void startExecution(long executionId) throws SQLException {

    }

    @Override
    public SsisExecutionStatus getExecutionStatus(long executionId) throws SQLException {
        return null;
    }
}
