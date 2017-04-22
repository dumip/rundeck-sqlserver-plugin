package ro.dpa.rundeck.plsugins.ssis;

import com.dtolabs.rundeck.core.plugins.configuration.ConfigurationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import ro.dpa.rundeck.plugins.sqlserver.SqlServerJob;
import ro.dpa.rundeck.plugins.sqlserver.SqlServerJobDao;
import ro.dpa.rundeck.plugins.sqlserver.SuccessSqlServerJobDao;
import ro.dpa.rundeck.plugins.ssis.SsisJob;
import ro.dpa.rundeck.plugins.ssis.SsisJobDao;

import java.sql.SQLException;

/**
 * Created by dumitru.pascu on 4/22/2017.
 */
@RunWith(PowerMockRunner.class)
public class SsisJobTest {
    
    private SsisJob buildValidSsisJob() throws ConfigurationException {
        SsisJob.SsisJobBuilder builder = new SsisJob.SsisJobBuilder();
        return builder
                .folderName("folderName")
                .packageName("packageName")
                .projectName("projectName")
                .serverName("server")
                .userName("username")
                .password("password")
                .port(9999)
                .build();
    }

    private SsisJob buildInvalidSsisJob() throws ConfigurationException {
        SsisJob.SsisJobBuilder builder = new SsisJob.SsisJobBuilder();
        return builder
                .packageName("packageName")
                .projectName("projectName")
                .serverName("server")
                .userName("username")
                .password("password")
                .port(9999)
                .build();
    }

    @Test
    public void testSuccessulExecution() throws Exception {
        SsisJobDao mockDao = new SuccessSsisJobDao();

        SsisJob jobUnderTest = PowerMockito.spy(this.buildValidSsisJob());
        PowerMockito.doReturn(mockDao).when(jobUnderTest, "buildDao");

        jobUnderTest.execute();
    }

    @Test(expected = SQLException.class)
    public void testFailedExecution() throws Exception {
        SsisJobDao mockDao = new FailedSsisJobDao();

        SsisJob jobUnderTest = PowerMockito.spy(this.buildValidSsisJob());
        PowerMockito.doReturn(mockDao).when(jobUnderTest, "buildDao");

        jobUnderTest.execute();
    }

    @Test(expected = ConfigurationException.class)
    public void testFailedJobConfiguration() throws ConfigurationException {
        this.buildInvalidSsisJob();
    }

    @Test
    public void testCorrectJobConfiguration() throws ConfigurationException {
        this.buildValidSsisJob();
    }
}
