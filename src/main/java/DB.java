import java.sql.*;
import java.io.File;

class QueryResult {
    boolean error = false;
    String result;
}

class DB {
    private Connection conn;

    private void connect()
       throws Exception {
        Class.forName("org.h2.Driver");
        String path = System.getProperty("java.io.tmpdir") + File.separator + "naloga2_db";
        conn = DriverManager.getConnection("jdbc:h2:" + path, "mitja", "dbnaloga2");
    }

    private void disconnect() throws Exception {
        if (null != conn)
            conn.close();
    }

    private QueryResult DBQuery(String query, boolean executeQuery) {
        Statement stmt = null;
        QueryResult qr = new QueryResult();

        try {
            connect();
            stmt = conn.createStatement();
            if (executeQuery) { stmt.executeQuery(query);
            } else {
                stmt.execute(query);
            }
            if (null != stmt) { stmt.close(); }
            disconnect();
        } catch (Exception e) {
            qr.error = true;
            qr.result = e.getMessage();
        }

        return qr;        
    }

    public QueryResult createTable() {
        String query = "CREATE TABLE IF NOT EXISTS vehicles (ID INT IDENTITY, YEAR SMALLINT)";
        return DBQuery(query, false);
    }

    public QueryResult insert(String query) {
        return DBQuery(query, false);
    }

    public QueryResult query(String query) {
        return DBQuery(query, true);
    }

    public QueryResult delete(String query) {
        return DBQuery(query, false);
    }
}