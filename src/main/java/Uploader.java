import java.io.IOException;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.io.FilenameUtils;
import org.apache.myfaces.custom.fileupload.UploadedFile;

import org.slf4j.*;

public class Uploader {
    Logger logger = LoggerFactory.getLogger(getClass());

    public boolean storeUploadedFile(UploadedFile uploadedFile) {
        byte[] bytes = null;
        try {
            String fileName = FilenameUtils.getName(uploadedFile.getName());

System.out.println("filename " + fileName);

            String contentType = uploadedFile.getContentType();

            if ("text/plain" != contentType) {
                logger.error("Uploaded file is of wrong MIME type.");
                return false;
            }

            bytes = uploadedFile.getBytes();
            System.out.println("content " + new String(bytes));
            return true;
        } catch (Exception ex) {
            logger.error("Could not retrieve uploaded file: " + ex.getMessage());
            return false;
        }
            // FacesContext.getCurrentInstance().addMessage(null, 
            //                                              new FacesMessage(String.format("File '%s' of type '%s' successfully uploaded!", fileName, contentType)));
    }

    // public UploadedFile getUploadedFile() {
    //     return uploadedFile;
    // }

    // public void setUploadedFile(UploadedFile uploadedFile) {
    //     System.out.println("setting uploaded file " + uploadedFile);
    //     this.uploadedFile = uploadedFile;
    // }

}