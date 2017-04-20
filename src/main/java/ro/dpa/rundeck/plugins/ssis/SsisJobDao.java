package ro.dpa.rundeck.plugins.ssis;

import ro.dpa.rundeck.plugins.JobDao;

import java.sql.SQLException;

/**
 * Created by dumitru.pascu on 4/20/2017.
 */
public interface SsisJobDao extends JobDao {

    /**
     * Creates an SSIS execution. Refer
     * {@here https://docs.microsoft.com/en-us/sql/integration-services/system-stored-procedures/catalog-create-execution-ssisdb-database}
     * for more details.
     *
     * @param packageName
     * @param folderName
     * @param projectName
     * @return the execution id
     * @throws SQLException
     */
    long createExecution(String packageName, String folderName, String projectName) throws SQLException;

    /**
     * Sets execution parameter value. Refer
     * {@here https://docs.microsoft.com/en-us/sql/integration-services/system-stored-procedures/catalog-set-execution-parameter-value-ssisdb-database}
     * for more details.
     *
     * @param executionId
     * @param objectType
     * @param parameterName
     * @param parameterValue
     * @throws SQLException
     */
    void setExecutionParameterValue(long executionId, ObjectType objectType, String parameterName, String parameterValue) throws  SQLException;

    /**
     * Starts an SSIS package execution. Refer
     * {@here https://docs.microsoft.com/en-us/sql/integration-services/system-stored-procedures/catalog-start-execution-ssisdb-database}
     * for more details.
     *
     * @param executionId
     * @throws SQLException
     */
    void startExecution(long executionId) throws SQLException;

    /**
     * Get execution status for a SSIS package execution. Status is retrieved from the
     * <b>catalog.executions</b> table described
     * {@here https://docs.microsoft.com/en-us/sql/integration-services/system-views/catalog-executions-ssisdb-database}.
     *
     * @param executionId
     * @return the execution status
     * @throws SQLException
     */
    SsisExecutionStatus getExecutionStatus(long executionId) throws SQLException;
}
