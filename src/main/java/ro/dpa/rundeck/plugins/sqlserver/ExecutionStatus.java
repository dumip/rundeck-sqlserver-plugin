package ro.dpa.rundeck.plugins.sqlserver;

/**
 * Enum describing last run outcome for a SQL Server job.
 * <p>
 * Check parameter last_run_outcome from sp_help_job documentation
 * {@link https://docs.microsoft.com/en-us/sql/relational-databases/system-stored-procedures/sp-help-job-transact-sql}
 *
 * Created by dumitru.pascu on 3/29/2017.
 */
public enum ExecutionStatus {
    Failed(0),
    Succeeded(1),
    Canceled(3),
    Idle(4),
    Unknown(5);

    private final int status;

    ExecutionStatus(int status) {
        this.status = status;
    }

    public static ExecutionStatus valueOf(int statusValue) {
        for (ExecutionStatus status : ExecutionStatus.values()) {
            if (statusValue == status.status) {
                return status;
            }
        }

        return ExecutionStatus.Unknown;
    }

    public int value() {
        return this.status;
    }
}
