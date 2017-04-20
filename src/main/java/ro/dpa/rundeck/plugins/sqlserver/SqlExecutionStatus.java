package ro.dpa.rundeck.plugins.sqlserver;

/**
 * Enum describing last run outcome for a SQL Server job.
 * <p>
 * Check parameter last_run_outcome from sp_help_job documentation
 * {@link https://docs.microsoft.com/en-us/sql/relational-databases/system-stored-procedures/sp-help-job-transact-sql}
 *
 * Created by dumitru.pascu on 3/29/2017.
 */
public enum SqlExecutionStatus {
    Failed(0),
    Succeeded(1),
    Canceled(3),
    Idle(4),
    Unknown(5);

    private final int status;

    SqlExecutionStatus(int status) {
        this.status = status;
    }

    public static SqlExecutionStatus valueOf(int statusValue) {
        for (SqlExecutionStatus status : SqlExecutionStatus.values()) {
            if (statusValue == status.status) {
                return status;
            }
        }

        return SqlExecutionStatus.Unknown;
    }

    public int value() {
        return this.status;
    }
}
