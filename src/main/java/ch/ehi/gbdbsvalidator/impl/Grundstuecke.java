package ch.ehi.gbdbsvalidator.impl;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.BergwerkType;
import ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.FlaecheType;
import ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.GrundstueckType;
import ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltGrundstueckType;
import ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.InhaltPersonGBType;
import ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.LiegenschaftType;
import ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.MiteigentumsAnteilType;
import ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.PersonGBType;
import ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.SelbstaendigesDauerndesRechtType;

public class Grundstuecke {
    private Grundstuecke() {}
    public static XMLGregorianCalendar getEintragungsdatum(GrundstueckType grundstueck) {
        InhaltGrundstueckType oldest=getFirstInhaltGrundstueck(grundstueck);
        return oldest.getBegruendungTagebuchDatumZeit();
    }
    public static InhaltGrundstueckType getFirstInhaltGrundstueck(GrundstueckType gs) {
        InhaltGrundstueckType ersterInhalt=null;
        for(JAXBElement<? extends InhaltGrundstueckType> inhaltEle:gs.getInhaltGrundstueck()){
            InhaltGrundstueckType inhalt=(InhaltGrundstueckType)inhaltEle.getValue();
            XMLGregorianCalendar von = inhalt.getBegruendungTagebuchDatumZeit();
            XMLGregorianCalendar bis = inhalt.getTagebuchDatumZeit();
            if(ersterInhalt==null) {
                ersterInhalt=inhalt;
            }else if(ersterInhalt.getBegruendungTagebuchDatumZeit().compare(von)==DatatypeConstants.GREATER) {
                ersterInhalt=inhalt;
            }
        }
        return ersterInhalt;
    }
    
    public static String convertGsArtEIGTYP(JAXBElement grundstueckEle) {
    	String ret="Grundstueck";
    	Object grundstueck=grundstueckEle.getValue();
    	if(grundstueck instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.LiegenschaftType) {
    		ret="Liegenschaft";
    	}else if(grundstueck instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.BergwerkType) {
    		ret="Bergwerk";
    	}else if(grundstueck instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.MiteigentumsAnteilType) {
    		ret="MiteigentumsAnteil";
    	}else if(grundstueck instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.SelbstaendigesDauerndesRechtType) {
    		ret="SelbstaendigesDauerndesRecht";
    	}else if(grundstueck instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.LiegenschaftType) {
            ret="Liegenschaft";
        }else if(grundstueck instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.BergwerkType) {
            ret="Bergwerk";
        }else if(grundstueck instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.MiteigentumsAnteilType) {
            ret="MiteigentumsAnteil";
        }else if(grundstueck instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.SelbstaendigesDauerndesRechtType) {
            ret="SelbstaendigesDauerndesRecht";
    	}
    	return ret;
    }
    public static String convertGsArtIMPI(JAXBElement grundstueckEle) {
        String ret="Grundstueck";
        Object grundstueck=grundstueckEle.getValue();
        if(grundstueck instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.LiegenschaftType) {
            ret="Liegenschaft";
        }else if(grundstueck instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.GewoehnlichesSDRType) {
            ret="GewoehnlichesSDR";
        }else if(grundstueck instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.KonzessionType) {
            ret="Konzession";
        }else if(grundstueck instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.SelbstaendigesDauerndesRechtType) {
            ret="SelbstaendigesDauerndesRecht";
        }else if(grundstueck instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.BergwerkType) {
            ret="Bergwerk";
        }else if(grundstueck instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.StockwerksEinheitType) {
            ret="StockwerksEinheit";
        }else if(grundstueck instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.GewoehnlichesMiteigentumType) {
            ret="GewoehnlichesMiteigentum";
        }else if(grundstueck instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.MiteigentumsAnteilType) {
            ret="MiteigentumsAnteil";
        }else if(grundstueck instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.LiegenschaftType) {
            ret="Liegenschaft";
        }else if(grundstueck instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.GewoehnlichesSDRType) {
            ret="GewoehnlichesSDR";
        }else if(grundstueck instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.KonzessionType) {
            ret="Konzession";
        }else if(grundstueck instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.SelbstaendigesDauerndesRechtType) {
            ret="SelbstaendigesDauerndesRecht";
        }else if(grundstueck instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.BergwerkType) {
            ret="Bergwerk";
        }else if(grundstueck instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.StockwerksEinheitType) {
            ret="StockwerksEinheit";
        }else if(grundstueck instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.GewoehnlichesMiteigentumType) {
            ret="GewoehnlichesMiteigentum";
        }else if(grundstueck instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_1.MiteigentumsAnteilType) {
            ret="MiteigentumsAnteil";
        }
        return ret;
    }
    public static FlaecheType getCurrentInhaltFlaeche(GrundstueckType grundstueck) {
        if(grundstueck instanceof ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.SelbstaendigesDauerndesRechtType) {
            ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.SelbstaendigesDauerndesRechtType sdr=(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.SelbstaendigesDauerndesRechtType)grundstueck;
            for(ch.ehi.gbdbsvalidator.jaxb.gbbasistypen._2_0.SelbstaendigesDauerndesRechtType.Flaeche flaecheEle:sdr.getFlaeche()) {
                FlaecheType inhalt=flaecheEle.getFlaeche();
                XMLGregorianCalendar bis = inhalt.getTagebuchDatumZeit();
                if(bis==null){
                    return inhalt;
                }
            }
        }
        return null;
    }

}
