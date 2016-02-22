/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServiceLayer;


import components.data.*;
import java.io.StringReader;
import java.util.List;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import Data.*;
import Business.*;

/**
 * REST Web Service
 *
 * @author Liz
 */
@Path("AppointmentService")
public class AppointmentService {

    
    @Context
    private UriInfo context;
    private static IComponentsData db;
    private DataAccess dac;
    private AppointmentBusiness apBiz;
    
    public AppointmentService()
    {
        
    }
    
    @Path("/Appointments")
    @PUT
    @Consumes({"text/xml","application/xml"})
    @Produces("application/xml")
    public String addAppointment(String apptXml)
    {
        String result = "";
        apBiz= new AppointmentBusiness(db);
        try{
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(apptXml)));
            doc.getDocumentElement().normalize();
            if(apBiz.validateXML(doc))
            {
               if(apBiz.checkPhlebByXML(doc))
                {
                   Appointment x = apBiz.createNewAppointment(doc);
                   db.addData(x);
                   result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><AppointmentList>"+
                           "<uri>"+this.context.getBaseUri().toString()+"AppointmentService/Appointments/"+x.getId()+"</uri></AppointmentList>";
                }else
               {
                   result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><AppointmentList>"+
                           "<error>ERROR: Appointment is not available.</error></AppointmentList>";
               }
            }else
            {
                result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><AppointmentList>"+
                           "<error>ERROR: Appointment is not available.</error></AppointmentList>";
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><AppointmentList>"+
                           "<error>"+e.getMessage()+"</error></AppointmentList>";
        }
        return result;
    }
    
    @GET
    @Produces("application/xml")
    public String getInfo()
    {
        db = new DB();
        db.initialLoad("LAMS");
      
        String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><AppointmentList><intro> Welcome to the LAMS Appointment Service</intro>"+
                        "<wadl>"+this.context.getBaseUri().toString()+"application.wadl</wadl></AppointmentList>";
        
        return result;
    }
  
    
    @Path("/Appointments")
    @GET
    @Produces("application/xml")
    public String getAppointments()
    {
        dac = new DataAccess(db);
        List<Object> appts = dac.getAppointments();
        String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<AppointmentList>\n";   
        for(Object appt : appts)
        {
            result += formatAppointmentXml(appt);
        }
        result += "</AppointmentList>";
        return result;
    }
    
    @Path("/Appointments/{id}")
    @GET
    @Produces("application/xml")
    @Consumes("text/plain")
    public String getAppointmentById(@PathParam("id") String id)
    {
        dac = new DataAccess(db);
        Appointment appt = (Appointment)dac.getObjectById("Appointment", ""+id);
        String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<AppointmentList>\n"+ formatAppointmentXml(appt) +"</AppointmentList>";   
        return result;
    }
    
    private String formatAppointmentXml(Object apptX)
    {
        Appointment appt = (Appointment)apptX;
        Patient patient = appt.getPatientid();
        Phlebotomist phleb = appt.getPhlebid();
        PSC psc = appt.getPscid();
        List<AppointmentLabTest> labTests = appt.getAppointmentLabTestCollection();
        String result =     "<appointment date=\""+appt.getApptdate()+"\" id=\""+appt.getId()+"\" time=\""+appt.getAppttime()+"\">\n"+
                                "<uri>[URI HERE]</uri>\n" +
                                "<patient id=\""+patient.getId()+"\">\n" +
                                    "<uri/>\n"+
                                    "<name>"+patient.getName()+"</name>\n"+
                                    "<address>"+patient.getAddress()+"</address>\n"+
                                    "<insurance>"+patient.getInsurance()+"</insurance>\n"+
                                    "<dob>"+patient.getDateofbirth()+"</dob>\n"+
                                "</patient>\n"+
                                "<phlebotomist id=\""+phleb.getId()+"\">\n"+
                                    "<uri/>\n"+
                                    "<name>"+phleb.getName()+"</name>\n"+
                                "</phlebotomist>\n"+
                                "<psc id=\""+psc.getId()+"\">\n"+
                                    "<uri/>\n"+
                                    "<name>"+psc.getName()+"</name>\n"+
                                "</psc>\n"+
                                "<allLabTests>\n";
        
        for(AppointmentLabTest test : labTests)
        {
            result += "<appointmentLabTest appointmentId=\""+appt.getId()+"\" dxcode=\""+test.getDiagnosis().getCode()+"\" labTestId=\""+test.getLabTest().getId()+"\">\n"+"<uri/>\n"+"</appointmentLabTest>\n";         
        }
        
        result += "</allLabTests>\n</appointment>\n";                     
        return result;
    }
  

    /**
     * PUT method for updating or creating an instance of AppointmentService
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @Consumes("application/xml")
    public void putXml(String content) {
    }
    
   
}
