package Data;

import java.util.*;
import components.data.*;
/**
 * @author Liz
 */
public class DataAccess {
    
    private IComponentsData db;
    public DataAccess(IComponentsData db)
    {
        this.db = db;
    }
    
    public List<Object> getAppointments()
    {
        List<Object> appts = db.getData("Appointment", "");
        return appts;
    }
    
    public Object getObjectById(String type, String id)
    {
        List<Object> appt = db.getData(type, "id='"+id+"'");
        if(appt.size() > 0){
           return appt.get(0); 
        }
        else
        {
            return null;
        }
        
    }
  
    public boolean checkDxCode(String code)
    {
        List<Object> dx = db.getData("Diagnosis", "code='"+code+"'");
        if(dx.size() > 0){
           return true;
        }
        else
        {
            return false;
        }
    }
    
    public Diagnosis getDiagnosis(String code)
    {
       List<Object> dx = db.getData("Diagnosis", "code='"+code+"'");
        if(dx.size() > 0){
           return (Diagnosis)dx.get(0);
        }
        else
        {
            return null;
        } 
    }
    
    public  List<Object> getApptsWithDetails(String phlebId, String time, String date)
    {
        List<Object> objs = db.getData("Appointment", "apptdate='"+date+"' AND phlebid='"+phlebId+"' AND appttime='"+time+"'");
        if(objs.size() > 0){
           return objs;
        }
        else
        {
            return null;
        }
    }
    
    
    
}
