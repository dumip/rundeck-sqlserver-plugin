package ro.dpa.rundeck.plugins.ssis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.dpa.rundeck.plugins.DaoWithConnection;
import ro.dpa.rundeck.plugins.sqlserver.SqlServerJobDaoImpl;

import java.sql.*;

/**
 * Created by dumitru.pascu on 4/22/2017.
 */
public class SsisJobDaoImpl extends DaoWithConnection implements SsisJobDao {

    private static final Logger logger = LoggerFactory.getLogger(SqlServerJobDaoImpl.class);

    public SsisJobDaoImpl() {
        super();
    }

    public SsisJobDaoImpl(String server, int port, String user, String password) throws SQLException {
        super(server, port, user, password);
    }

    @Override
    public long createExecution(String packageName, String folderName, String projectName) throws SQLException {
        logger.info("Call catalog.create_execution with parameters: folderName={}, projectName={}, packageName={}",
                folderName, projectName, packageName);

        String createExecutionCommand = "{call catalog.create_execution(?, ?, ?, null, null, null, null, ?)}";
        long executionId;
        try (CallableStatement cstmt = super.conn.prepareCall(createExecutionCommand)) {
            cstmt.setString(1, folderName);
            cstmt.setString(2, projectName);
            cstmt.setString(3, packageName);
            cstmt.registerOutParameter(4, Types.BIGINT);

            cstmt.execute();

            executionId = cstmt.getLong(4);
        }

        logger.info("Execution created successfully with executionId = {}", executionId);
        return executionId;
    }

    @Override
    public void setExecutionParameterValue(long executionId, ObjectType objectType, String parameterName, String parameterValue) throws SQLException {
        logger.info("Adding to executionId = {} parameter[parameterName={}, parameterValue={}, objectType={}",
                executionId, parameterName, parameterValue, objectType);

        String addParameterCommand = "{call catalog.set_execution_parameter_value(?, ?, ?, ?)}";
        try (CallableStatement cstmt = super.conn.prepareCall(addParameterCommand)) {
            cstmt.setLong(1, executionId);
            cstmt.setInt(2, objectType.getValue());
            cstmt.setString(3, parameterName);
            cstmt.setString(4, parameterValue);

            cstmt.execute();
        }
    }

    @Override
    public void startExecution(long executionId) throws SQLException {
        logger.info("Startig SSIS job with executionId = {}", executionId);
        String startExecutionCommand = "{call catalog.start_execution(?, ?)}";
        try (CallableStatement cstmt = super.conn.prepareCall(startExecutionCommand)) {
            cstmt.setLong(1, executionId);
            cstmt.setInt(2, 0); //don't retry if start fails

            cstmt.execute();
        }
    }

    @Override
    public SsisExecutionStatus getExecutionStatus(long executionId) throws SQLException {
        logger.info("Check status for SSIS job with executionId = {}", executionId);
        String sqlGetStatus = "SELECT status FROM catalog.executions WHERE execution_id = ?";
        try (PreparedStatement pstmt = super.conn.prepareCall(sqlGetStatus)) {
            pstmt.setLong(1, executionId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int status = rs.getInt(1);
                logger.info("Got following status value = {} for executionId = {}",
                        status, executionId);
                return SsisExecutionStatus.valueOf(status);
            } else {
                throw new SQLException("SSIS job with executionId = " + executionId +
                    " was not found.");
            }
        }
    }
}
