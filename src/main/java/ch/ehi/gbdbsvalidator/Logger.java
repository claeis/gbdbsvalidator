package ch.ehi.gbdbsvalidator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import ch.ehi.gbdbsvalidator.Validator.Status;
import ch.ehi.gbdbsvalidator.gui.LogListener;

public class Logger {
    
    public static final String ALLGEMEINE_MELDUNG="0.0";
    public static final String ID_0_1_FILTERVERSION = "0.1";
    public static final String ID_0_2_AUSFUEHRUNGSDATUM = "0.2";
    public static final String ID_0_3_STICHTAG = "0.3";
    public static final String ID_0_4_STATUS_VALIDIERUNG = "0.4";
    public static final String ID_0_5_AUSWERTUNGSJAHR = "0.5";
    public static final String ID_0_8_GB_AMT = "0.8";
    public static final String ID_0_9_XSD_VALIDIERUNG = "0.9";
    public static final String ID_0_10_TECHNISCHER_FEHLER = "0.10";
    public static final String ID_1_1_FEHLENDE_ID = "1.1";
    public static final String ID_1_2_ID_HAT_FALSCHES_FORMAT = "1.2";
    public static final String ID_1_3_KEIN_GRUNDSTUECK_ODER_PERSON_MIT_DIESER_ID = "1.3";
    public static final String ID_1_4_ID_NICHT_EINDEUTIG = "1.4";
    public static final String ID_1_5_FEHLENDE_EGRID = "1.5";
    public static final String ID_1_6_FEHLENDE_EREID = "1.6";
    public static final String ID_1_7_FEHLENDE_EGBPID = "1.7";
    public static final String ID_1_8_ANTEIL_FEHLENDE_EGRID = "1.8";
    public static final String ID_1_9_ANTEIL_FEHLENDE_EREID = "1.9";
    public static final String ID_1_10_ANTEIL_FEHLENDE_EGBPID = "1.10";
    public static final String ID_1_11_EGRID_HAT_FALSCHES_FORMAT = "1.11";
    public static final String ID_1_12_EREID_HAT_FALSCHES_FORMAT = "1.12";
    public static final String ID_1_13_EGBPID_HAT_FALSCHES_FORMAT = "1.13";
    public static final String ID_1_14_KEIN_GRUNDSTUECK_MIT_DIESER_ID = "1.14";
    public static final String ID_2_1_SUMME_DER_EIGENTUMSANTEILE_NICHT_1 = "2.1";
    public static final String ID_2_2_ANTEIL_EIGENTUM_ANTEIL_OHNE_ZAEHLER_NENNER = "2.2";
    public static final String ID_2_3_ZAEHLER_ODER_NENNER_FEHLT = "2.3";
    public static final String ID_2_4_EIGENTUM_ANTEIL_MIT_MEHREREN_INHALTEN = "2.4";
    public static final String ID_2_5_INHALT_BEI_EIGENTUM_ANTEIL_OHNE_VON_BIS = "2.5";
    public static final String ID_2_6_EIGENTUM_ANTEIL_OHNE_BERECHTIGTE = "2.6";
    public static final String ID_2_7_KEIN_EIGENTUM_ANTEIL = "2.7";
    public static final String ID_2_8_ANZAHL_EIGENTUMANTEIL = "2.8";
    public static final String ID_2_9_ANTEIL_EIGENTUEMERTYP_NULL = "2.9";
    public static final String ID_2_10_ANTEIL_PROSA = "2.10";
    public static final String ID_2_11_ANTEIL_EIGENTUM_ANTEIL_MIT_PROSA="2.11";
    public static final String ID_2_12_ANTEIL_EIGENTUM_ANTEIL_MIT_PROSA_IM_STICHJAR="2.12";
    public static final String ID_2_13_ANTEIL_SUMME_DER_EIGENTUMSANTEILE_NICHT_1="2.13";    
    public static final String ID_3_1_PERSON_OHNE_INHALT = "3.1";
    public static final String ID_3_2_KEINE_PERSONEN = "3.2";
    public static final String ID_3_3_PERSON_OHNE_VON = "3.3";
    public static final String ID_3_4_PERSON_MEHRERE_OHNE_BIS = "3.4";
    public static final String ID_3_5_PERSON_EINTRAGUNGSREIHENFOLGE="3.5";
    public static final String ID_4_1_ANTEIL_DER_JURISTISCHEN_PERSONEN_MIT_UID = "4.1";
    public static final String ID_4_2_ANZAHL_JURISTISCHEPERSONEN = "4.2";
    public static final String ID_5_1_ANTEIL_DER_NATUERLICHEN_PERSONEN_MIT_AHVN13 = "5.1";
    public static final String ID_5_2_ANZAHL_NATUERLICHEPERSONEN = "5.2";
    public static final String ID_6_1_ANTEIL_DER_GEMEINSCHAFTEN_OHNE_MITGLIEDER = "6.1";
    public static final String ID_6_2_ANZAHL_GEMEINSCHAFTEN = "6.2";
    public static final String ID_6_3_MITGLIED_OHNE_VON_BIS = "6.3";
    public static final String ID_7_1_KEINE_GRUNDSTUECKE = "7.1";
    public static final String ID_7_2_ABSTRAKTES_GRUNDSTUECK_OHNE_ISTKOPIE = "7.2";
    public static final String ID_7_3_GRUNDSTUECK_OHNE_VON = "7.3";
    public static final String ID_7_4_GRUNDSTUECK_MEHRERE_OHNE_BIS = "7.4";
    public static final String ID_7_5_GRUNDSTUECK_EINTRAGUNGSREIHENFOLGE="7.5";
    public static final String ID_8_1_ANZAHL_LIEGENSCHAFTEN = "8.1";
    public static final String ID_9_1_ANZAHL_MITEIGENTUMSANTEILE = "9.1";
    public static final String ID_9_2_STWE_OHNE_BESCHREIBUNG = "9.2";
    public static final String ID_10_1_ANZAHL_SDR = "10.1";
    public static final String ID_10_2_SDR_OHNE_DIENSTBARKEIT = "10.2";
    public static final String ID_11_1_ANZAHL_BERGWERKE = "11.1";
    public static final String ID_12_1_KEINE_RECHTE = "12.1";
    public static final String ID_12_2_INHALT_BEI_RECHT_OHNE_VON_BIS = "12.2";
    public static final String ID_12_3_RECHT_OHNE_VON = "12.3";
    public static final String ID_12_4_RECHT_MEHRERE_OHNE_BIS = "12.4";
    public static final String ID_12_5_RECHT_EINTRAGUNGSREIHENFOLGE="12.5";
    public static final String ID_13_1_DIENSTBARKEIT_OHNE_ISTVERSELBSTAENDIGT = "13.1";
    public static final String ID_13_2_LASTRECHT_BEI_DIENSTBARKEIT_BERECHTIGTE_OHNE_VON_BIS = "13.2";
    public static final String ID_13_3_ANZAHL_DIENSTBARKEIT = "13.3";
    public static final String ID_14_1_ANZAHL_GRUNDPFANDRECHT = "14.1";
    public static final String ID_14_2_VERPFAENDETESGRUNDSTUECKGRUNDPFANDRECHT_OHNE_VON = "14.2";
    public static final String ID_14_3_VERPFAENDETESGRUNDSTUECKGRUNDPFANDRECHT_MEHRERE_OHNE_BIS = "14.3";
    public static final String ID_14_4_VERPFAENDETESGRUNDSTUECKGRUNDPFANDRECHT_EINTRAGUNGSREIHENFOLGE="14.4";
    public static final String ID_15_1_ANZAHL_ANMERKUNG = "15.1";
    public static final String ID_15_2_KEINE_ANMERKUNGEN = "15.2";
    public static final String ID_15_3_ANTEIL_ANMERKUNG_OHNE_IST_OEFFENTLICH = "15.3";
    public static final String ID_15_4_ANTEIL_OEFFENTLICHE_ANMERKUNGEN = "15.4";
    public static final String ID_16_1_ANZAHL_GRUNDLAST = "16.1";
    public static final String ID_17_1_ANZAHL_VORMERKUNG = "17.1";
    
