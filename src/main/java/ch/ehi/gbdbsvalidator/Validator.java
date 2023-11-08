package ch.ehi.gbdbsvalidator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.util.JAXBSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import ch.ehi.basics.settings.Settings;
import ch.ehi.basics.types.OutParam;
import ch.ehi.gbdbsvalidator.impl.FilterMapDisk;
import ch.ehi.gbdbsvalidator.impl.FilterMapHeap;
import ch.ehi.gbdbsvalidator.impl.GbdbsValidator;

public class Validator {
    public static final String soapNs = "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String gbdbs20Ns = "http://schemas.terravis.ch/GBDBS/2.0";
    public static final String gbdbsTypen20Ns = "http://schemas.terravis.ch/GBBasisTypen/2.0";
    public static final String gbdbsDatei20Ns = "http://schemas.terravis.ch/GBDBS-Datei/2.0";
    public static final String gbdbs21Ns = "http://schemas.geo.admin.ch/BJ/TGBV/GBDBS/2.1";
    public static final String gbdbsTypen21Ns = "http://schemas.geo.admin.ch/BJ/TGBV/GBBasisTypen/2.1";
    public static final String gbdbsDatei21Ns = "http://schemas.geo.admin.ch/BJ/TGBV/GBDBS-Datei/2.1";
    public static final String eigtypNS = "http://schemas.bfs.admin.ch/EIGTYP/1.0";
    public static final String validationResultNS = "http://schemas.bfs.admin.ch/ValidationResult/1.0";
    public static final Class JAXB_CONTEXT_PATH[]={
        ch.ehi.gbdbsvalidator.jaxb.gbdbsegvt._2_0.ObjectFactory.class,
        ch.ehi.gbdbsvalidator.jaxb.gbdbsegvt._2_1.ObjectFactory.class,
        ch.ehi.gbdbsvalidator.jaxb.gbdbs._2_0.ObjectFactory.class,
        ch.ehi.gbdbsvalidator.jaxb.gbdbs._2_1.ObjectFactory.class,
        ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.ObjectFactory.class,
        ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.ObjectFactory.class,
        ch.ehi.gbdbsvalidator.jaxb.gbdbsdatei._2_0.ObjectFactory.class,
        ch.ehi.gbdbsvalidator.jaxb.gbdbsdatei._2_1.ObjectFactory.class,
        ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.ext1.ObjectFactory.class,
        ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.ext2.ObjectFactory.class,
        ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.ext1.ObjectFactory.class,
        ch.ehi.gbdbsvalidator.jaxb.versioning._1_0.ObjectFactory.class,
        ch.ehi.gbdbsvalidator.jaxb.versioning._1_1.ObjectFactory.class,
        ch.ehi.gbdbsvalidator.jaxb.eigtyp._1_0.ObjectFactory.class,
        ch.ehi.gbdbsvalidator.jaxb.validationresult._1_0.ObjectFactory.class,
        };
    public static final Class JAXB_GBDBS21_CONTEXT_PATH[]={
            ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.ObjectFactory.class,
            ch.ehi.gbdbsvalidator.jaxb.gbdbsdatei._2_1.ObjectFactory.class,
            };
    public static final QName GBDBS_20 = new QName(gbdbsDatei20Ns,"GBDBS");
    public static final QName HEADERSECTION_20 = new QName(gbdbsDatei20Ns,"HEADERSECTION");
    public static final QName DATASECTION_20 = new QName(gbdbsDatei20Ns,"DATASECTION");
    public static final QName VOLLBESTAND_20 = new QName(gbdbsDatei20Ns,"Vollbestand");
    public static final QName GBDBS_21 = new QName(gbdbsDatei21Ns,"GBDBS");
    public static final QName HEADERSECTION_21 = new QName(gbdbsDatei21Ns,"HEADERSECTION");
    public static final QName DATASECTION_21 = new QName(gbdbsDatei21Ns,"DATASECTION");
    public static final QName VOLLBESTAND_21 = new QName(gbdbsDatei21Ns,"Vollbestand");
    public static final String MSG_VALIDATION_DONE = "...validation done";
    public static final String MSG_VALIDATION_FAILED = "...validation failed";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public enum Status {
         schwarz, // kein Resultat, technischer bzw. Software-Fehler
         rot,   // kein Resultat, Fehler muessen zuerst durch das GB korrigiert werden
         gelb, // Resultat wurde erstellt, Fehler sollten aber durch das GB korrigiert werden
         gruen // kein Fehler, nur Info Meldungen
         };

