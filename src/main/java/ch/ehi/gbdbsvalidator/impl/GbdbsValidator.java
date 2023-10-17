package ch.ehi.gbdbsvalidator.impl;


import java.io.StringReader;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import ch.ehi.gbdbsvalidator.Validator;
import ch.ehi.gbdbsvalidator.FilterMap;
import ch.ehi.gbdbsvalidator.Logger;
import ch.ehi.basics.types.OutParam;
import ch.ehi.gbdbsvalidator.Validator.Status;
import ch.ehi.gbdbsvalidator.jaxb.eigtyp._1_0.Eigentuemer;
import ch.ehi.gbdbsvalidator.jaxb.eigtyp._1_0.Eigentuemerindex;

public class GbdbsValidator {
public static final String RECORD_TYP_EIGENTUEMER = "Eigentuemer";
public static final String RECORD_TYP_MITGLIED = "Mitglied";
public static final String PREFIX_PERS = "E_PERS:";
public static final String PREFIX_GS = "E_GS:";
private static final String PREFIX_EIG = "E_EIG:";
/*
            long beforeUsedMem=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
            long afterUsedMem=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
            long actualMemUsed=afterUsedMem-beforeUsedMem;
            System.out.println("unmarshal used memory "+actualMemUsed);
*/	
	private Unmarshaller um=null;
	private XMLInputFactory xmlif=null;
	private XMLOutputFactory xmlof = null;
	private Marshaller ms = null;
    private Marshaller fragmentMarshaller = null;
    private Marshaller outMarshaller = null;
	private Logger logger=null;
    OutParam<Status> status=null;
    private int stichjahr=0;
    private FilterMap index=null;
    private String gbdbsDateiname=null;
    public static final boolean NO_SHARED_MAP = false;
    private int anzahlJuristischePersonen=0;
    private int anzahlJuristischePersonenMitUID=0;
    private int anzahlNatuerlichePersonen;
    private int anzahlNatuerlichePersonenMitAHVN13;
    private int anzahlGemeinschaften;
    private int anzahlGemeinschaftenOhneMitglieder;
    private final DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.US);
    private final DecimalFormat decimalFormat = new DecimalFormat("#0.0", dfs);
    private int anzahlGrundstueckeOhneEGRID=0;
    private int anzahlGrundstuecke=0;
    private int anzahlPersonen=0;
    private int anzahlPersonenOhneEGBPID=0;
    private int anzahlRechte=0;
    private int anzahlRechteOhneEREID=0;
    private int anzahlEigentumAnteil=0;
    private int anzahlDienstbarkeit=0;
    private int anzahlGrundlast=0;
    private int anzahlGrundpfandrecht=0;
    private int anzahlAnmerkung=0;
    private int anzahlAnmerkungOhneOeffentlich=0;
    private int anzahlAnmerkungOeffentlich=0;
    private int anzahlVormerkung=0;
    private int anzahlBergwerk;
    private int anzahlLiegenschaft;
    private int anzahlMiteigentumsAnteil;
    private int anzahlSelbstaendigesDauerndesRecht;
    private int anzahlEigentuemerTypNull=0;

	public GbdbsValidator(Logger logger, OutParam<Status> status,LocalDate stichtag,FilterMap tempStore) {
	    this.logger=logger;
	    this.status=status;
	    index=tempStore;
        stichjahr=stichtag.getYear();
        xmlif = XMLInputFactory.newInstance();
        xmlof = XMLOutputFactory.newInstance();
          try {
              JAXBContext jaxbContext = JAXBContext
              .newInstance(Validator.JAXB_CONTEXT_PATH);
              um = jaxbContext.createUnmarshaller();
              ms = jaxbContext.createMarshaller();
              fragmentMarshaller=jaxbContext.createMarshaller();
              fragmentMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
          } catch (JAXBException e) {
              throw new IllegalStateException(e);
          }
    }

    public void finishFilter() throws JAXBException, XMLStreamException {
	            //long beforeUsedMem=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
                //long afterUsedMem=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
                //long actualMemUsed=afterUsedMem-beforeUsedMem;
                //System.out.println("unmarshal used memory "+actualMemUsed);
        int anzahlEigentumAnteilOhneZaehlerNenner=0;
        int anzahlEigentumAnteilProsa=0;
        int anzahlEigentumAnteilProsaLetztesJahr=0;
        int anzahlQuoteUngleichEins=0;
        java.util.Map<String,java.util.List<String>> gemeinschaften=new java.util.HashMap<String,java.util.List<String>>();
        
				for(String key:index){
				    if(key.startsWith(PREFIX_PERS)) {
				        continue;
				    }else if(key.startsWith(PREFIX_GS)) {
                        continue;
                    }else if(key.startsWith(PREFIX_EIG)) {
                        ; // ok
                    }else {
                        // ignore
                        continue;
                    }
                    String gsId=key.substring(PREFIX_EIG.length());
                    String egrid = GbdbsValidator.parseGbdbsGsId(gsId).getEGRID();
				    Eigentuemerindex idx=(Eigentuemerindex)parseXml(index.get(key));
                    long anteilKgv=1;
                    boolean validateQuote=true;
                    for(Eigentuemer eigentuemer : idx.getEigentuemer()) {
                        String eigentumAnteilId=eigentuemer.getEigentumAnteilId();
                        JAXBElement grundstueckEle = (JAXBElement) parseXml(index.get(PREFIX_GS+gsId));
                        if(grundstueckEle==null) {
                            warning(Logger.ID_1_14_KEIN_GRUNDSTUECK_MIT_DIESER_ID,eigentumAnteilId,gsId+" Grundstueck nicht vorhanden");
                        }else {
                            if(grundstueckEle.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.GrundstueckType) {
                                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.GrundstueckType grundstueck=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.GrundstueckType)grundstueckEle.getValue();
                                if(grundstueck.getGemeinde()!=null && grundstueck.getGemeinde().getGemeinde()!=null) {
                                    eigentuemer.setBfsNr(grundstueck.getGemeinde().getGemeinde().getMunicipalityId());
                                }
                            }else if(grundstueckEle.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.GrundstueckType){
                                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.GrundstueckType grundstueck=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.GrundstueckType)grundstueckEle.getValue();
                                if(grundstueck.getGemeinde()!=null) {
                                    eigentuemer.setBfsNr(grundstueck.getGemeinde().getMunicipalityId());
                                }
                            }
                        }
                        //ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.GrundstueckType eigentuemerGsEle = grundstuecke.get(eigentuemer.getEigentuemerId());
                        //ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.PersonGBType persEle = personen.get(eigentuemer.getEigentuemerId());
                        final String eigentuemerId = eigentuemer.getEigentuemerId();
                        if(eigentuemerId!=null) {
                            JAXBElement eigentuemerGsEle = (JAXBElement) parseXml(index.get(PREFIX_GS+eigentuemerId));
                            JAXBElement persEle = (JAXBElement) parseXml(index.get(PREFIX_PERS+eigentuemerId));
                            if(persEle==null && eigentuemerGsEle==null) {
                                warning(Logger.ID_1_3_KEIN_GRUNDSTUECK_ODER_PERSON_MIT_DIESER_ID,eigentumAnteilId,"kein Grundstueck/Person mit der Id '"+eigentuemerId+"' vorhanden");
                            }else if(persEle!=null && eigentuemerGsEle!=null) {
                                warning(Logger.ID_1_4_ID_NICHT_EINDEUTIG,eigentumAnteilId,"mehrere Grundstueck+Person mit der Id '"+eigentuemerId+"' vorhanden");
                            }else if(eigentuemerGsEle!=null) {
                                // ok; keine weiteren Angaben
                                eigentuemer.setEigentuemerTyp("Grundstueck");
                                ch.ehi.gbdbsvalidator.jaxb.gbbasisid._2_0.PersonIdDefType pId = GbdbsValidator.parseGbdbsPersId(eigentuemerId);
                                eigentuemer.setEigentuemerEGRID(pId.getEGBPID());
                            }else if(persEle!=null) {
                            }else {
                                throw new IllegalStateException();
                            }
                            if(persEle!=null) {
                                setPersonData(eigentuemer, eigentuemerId, persEle, gemeinschaften);
                            }
                        }
                        if(eigentuemer.getQuoteZaehler()==null || eigentuemer.getQuoteNenner()==null) {
                            anzahlEigentumAnteilOhneZaehlerNenner+=1;
                            validateQuote=false;
                            if(eigentuemer.getQuoteProsa()==null) {
                                info(Logger.ID_2_3_ZAEHLER_ODER_NENNER_FEHLT,eigentumAnteilId,"EigentumAnteil ohne Zaehler/Nenner "+eigentumAnteilId);
                            }else {
                                info(Logger.ID_2_10_ANTEIL_PROSA,eigentumAnteilId,"EigentumAnteil mit Quote als Prosa \""+eigentuemer.getQuoteProsa()+"\" bei "+eigentumAnteilId+" (EGRID "+eigentuemer.getEGRID()+")");
                                anzahlEigentumAnteilProsa+=1;
                                if(dateInCurrentYear(eigentuemer.getEigentumAnteilVom())) {
                                    anzahlEigentumAnteilProsaLetztesJahr+=1;
                                }
                            }
                        }else {
                            if(validateQuote) {
                                anteilKgv=kgv(anteilKgv,eigentuemer.getQuoteNenner().intValue());
                            }
                        }
                        if(eigentuemer.getEigentuemerTyp()==null) {
                            anzahlEigentuemerTypNull+=1;
                        }
                    }
                    if(validateQuote) {
                        long anteilZaehler=0;
                        for(Eigentuemer eigentuemer : idx.getEigentuemer()) {
                            int zaehler=eigentuemer.getQuoteZaehler().intValue();
                            int nenner=eigentuemer.getQuoteNenner().intValue();
                            anteilZaehler+=(zaehler*anteilKgv/nenner);
                        }
                        if(anteilZaehler!=anteilKgv) {
                            info(Logger.ID_2_1_SUMME_DER_EIGENTUMSANTEILE_NICHT_1,gsId,"Summe der Zaehler/Nenner ist nicht 1 ("+anteilZaehler+"/"+anteilKgv+")");
                            anzahlQuoteUngleichEins+=1;
                        }
                        
                    }
				    
				}
				java.util.Set<String> toVisit=new java.util.HashSet<String>(gemeinschaften.keySet());
                java.util.Set<String> visited=new java.util.HashSet<String>();
                while(!toVisit.isEmpty()) {
                    for(String gemId:toVisit) {
                        visited.add(gemId);
                        for(String persId:gemeinschaften.get(gemId)) {
                            JAXBElement persEle = (JAXBElement) parseXml(index.get(PREFIX_PERS+persId));
                            if(persEle==null) {
                                warning(Logger.ID_1_3_KEIN_GRUNDSTUECK_ODER_PERSON_MIT_DIESER_ID,gemId,"kein Grundstueck/Person mit der Id '"+persId+"' vorhanden");
                            }else {
                                Eigentuemer eigentuemer=new Eigentuemer();
                                eigentuemer.setRecordTyp(GbdbsValidator.RECORD_TYP_MITGLIED);
                                eigentuemer.setGemeinschaftId(gemId);
                                eigentuemer.setEigentuemerId(persId);
                                setPersonData(eigentuemer, persId, persEle, gemeinschaften);
                            }
                        }
                    }
                    toVisit=new java.util.HashSet<String>(gemeinschaften.keySet());
                    toVisit.removeAll(visited);
                }
                info(Logger.ID_2_8_ANZAHL_EIGENTUMANTEIL,"Anzahl Eigentumanteile: "+anzahlEigentumAnteil);
                info(Logger.ID_2_9_ANTEIL_EIGENTUEMERTYP_NULL,"Anteil ohne EigentuemerTyp: "+decimalFormat.format(100.0/anzahlEigentumAnteil*anzahlEigentuemerTypNull) +" %");
                if(anzahlEigentumAnteil>0) {
                    info(Logger.ID_2_2_ANTEIL_EIGENTUM_ANTEIL_OHNE_ZAEHLER_NENNER,"Anteil EigentumAnteil ohne Zaehler/Nenner: "+decimalFormat.format(100.0/anzahlEigentumAnteil*anzahlEigentumAnteilOhneZaehlerNenner) +" %");
                    info(Logger.ID_2_11_ANTEIL_EIGENTUM_ANTEIL_MIT_PROSA,"Anteil EigentumAnteil mit Prosa: "+decimalFormat.format(100.0/anzahlEigentumAnteil*anzahlEigentumAnteilProsa) +" % ("+anzahlEigentumAnteilProsa+")");
                    info(Logger.ID_2_12_ANTEIL_EIGENTUM_ANTEIL_MIT_PROSA_IM_STICHJAR,"Anzahl EigentumAnteil mit Prosa im "+stichjahr+": "+anzahlEigentumAnteilProsaLetztesJahr);
                }else {
                    error(Logger.ID_2_7_KEIN_EIGENTUM_ANTEIL,"Kein EigentumAnteil");
                }
                info(Logger.ID_2_13_ANTEIL_SUMME_DER_EIGENTUMSANTEILE_NICHT_1,"Anteil EigentumAnteil mit Summe der Quoten ungleich 1: "+decimalFormat.format(100.0/anzahlEigentumAnteil*anzahlQuoteUngleichEins) +" % ("+anzahlQuoteUngleichEins+")");
                info(Logger.ID_13_3_ANZAHL_DIENSTBARKEIT,"Anzahl Dienstbarkeiten: "+anzahlDienstbarkeit);
                info(Logger.ID_16_1_ANZAHL_GRUNDLAST,"Anzahl Grundlasten: "+anzahlGrundlast);
                info(Logger.ID_14_1_ANZAHL_GRUNDPFANDRECHT,"Anzahl Grundpfandrechte: "+anzahlGrundpfandrecht);
                info(Logger.ID_17_1_ANZAHL_VORMERKUNG,"Anzahl Vormerkungen: "+anzahlVormerkung);
                info(Logger.ID_15_1_ANZAHL_ANMERKUNG,"Anzahl Anmerkungen: "+anzahlAnmerkung);
                if(anzahlAnmerkung>0) {
                    info(Logger.ID_15_3_ANTEIL_ANMERKUNG_OHNE_IST_OEFFENTLICH,"Anteil Anmerkungen ohne istOeffentlich: "+decimalFormat.format(100.0/anzahlAnmerkung*anzahlAnmerkungOhneOeffentlich) +" %");
                    info(Logger.ID_15_4_ANTEIL_OEFFENTLICHE_ANMERKUNGEN,"Anteil oeffentliche Anmerkungen: "+decimalFormat.format(100.0/anzahlAnmerkung*anzahlAnmerkungOeffentlich) +" %");
                }else {
                    info(Logger.ID_15_2_KEINE_ANMERKUNGEN,"Keine Anmerkungen");
                }
                info(Logger.ID_5_2_ANZAHL_NATUERLICHEPERSONEN,"Anzahl natuerliche Personen: "+anzahlNatuerlichePersonen);
                info(Logger.ID_4_2_ANZAHL_JURISTISCHEPERSONEN,"Anzahl juristische Personen: "+anzahlJuristischePersonen);
                info(Logger.ID_6_2_ANZAHL_GEMEINSCHAFTEN,"Anzahl Gemeinschaften: "+anzahlGemeinschaften);
                if(anzahlJuristischePersonen>0) {
                    info(Logger.ID_4_1_ANTEIL_DER_JURISTISCHEN_PERSONEN_MIT_UID,"Anteil Juristische Personen mit UID: "+decimalFormat.format(100.0/anzahlJuristischePersonen*anzahlJuristischePersonenMitUID) +" %");
                }else {
                    info(Logger.ID_4_1_ANTEIL_DER_JURISTISCHEN_PERSONEN_MIT_UID,"Keine Juristische Personen");
                }
                if(anzahlNatuerlichePersonen>0) {
                    info(Logger.ID_5_1_ANTEIL_DER_NATUERLICHEN_PERSONEN_MIT_AHVN13,"Anteil Natuerliche Personen mit AHVN13: "+decimalFormat.format(100.0/anzahlNatuerlichePersonen*anzahlNatuerlichePersonenMitAHVN13) +" %");
                }else {
                    info(Logger.ID_5_1_ANTEIL_DER_NATUERLICHEN_PERSONEN_MIT_AHVN13,"Keine Natuerliche Personen");
                }
                if(anzahlGemeinschaften>0) {
                    info(Logger.ID_6_1_ANTEIL_DER_GEMEINSCHAFTEN_OHNE_MITGLIEDER,"Anteil Gemeinschaften ohne Mitglieder: "+decimalFormat.format(100.0/anzahlGemeinschaften*anzahlGemeinschaftenOhneMitglieder) +" %");
                }else {
                    info(Logger.ID_6_1_ANTEIL_DER_GEMEINSCHAFTEN_OHNE_MITGLIEDER,"Keine Gemeinschaften");
                }
                info(Logger.ID_8_1_ANZAHL_LIEGENSCHAFTEN,"Anzahl Liegenschaften: "+anzahlLiegenschaft);
                info(Logger.ID_9_1_ANZAHL_MITEIGENTUMSANTEILE,"Anzahl Miteigentumsanteile: "+anzahlMiteigentumsAnteil);
                info(Logger.ID_10_1_ANZAHL_SDR,"Anzahl SDR: "+anzahlSelbstaendigesDauerndesRecht);
                info(Logger.ID_11_1_ANZAHL_BERGWERKE,"Anzahl Bergwerke: "+anzahlBergwerk);
                if(anzahlGrundstuecke>0) {
                    final double anteilGrundstueckeOhneEGRID = 100.0/anzahlGrundstuecke*anzahlGrundstueckeOhneEGRID;
                    if(anteilGrundstueckeOhneEGRID>10.0) {
                        warning(Logger.ID_1_8_ANTEIL_FEHLENDE_EGRID,null,"Anteil Grundt\u00fccke ohne EGRID: "+decimalFormat.format(anteilGrundstueckeOhneEGRID) +" %");
                    }else {
                        info(Logger.ID_1_8_ANTEIL_FEHLENDE_EGRID,"Anteil Grundt\u00fccke ohne EGRID: "+decimalFormat.format(anteilGrundstueckeOhneEGRID) +" %");
                    }
                }else {
                    error(Logger.ID_7_1_KEINE_GRUNDSTUECKE,"Keine Grundt\u00fccke");
                }
                if(anzahlRechte>0) {
                    final double anteilRechteOhneEREID = 100.0/anzahlRechte*anzahlRechteOhneEREID;
                    if(anteilRechteOhneEREID>10.0) {
                        warning(Logger.ID_1_9_ANTEIL_FEHLENDE_EREID,null,"Anteil Rechte ohne EREID: "+decimalFormat.format(anteilRechteOhneEREID) +" %");
                    }
                }else {
                    error(Logger.ID_12_1_KEINE_RECHTE,"Keine Rechte");
                }
                if(anzahlPersonen>0) {
                    final double anteilPersonenOhneEGBPID = 100.0/anzahlPersonen*anzahlPersonenOhneEGBPID;
                    if(anteilPersonenOhneEGBPID>10.0) {
                        warning(Logger.ID_1_10_ANTEIL_FEHLENDE_EGBPID,null,"Anteil Personen ohne EGBPID: "+decimalFormat.format(anteilPersonenOhneEGBPID) +" %");
                    }
                }else {
                    error(Logger.ID_3_2_KEINE_PERSONEN,"Keine Personen");
                }
	}

    private void setPersonData(Eigentuemer eigentuemer, final String eigentuemerId, JAXBElement persEle,
            java.util.Map<String,java.util.List<String>> mitglieder) {
        Object inhalt=Personen.getNewestInhaltPers(persEle);
        if(inhalt==null) {
            warning(Logger.ID_3_1_PERSON_OHNE_INHALT,eigentuemerId,"PersonGB "+eigentuemerId+" ohne InhaltPersonGB");
        }else {
            ch.ehi.gbdbsvalidator.jaxb.gbbasisid._2_0.PersonIdDefType pId = GbdbsValidator.parseGbdbsPersId(eigentuemerId);
            eigentuemer.setEGBPID(pId.getEGBPID());
            eigentuemer.setKantPersNr(pId.getKantPersNr());
            eigentuemer.setUid(pId.getUID());
            eigentuemer.setAhvn13(pId.getAHVN13());
            if(inhalt instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltGemeinschaftType) {
                eigentuemer.setEigentuemerTyp("Gemeinschaft");
                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltGemeinschaftType inhaltGemeinschaft = (ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltGemeinschaftType) inhalt;
                eigentuemer.setName(inhaltGemeinschaft.getName());
                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.GemeinschaftArt art = inhaltGemeinschaft.getArt();
                eigentuemer.setGemeinschaftart(art!=null?art.name():null);
                // Mitglieder am Stichtag 
                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.GemeinschaftType gemeinschaft=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.GemeinschaftType)persEle.getValue();
                if(!mitglieder.containsKey(eigentuemerId)) {
                    java.util.List<String> mv=new java.util.ArrayList<String>();
                    mitglieder.put(eigentuemerId, mv);
                    for(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.PersonRefBegruendet mitglied:gemeinschaft.getMitglieder()) {
                        XMLGregorianCalendar von = mitglied.getBegruendungTagebuchDatumZeit();
                        XMLGregorianCalendar bis = mitglied.getTagebuchDatumZeit();
                        if(von!=null && bis==null){
                            mv.add(mitglied.getValue());
                        }else if(von==null && bis==null) {
                            warning(Logger.ID_6_3_MITGLIED_OHNE_VON_BIS,eigentuemerId,"Gemeinschaft/mitglieder ohne von/bis "+eigentuemerId);
                            mv.add(mitglied.getValue());
                        }
                    }
                }
            }else if(inhalt instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltGemeinschaftType) {
                    eigentuemer.setEigentuemerTyp("Gemeinschaft");
                    ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltGemeinschaftType inhaltGemeinschaft = (ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltGemeinschaftType) inhalt;
                    eigentuemer.setName(inhaltGemeinschaft.getName());
                    ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.GemeinschaftArt art = inhaltGemeinschaft.getArt();
                    eigentuemer.setGemeinschaftart(art!=null?art.name():null);
                    // Mitglieder am Stichtag 
                    ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.GemeinschaftType gemeinschaft=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.GemeinschaftType)persEle.getValue();
                    if(!mitglieder.containsKey(eigentuemerId)) {
                        java.util.List<String> mv=new java.util.ArrayList<String>();
                        mitglieder.put(eigentuemerId, mv);
                        for(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.PersonRefBegruendet mitglied:gemeinschaft.getMitglieder()) {
                            XMLGregorianCalendar von = mitglied.getVonTagebuchDatumZeit();
                            XMLGregorianCalendar bis = mitglied.getBisTagebuchDatumZeit();
                            if(von!=null && bis==null){
                                mv.add(mitglied.getRef());
                            }else if(von==null && bis==null) {
                                warning(Logger.ID_6_3_MITGLIED_OHNE_VON_BIS,eigentuemerId,"Gemeinschaft/mitglieder ohne von/bis "+eigentuemerId);
                                mv.add(mitglied.getRef());
                            }
                        }
                    }
            }else if(inhalt instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltJuristischePersonGBType) {
                eigentuemer.setEigentuemerTyp("JuristischePerson");
                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltJuristischePersonGBType inhaltJuristischePersonGB = (ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltJuristischePersonGBType) inhalt;
                eigentuemer.setName(inhaltJuristischePersonGB.getNameFirma());
                eigentuemer.setRechtsform(getRechtsform(inhaltJuristischePersonGB));
                if(eigentuemer.getUid()==null) {
                    eigentuemer.setUid(inhaltJuristischePersonGB.getUID());
                }
                eigentuemer.setSitz(inhaltJuristischePersonGB.getSitz());
                eigentuemer.setFirmennummer(inhaltJuristischePersonGB.getFirmennummer());
            }else if(inhalt instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltJuristischePersonGBType) {
                eigentuemer.setEigentuemerTyp("JuristischePerson");
                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltJuristischePersonGBType inhaltJuristischePersonGB = (ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltJuristischePersonGBType) inhalt;
                eigentuemer.setName(inhaltJuristischePersonGB.getNameFirma());
                eigentuemer.setRechtsform(getRechtsform(inhaltJuristischePersonGB));
                if(eigentuemer.getUid()==null) {
                    eigentuemer.setUid(inhaltJuristischePersonGB.getUID());
                }
                eigentuemer.setSitz(inhaltJuristischePersonGB.getSitz());
                eigentuemer.setFirmennummer(inhaltJuristischePersonGB.getFirmennummer());
            }else if(inhalt instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltNatuerlichePersonGBType) {
                eigentuemer.setEigentuemerTyp("NatuerlichePerson");
                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltNatuerlichePersonGBType inhaltNatuerlichePersonGB = (ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltNatuerlichePersonGBType) inhalt;
                eigentuemer.setName(inhaltNatuerlichePersonGB.getName());
                eigentuemer.setVornamen(inhaltNatuerlichePersonGB.getVorname()); 
                Integer geburtsjahr = inhaltNatuerlichePersonGB.getGeburtsjahr();
                if(geburtsjahr!=null) {
                    eigentuemer.setGeburtsjahr(BigInteger.valueOf(geburtsjahr)); 
                }
                eigentuemer.setGeburtsmonat(inhaltNatuerlichePersonGB.getGeburtsmonat()); 
                eigentuemer.setGeburtstag(inhaltNatuerlichePersonGB.getGeburtstag()); 
                eigentuemer.setHeimatort(inhaltNatuerlichePersonGB.getHeimatort());
                eigentuemer.setStaatsangehoerigkeit(inhaltNatuerlichePersonGB.getStaatsangehoerigkeit()); 
                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.PersonGBType pers=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.PersonGBType)persEle.getValue();
                if(pers.getPersonStamm()!=null && pers.getPersonStamm().getPersonStamm()!=null && pers.getPersonStamm().getPersonStamm().getValue()!=null) {
                    eigentuemer.setZivilstand(((ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.NatuerlichePersonType) pers.getPersonStamm().getPersonStamm().getValue()).getZivilstand()); 
                }
            }else if(inhalt instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltNatuerlichePersonGBType) {
                eigentuemer.setEigentuemerTyp("NatuerlichePerson");
                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltNatuerlichePersonGBType inhaltNatuerlichePersonGB = (ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltNatuerlichePersonGBType) inhalt;
                eigentuemer.setName(inhaltNatuerlichePersonGB.getName());
                eigentuemer.setVornamen(inhaltNatuerlichePersonGB.getVornamen()); 
                eigentuemer.setGeburtsjahr(inhaltNatuerlichePersonGB.getGeburtsjahr()); 
                eigentuemer.setGeburtsmonat(inhaltNatuerlichePersonGB.getGeburtsmonat()); 
                eigentuemer.setGeburtstag(inhaltNatuerlichePersonGB.getGeburtstag()); 
                eigentuemer.setHeimatort(inhaltNatuerlichePersonGB.getHeimatort());
                eigentuemer.setStaatsangehoerigkeit(inhaltNatuerlichePersonGB.getStaatsangehoerigkeit()); 
                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.PersonGBType pers=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.PersonGBType)persEle.getValue();
                if(pers.getPersonStamm()!=null && pers.getPersonStamm().getValue()!=null) {
                    eigentuemer.setZivilstand(((ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.NatuerlichePersonType) pers.getPersonStamm().getValue()).getZivilstand()); 
                }
            }else {
                throw new IllegalStateException(inhalt.getClass().getName());
            }
        }
        if(persEle.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.PersonGBType) {
            ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.PersonGBType pers=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.PersonGBType)persEle.getValue();
            if(pers.getPersonStamm()!=null) {
                if(pers.getPersonStamm().getPersonStamm()!=null) {
                    ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.AdresseType adr=null;
                    adr=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.AdresseType)getAdresse(pers.getPersonStamm().getPersonStamm());
                    if(adr!=null) {
                        eigentuemer.setStrasse(adr.getStrasse());
                        eigentuemer.setHausnr(adr.getHausnummer());
                        eigentuemer.setPlz(adr.getPLZ());
                        eigentuemer.setOrt(adr.getOrt());
                    }
                }
            }
        }else if(persEle.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.PersonGBType) {
            ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.PersonGBType pers=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.PersonGBType)persEle.getValue();
            if(pers.getPersonStamm()!=null) {
                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.AdresseType adr=null;
                adr=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.AdresseType)getAdresse(pers.getPersonStamm());
                if(adr!=null) {
                    eigentuemer.setStrasse(adr.getStrasse());
                    eigentuemer.setHausnr(adr.getHausnummer());
                    eigentuemer.setPlz(adr.getPLZ());
                    eigentuemer.setOrt(adr.getOrt());
                }
            }
        }
    }
    static public long ggt(long m,long n)
    {
        if (n==0)
            return m;
        else
            return ggt(n, m%n);
    }
    static public long kgv(long m,long n)
    {
        long o = ggt(m,n);
        long p = (m * n) / o;
        return p;
    }
	private void error(String id,String msg) {
	    if(okToLog(id)) {
	        logger.logRot(id,msg);
	    }
        status.value=Status.rot;
    }
    private void technicalError(String msg,Throwable ex) {
        logger.logSchwarz(msg,ex);
        status.value=Status.schwarz;
    }
    private void warning(String id,String key, String msg) {
        if(okToLog(id)) {
            logger.logGelb(id,key,msg);
        }
        if(status.value==Status.gruen) {
            status.value=Status.gelb;
        }
    }
    private void warningGruen(String id,String key, String msg) {
        if(okToLog(id)) {
            logger.logGelb(id,key,msg);
        }
    }

    private void info(String id,String msg) {
        if(okToLog(id)) {
            logger.logGruen(id,msg);
        }
    }
    private void info(String id,String key,String msg) {
        if(okToLog(id)) {
            logger.logGruen(id,key,msg);
        }
    }

    private HashMap<String,Integer> logCounter=new HashMap<String,Integer>();
	private boolean okToLog(String id) {
        Integer counter=logCounter.get(id);
        if(counter==null) {
            counter=new Integer(1);
        }else {
            counter=counter+1;
        }
        logCounter.put(id, counter);
        if(counter<1000) {
            return true; // write to log
        }
        return false; // do not write to log
    }

    private Object getAdresse(JAXBElement persEle) {
	    if(persEle.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.PersonStammType) {
	        ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.PersonStammType person=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.PersonStammType)persEle.getValue();
	        for(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.PersonStammType.Adresse adrEle:person.getAdresse()) {
	            ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.AdresseType adr = adrEle.getAdresse();
	            return adr;
	        }
	    }else if(persEle.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.PersonStammType) {
            ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.PersonStammType person=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.PersonStammType)persEle.getValue();
            for(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.AdresseType adr:person.getAdresse()) {
                return adr;
            }
        }
		return null;
	}

	private String getRechtsform(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltJuristischePersonGBType inhalt) {
	    ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltJuristischePersonGBType.RechtsformStichwort swEle=inhalt.getRechtsformStichwort();
		StringBuffer ret=new StringBuffer();
		if(swEle!=null) {
		    ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.JuristischePersonGBRechtsformType sw=swEle.getJuristischePersonGBRechtsform();
			if(sw!=null) {
				String txt=sw.getStichwort();
				if(txt!=null) {
					txt=txt.trim();
					if(txt.length()>0) {
						ret.append(txt);
					}
				}
			}
			
		}
		String zs=inhalt.getRechtsformZusatz();
		if(zs!=null) {
			zs=zs.trim();
			if(zs.length()>0) {
				ret.append(zs);
			}
		}
		if(ret.length()>0) {
			return ret.toString();
		}
		return null;
	}
    private String getRechtsform(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltJuristischePersonGBType inhalt) {
        ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.JuristischePersonGBRechtsformType sw=inhalt.getRechtsformStichwort();
        StringBuffer ret=new StringBuffer();
        if(sw!=null) {
            String txt=sw.getStichwort();
            if(txt!=null) {
                txt=txt.trim();
                if(txt.length()>0) {
                    ret.append(txt);
                }
            }
        }
        String zs=inhalt.getRechtsformZusatz();
        if(zs!=null) {
            zs=zs.trim();
            if(zs.length()>0) {
                ret.append(zs);
            }
        }
        if(ret.length()>0) {
            return ret.toString();
        }
        return null;
    }


    private void requireStartElement(XMLEvent ev, String soapNs2, String string) throws XMLStreamException {
        if(ev instanceof StartElement){
            StartElement start=(StartElement)ev;
            if (start.getName().getNamespaceURI().equals(soapNs2)
                    && start.getName().getLocalPart().equals(string)) {
                return;
            }
        }
        throw new XMLStreamException("unexpected Element "+ev);
    }
	public void convertGbdbsEle(JAXBElement ele) {
		try {
            if(ele.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.GrundstueckType){
                convertGrundstueck(ele);
            }else if(ele.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.GrundstueckType){
                convertGrundstueck(ele);
            }else if(ele.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.RechtType){
                convertRecht(ele);
            }else if(ele.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.RechtType){
                convertRecht(ele);
            }else if(ele.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.PersonGBType){
                convertPerson(ele);
            }else if(ele.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.PersonGBType){
                convertPerson(ele);
            }else if(ele.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.AnmeldungType){
            }else if(ele.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.AnmeldungType){
            }else{
                throw new IllegalArgumentException("unexpected type "+ele.getValue().getClass());
            }
		} catch (JAXBException e) {
			technicalError("failed to unmarshall",e);
			return;
		} catch (XMLStreamException e) {
            technicalError("failed to read",e);
            return;
        }
	}

	private void convertGrundstueck(JAXBElement gsEle)
			throws XMLStreamException, JAXBException {
        String gbdbsGsId = null;
	    if(gsEle.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.GrundstueckType) {
	        ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.GrundstueckType gs = (ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.GrundstueckType)gsEle.getValue();
	        gbdbsGsId = gs.getNummer();
	    }else if(gsEle.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.GrundstueckType) {
            ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.GrundstueckType gs = (ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.GrundstueckType)gsEle.getValue();
            gbdbsGsId = gs.getNummer();
        }else {
            throw new IllegalStateException();
        }
        boolean istAktuell=Grundstuecke.getCurrentInhaltGrundstueck(gsEle)!=null;
        if(istAktuell) {
            anzahlGrundstuecke++;
            if(gsEle.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.BergwerkType) {
                anzahlBergwerk++;
            }else if(gsEle.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.LiegenschaftType) {
                anzahlLiegenschaft++;
            }else if(gsEle.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.MiteigentumsAnteilType) {
                anzahlMiteigentumsAnteil++;
            }else if(gsEle.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.SelbstaendigesDauerndesRechtType) {
                anzahlSelbstaendigesDauerndesRecht++;
            }else if(gsEle.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.GrundstueckType) {
                if(!((ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.GrundstueckType)gsEle.getValue()).isIstKopie()){
                    warning(Logger.ID_7_2_ABSTRAKTES_GRUNDSTUECK_OHNE_ISTKOPIE,gbdbsGsId,"Abstraktes Grundst\\u00fcck ohne istKopie=true "+gbdbsGsId);
                }
            }else if(gsEle.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.BergwerkType) {
                anzahlBergwerk++;
            }else if(gsEle.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.LiegenschaftType) {
                anzahlLiegenschaft++;
            }else if(gsEle.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.MiteigentumsAnteilType) {
                anzahlMiteigentumsAnteil++;
            }else if(gsEle.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.SelbstaendigesDauerndesRechtType) {
                anzahlSelbstaendigesDauerndesRecht++;
            }else if(gsEle.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.GrundstueckType) {
                if(!((ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.GrundstueckType)gsEle.getValue()).isIstKopie()){
                    warning(Logger.ID_7_2_ABSTRAKTES_GRUNDSTUECK_OHNE_ISTKOPIE,gbdbsGsId,"Abstraktes Grundst\\u00fcck ohne istKopie=true "+gbdbsGsId);
                }
            }
        }
        ch.ehi.gbdbsvalidator.jaxb.gbbasisid._2_0.GrundstueckNummerDefType gsId = GbdbsValidator.parseGbdbsGsId(gbdbsGsId);
        final String egrid = gsId.getEGRID();
        if(egrid!=null) {
            if(!EgridGenerator.validateEgrid(egrid)) {
                if(istAktuell) {
                    warning(Logger.ID_1_11_EGRID_HAT_FALSCHES_FORMAT,gbdbsGsId,"Ung\u00fcltige EGRID "+gbdbsGsId);
                }
            }
        }else {
            if(istAktuell) {
                warningGruen(Logger.ID_1_5_FEHLENDE_EGRID,gbdbsGsId,"Grundst\u00fcck ohne EGRID "+gbdbsGsId);
                anzahlGrundstueckeOhneEGRID++;
            }
        }
        if(NO_SHARED_MAP || index.get(PREFIX_GS+gbdbsGsId)==null) {
            index.put(PREFIX_GS+gbdbsGsId,eleToString(gsEle));
        }
	}
	private void convertRecht(JAXBElement rEle)
			throws XMLStreamException, JAXBException {
	    Object recht = rEle.getValue();
		//logger.debug("gbdbsRechtId {}",gbdbsRechtId);
			// add refs that have to be fixed
        String gbdbsRechtId = null;
        if(recht instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.RechtType){
            gbdbsRechtId=((ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.RechtType)recht).getNummer();
        }else if(recht instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.RechtType){
            gbdbsRechtId=((ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.RechtType)recht).getNummer();
        }else {
            throw new IllegalStateException();
        }
        JAXBElement inhaltEle=Rechte.getCurrentInhaltRecht(rEle);
        boolean istAktuell=inhaltEle!=null;
        if(istAktuell) {
            anzahlRechte++;
        }
        ch.ehi.gbdbsvalidator.jaxb.gbbasisid._2_0.RechtIdDefType rId = GbdbsValidator.parseGbdbsRechtId(gbdbsRechtId);
        String ereid=rId.getEREID();
        if(ereid==null) {
            if(istAktuell) {
                warning(Logger.ID_1_6_FEHLENDE_EREID,gbdbsRechtId,"Recht ohne EREID "+gbdbsRechtId);
                anzahlRechteOhneEREID++;
            }
        }else {
            if(!EreidGenerator.validateEreid(ereid)) {
                if(istAktuell) {
                    warning(Logger.ID_1_12_EREID_HAT_FALSCHES_FORMAT,gbdbsRechtId,"Ung\u00fcltige EREID "+gbdbsRechtId);
                }
            }
        }
	    
			if(recht instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.EigentumAnteilType){
		        if(istAktuell) {
	                anzahlEigentumAnteil+=1;
		        }
			    ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.EigentumAnteilType r=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.EigentumAnteilType)recht;
				int inhaltc=r.getInhaltRecht().size();
                final String gsNr = r.getBelastetesGrundstueck();
                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltEigentumAnteilType inhalt=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltEigentumAnteilType)getCurrentInhaltEigentumAnteil(rEle);
				if(inhalt!=null){
					XMLGregorianCalendar von = inhalt.getBegruendungTagebuchDatumZeit();
					XMLGregorianCalendar bis = inhalt.getTagebuchDatumZeit();
                    final String eigentuemerId = r.getBerechtigte();
                    if(eigentuemerId==null && !"Herrenlos".equals(inhalt.getEigentumsform())) {
                        if(istAktuell) {
                            warning(Logger.ID_2_6_EIGENTUM_ANTEIL_OHNE_BERECHTIGTE,gbdbsRechtId,"EigentumAnteil ohne Berechtigte "+gbdbsRechtId);
                        }
                    }
                    Eigentuemer aenderung=new Eigentuemer();
                    aenderung.setRecordTyp(GbdbsValidator.RECORD_TYP_EIGENTUEMER);
                    setGsNr(gsNr,aenderung);
                    aenderung.setEigentumAnteilId(gbdbsRechtId);
                    aenderung.setEigentuemerId(eigentuemerId);
                    aenderung.setEigentumAnteilVom(von);
                    if(inhalt.getAnteilZaehler()!=null && inhalt.getAnteilNenner()!=null) {
                        aenderung.setQuoteZaehler(inhalt.getAnteilZaehler());
                        aenderung.setQuoteNenner(inhalt.getAnteilNenner());
                    }else {
                        aenderung.setQuoteProsa(inhalt.getAnteilInProsa());
                    }
                    addEigentuemer(gsNr,aenderung);
				}
			}else if(recht instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.EigentumAnteilType){
		        if(istAktuell) {
	                anzahlEigentumAnteil+=1;
		        }
                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.EigentumAnteilType r=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.EigentumAnteilType)recht;
                final String gsNr = r.getBelastetesGrundstueck();
                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltEigentumAnteilType inhalt=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltEigentumAnteilType)getCurrentInhaltEigentumAnteil(rEle);
                if(inhalt!=null){
                    XMLGregorianCalendar von = inhalt.getVonTagebuchDatumZeit();
                    XMLGregorianCalendar bis = inhalt.getBisTagebuchDatumZeit();
                    //System.out.println("Eigentuemer "+von+", "+bis);
                    final String eigentuemerId = r.getBerechtigte();
                    if(eigentuemerId==null && !"Herrenlos".equals(inhalt.getEigentumsform())) {
                        if(istAktuell) {
                            warning(Logger.ID_2_6_EIGENTUM_ANTEIL_OHNE_BERECHTIGTE,gbdbsRechtId,"EigentumAnteil ohne Berechtigte "+gbdbsRechtId);
                        }
                    }
                    Eigentuemer aenderung=new Eigentuemer();
                    aenderung.setRecordTyp(GbdbsValidator.RECORD_TYP_EIGENTUEMER);
                    setGsNr(gsNr,aenderung);
                    aenderung.setEigentumAnteilId(gbdbsRechtId);
                    aenderung.setEigentuemerId(eigentuemerId);
                    aenderung.setEigentumAnteilVom(von);
                    if(inhalt.getAnteilZaehler()!=null && inhalt.getAnteilNenner()!=null) {
                        aenderung.setQuoteZaehler(inhalt.getAnteilZaehler());
                        aenderung.setQuoteNenner(inhalt.getAnteilNenner());
                    }else {
                        aenderung.setQuoteProsa(inhalt.getAnteilInProsa());
                    }
                    addEigentuemer(gsNr,aenderung);
                }
			}else if(recht instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.GrundpfandrechtType){
                if(istAktuell) {
                    anzahlGrundpfandrecht++;
                }
            }else if(recht instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.GrundpfandrechtType){
                if(istAktuell) {
                    anzahlGrundpfandrecht++;
                }
			}else if(recht instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.DienstbarkeitType){
                if(istAktuell) {
                    anzahlDienstbarkeit++;
                }
            }else if(recht instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.DienstbarkeitType){
                if(istAktuell) {
                    anzahlDienstbarkeit++;
                }
			}else if(recht instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.GrundlastType){
                if(istAktuell) {
                    anzahlGrundlast++;
                }
            }else if(recht instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.GrundlastType){
                if(istAktuell) {
                    anzahlGrundlast++;
                }
			}else if(recht instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.VormerkungType){
                if(istAktuell) {
                    anzahlVormerkung++;
                }
            }else if(recht instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.VormerkungType){
                if(istAktuell) {
                    anzahlVormerkung++;
                }
			}else if(recht instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.AnmerkungType){
		        if(istAktuell) {
	                anzahlAnmerkung++;
		        }
                if(inhaltEle!=null) {
                    ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltAnmerkungType inhalt=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltAnmerkungType)inhaltEle.getValue();
                    if(inhalt.isIstOeffentlich()==null) {
                        anzahlAnmerkungOhneOeffentlich++;
                    }else if(inhalt.isIstOeffentlich()){
                        anzahlAnmerkungOeffentlich++;
                    }
                }
            }else if(recht instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.AnmerkungType){
                if(istAktuell) {
                    anzahlAnmerkung++;
                }
                if(inhaltEle!=null) {
                    ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltAnmerkungType inhalt=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltAnmerkungType)inhaltEle.getValue();
                    if(inhalt.isIstOeffentlich()==null) {
                        anzahlAnmerkungOhneOeffentlich++;
                    }else if(inhalt.isIstOeffentlich()){
                        anzahlAnmerkungOeffentlich++;
                    }
                }
			}else{
				throw new IllegalStateException("unexpected type "+recht.getClass());
			}
	}

	private Object getCurrentInhaltEigentumAnteil(JAXBElement rEle) {
        Object recht = rEle.getValue();
        ArrayList<Object> ret=new ArrayList<Object>();
            if(recht instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.EigentumAnteilType){
                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.EigentumAnteilType r=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.EigentumAnteilType)recht;
                int inhaltc=r.getInhaltRecht().size();
                String gbdbsRechtId = ((ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.EigentumAnteilType)recht).getNummer();
                final String gsNr = r.getBelastetesGrundstueck();
                for(JAXBElement<? extends ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltRechtType> inhaltEle:r.getInhaltRecht()){
                    ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltEigentumAnteilType inhalt=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltEigentumAnteilType)inhaltEle.getValue();
                    XMLGregorianCalendar von = inhalt.getBegruendungTagebuchDatumZeit();
                    XMLGregorianCalendar bis = inhalt.getTagebuchDatumZeit();
                    if(von!=null && bis==null){
                        ret.add(inhalt);
                    }else if(inhaltc==1 && von==null && bis==null) { // fix issue (kein "von" und kein "bis")
                        warning(Logger.ID_2_5_INHALT_BEI_EIGENTUM_ANTEIL_OHNE_VON_BIS,gbdbsRechtId,"EigentumAnteil ohne von/bis "+gbdbsRechtId);
                        ret.add(inhalt);
                    }
                }
            }else if(recht instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.EigentumAnteilType){
                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.EigentumAnteilType r=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.EigentumAnteilType)recht;
                int inhaltc=r.getInhaltRecht().size();
                String gbdbsRechtId = ((ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.EigentumAnteilType)recht).getNummer();
                for(JAXBElement<? extends ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltRechtType> inhaltEle:r.getInhaltRecht()){
                    ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltEigentumAnteilType inhalt=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltEigentumAnteilType)inhaltEle.getValue();
                    XMLGregorianCalendar von = inhalt.getVonTagebuchDatumZeit();
                    XMLGregorianCalendar bis = inhalt.getBisTagebuchDatumZeit();
                    if(von!=null && bis==null){
                        ret.add(inhalt);
                    }else if(inhaltc==1 && von==null && bis==null) { // fix issue (kein "von" und kein "bis")
                        warning(Logger.ID_2_5_INHALT_BEI_EIGENTUM_ANTEIL_OHNE_VON_BIS,gbdbsRechtId,"EigentumAnteil ohne von/bis "+gbdbsRechtId);
                        ret.add(inhalt);
                    }
                }
            }
        if(ret.size()==0) {
            return null;
        }
        return ret.get(ret.size()-1);
    }
    private Object getCurrentInhaltRecht(JAXBElement rEle) {
        Object recht = rEle.getValue();
        ArrayList<Object> ret=new ArrayList<Object>();
            if(recht instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.RechtType){
                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.RechtType r=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.RechtType)recht;
                int inhaltc=r.getInhaltRecht().size();
                String gbdbsRechtId = ((ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.RechtType)recht).getNummer();
                //final String gsNr = r.getBelastetesGrundstueck();
                for(JAXBElement<? extends ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltRechtType> inhaltEle:r.getInhaltRecht()){
                    ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltRechtType inhalt=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltRechtType)inhaltEle.getValue();
                    XMLGregorianCalendar von = inhalt.getBegruendungTagebuchDatumZeit();
                    XMLGregorianCalendar bis = inhalt.getTagebuchDatumZeit();
                    if(von!=null && bis==null){
                        ret.add(inhalt);
                    }else if(inhaltc==1 && von==null && bis==null) { // fix issue (kein "von" und kein "bis")
                        warning(Logger.ID_12_2_INHALT_BEI_RECHT_OHNE_VON_BIS,gbdbsRechtId,"Recht ohne von/bis "+gbdbsRechtId);
                        ret.add(inhalt);
                    }
                }
            }else if(recht instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.RechtType){
                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.RechtType r=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.RechtType)recht;
                int inhaltc=r.getInhaltRecht().size();
                String gbdbsRechtId = ((ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.RechtType)recht).getNummer();
                for(JAXBElement<? extends ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltRechtType> inhaltEle:r.getInhaltRecht()){
                    ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltRechtType inhalt=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltRechtType)inhaltEle.getValue();
                    XMLGregorianCalendar von = inhalt.getVonTagebuchDatumZeit();
                    XMLGregorianCalendar bis = inhalt.getBisTagebuchDatumZeit();
                    if(von!=null && bis==null){
                        ret.add(inhalt);
                    }else if(inhaltc==1 && von==null && bis==null) { // fix issue (kein "von" und kein "bis")
                        warning(Logger.ID_12_2_INHALT_BEI_RECHT_OHNE_VON_BIS,gbdbsRechtId,"Recht ohne von/bis "+gbdbsRechtId);
                        ret.add(inhalt);
                    }
                }
            }
        if(ret.size()==0) {
            return null;
        }
        return ret.get(ret.size()-1);
    }

    private void setGsNr(String gsNr, Eigentuemer aenderung) {
        ch.ehi.gbdbsvalidator.jaxb.gbbasisid._2_0.GrundstueckNummerDefType gsId = GbdbsValidator.parseGbdbsGsId(gsNr);
        aenderung.setEGRID(gsId.getEGRID());
        aenderung.setGsNummer(gsId.getNummer());
        aenderung.setGsNummerZusatz(gsId.getNummerZusatz());
        aenderung.setGsSubKreis(gsId.getSubKreis());
        aenderung.setGsLos(gsId.getLos());
        
    }

    private void addEigentuemer(String gsNr,Eigentuemer aenderung) throws XMLStreamException, JAXBException {
        String idxTxt=index.get(PREFIX_EIG+gsNr);
        Eigentuemerindex idx=null;
        if(idxTxt!=null) {
            idx=(Eigentuemerindex)parseXml(idxTxt);
        }else {
            idx=new Eigentuemerindex();
        }
        idx.getEigentuemer().add(aenderung);
		index.put(PREFIX_EIG+gsNr,eleToString(idx));
	}

	private void convertPerson(JAXBElement pEle)
			throws XMLStreamException, JAXBException {
        String gbdbsPersId = null;
	    final Object pers = pEle.getValue();
        if(pers instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.PersonGBType) {
	        gbdbsPersId = ((ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.PersonGBType)pers).getNummer();
	    }else if(pers instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.PersonGBType) {
            gbdbsPersId = ((ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.PersonGBType)pers).getNummer();
        }else {
            throw new IllegalStateException();
        }
        JAXBElement inhaltEle=Personen.getCurrentInhaltPersonGB(pEle);
        boolean istAktuell=inhaltEle!=null;
        if(istAktuell) {
            anzahlPersonen++;
        }
        ch.ehi.gbdbsvalidator.jaxb.gbbasisid._2_0.PersonIdDefType pId = GbdbsValidator.parseGbdbsPersId(gbdbsPersId);
        String egbpid=pId.getEGBPID();
        if(egbpid==null) {
            if(istAktuell) {
                warningGruen(Logger.ID_1_7_FEHLENDE_EGBPID,gbdbsPersId,"Person ohne EGBPID "+gbdbsPersId);
                anzahlPersonenOhneEGBPID++;
            }
        }else {
            if(!EgridGenerator.validateEgrid(egbpid)) {
                if(istAktuell) {
                    warning(Logger.ID_1_13_EGBPID_HAT_FALSCHES_FORMAT,gbdbsPersId,"Ung\u00fcltige EGBPID "+gbdbsPersId);
                }
            }
        }
        String uid=pId.getUID();
        String ahvn13=pId.getAHVN13();
        if(inhaltEle!=null) {
            Object inhalt=inhaltEle.getValue();
            if(inhalt instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltJuristischePersonGBType) {
                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltJuristischePersonGBType inhaltJuristischePersonGB = (ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltJuristischePersonGBType) inhalt;
                if(uid==null) {
                    uid=inhaltJuristischePersonGB.getUID();
                }
            }else if(inhalt instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltJuristischePersonGBType) {
                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltJuristischePersonGBType inhaltJuristischePersonGB = (ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltJuristischePersonGBType) inhalt;
                if(uid==null) {
                    uid=inhaltJuristischePersonGB.getUID();
                }
            }
        }
        if(pers instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.JuristischePersonGBType || pers instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.JuristischePersonGBType) {
            if(istAktuell) {
                anzahlJuristischePersonen++;
            }
            if(uid!=null) {
                if(istAktuell) {
                    anzahlJuristischePersonenMitUID++;
                }
            }
        }else if(pers instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.NatuerlichePersonGBType || pers instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.NatuerlichePersonGBType) {
            if(istAktuell) {
                anzahlNatuerlichePersonen++;
            }
            if(ahvn13!=null) {
                if(istAktuell) {
                    anzahlNatuerlichePersonenMitAHVN13++;
                }
            }
        }else if(pers instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.GemeinschaftType) {
            ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.GemeinschaftType p=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.GemeinschaftType)pers;
            if(istAktuell) {
                anzahlGemeinschaften++;
            }
            if(p.getMitglieder().size()==0) {
                if(istAktuell) {
                    anzahlGemeinschaftenOhneMitglieder++;
                }
            }
        }else if(pers instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.GemeinschaftType) {
            ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.GemeinschaftType p=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.GemeinschaftType)pers;
            if(istAktuell) {
                anzahlGemeinschaften++;
            }
            if(p.getMitglieder().size()==0) {
                if(istAktuell) {
                    anzahlGemeinschaftenOhneMitglieder++;
                }
            }
        }
        if(NO_SHARED_MAP || index.get(PREFIX_PERS+gbdbsPersId)==null) {
            index.put(PREFIX_PERS+gbdbsPersId, eleToString(pEle));
        }
	}

	private String eleToString(Object obj) throws XMLStreamException,
			JAXBException {
		java.io.StringWriter xmlgs = new java.io.StringWriter();
		XMLStreamWriter w = xmlof.createXMLStreamWriter(xmlgs);
		ms.marshal(obj, w);
		w.flush();
		w.close();
		return xmlgs.toString();

	}
	private Object parseXml(String xml) throws JAXBException
	  {
	    if(xml==null) {
	        return null;
	    }
			java.io.StringReader xmlr=new StringReader(xml);
			  javax.xml.stream.XMLStreamReader r;
			try {
				r = xmlif.createXMLStreamReader(xmlr);
			} catch (XMLStreamException e) {
				throw new IllegalStateException(e);
			}
			  return um.unmarshal(r);
	  }

	
	static public ch.ehi.gbdbsvalidator.jaxb.gbbasisid._2_0.GrundstueckNummerDefType parseGbdbsGsId(String gbdbsId) {
	    ch.ehi.gbdbsvalidator.jaxb.gbbasisid._2_0.GrundstueckNummerDefType ret = new ch.ehi.gbdbsvalidator.jaxb.gbbasisid._2_0.GrundstueckNummerDefType();
		String idv[] = gbdbsId.split(":", 6);
		String egrid = idv[0];
		if (egrid.length() != 0) {
			ret.setEGRID(idv[0]);
		}
		String nummer = idv[1];
		if (nummer.length() != 0) {
			ret.setNummer(nummer);
		}
		String nummerZusatz = idv[2];
		if (nummerZusatz.length() != 0) {
			ret.setNummerZusatz(nummerZusatz);
		}
		String subKreis = idv[3];
		if (subKreis.length() != 0) {
			ret.setSubKreis(subKreis);
		}
		String los = idv[4];
		if (los.length() != 0) {
			ret.setLos(los);
		}
		return ret;
	}

	static public ch.ehi.gbdbsvalidator.jaxb.gbbasisid._2_0.PersonIdDefType parseGbdbsPersId(String gbdbsId) {
	    ch.ehi.gbdbsvalidator.jaxb.gbbasisid._2_0.PersonIdDefType ret = new ch.ehi.gbdbsvalidator.jaxb.gbbasisid._2_0.PersonIdDefType();
		String idv[] = gbdbsId.split(":", 6);
		String egbpid = idv[0].trim();
		if (egbpid.length() > 0) {
			ret.setEGBPID(idv[0]);
		}
		String kantPersNr = idv[1].trim();
		if (kantPersNr.length() > 0) {
			ret.setKantPersNr(kantPersNr);
		}
		String uid = idv[2].trim();
		if (uid.length() > 0) {
			ret.setUID(uid);
		}
		String ahvn13 = idv[3].trim();
		if (ahvn13.length() > 0) {
			ret.setAHVN13(ahvn13);
		}
		String localId = idv[4].trim();
		if (localId.length() > 0) {
			ret.setLocalId(localId);
		}
		return ret;
	}

	static public ch.ehi.gbdbsvalidator.jaxb.gbbasisid._2_0.RechtIdDefType parseGbdbsRechtId(String gbdbsId) {
	    ch.ehi.gbdbsvalidator.jaxb.gbbasisid._2_0.RechtIdDefType ret = new ch.ehi.gbdbsvalidator.jaxb.gbbasisid._2_0.RechtIdDefType();
		String idv[] = gbdbsId.split(":", 4);
		String ereid = idv[0];
		if (ereid.length() != 0) {
			ret.setEREID(ereid);
		}
		String alteNummer = idv[1];
		if (alteNummer.length() != 0) {
			ret.setAlteNummer(alteNummer);
		}
		String localId = idv[2];
		if (localId.length() != 0) {
			ret.setLocalId(localId);
		}
		return ret;
	}
	static public boolean isEqualPersGbdbsId(String id1,String id2)
	{
		if(id1.equals(id2)){
			return true;
		}
		ch.ehi.gbdbsvalidator.jaxb.gbbasisid._2_0.PersonIdDefType id1s = parseGbdbsPersId(id1);
		ch.ehi.gbdbsvalidator.jaxb.gbbasisid._2_0.PersonIdDefType id2s = parseGbdbsPersId(id2);
		if(id1s.getEGBPID()!=null && id2s.getEGBPID()!=null && id1s.getEGBPID().equals(id2s.getEGBPID())){
			return true;
		}
		if(id1s.getUID()!=null && id2s.getUID()!=null && id1s.getUID().equals(id2s.getUID())){
			return true;
		}
		if(id1s.getAHVN13()!=null && id2s.getAHVN13()!=null && id1s.getAHVN13().equals(id2s.getAHVN13())){
			return true;
		}
		return false;
	}
	public static boolean isEqualGsGbdbsId(String id1,String id2) {
		if(id1.equals(id2)){
			return true;
		}
		ch.ehi.gbdbsvalidator.jaxb.gbbasisid._2_0.GrundstueckNummerDefType id1s = parseGbdbsGsId(id1);
		ch.ehi.gbdbsvalidator.jaxb.gbbasisid._2_0.GrundstueckNummerDefType id2s = parseGbdbsGsId(id2);
		if(id1s.getEGRID()!=null && id2s.getEGRID()!=null && id1s.getEGRID().equals(id2s.getEGRID())){
			return true;
		}
		if(id1s.getNummer()!=null && id2s.getNummer()!=null && id1s.getNummer().equals(id2s.getNummer())
				&& ((id1s.getNummerZusatz()==null && id2s.getNummerZusatz()==null) || (id1s.getNummerZusatz()!=null && id2s.getNummerZusatz()!=null && id1s.getNummerZusatz().equals(id2s.getNummerZusatz())))
				&& id1s.getSubKreis()!=null && id2s.getSubKreis()!=null && id1s.getSubKreis().equals(id2s.getSubKreis())
				&& ((id1s.getLos()==null && id2s.getLos()==null)||(id1s.getLos()!=null && id2s.getLos()!=null && id1s.getLos().equals(id2s.getLos())))){
			return true;
		}
		return false;
	}

	public static String toString(ch.ehi.gbdbsvalidator.jaxb.gbbasisid._2_0.GrundstueckNummerDefType gbdbsId) {
		return (gbdbsId.getEGRID()!=null?gbdbsId.getEGRID():"")
		+":"+(gbdbsId.getNummer()!=null?gbdbsId.getNummer():"")
		+":"+(gbdbsId.getNummerZusatz()!=null?gbdbsId.getNummerZusatz():"")
		+":"+(gbdbsId.getSubKreis()!=null?gbdbsId.getSubKreis():"")
		+":"+(gbdbsId.getLos()!=null?gbdbsId.getLos():"")
		+":"+(gbdbsId.getSystemPrefix()!=null?gbdbsId.getSystemPrefix():"")
		;
	}
    public static String toString(ch.ehi.gbdbsvalidator.jaxb.gbbasisid._2_1.GrundstueckNummerDefType gbdbsId) {
        return (gbdbsId.getEGRID()!=null?gbdbsId.getEGRID():"")
        +":"+(gbdbsId.getNummer()!=null?gbdbsId.getNummer():"")
        +":"+(gbdbsId.getNummerZusatz()!=null?gbdbsId.getNummerZusatz():"")
        +":"+(gbdbsId.getSubKreis()!=null?gbdbsId.getSubKreis():"")
        +":"+(gbdbsId.getLos()!=null?gbdbsId.getLos():"")
        ;
    }

	public static String toString(ch.ehi.gbdbsvalidator.jaxb.gbbasisid._2_0.PersonIdDefType gbdbsId) {
		return (gbdbsId.getEGBPID()!=null?gbdbsId.getEGBPID():"")
		+":"+(gbdbsId.getKantPersNr()!=null?gbdbsId.getKantPersNr():"")
		+":"+(gbdbsId.getUID()!=null?gbdbsId.getUID():"")
		+":"+(gbdbsId.getAHVN13()!=null?gbdbsId.getAHVN13():"")
		+":"+(gbdbsId.getLocalId()!=null?gbdbsId.getLocalId():"")
		+":"+(gbdbsId.getSystemPrefix()!=null?gbdbsId.getSystemPrefix():"")
		;
	}
    public static String toString(ch.ehi.gbdbsvalidator.jaxb.gbbasisid._2_1.PersonIdDefType gbdbsId) {
        return (gbdbsId.getEGBPID()!=null?gbdbsId.getEGBPID():"")
        +":"+(gbdbsId.getKantPersNr()!=null?gbdbsId.getKantPersNr():"")
        +":"+(gbdbsId.getUID()!=null?gbdbsId.getUID():"")
        +":"+(gbdbsId.getAHVN13()!=null?gbdbsId.getAHVN13():"")
        +":"+(gbdbsId.getLocalId()!=null?gbdbsId.getLocalId():"")
        ;
    }
	public static String toString(ch.ehi.gbdbsvalidator.jaxb.gbbasisid._2_0.RechtIdDefType gbdbsId) {
		return (gbdbsId.getEREID()!=null?gbdbsId.getEREID():"")
			+":"+(gbdbsId.getAlteNummer()!=null?gbdbsId.getAlteNummer():"")
			+":"+(gbdbsId.getLocalId()!=null?gbdbsId.getLocalId():"")
			+":"+(gbdbsId.getSystemPrefix()!=null?gbdbsId.getSystemPrefix():"")
			;
	}
    public static String toString(ch.ehi.gbdbsvalidator.jaxb.gbbasisid._2_1.RechtIdDefType gbdbsId) {
        return (gbdbsId.getEREID()!=null?gbdbsId.getEREID():"")
            +":"+(gbdbsId.getAlteNummer()!=null?gbdbsId.getAlteNummer():"")
            +":"+(gbdbsId.getLocalId()!=null?gbdbsId.getLocalId():"")
            ;
    }
    private boolean dateInCurrentYear(XMLGregorianCalendar val) {
        return dateInRange(val,stichjahr);
    }
    private static boolean dateInRange(XMLGregorianCalendar von, int jahr) {
        return von.toGregorianCalendar().after(new GregorianCalendar(jahr-1,11,31)) && von.toGregorianCalendar().before(new GregorianCalendar(jahr+1,0,1));
    }

}
