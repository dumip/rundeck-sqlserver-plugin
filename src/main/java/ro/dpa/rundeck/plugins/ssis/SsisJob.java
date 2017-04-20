package ro.dpa.rundeck.plugins.ssis;

import com.dtolabs.rundeck.core.plugins.configuration.ConfigurationException;
import com.google.common.base.Strings;
import ro.dpa.rundeck.plugins.SubmitAndPollJob;

import java.sql.SQLException;
import java.util.Map;

/**
 * Created by dumitru.pascu on 4/20/2017.
 */
public class SsisJob extends SubmitAndPollJob<SsisJobDao> {

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
        return null;
    }

    @Override
    protected void startJob(SsisJobDao ssisJobDao) throws Exception {
        this.executionId = ssisJobDao.createExecution(packageName, folderName, projectName);

        //add project params
        this.addParameters(ssisJobDao, projectParameters, ObjectType.ProjectParameter);
        //add package params
        this.addParameters(ssisJobDao, packageParameters, ObjectType.PackageParameter);
        //add logging params
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
    }

    public static class SsisJobBuilder extends SubmitAndPollJob.Builder<SsisJob> {

        private String nestedPackageName;
        private String nestedFolderName;
        private String nestedProjectName;
        private Map<String, String> nestedProjectParameters;
        private Map<String, String> nestedPackageParameters;
        private Map<String, String> nestedLoggingParameters;


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
            ssisJob.projectParameters = this.nestedProjectParameters;
            ssisJob.packageParameters = this.nestedPackageParameters;
            ssisJob.loggingParameters = this.nestedLoggingParameters;

            return ssisJob;
        }
    }
}
