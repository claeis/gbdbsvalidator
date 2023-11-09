package ch.ehi.gbdbsvalidator;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Assert;
import org.junit.Test;

import ch.ehi.gbdbsvalidator.impl.GbdbsValidator;

public class DateComparisonTest {
    
    @Test
    public void lessDate() throws Exception {
        final DatatypeFactory factory = javax.xml.datatype.DatatypeFactory.newInstance();
        XMLGregorianCalendar von1=factory.newXMLGregorianCalendar("2014-12-31");
        XMLGregorianCalendar von2=factory.newXMLGregorianCalendar("2015-01-01");
        Assert.assertEquals(-1, GbdbsValidator.compare(von1, von2));
    }
    @Test
    public void equalDate() throws Exception {
        final DatatypeFactory factory = javax.xml.datatype.DatatypeFactory.newInstance();
        XMLGregorianCalendar von1=factory.newXMLGregorianCalendar("2015-01-01");
        XMLGregorianCalendar von2=factory.newXMLGregorianCalendar("2015-01-01");
        Assert.assertEquals(0, GbdbsValidator.compare(von1, von2));
    }
    @Test
    public void greaterDate() throws Exception {
        final DatatypeFactory factory = javax.xml.datatype.DatatypeFactory.newInstance();
        XMLGregorianCalendar von1=factory.newXMLGregorianCalendar("2015-01-01");
        XMLGregorianCalendar von2=factory.newXMLGregorianCalendar("2014-12-31");
        Assert.assertEquals(1, GbdbsValidator.compare(von1, von2));
    }
    @Test
    public void lessDateTime() throws Exception {
        final DatatypeFactory factory = javax.xml.datatype.DatatypeFactory.newInstance();
        XMLGregorianCalendar von1=factory.newXMLGregorianCalendar("2014-12-31T00:00:01");
        XMLGregorianCalendar von2=factory.newXMLGregorianCalendar("2015-01-01T00:00:01");
        Assert.assertEquals(-1, GbdbsValidator.compare(von1, von2));
    }
    @Test
    public void equalDateTime() throws Exception {
        final DatatypeFactory factory = javax.xml.datatype.DatatypeFactory.newInstance();
        XMLGregorianCalendar von1=factory.newXMLGregorianCalendar("2015-01-01T00:00:01");
        XMLGregorianCalendar von2=factory.newXMLGregorianCalendar("2015-01-01T00:00:01");
        Assert.assertEquals(0, GbdbsValidator.compare(von1, von2));
    }
    @Test
    public void greaterDateTime() throws Exception {
        final DatatypeFactory factory = javax.xml.datatype.DatatypeFactory.newInstance();
        XMLGregorianCalendar von1=factory.newXMLGregorianCalendar("2015-01-01T00:00:01");
        XMLGregorianCalendar von2=factory.newXMLGregorianCalendar("2014-12-31T00:00:01");
        Assert.assertEquals(1, GbdbsValidator.compare(von1, von2));
    }
    @Test
    public void lessMixed() throws Exception {
        final DatatypeFactory factory = javax.xml.datatype.DatatypeFactory.newInstance();
        XMLGregorianCalendar von1=factory.newXMLGregorianCalendar("2014-12-31");
        XMLGregorianCalendar von2=factory.newXMLGregorianCalendar("2015-01-01T00:00:01");
        Assert.assertEquals(-1, GbdbsValidator.compare(von1, von2));
    }
    @Test
    public void equalMixed() throws Exception {
        final DatatypeFactory factory = javax.xml.datatype.DatatypeFactory.newInstance();
        XMLGregorianCalendar von1=factory.newXMLGregorianCalendar("2015-01-01");
        XMLGregorianCalendar von2=factory.newXMLGregorianCalendar("2015-01-01T00:00:01");
        Assert.assertEquals(-1, GbdbsValidator.compare(von1, von2));
    }
    @Test
    public void greaterMixed() throws Exception {
        final DatatypeFactory factory = javax.xml.datatype.DatatypeFactory.newInstance();
        XMLGregorianCalendar von1=factory.newXMLGregorianCalendar("2015-01-01");
        XMLGregorianCalendar von2=factory.newXMLGregorianCalendar("2014-12-31T00:00:01");
        Assert.assertEquals(1, GbdbsValidator.compare(von1, von2));
    }

}