         private Unmarshaller um=null;
         private XMLInputFactory xmlif=null;
         private JAXBContext jaxbContext =null;
    public Validator() {
        xmlif = XMLInputFactory.newInstance();
          try {
              jaxbContext = JAXBContext
              .newInstance(JAXB_CONTEXT_PATH);
              um = jaxbContext.createUnmarshaller();
          } catch (JAXBException e) {
              throw new IllegalStateException(e);
          }
        
    }
    public boolean validate(File gbdbsFilename, Settings settings) {

        boolean ok=false;
        FilterMapDisk diskStore=null;
        InputStream gbdbsFile=null;
        OutputStream xmllogFile=null;
        Logger logger =null;
        try {
            String DATE_FORMAT = "yyyy-MM-dd HH:mm";
            SimpleDateFormat dateFormat = new java.text.SimpleDateFormat(DATE_FORMAT);
            
            String logFilename=settings.getValue(Main.SETTING_LOGFILE);
            String xmllogFilename=settings.getValue(Main.SETTING_XMLLOG);
            String diskStoreFilename=settings.getValue(Main.SETTING_DISKFILE);
            
            if(xmllogFilename!=null) {
                xmllogFile=new java.io.BufferedOutputStream(new java.io.FileOutputStream(xmllogFilename));
            }else {
                xmllogFile=null;
            }
            logger=Logger.getInstance();
            if(xmllogFile!=null) {
                logger.setXmllog(xmllogFile);
            }
            if(logFilename!=null) {
                logger.setLogfile(new File(logFilename));
            }
            logger.logGruen(Logger.ALLGEMEINE_MELDUNG,"User <"+java.lang.System.getProperty("user.name")+">");
            logger.logGruen(Logger.ALLGEMEINE_MELDUNG,"Start "+dateFormat.format(new java.util.Date()));
            logger.logGruen(Logger.ALLGEMEINE_MELDUNG,"maxMemory "+java.lang.Runtime.getRuntime().maxMemory()/1024L+" KB");
            
            gbdbsFile=new java.io.BufferedInputStream(new java.io.FileInputStream(gbdbsFilename));
            
            OutParam<Status> status=new OutParam<Status>();
            
            FilterMap tempStore=new FilterMapHeap();
            if(diskStoreFilename!=null) {
                tempStore=diskStore=new FilterMapDisk(new java.io.File(diskStoreFilename));
            }
            filter(gbdbsFilename.getPath(),gbdbsFile, status,logger, tempStore);
            
            
            ok=(status.value.equals(Status.gruen)?true:false);
        }catch(Exception e) {
            Logger.getInstance().logSchwarz("failed",e);
            ok=false;
        }finally {
            if(diskStore!=null) {
                diskStore.close();
            }
            if(gbdbsFile!=null) {
                try {
                    gbdbsFile.close();
                } catch (IOException e) {
                }
            }
            if(logger!=null && !logger.isClosed()) {
                logger.close();
            }
            if(xmllogFile!=null) {
                try {
                    xmllogFile.flush();
                    xmllogFile.close();
                } catch (IOException e) {
                }
            }
            
        }
        
        return ok;
    }
    private class ValidationEventHandler implements javax.xml.bind.ValidationEventHandler {
        // import javax.xml.bind.ValidationEvent;
        // import javax.xml.bind.ValidationEventHandler;
        private OutParam<Status> status;
        private Logger logger;
        public ValidationEventHandler(OutParam<Status> status,Logger logger) {
            this.status=status;
            this.logger=logger;
        }
        @Override
        public boolean handleEvent(javax.xml.bind.ValidationEvent event) {
            status.value=Status.schwarz;
            ValidationEventLocator loc = event.getLocator();
            String locTxt="";
            if(loc!=null) {
                int colNr=loc.getColumnNumber();
                int linNr=loc.getLineNumber();
                if(linNr!=-1 && colNr!=-1) {
                    locTxt=linNr+","+colNr+": ";
                }
            }
            Throwable ex=null; event.getLinkedException();
            if(ex!=null) {
                logger.logSchwarz(Logger.ID_0_9_XSD_VALIDIERUNG,event.getMessage(),ex);
            }else {
                logger.logSchwarz(Logger.ID_0_9_XSD_VALIDIERUNG,locTxt+event.getMessage());
            }
            return true;
        }
    }
    private void filter(
            String gbdbsDateiname,
            java.io.InputStream gbdbsFile, // schon xsd validiert
            OutParam<Status> status,
            Logger logger,
            FilterMap tempStore)
    {
        if(status==null) {
            status=new OutParam<Status>();
        }
        status.value=Status.gruen;
        logger.logGruen(Logger.ALLGEMEINE_MELDUNG, "GBDBS-Datei "+gbdbsDateiname);
        logger.logGruen(Logger.ID_0_1_FILTERVERSION, "Filterversion "+Main.getVersion());
        javax.xml.validation.Schema xmlSchema=null;
        try {
            String DATE_FORMAT = "yyyy-MM-dd HH:mm";
            SimpleDateFormat dateFormat = new java.text.SimpleDateFormat(DATE_FORMAT);
            XMLEventReader xmlr = xmlif
                    .createXMLEventReader(gbdbsFile);
            XMLEvent ev=null;
            try {
                xmlSchema=loadXmlSchema();
                javax.xml.validation.Validator xsdValidator = xmlSchema.newValidator();
                um.setSchema(xmlSchema);
                um.setEventHandler(new ValidationEventHandler(status, logger));
                
                // move to the root element and check its name.
                do{
                    ev=xmlr.nextEvent();
                }while(!(ev instanceof StartElement));
                if(ev.asStartElement().getName().equals(GBDBS_20) || ev.asStartElement().getName().equals(GBDBS_21)) {
                    boolean isGbdbs20=ev.asStartElement().getName().equals(GBDBS_20);
                    GbdbsValidator gbdbsValidator=null;
                    LocalDate stichtag=LocalDate.now();
                    gbdbsValidator=new GbdbsValidator(logger,status,stichtag,tempStore);
                    ev=xmlr.nextEvent();
                    ev=skipSpacesAndGetNextEvent(xmlr, ev);
                    if(ev.asStartElement().getName().equals(HEADERSECTION_20) || ev.asStartElement().getName().equals(HEADERSECTION_21)) {
                        skip(xmlr);
                    }
                    ev=xmlr.nextEvent();
                    ev=skipSpacesAndGetNextEvent(xmlr, ev);
                    if(ev.asStartElement().getName().equals(DATASECTION_20) || ev.asStartElement().getName().equals(DATASECTION_21)) {
                        ev=xmlr.nextEvent();
                        ev=skipSpacesAndGetNextEvent(xmlr, ev);
                        if(ev.asStartElement().getName().equals(VOLLBESTAND_20) || ev.asStartElement().getName().equals(VOLLBESTAND_21)) {
                            skipSpaces(xmlr);
                            while(xmlr.peek().isStartElement()) {
                                JAXBElement ele=(JAXBElement) um.unmarshal(xmlr);
                                if(gbdbsValidator!=null) {
                                    try {
                                        gbdbsValidator.convertGbdbsEle(ele);
                                    }catch(Exception ex) {
                                        gbdbsValidator=null;
                                        if(logger!=null) {
                                            logger.logSchwarz("Technischer Fehler",ex);
                                        }
                                        if(status!=null) {
                                            status.value=Status.rot;
                                        }
                                    }
                                }
                                skipSpaces(xmlr);
                            }
                        }
                    }
                    if(gbdbsValidator!=null) {
                        try {
                            gbdbsValidator.finishFilter();
                        }catch(Exception ex) {
                            gbdbsValidator=null;
                            if(logger!=null) {
                                logger.logSchwarz("Technischer Fehler",ex);
                            }
                            if(status!=null) {
                                status.value=Status.rot;
                            }
                        }
                    }
                }else {
                    throw new Exception("keine GBDBS Datei ("+ev.asStartElement().getName()+")");
                }
            }catch(Exception ex) {
                if(logger!=null) {
                    logger.logSchwarz("Technischer Fehler",ex);
                }
                if(status!=null) {
                    status.value=Status.rot;
                }
            }finally {
                if(logger!=null) {
                    logger.logGruen(Logger.ID_0_2_AUSFUEHRUNGSDATUM, "Ausf\u00fchrungsdatum "+dateFormat.format(new java.util.Date()));
                    logger.logGruen(Logger.ID_0_4_STATUS_VALIDIERUNG, "Status Validierung "+status.value);
                }

                xmlr.close();
            }
        } catch (XMLStreamException e) {
            throw new IllegalStateException(e);
        }
        return;
    }
    
