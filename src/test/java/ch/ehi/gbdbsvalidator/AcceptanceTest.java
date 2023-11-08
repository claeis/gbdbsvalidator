package ch.ehi.gbdbsvalidator;

import static org.junit.Assert.*;

import java.io.File;
import org.junit.Test;

import ch.ehi.basics.settings.Settings;


public class AcceptanceTest {
    private static final String TEST_DATA="src/test/data/Acceptance/";
    private static final String OUT_FOLDER = "build/out";
    
    @org.junit.BeforeClass
    public static void setup() {
        new File(OUT_FOLDER).mkdirs();
    }

    @Test
    public void gbdbs21_gruen() throws Exception {
        Settings settings = new Settings();
        final File xmllogFile = new File(OUT_FOLDER,"log.xml");
        settings.setValue(Main.SETTING_XMLLOG, xmllogFile.getAbsolutePath());
        final File logFile = new File(OUT_FOLDER,"log.txt");
        settings.setValue(Main.SETTING_LOGFILE, logFile.getAbsolutePath());
        boolean ret = new Validator().validate(new File(TEST_DATA,"gbdbs21-gruen.xml"),settings);
        assertTrue(ret);

    }
    @Test
    public void gbdbs20_gruen() throws Exception {
        Settings settings = new Settings();
        final File xmllogFile = new File(OUT_FOLDER,"log.xml");
        settings.setValue(Main.SETTING_XMLLOG, xmllogFile.getAbsolutePath());
        final File logFile = new File(OUT_FOLDER,"log.txt");
        settings.setValue(Main.SETTING_LOGFILE, logFile.getAbsolutePath());
        boolean ret = new Validator().validate(new File(TEST_DATA,"gbdbs20-gruen.xml"),settings);
        assertTrue(ret);

    }
    

}
