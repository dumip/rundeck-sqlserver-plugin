package ro.dpa.rundeck.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by dumitru.pascu on 4/10/2017.
 */
public abstract class DaoWithConnection implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(DaoWithConnection.class);

    private Connection conn;

    public DaoWithConnection() {

    }

    public DaoWithConnection(String server, int port, String user, String password) throws SQLException {
        this.conn = this.getConnection(server, port, user, password);
    }

    private Connection getConnection(String serverName, int port, String userName, String password) throws SQLException {
        String connectionUrl = "jdbc:sqlserver://" + serverName + ":" + port + ";" + "username="
                + userName + ";password=" + password + ";";

        // Establish the connection.
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException ex) {
            //should never reach here, unless we have bad jdbc driver configuration in pom.xml
            throw new RuntimeException(ex);
        }

        Connection conn = DriverManager.getConnection(connectionUrl);
        logger.info("Connected successfully to DB for following URL={}", connectionUrl);
        return conn;
    }

    @Override
    public void close() throws SQLException {
        conn.close();
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }
}
