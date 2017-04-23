package ro.dpa.rundeck.plugins.ssis;

import com.dtolabs.rundeck.core.plugins.configuration.ConfigurationException;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.dpa.rundeck.plugins.SubmitAndPollJob;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dumitru.pascu on 4/20/2017.
 */
public class SsisJob extends SubmitAndPollJob<SsisJobDao> {

    private static final Logger logger = LoggerFactory.getLogger(SsisJob.class);

    private String packageName;
    private String folderName;
    private String projectName;
    private Map<String, String> projectParameters;
    private Map<String, String> packageParameters;
    private Map<String, String> loggingParameters;
    private long executionId;

    private SsisJob() {

    }

    @Override
    protected SsisJobDao buildDao() throws SQLException {
        SsisJobDao dao = new SsisJobDaoImpl(this.serverName, this.port, this.userName, this.password);
        return dao;
    }

    @Override
    protected void startJob(SsisJobDao ssisJobDao) throws Exception {
        logger.info("Starting job for packageName = {}, folderName = {}, projectName = {}");
        this.executionId = ssisJobDao.createExecution(packageName, folderName, projectName);
        logger.info("Got executionId = {}", executionId);

        //add project params
        logger.info("Adding projectParameters: {}", projectParameters);
        this.addParameters(ssisJobDao, projectParameters, ObjectType.ProjectParameter);
        //add package params
        logger.info("Adding packageParameters: {}", packageParameters);
        this.addParameters(ssisJobDao, packageParameters, ObjectType.PackageParameter);
        //add logging params
        logger.info("Adding loggingParameters: {}", loggingParameters);
        this.addParameters(ssisJobDao, loggingParameters, ObjectType.LoggingParameter);

        //start the execution
        ssisJobDao.startExecution(executionId);
    }

    private void addParameters(SsisJobDao dao, Map<String, String> params, ObjectType type) throws SQLException {
        for (String paramName : params.keySet()) {
            dao.setExecutionParameterValue(executionId, type, paramName, projectParameters.get(paramName));
        }
    }

    @Override
    protected void waitForJobExecution(SsisJobDao dao) throws Exception, InterruptedException {
        boolean isFinished = false;

        while (!isFinished) {
            SsisExecutionStatus status = dao.getExecutionStatus(this.executionId);
            logger.info("Status for executionId = {} is {}", this.executionId, status);
            switch (status) {
                case Failed:
                    throw new SQLException("SSIS Job with executionId='{"+this.executionId+"}' " +
                            "failed with status={"+status+"}. Check SSIS Server logs for more details.");
                case EndedUnexpectedly:
                    throw new SQLException("SSIS Job with executionId='{"+this.executionId+"}' " +
                            "ended unexpectedly. Check SSIS Server logs for more details.");
                case Succeeded:
                    logger.info("SSIS Job with executionId={} processed successfully.", this.executionId);
                    isFinished = true;
                    break;
                default:
                    logger.info("SSIS Job with executionId={} has status={}.", this.executionId, status.toString());
                    break;
            }

            logger.debug("Job with executionId={} in progress. Waiting for {} seconds until next check...",
                    this.executionId, this.sleepInterval / 1000);
            Thread.sleep(this.sleepInterval);
        }
    }

    public static class SsisJobBuilder extends SubmitAndPollJob.Builder<SsisJob> {

        private String nestedPackageName;
        private String nestedFolderName;
        private String nestedProjectName;
        private Map<String, String> nestedProjectParameters;
        private Map<String, String> nestedPackageParameters;
        private Map<String, String> nestedLoggingParameters;

        public SsisJobBuilder packageName(String packageName) {
            this.nestedPackageName = packageName;
            return this;
        }

        public SsisJobBuilder folderName(String folderName) {
            this.nestedFolderName = folderName;
            return this;
        }

        public SsisJobBuilder projectName(String projectName) {
            this.nestedProjectName = projectName;
            return this;
        }

        public SsisJobBuilder projectParameters(Map<String, String> projectParameters) {
            this.nestedProjectParameters = projectParameters;
            return this;
        }

        public SsisJobBuilder packageParameters(Map<String, String> packageParameters) {
            this.nestedPackageParameters = packageParameters;
            return this;
        }

        public SsisJobBuilder loggingParameters(Map<String, String> loggingParameters) {
            this.nestedLoggingParameters = loggingParameters;
            return this;
        }

        @Override
        protected void validate() throws ConfigurationException {
            if (Strings.isNullOrEmpty(nestedPackageName) ||
                    Strings.isNullOrEmpty(nestedFolderName) ||
                    Strings.isNullOrEmpty(nestedProjectName)) {
                throw new ConfigurationException("Following parameters are mandatory: " +
                    "packageName, folderName, projectName");
            }
        }

        @Override
        protected SsisJob createInstance() {
            SsisJob ssisJob = new SsisJob();

            ssisJob.serverName = this.nestedServerName;
            ssisJob.port = this.nestedPort;
            ssisJob.userName = this.nestedUserName;
            ssisJob.password = this.nestedPassword;
            ssisJob.packageName = this.nestedPackageName;
            ssisJob.folderName = this.nestedFolderName;
            ssisJob.projectName = this.nestedProjectName;
            ssisJob.projectParameters = this.nestedProjectParameters != null ? this.nestedProjectParameters : new HashMap<String, String>();
            ssisJob.packageParameters = this.nestedPackageParameters != null ? this.nestedPackageParameters : new HashMap<String, String>();
            ssisJob.loggingParameters = this.nestedLoggingParameters != null ? this.nestedLoggingParameters : new HashMap<String, String>();

            return ssisJob;
        }
    }
}
