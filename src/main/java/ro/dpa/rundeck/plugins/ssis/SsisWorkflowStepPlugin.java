package ro.dpa.rundeck.plugins.ssis;

import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.step.StepPlugin;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import com.dtolabs.rundeck.plugins.util.PropertyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Rundeck plugin that starts an SSIS ETL package using the <b>start_execution</b>
 * stored procedures described {@here https://docs.microsoft.com/en-us/sql/integration-services/system-stored-procedures/catalog-start-execution-ssisdb-database}
 *
 * After the execution is created, the status is periodically polled until the job ends, by using the <b>catalog.executions</b>
 * table described {@here https://docs.microsoft.com/en-us/sql/integration-services/system-views/catalog-executions-ssisdb-database}
 *
 * Created by dumitru.pascu on 4/9/2017.
 */
@Plugin(name = SsisWorkflowStepPlugin.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.WorkflowStep)
public class SsisWorkflowStepPlugin implements StepPlugin, Describable {
    public static final String SERVICE_PROVIDER_NAME = "ro.dpa.rundeck.plugins.sqlserver.SsisWorkflowStepPlugin";

    private static final Logger logger = LoggerFactory.getLogger(SsisWorkflowStepPlugin.class);
    private static final String USER = "user";
    private static final String PASSWORD = "password";
    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String PACKAGE_NAME = "packageName";
    private static final String PROJECT_NAME = "projectName";
    private static final String FOLDER_NAME = "folderName";
    private static final String PROJECT_PARAMETERS = "projectParameters";
    private static final String PACKAGE_PARAMETERS = "packageParameters";
    private static final String LOGGING_PARAMETERS = "loggingParameters";

    @Override
    public void executeStep(PluginStepContext pluginStepContext, Map<String, Object> map) throws StepException {

    }

    @Override
    public Description getDescription() {
        return DescriptionBuilder.builder()
                .name(SERVICE_PROVIDER_NAME)
                .title("SSIS Plugin")
                .description("Executes SSIS job")
                .property(PropertyBuilder.builder()
                        .string(USER)
                        .title("User")
                        .description("User to connect to the SQL database")
                        .required(true)
                        .build())
                .property(PropertyBuilder.builder()
                        .string(PASSWORD)
                        .title("Password")
                        .description("Password to connect to the SQL database")
                        .required(true)
                        .build())
                .property(PropertyBuilder.builder()
                        .string(HOST)
                        .title("Host")
                        .description("SQL database host")
                        .required(true)
                        .build())
                .property(PropertyBuilder.builder()
                        .string(PORT)
                        .title("Port")
                        .description("SQL database port")
                        .required(true)
                        .build())
                .property(PropertyBuilder.builder()
                        .string(PROJECT_NAME)
                        .title("Project name")
                        .description("The name of the SSIS project")
                        .required(true)
                        .build())
                .property(PropertyBuilder.builder()
                        .string(PACKAGE_NAME)
                        .title("Package name")
                        .description("The name of the SSIS package")
                        .required(true)
                        .build())
                .property(PropertyBuilder.builder()
                        .string(FOLDER_NAME)
                        .title("Folder name")
                        .description("The name of the SSIS folder")
                        .required(false)
                        .build())
                .property(PropertyBuilder.builder()
                        .string(PROJECT_PARAMETERS)
                        .title("Project parameters")
                        .description("The project parameters. \n Format is param1=value1;param2=value2...")
                        .required(false)
                        .build())
                .property(PropertyBuilder.builder()
                        .string(PACKAGE_PARAMETERS)
                        .title("Pacakge parameters")
                        .description("The package parameters. \n Format is param1=value1;param2=value2...")
                        .required(false)
                        .build())
                .property(PropertyBuilder.builder()
                        .string(LOGGING_PARAMETERS)
                        .title("Logging parameters")
                        .description("The logging parameters. \n Format is param1=value1;param2=value2...")
                        .required(false)
                        .build())
                .build();
    }
}
