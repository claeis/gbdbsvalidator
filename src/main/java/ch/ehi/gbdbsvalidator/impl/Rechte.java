package ch.ehi.gbdbsvalidator.impl;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;

public class Rechte {
    private Rechte() {}
    public static JAXBElement getCurrentInhaltRecht(JAXBElement rEle) {
        Object recht = rEle.getValue();
            if(recht instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.RechtType){
                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.RechtType r=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.RechtType)recht;
                for(JAXBElement<? extends ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltRechtType> inhaltEle:r.getInhaltRecht()){
                    ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltRechtType inhalt=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltRechtType)inhaltEle.getValue();
                    XMLGregorianCalendar bis = inhalt.getTagebuchDatumZeit();
                    if(bis==null){
                        return inhaltEle;
                    }
                }
            }else if(recht instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.RechtType){
                ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.RechtType r=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.RechtType)recht;
                for(JAXBElement<? extends ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltRechtType> inhaltEle:r.getInhaltRecht()){
                    ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltRechtType inhalt=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.InhaltRechtType)inhaltEle.getValue();
                    XMLGregorianCalendar bis = inhalt.getBisTagebuchDatumZeit();
                    if(bis==null){
                        return inhaltEle;
                    }
                }
            }
            return null;
    }

}
