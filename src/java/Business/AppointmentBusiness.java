package Business;

import java.util.*;
import components.data.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import Data.*;

/**
 *
 * @author Liz
 */
public class AppointmentBusiness {
    
    private IComponentsData db;
    private DataAccess dac;
    public AppointmentBusiness(IComponentsData db)
    {
        dac = new DataAccess(db);
      
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
    
    
    
    public boolean validateXML(Document doc)
    {  
        boolean valid = false;
   
        NodeList appt = doc.getElementsByTagName("appointment");
        if(appt.getLength() > 1)
        {
            return valid;
        }
        
        NodeList nodes = appt.item(0).getChildNodes();
        
        if(nodes.item(0).getNodeName().equals("date"))
        {
            String text = nodes.item(0).getTextContent(); 
            if(isValidInput("date", text) && nodes.item(1).getNodeName().equals("time"))
            {
                text = nodes.item(1).getTextContent(); 
                if(isValidInput("time", text) && nodes.item(2).getNodeName().equals("patientId"))
                {
                    text = nodes.item(2).getTextContent(); 
                    if(isValidInput("patientId", text) && nodes.item(3).getNodeName().equals("physicianId"))
                    {
                        text = nodes.item(3).getTextContent(); 
                        if(isValidInput("physicianId", text)&&nodes.item(4).getNodeName().equals("pscId"))
                        {
                           text = nodes.item(4).getTextContent();
                           if(isValidInput("pscId", text)&&nodes.item(5).getNodeName().equals("phlebotomistId"))
                           {
                                text = nodes.item(5).getTextContent();
                                if(isValidInput("phlebotomistId", text)&&nodes.item(6).getNodeName().equals("labTests"))
                                {
                                    NodeList labtests = nodes.item(6).getChildNodes();
                                    for(int i=0; i<labtests.getLength(); i++)
                                    {
                                        NamedNodeMap nnm = labtests.item(i).getAttributes();
                                        String code = nnm.getNamedItem("dxcode").getNodeValue();
                                        String testId = nnm.getNamedItem("id").getNodeValue();
                                        if(isValidInput("labTestId",testId)  && isValidInput("dxcode",code))
                                        {
                                            valid = true;
                                        }
                                        else{
                                            valid = false;
                                            break;
                                        } 
                                     
                                    }
                                }
                           }
                        }
                    }
                }         
            }
        }
        return valid;
    }
    
    public boolean isValidInput(String attribute, Object value)
    {
        boolean result = false;
        
        switch (attribute){
            case "date": result = checkDate(""+value);
                break;
            case "time": result = checkTime(""+value);
                break;
            case "patientId": result = checkId("Patient",""+value);
                break;
            case "physicianId": result = checkId("Physician",""+value);
                break;
            case "pscId": result = checkId("PSC",""+value);
                break;
            case "phlebotomistId": result = checkId("Phlebotomist", ""+value);
                break;
            case "labTestId": result = checkId("LabTest", ""+value);
                break;
            case "dxcode": result = dac.checkDxCode(""+value);
                break;
        }       
        return result;
    }
    
    public boolean checkDate(String date)
    {
        //check that the date is in yyyy-MM-dd format
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
    
    public boolean checkTime(String time)
    {
        boolean result = false;
        String hourFormat = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
        Pattern pattern = Pattern.compile(hourFormat);
        Matcher matcher = pattern.matcher(time);
        //check that the input is in hh:mm format
        if(matcher.matches())
        {
            String[] values = time.split(":");
            //check if time is between 08:00 and 17:00 
            int hour = Integer.parseInt(values[0]);
            int min = Integer.parseInt(values[1]);
            if(hour > 7 && hour < 17)
            {
                if(min == 0 || min == 15 || min == 30 || min == 45)
                {
                    result = true; 
                }  
            }   
        }            
        return result;
    }
    
    public boolean checkId(String table, String id)
    {
        //verify that the patient exists
        if(dac.getObjectById(table,id) != null)
        {
            return true;
        }
        return false;
    }
    
    public boolean checkPhlebByXML(Document doc)
    {
        NodeList nodes = doc.getElementsByTagName("appointment").item(0).getChildNodes();
        String phlebId = nodes.item(5).getTextContent();
        String pscId = nodes.item(4).getTextContent();
        String date = nodes.item(0).getTextContent();
        String time = nodes.item(1).getTextContent();
        
        return isPhlebAvailable(phlebId, pscId, time, date);
    }
    
    public Appointment createNewAppointment(Document doc)
    {
        NodeList nodes = doc.getElementsByTagName("appointment").item(0).getChildNodes();
        String date = nodes.item(0).getTextContent();
        String time = nodes.item(1).getTextContent();
        String phlebId = nodes.item(5).getTextContent();
        String pscId = nodes.item(4).getTextContent();
        String patientId = nodes.item(2).getTextContent();
        
        Appointment newAppt = new Appointment("900");
        newAppt.setApptdate(java.sql.Date.valueOf(date));
        newAppt.setAppttime(java.sql.Time.valueOf(time+":00"));
        //extra steps here due to persistence api and join, need to create objects in list
        List<AppointmentLabTest> tests = new ArrayList<AppointmentLabTest>();
        NodeList labtests = nodes.item(6).getChildNodes();
        for(int i=0; i<labtests.getLength(); i++)
        {
            NamedNodeMap nnm = labtests.item(i).getAttributes();
            String code = nnm.getNamedItem("dxcode").getNodeValue();
            String testId = nnm.getNamedItem("id").getNodeValue();
            AppointmentLabTest test = new AppointmentLabTest("900",testId,code);  
            test.setDiagnosis(dac.getDiagnosis(code));
            test.setLabTest((LabTest)dac.getObjectById("LabTest",testId));
            tests.add(test);
        }
         
        newAppt.setAppointmentLabTestCollection(tests);
        newAppt.setPatientid((Patient)dac.getObjectById("Patient", patientId));
        newAppt.setPhlebid((Phlebotomist)dac.getObjectById("Phlebotomist", phlebId));
        newAppt.setPscid((PSC)dac.getObjectById("PSC", pscId));
        
        return newAppt;
    }
    
    public boolean isPhlebAvailable(String phlebId, String pscId, String time, String date)
    {
        String fifteenBefore;
        String fifteenAfter;
        String[] values = time.split(":");
        int hour = Integer.parseInt(values[0]);
        int min = Integer.parseInt(values[1]);
        
        //see if phleb is avail at that time
        List<Object> appts_O = dac.getApptsWithDetails(phlebId, time+":00", date);
        if(appts_O != null)
        {
            return false;
        }
        //see where phleb is 15 min before 
        if(min == 0)
        {
            //decrease time by 15 minutes
            hour--;
            min = 45;
            fifteenBefore = hour+":"+min+":00"; 
            List<Object> appts = dac.getApptsWithDetails(phlebId, fifteenBefore, date);
            if(appts != null)
            {
                Appointment ap = (Appointment)appts.get(0);
                String psc = ap.getPscid().getId();
                if(!psc.equals(pscId))
                {
                    return false;
                }
            }
        }
        else{
           min = min-15;
           if(min < 10)
           {
               fifteenBefore = hour+":0"+min+":00";
           }
           else
           {
               fifteenBefore = hour+":"+min+":00";
           }
           
           List<Object> appts = dac.getApptsWithDetails(phlebId, fifteenBefore, date);
           if(appts != null)
            {
                Appointment ap = (Appointment)appts.get(0);
                String psc = ap.getPscid().getId();
                if(!psc.equals(pscId))
                {
                    return false;
                }
            }
        }
        hour = Integer.parseInt(values[0]);
        min = Integer.parseInt(values[1]);
        
        if(min == 45)
        {
            hour++;
            min = 0;
            fifteenAfter = hour+":0"+min+":00";
            List<Object> appts = dac.getApptsWithDetails(phlebId, fifteenAfter, date);
            if(appts != null)
             {
                 Appointment ap = (Appointment)appts.get(0);
                 String psc = ap.getPscid().getId();
                 if(!psc.equals(pscId))
                 {
                     return false;
                 }
             }
        }
        else{
           min = min+15;
           if(min < 10)
           {
               fifteenAfter = hour+":0"+min+":00";
           }
           else
           {
               fifteenAfter = hour+":"+min+":00";
           }
           
            List<Object> appts = dac.getApptsWithDetails(phlebId, fifteenAfter, date);
            if(appts != null)
             {
                 Appointment ap = (Appointment)appts.get(0);
                 String psc = ap.getPscid().getId();
                 if(!psc.equals(pscId))
                 {
                     return false;
                 }
             }
        }
        return true;
    }
    
}
