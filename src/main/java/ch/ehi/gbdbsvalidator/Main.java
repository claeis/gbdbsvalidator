package ch.ehi.gbdbsvalidator;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ch.ehi.basics.settings.Settings;

/** Main program and commandline interface of gbdbsvalidator.
 */
public class Main {
	
	/** name of application as shown to user.
	 */
	public static final String APP_NAME="gbdbsvalidator";
	/** name of jar file.
	 */
	public static final String APP_JAR="gbdbsvalidator.jar";
	/** version of application.
	 */
	private static String version=null;
    private static int FC_NOOP=0;
    private static int FC_VALIDATE=1;
	
	/** main program entry.
	 * @param args command line arguments.
	 */
	static public void main(String args[]){
		Settings settings = newSettings();
		Class mainFrame=null;
		try {
            mainFrame=Class.forName(preventOptimziation("ch.ehi.gbdbsvalidator.gui.MainFrame")); // avoid, that graalvm native-image detects a reference to MainFrame
		}catch(ClassNotFoundException ex){
		    // ignore; report later
		}
		Method mainFrameMain=null;
		if(mainFrame!=null) {
		    try {
	            mainFrameMain = mainFrame.getMethod ("showDialog");
		    }catch(NoSuchMethodException ex) {
	            // ignore; report later
		    }
		}
		String outFile=null;
        File xtfFile=null;
		if(args.length==0){
			runGui(mainFrameMain);     
			return;
		}
		int argi=0;
		int function=FC_VALIDATE;
		boolean doGui=false;
		for(;argi<args.length;argi++){
			String arg=args[argi];
			if(arg.equals("--trace")){
				Logger.getInstance().setTraceFilter(false);
			}else if(arg.equals("--gui")){
				readSettings(settings);
				doGui=true;
			}else if(arg.equals("--log")) {
			    argi++;
			    settings.setValue(Main.SETTING_LOGFILE, args[argi]);
            }else if(arg.equals("--xmllog")) {
                argi++;
                settings.setValue(Main.SETTING_XMLLOG, args[argi]);
			}else if(arg.equals("--plugins")) {
			    argi++;
			    settings.setValue(Main.SETTING_PLUGINFOLDER, args[argi]);
			}else if(arg.equals("--version")){
				printVersion();
				return;
			}else if(arg.equals("--help")){
					printVersion ();
					System.err.println();
					printDescription ();
					System.err.println();
					printUsage ();
					System.err.println();
					System.err.println("OPTIONS");
					System.err.println();
					System.err.println("--gui                 start GUI.");
				    System.err.println("--log file            text file, that receives validation results.");
                    System.err.println("--xmllog file         xml file, that receives validation results.");
				    System.err.println("--plugins folder      directory with jar files that contain user defined functions.");
					System.err.println("--trace               enable trace messages.");
					System.err.println("--help                Display this help text.");
					System.err.println("--version             Display the version of "+APP_NAME+".");
					System.err.println();
					return;
				
			}else if(arg.startsWith("-")){
				Logger.getInstance().logGruen(Logger.ALLGEMEINE_MELDUNG,arg+": unknown option; ignored");
			}else{
				break;
			}
		}
        xtfFile=new File(args[argi]);
		if(doGui){
			//MainFrame.main(xtfFile,settings);
            runGui(mainFrameMain);                     
            return;
		}else{
            boolean ok=false;
            if(function==FC_NOOP) {
                ok=true;
            }else if(function==FC_VALIDATE) {
                Validator validator=new Validator();
                ok=validator.validate(xtfFile, settings);
            }else {
                throw new IllegalStateException("function=="+function);
            }
            System.exit(ok ? 0 : 1);
		}
		
	}
    public static Settings newSettings() {
        Settings settings=new Settings();
		String appHome=getAppHome();
		if(appHome!=null){
		    settings.setValue(Main.SETTING_PLUGINFOLDER, new java.io.File(appHome,"plugins").getAbsolutePath());
		    settings.setValue(Main.SETTING_APPHOME, appHome);
		}else{
		    settings.setValue(Main.SETTING_PLUGINFOLDER, new java.io.File("plugins").getAbsolutePath());
		}
        return settings;
    }
    private static String preventOptimziation(String val) {
        StringBuffer buf=new StringBuffer(val.length());
        buf.append(val);
        return buf.toString();
    }
    private static void runGui(Method mainFrameMain) {
        if(mainFrameMain!=null) {
            try {
                mainFrameMain.invoke(null);
                return;                 
            } catch (IllegalArgumentException ex) {
                Logger.getInstance().logSchwarz("failed to open GUI",ex);
            } catch (IllegalAccessException ex) {
                Logger.getInstance().logSchwarz("failed to open GUI",ex);
            } catch (InvocationTargetException ex) {
                Logger.getInstance().logSchwarz("failed to open GUI",ex);
            }
        }else {
            Logger.getInstance().logSchwarz(APP_NAME+": no GUI available");
        }
        System.exit(2);
    }
	/** Name of file with program settings. Only used by GUI, not used by commandline version.
	 */
	private final static String SETTINGS_FILE = System.getProperty("user.home") + "/.gbdbsvalidator";

