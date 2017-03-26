package ro.dpa.rundeck.plugins.sqlserver;

import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.step.StepPlugin;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import com.dtolabs.rundeck.plugins.util.PropertyBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ro.dpa.rundeck.plugins.params.ParamUtils;

import java.util.Map;

/**
 * TODO: explain how the plugin works
 */
@Plugin(name = SqlServerAgentWorkflowStepPlugin.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.WorkflowStep)
public class SqlServerAgentWorkflowStepPlugin implements StepPlugin, Describable {
    public static final String SERVICE_PROVIDER_NAME = "ro.dpa.rundeck.plugins.sqlserver.SqlServerAgentWorkflowStepPlugin";

    private static final Log log = LogFactory.getLog(SqlServerAgentWorkflowStepPlugin.class);
    private static final String USER = "user";
    private static final String PASSWORD = "password";
    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String JOB_NAME = "jobName";
    private static final String JOB_PARAMS = "jobParams";

    @Override
    public void executeStep(PluginStepContext pluginStepContext, Map<String, Object> options) throws StepException {
        String user = ParamUtils.getStringValue(USER, options);
        String password = ParamUtils.getStringValue(PASSWORD, options);
        String host = ParamUtils.getStringValue(HOST, options);
        int port = ParamUtils.getIntValue(PORT, options);
        String jobName = ParamUtils.getStringValue(JOB_NAME, options);
        Map<String, String> jobParams = ParamUtils.getMapValues(JOB_PARAMS, options);

        String logMessage = "Received following input params: user="+user+", password="+password+", host="+host+", " +
                "port="+port+", jobName="+jobName+", jobParams="+jobParams;
        log.info(logMessage);
        System.out.println(logMessage);
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
                    .string(JOB_PARAMS)
                    .title("Job Parameters")
                    .description("Job parameters. Enter a single param / value pair on each line, e.g.\r\nparam1=value1\r\nparam2=value2\r\n...")
                    .required(false)
                    .renderingAsTextarea()
                    .build())
                .build();
    }
}
