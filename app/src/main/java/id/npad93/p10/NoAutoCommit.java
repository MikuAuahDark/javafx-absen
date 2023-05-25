package id.npad93.p10;

import java.sql.Connection;
import java.sql.SQLException;

public class NoAutoCommit implements AutoCloseable {
    public NoAutoCommit(Connection conn) throws SQLException {
        this.conn = conn;
        autoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
    }

    @Override
    public void close() {
        try {
            conn.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            // Oh no
            e.printStackTrace();
        }
    }

    private Connection conn;
    private boolean autoCommit;
}
