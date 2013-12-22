import java.sql.*;
import javax.faces.bean.*;
import java.io.*;
import java.util.*;
import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;

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

    // CSV file path.
    private final String filePath = System.getProperty("java.io.tmpdir") + File.separator + "data.txt";

    // Holder of all vehicles. From CSV and database.
    ArrayList<VehicleBean> vehicles = new ArrayList<VehicleBean>();
    private Integer numVehicles = 0;  // Total number of vehicles.
    // List of all vehicles. Those stored in the database have the value of "true".
    private Map<String, Boolean> vehiclesInDb = new HashMap<String, Boolean>();
    // Populated when user selects records for storage.
    private Map<String, Boolean> checkedVehicles = new HashMap<String, Boolean>();

    private UploadedFile uploadedFile = null;
    private String errorMsg;
    private final String RESOURCE_BUNDLE = "Lang";
    private final String SI = "si";
    private final String EN = "en";
    private final String LOCALE_COOKIE = "lang";
    private String locale = SI;

    private void loadVehiclesFromDb() {
        QueryResult qr = db.getVehicles();

        if (qr.error) {
            errorMsg = getMsg(Errors.DB);
            logger.error(errorMsg + " " + qr.errorMsg());
            return;
        }

        vehicles.addAll(qr.result);

        // Mark which vehicles came from database.
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
        ResourceBundle rb = ResourceBundle.getBundle(RESOURCE_BUNDLE, new Locale(locale));
        return rb.getString(which);
    }

    private void countVehicles() {
        numVehicles = vehicles.size();
        errorMsg = (numVehicles <= 0) ? getMsg(Errors.FILE_ERROR) : errorMsg;
    }

    private void start() {
        errorMsg = "";
        loadVehiclesFromDb();
        loadVehiclesFromFile();
        sortVehicles();
        countVehicles();
    }

    private void refresh() {
       try {
           FacesContext.getCurrentInstance().getExternalContext().redirect("/");
       } catch (Exception e) {
           logger.error("Could not redirect.");
       }
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

    private void setLocale(String arg_locale) {
        String in_locale;
        Map<String, Object> cookies = FacesContext.getCurrentInstance()
            .getExternalContext().getRequestCookieMap();
        if (!cookies.containsKey(LOCALE_COOKIE)) {
            FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale(arg_locale));
            return;
        }
        in_locale = ((Cookie)cookies.get(LOCALE_COOKIE)).getValue();
        locale = in_locale.equals(EN) ? EN : SI;
        FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale(locale));
    }

    public Naloga2() {
        setLocale(SI);
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

       refresh();
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
        errorMsg = "";
        removeVehiclesFromFile();
        loadVehiclesFromFile();
        sortVehicles();
        countVehicles();
    }

    public void changeLanguage() {
        locale = locale.equals(SI) ? EN : SI;
        Map<String, Object> cookieProps = new HashMap<String, Object>();
        // Remember language for 1 year.
        cookieProps.put("maxAge", new Integer(365 * 24 * 60 * 60));
        FacesContext.getCurrentInstance()
            .getExternalContext()
            .addResponseCookie(LOCALE_COOKIE, locale, cookieProps);

        refresh();
    }

    public Map<String, Boolean> getVehiclesInDb() {
        return vehiclesInDb;
    }

    public Map<String, Boolean> getCheckedVehicles() {
        return checkedVehicles;
    }

    public boolean getIsDataValid() {
        return (0 < numVehicles);
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
        return vehiclesInDb.values().contains(true);
    }

    public boolean getAnyInFile() {
        return vehiclesInDb.values().contains(false);
    }

    public String getLocale() {
        return locale;
    }
}
