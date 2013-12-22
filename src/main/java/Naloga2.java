import java.sql.*;
import javax.faces.bean.*;
import java.io.*;
import java.util.*;

import org.apache.commons.io.FilenameUtils;
import org.apache.myfaces.custom.fileupload.UploadedFile;

import org.slf4j.*;

interface Errors {
    String FILE_ERROR = "file_error";
    String FILE_WRONG_TYPE = "file_wrong_type";
    String FILE_EMPTY = "file_empty";
    String NONE_CHOSEN = "none_chosen";
    String TABLE_STORE = "table_store_error";
    String VEHICLE_STORE = "vehicle_store_error";
    String REMOVE = "error_removing";
    String DB = "db_error";
}

@ManagedBean
public class Naloga2 {
    private DB db = new DB();
    private Logger logger = LoggerFactory.getLogger(getClass());

    private final String filePath = System.getProperty("java.io.tmpdir") + File.separator + "data.txt";

    ArrayList<VehicleBean> vehicles = new ArrayList<VehicleBean>();
    private Integer numVehicles = 0;
    private boolean isDataValid = false;
    private boolean anyInDb = false;
    private boolean anyInFile = false;
    private Map<String, Boolean> vehiclesInDb = new HashMap<String, Boolean>();
    private Map<String, Boolean> checkedVehicles = new HashMap<String, Boolean>();

    private UploadedFile uploadedFile = null;
    private String errorMsg;
    private final String RESOURCE_BUNDLE = "Lang";

    private void loadVehiclesFromDb() {
        QueryResult qr = db.getVehicles();

        if (qr.error) {
            errorMsg = getMsg(Errors.DB);
            logger.error(errorMsg + " " + qr.errorMsg());
            return;
        }

        vehicles.addAll(qr.result);

        if ((null != qr.result) && (0 < qr.result.size())) {
            for (VehicleBean vehicle : qr.result) {
                vehiclesInDb.put(vehicle.getHash(), true);
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

        if (newVehicles.size() <= 0) {
            errorMsg = getMsg(Errors.FILE_EMPTY);
            logger.error(errorMsg);
            return;
        }

        for (VehicleBean vehicle : newVehicles) {
            if (!vehiclesInDb.containsKey(vehicle.getHash())) {
                vehicles.add(vehicle);
                vehiclesInDb.put(vehicle.getHash(), false);
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
            } else {
                vehiclesInDb.remove(vehicle.getHash());
                checkedVehicles.remove(vehicle.getHash());
            }
        }
        vehicles = newVehicles;
    }

    private String getMsg(String which) {
        ResourceBundle rb = ResourceBundle.getBundle(RESOURCE_BUNDLE);
        return rb.getString(which);
    }

    private void countVehicles() {
        numVehicles = vehicles.size();
        isDataValid = (0 < numVehicles);
        errorMsg = (numVehicles <= 0) ? getMsg(Errors.FILE_ERROR) : errorMsg;
        anyInDb = vehiclesInDb.values().contains(true);
        anyInFile = vehiclesInDb.values().contains(false);
    }

    private void start() {
        errorMsg = "";
        loadVehiclesFromDb();
        loadVehiclesFromFile();
        sortVehicles();
        countVehicles();
    }

    /*
     * Sort vehicles by year in descending order.
     */
    private void sortVehicles() {
        Collections.sort(vehicles, new Comparator<VehicleBean>() {
                public int compare(VehicleBean v1, VehicleBean v2) {
                    if (v1.getYear() < v2.getYear()) {
                        return 1;
                    } else if (v2.getYear() < v1.getYear()) {
                        return -1;
                    }
                    return 0;
                }
        });
    }

    public Naloga2() {
        start();
    }

    public void storeVehicles() {
        ArrayList<VehicleBean> newVehicles = new ArrayList<VehicleBean>();

        QueryResult qr = db.createTable();
        errorMsg = "";
        if (qr.error) {
            errorMsg = getMsg(Errors.TABLE_STORE);
            logger.error(errorMsg + " " + qr.errorMsg());
            return;
        }

        for (VehicleBean vehicle : vehicles) {
            if (checkedVehicles.containsKey(vehicle.getHash()) &&
                checkedVehicles.get(vehicle.getHash()) &&
                !vehiclesInDb.get(vehicle.getHash())) {
                newVehicles.add(vehicle);
            }
        }

        checkedVehicles.clear();

        if (newVehicles.size() <= 0) {
            errorMsg = getMsg(Errors.NONE_CHOSEN);
            return;
        }

        qr = db.storeVehicles(newVehicles);
        if (qr.error) {
            errorMsg = getMsg(Errors.VEHICLE_STORE);
            logger.error(errorMsg + " " + qr.errorMsg());
            return;
        }

        // Update vehicles' statuses.
       for (VehicleBean vehicle : newVehicles) {
           vehiclesInDb.put(vehicle.getHash(), true);
       }
       countVehicles();
    }

    /*
     * Remove vehicles from the database.
     */
    public void removeVehicles() {
        QueryResult qr = db.removeVehicles();
        errorMsg = "";
        if (qr.error) {
            errorMsg = getMsg(Errors.REMOVE);
            logger.error(errorMsg + " " + qr.errorMsg());
            return;
        }
        vehicles.clear();
        vehiclesInDb.clear();
        checkedVehicles.clear();
        errorMsg = "";
        loadVehiclesFromFile();
        sortVehicles();
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
            errorMsg = getMsg(Errors.FILE_ERROR);
            logger.error(errorMsg);
            return;
        }

        if (!uploader.storeUploadedFile(uploadedFile, filePath)) {
            errorMsg = getMsg(Errors.FILE_WRONG_TYPE);
            logger.error(errorMsg);
            return;
        }
        removeVehiclesFromFile();
        errorMsg = "";
        loadVehiclesFromFile();
        sortVehicles();
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

    public String getErrorMsg() {
        return errorMsg;
    }

    public boolean getAnyInDb() {
        return anyInDb;
    }

    public boolean getAnyInFile() {
        return anyInFile;
    }
}
