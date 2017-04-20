package ro.dpa.rundeck.plugins.sqlserver;

import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.core.execution.workflow.steps.StepFailureReason;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.ConfigurationException;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.step.StepPlugin;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import com.dtolabs.rundeck.plugins.util.PropertyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.dpa.rundeck.plugins.params.ParamUtils;


import java.sql.SQLException;
import java.util.Map;

/**
 * Plugin that starts SQL Server Agent jobs by using sp_start_job
 * MS SQL Server stored procedures, as described {@here https://docs.microsoft.com/en-us/sql/relational-databases/system-stored-procedures/sp-start-job-transact-sql}
 *
 * After the job execution is created, the execution status is polled periodically until the job
 * ends.
 */
@Plugin(name = SqlServerAgentWorkflowStepPlugin.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.WorkflowStep)
public class SqlServerAgentWorkflowStepPlugin implements StepPlugin, Describable {
    public static final String SERVICE_PROVIDER_NAME = "ro.dpa.rundeck.plugins.sqlserver.SqlServerAgentWorkflowStepPlugin";

    private static final Logger logger = LoggerFactory.getLogger(SqlServerAgentWorkflowStepPlugin.class);
    private static final String USER = "user";
    private static final String PASSWORD = "password";
    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String JOB_NAME = "jobName";
    private static final String STEP_NAME = "stepName";
    private static final String SLEEP_INTERVAL = "sleepInterval";

    @Override
    public void executeStep(PluginStepContext pluginStepContext, Map<String, Object> inputParams) throws StepException {
        SqlServerJob sqlServerJob = null;
        try {
             sqlServerJob = this.buildSqlServerJob(inputParams);
        } catch (ConfigurationException ex) {
            logger.error("Could not configure SQL Server job", ex);
            throw new StepException("Could not configure SQL Server job", StepFailureReason.ConfigurationFailure, inputParams);
        }
        try {
            sqlServerJob.execute();
        } catch (InterruptedException ex) {
            logger.error("Job execution was interrupted.", ex);
            throw new StepException(ex, StepFailureReason.Interrupted);
        } catch (Exception ex) {
            logger.error("Execution error", ex);
            throw new StepException(ex, StepFailureReason.PluginFailed);
        }
    }

    private SqlServerJob buildSqlServerJob(Map<String, Object> inputParams) throws ConfigurationException {
        String user = ParamUtils.getStringValue(USER, inputParams);
        String password = ParamUtils.getStringValue(PASSWORD, inputParams);
        String host = ParamUtils.getStringValue(HOST, inputParams);
        int port = ParamUtils.getIntValue(PORT, inputParams);
        String jobName = ParamUtils.getStringValue(JOB_NAME, inputParams);
        String stepName = ParamUtils.getStringValue(STEP_NAME, inputParams);
        int sleepInterval = ParamUtils.getIntValue(SLEEP_INTERVAL, inputParams);

        String logMessage = "Building SqlServerJob for following input params: user="+user+", password="+password+", host="+host+", " +
                "port="+port+", jobName="+jobName+", stepName="+stepName;
        logger.info(logMessage);
        System.out.println(logMessage);

        SqlServerJob.SqlServerJobBuilder builder = new SqlServerJob.SqlServerJobBuilder();
        builder
                .jobName(jobName)
                .stepName(stepName)
                .userName(user)
                .password(password)
                .serverName(host)
                .port(port);

        if (sleepInterval > 0) {
            builder.sleepInterval(sleepInterval * 1000);
        }

        return builder.build();
    }

    @Override
    public Description getDescription() {
        return DescriptionBuilder.builder()
                .name(SERVICE_PROVIDER_NAME)
                .title("Sql Server Agent Plugin")
                .description("Executes sql server job")
                .property(PropertyBuilder.builder()
                    .string(USER)
                    .title("User")
                    .description("User to connect to the SQL database")
                    .required(true)
                    .build())
                .property(PropertyBuilder.builder()
                    .string(PASSWORD)
                    .title("Password")
                    .description("Password used to connect to the SQL database")
                    .required(true)
                    .build())
                .property(PropertyBuilder.builder()
                    .string(HOST)
                    .title("Host")
                    .description("Host where the job is executed")
                    .required(true)
                    .build())
                .property(PropertyBuilder.builder()
                    .string(PORT)
                    .title("Port")
                    .description("Port for the SQL connection")
                    .required(true)
                    .build())
                .property(PropertyBuilder.builder()
                    .string(JOB_NAME)
                    .title("Job Name")
                    .description("The name of the SQL Server job")
                    .required(true)
                    .build())
                .property(PropertyBuilder.builder()
                    .string(STEP_NAME)
                    .title("Step name")
                    .description("The job will start processing from the step indicated by this parameter. If not provided, job starts from the first step")
                    .required(false)
                    .build())
                .property(PropertyBuilder.builder()
                    .string(SLEEP_INTERVAL)
                    .title("Sleep interval")
                    .description("The sleep interval (in seconds) between two consecutive job checks. Default value is 5.")
                    .required(false)
                    .build())
                .build();
    }
}
