import java.sql.*;
import java.util.*;
import java.io.File;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;

class QueryResult {
    boolean error = false;
    private String errorMessage;
    ArrayList<VehicleBean> result;

    public void errorMsg(String msg) {
        error = true;
        errorMessage = msg;
    }
    public String errorMsg() {
        return errorMessage;
    }
}

class DB {
    private final String VEHICLES_SQL = "naloga2.sql";
    private final String DB_NAME = "naloga2db";
    private final String USERNAME = "admin";
    private final String PASSWORD = "password";

    private Connection conn;

    private void connect()
       throws Exception {
        Class.forName("org.h2.Driver");
        String path = System.getProperty("java.io.tmpdir") + File.separator + DB_NAME;
        conn = DriverManager.getConnection("jdbc:h2:" + path, USERNAME, PASSWORD);
    }

    private boolean disconnect() {
        try {
            if (null != conn) {
                conn.close();
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    /**
     * Creates vehicle table.
     */
    public QueryResult createTable() {
        QueryResult qr = new QueryResult();
        try {
            InputStream str = Naloga2.class.getResourceAsStream(VEHICLES_SQL);
            String query = IOUtils.toString(str);
            connect();
            Statement stmt = conn.createStatement();
            stmt.execute(query);
            stmt.close();
        } catch (Exception e) {
            qr.errorMsg(e.getMessage());
        } finally {
            if (!disconnect()) {
                qr.errorMsg(qr.errorMsg() + "\nCould not disconnect.");
            }
        }
        return qr;
    }

    /**
     * Inserts given vehicle objects into vehicle table.
     * @param vehicles to insert
     * @return result object
     */
    public QueryResult storeVehicles(ArrayList<VehicleBean> vehicles) {
        QueryResult qr = new QueryResult();
        String query = "INSERT INTO vehicles (year, make, model, comment, price) VALUES";
        String holders = "(?, ?, ?, ?, ?)";

        for (int i = 0; i < vehicles.size(); i += 1) {
            query += holders;
            query += (i < (vehicles.size() - 1)) ? "," : "";
        }

        try {
            connect();
            PreparedStatement stmt = conn.prepareStatement(query);
            int i = 1;
            for (VehicleBean vehicle : vehicles) {
                stmt.setInt(i++, vehicle.getYear());
                stmt.setString(i++, vehicle.getMake());
                stmt.setString(i++, vehicle.getModel());
                stmt.setString(i++, vehicle.getComment());
                stmt.setDouble(i++, vehicle.getPrice());
            }
            stmt.executeUpdate();
            stmt.close();
        } catch (Exception ex) {
            qr.errorMsg(ex.getMessage());
        } finally {
            if (!disconnect()) {
                qr.errorMsg(qr.errorMsg() + "\nCould not disconnect.");
            }
        }
        return qr;
    }

    /**
     * Selects all vehicles from vehicle table.
     * @return result object
     */
    public QueryResult getVehicles() {
        QueryResult qr = new QueryResult();
        String query = "SELECT year, make, model, comment, price FROM vehicles";
        ResultSet rs;
        ArrayList<VehicleBean> vehicles = new ArrayList<VehicleBean>();

        try {
            connect();
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                VehicleBean vehicle = new VehicleBean();
                vehicle.setYear(rs.getInt("year"));
                vehicle.setMake(rs.getString("make"));
                vehicle.setModel(rs.getString("model"));
                vehicle.setComment(rs.getString("comment"));
                vehicle.setPrice(rs.getDouble("price"));
                vehicle.setHash();
                vehicles.add(vehicle);
            }
            stmt.close();
            qr.result = vehicles;
        } catch (Exception ex) {
            qr.errorMsg(ex.getMessage());
        } finally {
            if (!disconnect()) {
                qr.errorMsg(qr.errorMsg() + "\nCould not disconnect.");
            }
        }
        return qr;
    }

    /**
     * Empties vehicle table.
     * @return result object
     */
    public QueryResult removeVehicles() {
        QueryResult qr = new QueryResult();
        String query = "DELETE FROM vehicles";
        try {
            connect();
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
        } catch (Exception e) {
            qr.errorMsg(e.getMessage());
        } finally {
            if (!disconnect()) {
                qr.errorMsg(qr.errorMsg() + "\nCould not disconnect.");
            }
        }
        return qr;
    }
}