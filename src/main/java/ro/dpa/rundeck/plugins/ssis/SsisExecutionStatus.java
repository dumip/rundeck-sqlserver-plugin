package ro.dpa.rundeck.plugins.ssis;

/**
 * Enum describing all SSIS possible execution status values.
 * Values retrieved from the <b>catalog.executions</b> table described
 * {@here https://docs.microsoft.com/en-us/sql/integration-services/system-views/catalog-executions-ssisdb-database}
 *
 */
public enum SsisExecutionStatus {
    Created(1),
    Running(2),
    Canceled(3),
    Failed(4),
    Pending(5),
    EndedUnexpectedly(6),
    Succeeded(7),
    Stopping(8),
    Completed(9),
    Unknown(-1);

    private final int status;

    SsisExecutionStatus(int status) {
        this.status = status;
    }

    public static SsisExecutionStatus valueOf(int statusValue) {
        for (SsisExecutionStatus status : SsisExecutionStatus.values()) {
            if (statusValue == status.status) {
                return status;
            }
        }

        return SsisExecutionStatus.Unknown;
    }

    public int value() {
        return this.status;
    }
}
