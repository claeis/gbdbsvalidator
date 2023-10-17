package ch.ehi.gbdbsvalidator.impl;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltPersonGBType;
import ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.PersonGBType;

public class Personen {
    private Personen() {}

    public static Object getNewestInhaltPers(JAXBElement persEle) {
        if(persEle.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.PersonGBType) {
            ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltPersonGBType aktuellsterInhalt=null;
            ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.PersonGBType pers=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.PersonGBType)persEle.getValue();
            for(JAXBElement<? extends ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltPersonGBType> inhaltEle:pers.getInhaltPersonGB()){
                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltPersonGBType inhalt=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltPersonGBType)inhaltEle.getValue();
                XMLGregorianCalendar von = inhalt.getBegruendungTagebuchDatumZeit();
                XMLGregorianCalendar bis = inhalt.getTagebuchDatumZeit();
                if(bis==null){
                    return inhalt;
                }
                if(aktuellsterInhalt==null) {
                    aktuellsterInhalt=inhalt;
                }else if(aktuellsterInhalt.getTagebuchDatumZeit().compare(von)==DatatypeConstants.LESSER) {
                    aktuellsterInhalt=inhalt;
                }
            }
            return aktuellsterInhalt;
        }else if(persEle.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.PersonGBType) {
            ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltPersonGBType aktuellsterInhalt=null;
            ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.PersonGBType pers=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.PersonGBType)persEle.getValue();
            for(JAXBElement<? extends ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltPersonGBType> inhaltEle:pers.getInhaltPersonGB()){
                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltPersonGBType inhalt=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltPersonGBType)inhaltEle.getValue();
                XMLGregorianCalendar von = inhalt.getVonTagebuchDatumZeit();
                XMLGregorianCalendar bis = inhalt.getBisTagebuchDatumZeit();
                if(bis==null){
                    return inhalt;
                }
                if(aktuellsterInhalt==null) {
                    aktuellsterInhalt=inhalt;
                }else if(aktuellsterInhalt.getBisTagebuchDatumZeit().compare(von)==DatatypeConstants.LESSER) {
                    aktuellsterInhalt=inhalt;
                }
            }
            return aktuellsterInhalt;
        }
        return null;
    }
    public static JAXBElement getCurrentInhaltPersonGB(JAXBElement persEle) {
        if(persEle.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.PersonGBType) {
            ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.PersonGBType pers=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.PersonGBType)persEle.getValue();
            for(JAXBElement<? extends ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltPersonGBType> inhaltEle:pers.getInhaltPersonGB()){
                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltPersonGBType inhalt=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltPersonGBType)inhaltEle.getValue();
                XMLGregorianCalendar bis = inhalt.getTagebuchDatumZeit();
                if(bis==null){
                    return inhaltEle;
                }
            }
        }else if(persEle.getValue() instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.PersonGBType) {
            ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.PersonGBType pers=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.PersonGBType)persEle.getValue();
            for(JAXBElement<? extends ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltPersonGBType> inhaltEle:pers.getInhaltPersonGB()){
                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltPersonGBType inhalt=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltPersonGBType)inhaltEle.getValue();
                XMLGregorianCalendar bis = inhalt.getBisTagebuchDatumZeit();
                if(bis==null){
                    return inhaltEle;
                }
            }
        }
        return null;
    }
    
}
