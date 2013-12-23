import org.slf4j.*;

import java.util.*;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.supercsv.io.*;
import org.supercsv.prefs.*;
import org.supercsv.cellprocessor.*;
import org.supercsv.util.CsvContext;
import org.supercsv.cellprocessor.constraint.*;
import org.supercsv.cellprocessor.ift.*;
import org.supercsv.exception.SuperCsvCellProcessorException;

public class CsvReader {
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Processes given year string into integer.
     * Valid year is within given (min, max) limits.
     */
    private static class ParseYear extends CellProcessorAdaptor {
        static Integer min;
        static Integer max;
        public ParseYear() {
            super();
        }
        public ParseYear(CellProcessor next) {
            super(next);
        }
        public ParseYear(Integer min_val, Integer max_val) {
            min = min_val;
            max = max_val;
        }
        public Object execute(Object value, CsvContext context) {
            Integer year = Integer.valueOf((String) value);
            validateInputNotNull(year, context);
            if ((min < year) &&
                (year <= max)) {
                return next.execute(year, context);
            }
                
            throw new SuperCsvCellProcessorException(
                String.format("Could not parse '%s' as a year.", (String) value), context, this);
        }
    }

    /**
     * Returns processors for parsing vehicle CSV file.
     * @return processors for parsing vehicle CSV file.
     */
    private static CellProcessor[] getProcessors() {
        final CellProcessor[] processors = new CellProcessor[] { 
            new ParseYear(1800,
                          Calendar.getInstance().get(Calendar.YEAR)), // Year
            new NotNull(new StrMinMax(1, 20)), // Make
            new NotNull(new StrMinMax(1, 50)), // Model
            new Optional(new StrMinMax(0, 1000)), // Comment
            new DMinMax(0, 10000000) // Price
        };
        return processors;
    }

    /**
     * Returns array of vehicle objects parsed from file given
     * by the file path.
     * @param filePath path to the CSV file with vehicle records
     * @return         array of vehicle objects
     */
    public ArrayList<VehicleBean> read(String filePath) throws Exception {
        ICsvBeanReader beanReader = null;
        final String[] header;
        final CellProcessor[] processors;
        VehicleBean vehicle;
        boolean continueReading = true;
        ArrayList<VehicleBean> vehicles = new ArrayList<VehicleBean>();

        beanReader = new CsvBeanReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"),
                                       CsvPreference.STANDARD_PREFERENCE);
        header = beanReader.getHeader(true);

        if (null == header) {
            beanReader.close();
            return vehicles;
        }
                
        processors = getProcessors();
        while (continueReading) {
            try {
                vehicle = beanReader.read(VehicleBean.class, header, processors);
                continueReading = (vehicle != null);

                if (null != vehicle) {
                    vehicle.setHash();
                    vehicles.add(vehicle);
                }

            } catch (org.supercsv.exception.SuperCsvReflectionException ex) {
                logger.error("CSV reader: " + ex.getMessage());
                continueReading = false;
            } catch (Exception e) {
                logger.warn("CSV reader: " + e.getMessage());
            }
        }

        beanReader.close();
        return vehicles;
    }
}