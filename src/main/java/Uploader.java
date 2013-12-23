import java.io.IOException;
import java.io.PrintWriter;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.io.FilenameUtils;
import org.apache.myfaces.custom.fileupload.UploadedFile;

import org.slf4j.*;

public class Uploader {
    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Stores given file onto given path on disk.
     * @param uploadedFile file to store
     * @param outFilePath path to store the file to
     * @return true on success, false otherwise
     */
    public boolean storeUploadedFile(UploadedFile uploadedFile, String outFilePath) {
        try {
            String contentType = uploadedFile.getContentType();
            if (!contentType.equals("text/plain") &&
                !contentType.equals("text/csv")) {
                logger.error("Uploaded file is of wrong MIME type.");
                return false;
            }

            PrintWriter out = new PrintWriter(outFilePath);
            out.println(new String(uploadedFile.getBytes()));
            out.close();
            return true;
        } catch (Exception ex) {
            logger.error("Could not retrieve uploaded file: " + ex.getMessage());
            return false;
        }
    }
}