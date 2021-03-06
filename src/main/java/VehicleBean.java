import java.security.MessageDigest;

public class VehicleBean {
    private Integer year;
    private String make;
    private String model;
    private String comment;
    private Double price;
    private String hash;

    public VehicleBean() {}

    public VehicleBean(final Integer year,
                       final String make,
                       final String model,
                       final String comment,
                       final Double price) {
        this.year = year;
        this.make = make;
        this.model = model;
        this.comment = comment;
        this.price = price;
    }

    public int getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof VehicleBean)) {
            return false;
        }
        
        VehicleBean other = (VehicleBean) obj;

        if ((year == null) && (other.year != null)) {
            return false;
        }
        if (!year.equals(other.year)) {
            return false;
        }

        if ((make == null) && (other.make != null)) {
            return false;
        }
        if (!make.equals(other.make)) {
            return false;
        }

        if ((model == null) && (other.model != null)) {
            return false;
        }
        if (!model.equals(other.model)) {
            return false;
        }

        if ((comment == null) && (other.comment != null)) {
            return false;
        }
        if (!comment.equals(other.comment)) {
            return false;
        }

        if ((price == null) && (other.price != null)) {
            return false;
        }
        if (!price.equals(other.price)) {
            return false;
        }

        return true;
    }

    public String toString() {
        return String.format("VehicleBean [year=%s, make=%s, model=%s, comment=%s, price=%s]",
                             year, make, model, comment, price);
    }

    /**
     * Serializes, hashes and stores the hash of the current instance.
     */
    public void setHash() {
        StringBuffer sb = new StringBuffer();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(this.toString().getBytes());
            byte byteData[] = md.digest();
 
            //convert the byte to hex format method 1
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }
            this.hash = sb.toString();
        } catch (Exception ex) {}
    }

    public String getHash() {
        return hash;
    }
}