    private XMLEvent skipSpacesAndGetNextEvent(XMLEventReader reader,XMLEvent event) throws XMLStreamException{
        while(event.isCharacters() || event.getEventType()==XMLEvent.COMMENT){
            if(event.isCharacters() && event.getEventType()!=XMLEvent.COMMENT) {
                Characters characters = (Characters) event;
                if(!characters.isWhiteSpace()){
                    throw new IllegalStateException("unexpected non-whitespace in XML");
                }
            }
            event=reader.nextEvent();
        }
        return event;
    }
    private void skipSpaces(XMLEventReader reader) throws XMLStreamException{
        XMLEvent event=reader.peek();
        while(event.isCharacters() || event.getEventType()==XMLEvent.COMMENT){
            if(event.isCharacters() && event.getEventType()!=XMLEvent.COMMENT) {
                Characters characters = (Characters) event;
                if(!characters.isWhiteSpace()){
                    throw new IllegalStateException("unexpected non-whitespace in XML");
                }
            }
            reader.nextEvent();
            event=reader.peek();
        }
    }
    static void skip(XMLEventReader parser) throws XMLStreamException {
        int inHeader = 0;
        for (XMLEvent event = parser.nextEvent(); !event.isEndDocument(); event = parser
                .nextEvent()) {
            switch (event.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                inHeader++;
                break;
            case XMLStreamConstants.END_ELEMENT:
                inHeader--;
                break;
            } // end switch
            if (inHeader < 0) {
                break;
            }
        } // end while
    }
    private javax.xml.validation.Schema loadXmlSchema() throws Exception{
        // load a WXS schema, represented by a Schema instance
        javax.xml.validation.Schema schema=null;
        javax.xml.transform.Source schemaFiles[] =  null;
        javax.xml.transform.Source schemaFiles2[] =  {
                    new StreamSource(getClass().getResource("/eCH-0007/3/eCH-0007-3-0.xsd").toString()),
                    new StreamSource(getClass().getResource("/eCH-0007/4/eCH-0007-4-0.xsd").toString()),
                    new StreamSource(getClass().getResource("/eCH-0007/5/eCH-0007-5-0.xsd").toString()),
                    new StreamSource(getClass().getResource("/eCH-0007/6/eCH-0007-6-0.xsd").toString()),
                    new StreamSource(getClass().getResource("/eCH-0006/2/eCH-0006-2-0.xsd").toString()),
                    new StreamSource(getClass().getResource("/eCH-0008/2/eCH-0008-2-0.xsd").toString()),
                    new StreamSource(getClass().getResource("/eCH-0008/3/eCH-0008-3-0.xsd").toString()),
                    new StreamSource(getClass().getResource("/eCH-0010/3/eCH-0010-3-1.xsd").toString()),
                    new StreamSource(getClass().getResource("/eCH-0010/5/eCH-0010-5-0.xsd").toString()),
                    new StreamSource(getClass().getResource("/eCH-0010/6/eCH-0010-6-0.xsd").toString()),
                    new StreamSource(getClass().getResource("/eCH-0044/1/eCH-0044-1-1.xsd").toString()),
                    new StreamSource(getClass().getResource("/eCH-0044/4/eCH-0044-4-0.xsd").toString()),
                    new StreamSource(getClass().getResource("/eCH-0011/4/eCH-0011-4-0.xsd").toString()),
                    new StreamSource(getClass().getResource("/eCH-0011/8/eCH-0011-8-0.xsd").toString()),
                    new StreamSource(getClass().getResource("/eCH-0046/1/eCH-0046-1-0.xsd").toString()),
                    new StreamSource(getClass().getResource("/eCH-0046/3/eCH-0046-3-0.xsd").toString()),
                    new StreamSource(getClass().getResource("/eCH-0097/1/eCH-0097-1-0.xsd").toString()),
                    new StreamSource(getClass().getResource("/eCH-0097/2/eCH-0097-2-0.xsd").toString()),
                    new StreamSource(getClass().getResource("/eCH-0098/1/eCH-0098-1-0.xsd").toString()),
                    new StreamSource(getClass().getResource("/eCH-0098/3/eCH-0098-3-0.xsd").toString()),
                    new StreamSource(getClass().getResource("/eCH-0058/4/eCH-0058-4-0.xsd").toString()),
                    new StreamSource(getClass().getResource("/eCH-0058/5/eCH-0058-5-0.xsd").toString()),
                    new StreamSource(getClass().getResource("/eCH-0135/1/eCH-0135-1-0.xsd").toString()),
                    new StreamSource(getClass().getResource("/GBBasisId/2.0/GBBasisId.xsd").toString()),
                    new StreamSource(getClass().getResource("/GBBasisId/2.1/GBBasisId.xsd").toString()),
                    new StreamSource(getClass().getResource("/GBBasisTypen/2.0/GBBasisTypen.xsd").toString()),
                    new StreamSource(getClass().getResource("/GBBasisTypen/2.1/GBBasisTypen.xsd").toString()),
                    new StreamSource(getClass().getResource("/GBDBS/2.0/GBDBS.xsd").toString()),
                    new StreamSource(getClass().getResource("/GBDBS/2.1/GBDBS.xsd").toString()),
                    new StreamSource(getClass().getResource("/GBDBS-eGVT/2.0/GBDBS-eGVT.xsd").toString()),
                    new StreamSource(getClass().getResource("/GBDBS-eGVT/2.1/GBDBS-eGVT.xsd").toString()),
                    new StreamSource(getClass().getResource("/GBDBS-Datei/2.0/GBDBS-Datei.xsd").toString()),
                    new StreamSource(getClass().getResource("/GBDBS-Datei/2.1/GBDBS-Datei.xsd").toString()),
                    new StreamSource(getClass().getResource("/TerravisContact/1.0/TerravisContact.xsd").toString()),
                    new StreamSource(getClass().getResource("/TerravisContact/1.1/TerravisContact.xsd").toString()),
                    new StreamSource(getClass().getResource("/TerravisHeader/1.0/TerravisHeader.xsd").toString()),
                    new StreamSource(getClass().getResource("/Versioning/1.0/Versioning.xsd").toString()),
                    new StreamSource(getClass().getResource("/Versioning/1.1/Versioning.xsd").toString()),
                    new StreamSource(getClass().getResource("/xmldsig/2000-09/xmldsig-core-schema.xsd").toString()),
                    new StreamSource(getClass().getResource("/xmlmime/2005-05/xmlmime.xsd").toString()),
                    new StreamSource(getClass().getResource("/EIGTYP/1.0/EIGTYP.xsd").toString()),
                    new StreamSource(getClass().getResource("/ValidationResult/1.0/ValidationResult.xsd").toString()),
                    };
            schemaFiles=schemaFiles2;
        SchemaFactory factory = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schema = factory.newSchema(schemaFiles);
        return schema;
    }
}
