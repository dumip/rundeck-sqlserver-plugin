package ro.dpa.rundeck.plsugins.ssis;

import ro.dpa.rundeck.plugins.ssis.ObjectType;
import ro.dpa.rundeck.plugins.ssis.SsisExecutionStatus;
import ro.dpa.rundeck.plugins.ssis.SsisJobDao;

import java.sql.SQLException;

/**
 * Created by dumitru.pascu on 4/22/2017.
 */
public class SuccessSsisJobDao implements SsisJobDao {
    @Override
    public long createExecution(String packageName, String folderName, String projectName) throws SQLException {
        return 1;
    }

    @Override
    public void setExecutionParameterValue(long executionId, ObjectType objectType, String parameterName, String parameterValue) throws SQLException {

    }

    @Override
    public void startExecution(long executionId) throws SQLException {

    }

    @Override
    public SsisExecutionStatus getExecutionStatus(long executionId) throws SQLException {
        return SsisExecutionStatus.Succeeded;
    }

    @Override
    public void close() throws Exception {

    }
}
