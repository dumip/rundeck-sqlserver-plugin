package ro.dpa.rundeck.plugins.sqlserver;

import com.dtolabs.rundeck.core.plugins.configuration.ConfigurationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.SQLException;

/**
 * Created by dumitru.pascu on 3/29/2017.
 */
@RunWith(PowerMockRunner.class)
public class SqlServerJobTest {

    private SqlServerJob buildValidSqlServerJob() throws ConfigurationException {
        SqlServerJob.SqlServerJobBuilder builder = new SqlServerJob.SqlServerJobBuilder();
        return builder.jobName("jobName")
                .stepName("step")
                .serverName("server")
                .userName("username")
                .password("password")
                .port(9999)
                .build();
    }

    private SqlServerJob buildInvalidSqlServerJob() throws ConfigurationException {
        SqlServerJob.SqlServerJobBuilder builder = new SqlServerJob.SqlServerJobBuilder();
        return builder.jobName("jobName")
                .stepName("step")
                .serverName("server")
                .userName("username")
                .build();
    }

    @Test
    public void testSuccessulExecution() throws Exception {
        SqlServerJobDao mockDao = new SuccessSqlServerJobDao();

        SqlServerJob jobUnderTest = PowerMockito.spy(this.buildValidSqlServerJob());
        PowerMockito.doReturn(mockDao).when(jobUnderTest, "getSqlServerJobDao");

        jobUnderTest.execute();
    }

    @Test(expected = SQLException.class)
    public void testFailedExecution() throws Exception {
        SqlServerJobDao mockDao = new FailedSqlServerJobDao();

        SqlServerJob jobUnderTest = PowerMockito.spy(this.buildValidSqlServerJob());
        PowerMockito.doReturn(mockDao).when(jobUnderTest, "getSqlServerJobDao");

        jobUnderTest.execute();
    }

    @Test(expected = ConfigurationException.class)
    public void testFailedJobConfiguration() throws ConfigurationException {
        this.buildInvalidSqlServerJob();
    }
}