	/** Reads program settings.
	 * @param settings Program configuration as read from file.
	 */
	public static void readSettings(Settings settings)
	{
		java.io.File file=new java.io.File(SETTINGS_FILE);
		try{
			if(file.exists()){
				settings.load(file);
			}
		}catch(java.io.IOException ex){
			Logger.getInstance().logSchwarz("failed to load settings from file "+SETTINGS_FILE,ex);
		}
	}
	/** Writes program settings.
	 * @param settings Program configuration to write.
	 */
	public static void writeSettings(Settings settings)
	{
		java.io.File file=new java.io.File(SETTINGS_FILE);
		try{
			settings.store(file,APP_NAME+" settings");
		}catch(java.io.IOException ex){
			Logger.getInstance().logSchwarz("failed to settings settings to file "+SETTINGS_FILE,ex);
		}
	}
	
	/** Prints program version.
	 */
	protected static void printVersion ()
	{
	  System.err.println(APP_NAME+", Version "+getVersion());
	  System.err.println("  Developed by Eisenhut Informatik AG, CH-3400 Burgdorf");
	}

	/** Prints program description.
	 */
	protected static void printDescription ()
	{
	  System.err.println("DESCRIPTION");
	  System.err.println("  creates simplified, derived Interlis data.");
	}

	/** Prints program usage.
	 */
	protected static void printUsage()
	{
	  System.err.println ("USAGE");
	  System.err.println("  java -jar "+APP_JAR+" [Options]");
	}
	/** Gets version of program.
	 * @return version e.g. "1.0.0"
	 */
	public static String getVersion() {
		  if(version==null){
		java.util.ResourceBundle resVersion = java.util.ResourceBundle.getBundle(class2qpackageName(Main.class)+".Version");
			StringBuffer ret=new StringBuffer(20);
		ret.append(resVersion.getString("version"));
			ret.append('-');
		ret.append(resVersion.getString("versionCommit"));
			version=ret.toString();
		  }
		  return version;
	}
    public static String class2qpackageName(Class aClass){
        String className = aClass.getName();
        int index = className.lastIndexOf('.');
        String file = className.substring(0, index);    
        return file;
      }
	
	/** Gets main folder of program.
	 * 
	 * @return folder Main folder of program.
	 */
	static public String getAppHome()
	{
	  String[] classpaths = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
	  for(String classpath:classpaths) {
		  if(classpath.toLowerCase().endsWith(".jar")) {
			  File file = new File(classpath);
			  String jarName=file.getName();
			  if(jarName.toLowerCase().startsWith(APP_NAME)) {
				  file=new File(file.getAbsolutePath());
				  if(file.exists()) {
					  return file.getParent();
				  }
			  }
		  }
	  }
	  return null;
	}

	
    /** the main folder of program.
     */
    public static final String SETTING_APPHOME="ch.ehi.gbdbsvalidator.appHome";
    /** Name of the folder that contains jar files with plugins.
     */
    public static final String SETTING_PLUGINFOLDER = "ch.ehi.gbdbsvalidator.pluginfolder";
    /** Name of the log file that receives the log messages.
     */
    public static final String SETTING_LOGFILE = "ch.ehi.gbdbsvalidator.log";
    public static final String SETTING_XMLLOG = "ch.ehi.gbdbsvalidator.xtflog";
    public static final String SETTING_DISKFILE = "ch.ehi.gbdbsvalidator.diskfile";
    public static final String WORKING_DIRECTORY = "ch.ehi.gbdbsvalidator.workingdir";
    public static final String SETTING_LOGFILE_TIMESTAMP = "ch.ehi.gbdbsvalidator.logtime";
}
