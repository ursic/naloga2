import java.sql.*;
import javax.faces.bean.*;
import org.slf4j.*;
import java.io.File;
import java.io.FileReader;
import org.supercsv.io.*;
import org.supercsv.prefs.*;
import org.supercsv.cellprocessor.*;
import org.supercsv.cellprocessor.constraint.*;
import org.supercsv.cellprocessor.ift.*;

@ManagedBean
public class Naloga2 {
    Logger logger = LoggerFactory.getLogger(getClass());
    private boolean isDataValid = false;
    private final String filePath = System.getProperty("java.io.tmpdir") + File.separator + "data.txt";

    public Naloga2() {
        logger.info("path " + filePath);
        loadData();

        try {
            readWithCsvBeanReader();
        } catch (Exception e) {
            logger.info("Could not read CSV file: " + e.getMessage());
        }
    }

    private void loadData() {
        File f = new File(filePath);
        if (!f.exists() || !f.canRead()) return;
        
        isDataValid = true;
    }
    
    public boolean getIsDataValid() {
        return isDataValid;
    }

    public void insert() {
        DB db = new DB();
        QueryResult qr;
        db.createTable();
        qr = db.insert("INSERT INTO vehicles (YEAR) VALUES (1988)");
        if (qr.error) {
            logger.error("Could not insert: " + qr.result);
        }
    }
  
    public String someActionControllerMethod() {

        logger.info("path " + System.getProperty("java.io.tmpdir"));

        insert();

        return("page-b");  // Means to go to page-b.xhtml (since condition is not mapped in faces-config.xml)
    }
  
    public String someOtherActionControllerMethod() {
        return("index");  // Means to go to page-a.xhtml (since condition is not mapped in faces-config.xml)
    }

    private static CellProcessor[] getProcessors() {
        final CellProcessor[] processors = new CellProcessor[] { 
            new ParseInt(),     // Year
            new NotNull(),      // Make
            new NotNull(),      // Model
            new Optional(new NotNull()), // Comment
            new ParseDouble()    // Price
        };
        return processors;
    }

    private void readWithCsvBeanReader() throws Exception {
        ICsvBeanReader beanReader = null;
//        try {
            beanReader = new CsvBeanReader(new FileReader(filePath), CsvPreference.STANDARD_PREFERENCE);
                
            // the header elements are used to map the values to the bean (names must match)
            final String[] header = beanReader.getHeader(true);
//            final CellProcessor[] processors = getProcessors();
                
            VehicleBean vehicle;
//            while( (customer = beanReader.read(CustomerBean.class, header, processors)) != null ) {
            while ((vehicle = beanReader.read(VehicleBean.class, header)) != null) {
                System.out.println(String.format("year=%s, make=%s, model=%s, comment=%s, price=%s",
                                                 beanReader.getLineNumber(),
                                                 beanReader.getRowNumber(),
                                                 vehicle));
            }
        // } catch (Exception e) {
        //     logger.info("Could not read the CSV file: " + e.getMessage());
        // }
//        finally {
            if (beanReader != null) {
                beanReader.close();
            }
//      }
    }
}
