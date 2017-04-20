package ro.dpa.rundeck.plugins.ssis;

/**
 * Enum for the @object_type param to be passed in the <b>set_execution_parameter_value</b>.
 *
 * Refer {@here https://docs.microsoft.com/en-us/sql/integration-services/system-stored-procedures/catalog-set-execution-parameter-value-ssisdb-database}
 * to understand the <b>object_type</b> parameter meaning.
 *
 * Created by dumitru.pascu on 4/20/2017.
 */
public enum ObjectType {
    LoggingParameter(50),
    ProjectParameter(30),
    PackageParameter(20);

    private final int value;

    ObjectType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
