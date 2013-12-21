import java.sql.*;
import javax.faces.bean.*;
import java.io.*;
import java.util.*;

import org.apache.commons.io.FilenameUtils;
import org.apache.myfaces.custom.fileupload.UploadedFile;

import org.slf4j.*;

@ManagedBean
public class Naloga2 {
    DB db = new DB();
    Logger logger = LoggerFactory.getLogger(getClass());
    private boolean isDataValid = false;
    private final String filePath = System.getProperty("java.io.tmpdir") + File.separator + "data.txt";
    ArrayList<VehicleBean> vehicles = new ArrayList<VehicleBean>();
    private Integer numVehicles = 0;
    private Map<String, Boolean> vehiclesInDb = new HashMap<String, Boolean>();
    private Map<String, Boolean> checkedVehicles = new HashMap<String, Boolean>();
    private UploadedFile uploadedFile = null;

    private void loadVehiclesFromDb() {
        QueryResult qr = db.getVehicles();

        if (qr.error) {
            logger.error("Could not load data: " + qr.errorMsg());
            return;
        }

        vehicles.addAll(qr.result);

        if ((null != qr.result) && (0 < qr.result.size())) {
            for (VehicleBean vehicle : qr.result) {
                vehiclesInDb.put(vehicle.getHash(), true);
                checkedVehicles.put(vehicle.getHash(), true);
            }
        }
    }

    /*
     * Load vehicle data from file.
     * Load only those that aren't already
     * loaded from the database.
     */
    private void loadVehiclesFromFile() {
        ArrayList<VehicleBean> newVehicles;
        File f = new File(filePath);
        if (!f.exists() || !f.canRead()) return;

        try {
            CsvReader csv = new CsvReader();
            newVehicles = csv.read(filePath);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return;
        }

        for (VehicleBean vehicle : newVehicles) {
            if (!vehiclesInDb.containsKey(vehicle.getHash())) {
                vehicles.add(vehicle);
                vehiclesInDb.put(vehicle.getHash(), false);
                checkedVehicles.put(vehicle.getHash(), false);
            }
        }
    }

    /*
     * Remove vehicles which are not in the database.
     */
    private void removeVehiclesFromFile() {
        ArrayList<VehicleBean> newVehicles = new ArrayList<VehicleBean>();
        for (VehicleBean vehicle : vehicles) {
            if (vehiclesInDb.get(vehicle.getHash())) {
                newVehicles.add(vehicle);                
            }
        }
        vehicles = newVehicles;
    }

    private void countVehicles() {
        numVehicles = vehicles.size();
        isDataValid = (0 < numVehicles);
    }

    public Naloga2() {
        loadVehiclesFromDb();
        loadVehiclesFromFile();
        countVehicles();
    }

    public void storeVehicles() {
        ArrayList<VehicleBean> newVehicles = new ArrayList<VehicleBean>();

        QueryResult qr = db.createTable();
        if (qr.error) {
            logger.error("Could not create table: " + qr.errorMsg());
        }

        for (VehicleBean vehicle : vehicles) {
            if (checkedVehicles.get(vehicle.getHash()) &&
                !vehiclesInDb.get(vehicle.getHash())) {
                newVehicles.add(vehicle);
            }
        }

        if (newVehicles.size() <= 0) return;

        qr = db.storeVehicles(newVehicles);

        // Update vehicles' statuses.
        if (!qr.error) {
            for (VehicleBean vehicle : newVehicles) {
                vehiclesInDb.put(vehicle.getHash(), true);
                checkedVehicles.put(vehicle.getHash(), true);
            }
        } else {
            logger.error("Could not store vehicles: " + qr.errorMsg());
        }
    }

    /*
     * Remove vehicles from the database.
     */
    public void removeVehicles() {
        QueryResult qr = db.removeVehicles();
        if (qr.error) {
            logger.error("Could not remove vehicles from database: " + qr.errorMsg());
        }
        vehicles.clear();
        vehiclesInDb.clear();
        checkedVehicles.clear();
        loadVehiclesFromFile();
        countVehicles();
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void upload() {
        Uploader uploader = new Uploader();

        if (null == uploadedFile) {
            logger.error("Could not receive uploaded file.");
            return;
        }

        if (!uploader.storeUploadedFile(uploadedFile, filePath)) {
            logger.error("Could not store uploaded file.");
            return;
        }
        removeVehiclesFromFile();
        loadVehiclesFromFile();
        countVehicles();
    }

    public Map<String, Boolean> getVehiclesInDb() {
        return vehiclesInDb;
    }

    public Map<String, Boolean> getCheckedVehicles() {
        return checkedVehicles;
    }

    public boolean getIsDataValid() {
        return isDataValid;
    }

    public Integer getNumVehicles() {
        return numVehicles;
    }

    public ArrayList<VehicleBean> getVehicles() {
        return vehicles;
    }
}