    private LogListener listener=null;
        
    private JAXBContext jaxbContext = null;
    private XMLStreamWriter out=null;
    private java.io.PrintWriter txtOut=null;
    private Logger()
    {
        
    }
    private static Logger singleton=null;
    public static Logger getInstance() {
        if(singleton==null) {
            singleton=new Logger();
        }
        return singleton;
    }
    public void setXmllog(java.io.OutputStream log)
    {
        if(log!=null) {
            // init
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            try {
                jaxbContext = JAXBContext
                .newInstance(ch.ehi.gbdbsvalidator.jaxb.validationresult._1_0.ObjectFactory.class);
                Marshaller ms=null;
                ms = jaxbContext.createMarshaller();
                ms.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
                out = xmlof.createXMLStreamWriter(log,"UTF-8");
                out.writeStartDocument();
                out.setDefaultNamespace(Validator.validationResultNS);
                out.writeStartElement(Validator.validationResultNS, "ValidationResult");
                out.writeDefaultNamespace(Validator.validationResultNS);
            } catch (JAXBException e) {
                throw new IllegalStateException(e);
            } catch (XMLStreamException e) {
                throw new IllegalStateException(e);
            }
        }
    }
    public void logSchwarz(String msg) {
        logSchwarz(ID_0_10_TECHNISCHER_FEHLER,msg);
    }
    public void logSchwarz(String id,String msg) {
        logMessage(id,Status.schwarz.name(),null,msg);
    }
    public void logSchwarz(String msg,Throwable ex) {
        logSchwarz(ID_0_10_TECHNISCHER_FEHLER, msg, ex);
    }
    public void logSchwarz(String id,String msg,Throwable ex) {
        logMessage(id,Status.schwarz.name(),null,msg);
        ArrayList<String> buf=new ArrayList<String>();
        logThrowable(buf,"    ", ex, true);
        for(String lin:buf) {
            logMessage(id,Status.schwarz.name(),null,lin);
        }
    }
    private static void logThrowable(ArrayList<String> out,String ind,Throwable ex,boolean doStacktrace){
        String msg=ex.getLocalizedMessage();
        if(msg!=null){
            msg=msg.trim();
            if(msg.length()==0){
                msg=null;
            }
        }
        if(msg==null){
            msg=ex.getClass().getName();
        }
        out.add(ind+msg);
        if(doStacktrace){
            StackTraceElement[] stackv=ex.getStackTrace();
            for(int i=0;i<stackv.length;i++){
                out.add(ind+"    "+stackv[i].toString());
            }
        }
        Throwable ex2=ex.getCause();
        if(ex2!=null){
            logThrowable(out,ind+"  ",ex2,doStacktrace);
        }
        if(ex instanceof java.sql.SQLException){
            java.sql.SQLException exTarget=(java.sql.SQLException)ex;
            java.sql.SQLException exTarget2=exTarget.getNextException();
            if(exTarget2!=null){
                logThrowable(out,ind+"  ",exTarget2,doStacktrace);
            }
        }
        if(ex instanceof InvocationTargetException){
            InvocationTargetException exTarget=(InvocationTargetException)ex;
            Throwable exTarget2=exTarget.getTargetException();
            if(exTarget2!=null){
                logThrowable(out,ind+"  ",exTarget2,doStacktrace);
            }
        }
    }
    private void logMessage(String id, String kind, String key, String msg) {
        StringBuffer text=new StringBuffer();
        text.append(id);
        text.append(" ");
        text.append(kind);
        text.append(": ");
        text.append(msg);
        if(key!=null && key.length()>0) {
            text.append(" (");
            text.append(key);
            text.append(")");
        }
        System.err.println(text.toString());
        if(txtOut!=null) {
            txtOut.println(text.toString());
        }
        if(listener!=null) {
            listener.outputMsgLine(text.toString());
        }
        if(out!=null) {
            ch.ehi.gbdbsvalidator.jaxb.validationresult._1_0.Message msgEle=new ch.ehi.gbdbsvalidator.jaxb.validationresult._1_0.Message();
            msgEle.setId(id);
            msgEle.setKind(kind);
            msgEle.setSourceKey(key);
            msgEle.setMessage(msg);
            try {
                Marshaller ms=null;
                ms = jaxbContext.createMarshaller();
                ms.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
                ms.marshal(msgEle, out);
            } catch (JAXBException e) {
                throw new IllegalStateException(e);
            }
        }
    }
    public void logRot(String id,String msg) {
        logMessage(id,Status.rot.name(),null,msg);
    }
    public void logRot(String id,String key,String msg) {
        logMessage(id,Status.rot.name(),key,msg);
    }
    public void logGelb(String id,String key,String msg) {
        logMessage(id,Status.gelb.name(),key,msg);
    }

    public void logGruen(String id,String msg) {
        logMessage(id,Status.gruen.name(),null,msg);
    }
    public void logGruen(String id,String key,String msg) {
        logMessage(id,Status.gruen.name(),key,msg);
    }
    public void close() {
        if(out!=null) {
            try {
                out.writeEndElement();
                out.writeEndDocument();
                out.flush();
                out=null;
            } catch (XMLStreamException e) {
                throw new IllegalStateException(e);
            }
        }
        if(txtOut!=null) {
            txtOut.close();
            txtOut=null;
        }
    }
    public boolean isClosed()
    {
        return out==null && txtOut==null;
    }
    public void setTraceFilter(boolean b) {
        // TODO Auto-generated method stub
        
    }
    public void addListener(LogListener logListener) {
        listener=logListener;
    }
    public void setLogfile(File file) {
        try {
            txtOut=new PrintWriter(new java.io.BufferedWriter(new java.io.FileWriter(file)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
