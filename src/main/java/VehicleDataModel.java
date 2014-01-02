import java.util.List;  
import javax.faces.model.ListDataModel;
import org.primefaces.model.SelectableDataModel;
  
public class VehicleDataModel extends ListDataModel<VehicleBean> implements SelectableDataModel<VehicleBean> {
  
    public VehicleDataModel() {}  
  
    public VehicleDataModel(List<VehicleBean> data) {  
        super(data);  
    }  
      
    @Override  
    public VehicleBean getRowData(String rowKey) {  
        List<VehicleBean> vehicles = (List<VehicleBean>) getWrappedData();  
          
        for (VehicleBean vehicle : vehicles) {
            if (vehicle.getModel().equals(rowKey))  
                return vehicle;  
        }  
          
        return null;  
    }  
  
    @Override  
    public Object getRowKey(VehicleBean vehicle) {  
        return vehicle.getModel();  
    }  
}