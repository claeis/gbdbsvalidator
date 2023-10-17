package ch.ehi.gbdbsvalidator.gui;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import ch.ehi.basics.settings.Settings;
import ch.ehi.basics.swing.SwingWorker;
import ch.ehi.basics.tools.StringUtility;
import ch.ehi.basics.view.FileChooser;
import ch.ehi.gbdbsvalidator.Logger;
import ch.ehi.gbdbsvalidator.Main;
import ch.ehi.gbdbsvalidator.Validator;

/** GUI of gbdbsvalidator.
 */
public class MainFrame extends JFrame {
	private static final String WINDOW_HEIGHT = "ch.ehi.gbdbsvalidator.gui.MainFrame.windowHeight";
	private static final String WINDOW_WIDTH = "ch.ehi.gbdbsvalidator.gui.MainFrame.windowWidth";
	private static final String WINDOW_X = "ch.ehi.gbdbsvalidator.gui.MainFrame.windowX";
	private static final String WINDOW_Y = "ch.ehi.gbdbsvalidator.gui.MainFrame.windowY";

	private java.util.ResourceBundle rsrc=java.util.ResourceBundle.getBundle("ch.ehi.gbdbsvalidator.gui.gbdbsvalidator");
	private Settings settings=null;
	private javax.swing.JPanel jContentPane = null;

	private javax.swing.JLabel gbdbsFileLabel = null;
	private javax.swing.JTextField gbdbsFileUi = null;
	private javax.swing.JButton doGbdbsFileSelBtn = null;
	
	private javax.swing.JLabel logFileLabel = null;
	private javax.swing.JTextField logFileUi = null;
	private javax.swing.JButton doLogFileSelBtn = null;
	
	private javax.swing.JLabel xmllogFileLabel = null;
	private javax.swing.JTextField xmllogFileUi = null;
	private javax.swing.JButton doXmllogFileSelBtn = null;
	
	private javax.swing.JTextArea logUi = null;
	private javax.swing.JButton clearlogBtn = null;
	
    private JCheckBoxMenuItem optionsLogTimeItem = null;
	private JCheckBoxMenuItem optionsTraceItem = null;
	
	
	public MainFrame() {
		super();
		initialize();
	}
	private void initialize() {
		this.setSize(550, 370);
		this.setLocationByPlatform(true);
		this.setContentPane(getJContentPane());
		this.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		this.setName(Main.APP_NAME);
		this.setTitle(rsrc.getString("MainFrame.Title"));
		
		//creates a border, which looks like jtextfield border
		gbdbsFileUi.setBorder(new JTextField().getBorder());
		
	    //Create the menu bar.
		JMenuBar menuBar = new JMenuBar();
	    setJMenuBar(menuBar);

	    JMenu menu = new JMenu(rsrc.getString("MainFrame.ToolsMenu"));
	    menu.setMnemonic(KeyEvent.VK_T);
	    menuBar.add(menu);
		
		// Add Options Menu in the Menu Bar
        JMenu optionsMenu = new JMenu(rsrc.getString("MainFrame.OptionsMenu"));
        optionsMenu.setMnemonic(KeyEvent.VK_T);
        menuBar.add(optionsMenu);
        
        // Add Checkboxes to Options Menu
        optionsLogTimeItem = new JCheckBoxMenuItem(rsrc.getString("MainFrame.OptionsLogTimeItem"));
        optionsMenu.add(optionsLogTimeItem);
        
        optionsTraceItem = new JCheckBoxMenuItem(rsrc.getString("MainFrame.OptionsTraceItem"));
        optionsMenu.add(optionsTraceItem);

        // Add Help Menu in the Menu Bar
		JMenu helpMenu = new JMenu(rsrc.getString("MainFrame.HelpMenu"));
		menuBar.add(helpMenu);

		JMenuItem onlineDocumentation = new JMenuItem(rsrc.getString("MainFrame.OnlineHelpMenuItem"));
		helpMenu.add(onlineDocumentation);
		onlineDocumentation.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try {
					Desktop currentDesktop = Desktop.getDesktop();
					if (Desktop.isDesktopSupported() && currentDesktop.isSupported(Desktop.Action.BROWSE)) {
						URI docUri = URI.create(rsrc.getString("MainFrame.DocURL"));
						currentDesktop.browse(docUri);
					}
				} catch (IOException ioException) {
					ioException.printStackTrace();
				}
			}
		});

		final JDialog aboutDialog = new AboutDialog(this);
		JMenuItem aboutMenuItem = new JMenuItem(rsrc.getString("MainFrame.AboutMenuItem"));
		helpMenu.add(aboutMenuItem);
		aboutMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				aboutDialog.setLocationRelativeTo(getJContentPane());
				aboutDialog.setVisible(true);
			}
		});

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
              saveSettings(getSettings());
  	    	System.exit(0);
            }
          });
	    
	}
    public static void showDialog(){
        MainFrame frame=new MainFrame();
        frame.settings=Main.newSettings();
        Main.readSettings(frame.settings);
        restoreWindowSizeAndLocation(frame, frame.settings);
        Logger.getInstance().addListener(new LogListener(frame));
        frame.setVisible(true);
        return;
    }
	private void saveSettings(Settings settings) {
        // save not all, but only some values 
		Settings toSave=new Settings();
		toSave.setValue(Main.WORKING_DIRECTORY,settings.getValue(Main.WORKING_DIRECTORY));
        toSave.setValue(Main.SETTING_LOGFILE_TIMESTAMP,settings.getValue(Main.SETTING_LOGFILE_TIMESTAMP));
		toSave.setValue(WINDOW_WIDTH, settings.getValue(WINDOW_WIDTH));
		toSave.setValue(WINDOW_HEIGHT, settings.getValue(WINDOW_HEIGHT));
		toSave.setValue(WINDOW_X, settings.getValue(WINDOW_X));
		toSave.setValue(WINDOW_Y, settings.getValue(WINDOW_Y));

		Main.writeSettings(toSave);
	}
	private javax.swing.JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new javax.swing.JPanel();
            jContentPane.setLayout(new java.awt.GridBagLayout());
			
			int cy=0;
			{
	            java.awt.GridBagConstraints gbdbsFileLabelConstraints = new java.awt.GridBagConstraints();
	            java.awt.GridBagConstraints gbdbsFileUiConstraints = new java.awt.GridBagConstraints();
	            java.awt.GridBagConstraints doGbdbsFileSelBtnConstraints = new java.awt.GridBagConstraints();
			    
	            gbdbsFileLabelConstraints.gridx = 0;
	            gbdbsFileLabelConstraints.gridy = cy;
	            gbdbsFileLabelConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
	            gbdbsFileUiConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	            gbdbsFileUiConstraints.weightx = 1.0;
	            gbdbsFileUiConstraints.gridx = 1;
	            gbdbsFileUiConstraints.gridy = cy;
	            gbdbsFileUiConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
	            doGbdbsFileSelBtnConstraints.gridx = 2;
	            doGbdbsFileSelBtnConstraints.gridy = cy;
	            doGbdbsFileSelBtnConstraints.anchor = java.awt.GridBagConstraints.WEST;
	            
	            jContentPane.add(getGbdbsFileLabel(), gbdbsFileLabelConstraints);
	            jContentPane.add(getGbdbsFileUi(), gbdbsFileUiConstraints);
	            jContentPane.add(getDoGbdbsFileSelBtn(), doGbdbsFileSelBtnConstraints);
			}
			
			cy++;
			{
	            java.awt.GridBagConstraints logFileLabelConstraints = new java.awt.GridBagConstraints();
	            java.awt.GridBagConstraints logFileUiConstraints = new java.awt.GridBagConstraints();
	            java.awt.GridBagConstraints doLogFileSelBtnConstraints = new java.awt.GridBagConstraints();
	            logFileLabelConstraints.gridx = 0;
	            logFileLabelConstraints.gridy = cy;
	            logFileLabelConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
	            logFileUiConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	            logFileUiConstraints.weightx = 1.0;
	            logFileUiConstraints.gridx = 1;
	            logFileUiConstraints.gridy = cy;
	            logFileUiConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
	            doLogFileSelBtnConstraints.gridx = 2;
	            doLogFileSelBtnConstraints.gridy = cy;
	            doLogFileSelBtnConstraints.anchor = java.awt.GridBagConstraints.WEST;
	            
	            jContentPane.add(getLogFileLabel(), logFileLabelConstraints);
	            jContentPane.add(getLogFileUi(), logFileUiConstraints);
	            jContentPane.add(getDoLogFileSelBtn(), doLogFileSelBtnConstraints);
			}
			
			// row 4
			cy++;
			{
	            java.awt.GridBagConstraints xmllogFileLabelConstraints = new java.awt.GridBagConstraints();
	            java.awt.GridBagConstraints xmllogFileUiConstraints = new java.awt.GridBagConstraints();
	            java.awt.GridBagConstraints doXmllogFileSelBtnConstraints = new java.awt.GridBagConstraints();
	            xmllogFileLabelConstraints.gridx = 0;
	            xmllogFileLabelConstraints.gridy = cy;
	            xmllogFileLabelConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
	            xmllogFileUiConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	            xmllogFileUiConstraints.weightx = 1.0;
	            xmllogFileUiConstraints.gridx = 1;
	            xmllogFileUiConstraints.gridy = cy;
	            xmllogFileUiConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
	            doXmllogFileSelBtnConstraints.gridx = 2;
	            doXmllogFileSelBtnConstraints.gridy = cy;
	            doXmllogFileSelBtnConstraints.anchor = java.awt.GridBagConstraints.WEST;
	            
                jContentPane.add(getXmllogFileLabel(), xmllogFileLabelConstraints);
                jContentPane.add(getXmllogFileUi(), xmllogFileUiConstraints);
                jContentPane.add(getDoXmllogFileSelBtn(), doXmllogFileSelBtnConstraints);
			}
			
            
			cy++;
			{
	            java.awt.GridBagConstraints logPaneConstraints = new java.awt.GridBagConstraints();
	            java.awt.GridBagConstraints doValidateConstraints = new java.awt.GridBagConstraints();
	            logPaneConstraints.fill = java.awt.GridBagConstraints.BOTH;
	            logPaneConstraints.weightx = 1.0;
	            logPaneConstraints.weighty = 1.0;
	            logPaneConstraints.gridx = 0;
	            logPaneConstraints.gridy = cy;
	            logPaneConstraints.gridheight = 2;
	            logPaneConstraints.gridwidth = 2;
	            doValidateConstraints.gridy = cy;
	            doValidateConstraints.gridx = 2;
	            doValidateConstraints.gridwidth = 2;
	            doValidateConstraints.anchor = java.awt.GridBagConstraints.WEST;
                jContentPane.add(getJScrollPane(), logPaneConstraints);
                jContentPane.add(getDoValidateBtn(), doValidateConstraints);
			}
			
			cy++;
			{
                java.awt.GridBagConstraints clearlogBtnConstraints = new java.awt.GridBagConstraints();
	            clearlogBtnConstraints.gridx = 2;//2
	            clearlogBtnConstraints.gridy = cy;
	            clearlogBtnConstraints.gridwidth = 2;
	            clearlogBtnConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
	            jContentPane.add(getClearlogBtn(), clearlogBtnConstraints);
			}
		}
		return jContentPane;
	}
	private javax.swing.JLabel getGbdbsFileLabel() {
		if(gbdbsFileLabel == null) {
			gbdbsFileLabel = new javax.swing.JLabel();
			gbdbsFileLabel.setText(rsrc.getString("MainFrame.gbdbsFileLabel"));
		}
		return gbdbsFileLabel;
	}
	private javax.swing.JLabel getLogFileLabel() {
		if(logFileLabel == null) {
			logFileLabel = new javax.swing.JLabel();
			logFileLabel.setText(rsrc.getString("MainFrame.logFileLabel"));
		}
		return logFileLabel;
	}
	private javax.swing.JLabel getXmllogFileLabel() {
		if(xmllogFileLabel == null) {
			xmllogFileLabel = new javax.swing.JLabel();
			xmllogFileLabel.setText(rsrc.getString("MainFrame.xmllogFileLabel"));
		}
		return xmllogFileLabel;
	}
	private javax.swing.JTextField getGbdbsFileUi() {
		if(gbdbsFileUi == null) {
			gbdbsFileUi = new javax.swing.JTextField();
			new DropTarget(gbdbsFileUi, getDragAndDropHandler());
		}
		return gbdbsFileUi;
	}
	private javax.swing.JTextField getLogFileUi() {
		if(logFileUi == null) {
			logFileUi = new javax.swing.JTextField();
		}
		return logFileUi;
	}
	private javax.swing.JTextField getXmllogFileUi() {
		if(xmllogFileUi == null) {
			xmllogFileUi = new javax.swing.JTextField();
		}
		return xmllogFileUi;
	}
	private javax.swing.JTextArea getLogUi() {
		if(logUi == null) {
			logUi = new javax.swing.JTextArea();
			logUi.setEditable(false);
		}
		return logUi;
	}
	private javax.swing.JButton getClearlogBtn() {
		if(clearlogBtn == null) {
			clearlogBtn = new javax.swing.JButton();
			clearlogBtn.setText(rsrc.getString("MainFrame.clearLogButton"));
			clearlogBtn.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					logClear();
				}
			});
		}
		return clearlogBtn;
	}
	private DropTargetListener getDragAndDropHandler() {
		return new DropTargetListener() {
			public void dragEnter(DropTargetDragEvent evt) {}
			public void dragOver(DropTargetDragEvent evt) {}
			public void dropActionChanged(DropTargetDragEvent evt) {}
			public void dragExit(DropTargetEvent dte) {}
			public void drop(DropTargetDropEvent evt) {
				try {
					Transferable tr = evt.getTransferable();
					for (DataFlavor flavor : tr.getTransferDataFlavors()) {
						if (flavor.isFlavorJavaFileListType()){
							evt.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
							List<File> files = (List<File>)tr.getTransferData(flavor);
							if(files.size()>0) {
	                            File absolutePath = files.get(0).getAbsoluteFile();
	                            setGbdbsFile(absolutePath);
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (UnsupportedFlavorException e) {
					e.printStackTrace();
				}
			}
		};
	}
	// selected files
	public File getGbdbsFile(){
		String fileTextInUi=getGbdbsFileUi().getText();
		File file=new File(fileTextInUi);
		return file;
	}
	public void setGbdbsFile(File gbdbsFile){
		getGbdbsFileUi().setText(gbdbsFile.getPath());
	}
	
	public String getLogFile(){
		return StringUtility.purge(getLogFileUi().getText());
	}
	public void setLogFile(String logfile){
		getLogFileUi().setText(logfile);
	}
	public String getXmllogFile(){
		return StringUtility.purge(getXmllogFileUi().getText());
	}
	public void setXmllogFile(String xmllogfile){
		getXmllogFileUi().setText(xmllogfile);
	}
	public Settings getSettings()
	{
		// get values from UI
		String logFile=getLogFile();
		String xmllogFile=getXmllogFile();
		
		// keep some values from current settings
		String workingDir=settings.getValue(Main.WORKING_DIRECTORY);
        String appHome=settings.getValue(Main.SETTING_APPHOME);

		// save window location and size
		Dimension dimension = getSize();
		String windowWidth = Integer.toString((int) dimension.getWidth());
		String windowHeight = Integer.toString((int) dimension.getHeight());
		Point origin = getLocation();
		String windowX = Integer.toString((int) origin.getX());
		String windowY = Integer.toString((int) origin.getY());

		
		Settings newSettings=new Settings();
		
		newSettings.setValue(Main.WORKING_DIRECTORY,workingDir);
		newSettings.setValue(Main.SETTING_LOGFILE,logFile);
		newSettings.setValue(Main.SETTING_XMLLOG,xmllogFile);
		newSettings.setValue(Main.SETTING_APPHOME, appHome);
		newSettings.setValue(WINDOW_WIDTH, windowWidth);
		newSettings.setValue(WINDOW_HEIGHT, windowHeight);
		newSettings.setValue(WINDOW_X, windowX);
		newSettings.setValue(WINDOW_Y, windowY);
		
        if (optionsLogTimeItem.isSelected()) {
            newSettings.setValue(Main.SETTING_LOGFILE_TIMESTAMP,Validator.TRUE);
        }
		if (optionsTraceItem.isSelected()) {
		    Logger.getInstance().setTraceFilter(false);
		} else {
		    Logger.getInstance().setTraceFilter(true);
		}
		
		return newSettings;
	}
	public void setSettings(Settings settings)
	{
		this.settings=settings;
	}
	private StringBuffer body=new StringBuffer();
	private javax.swing.JScrollPane jScrollPane = null;
	private javax.swing.JButton doValidateBtn = null;
	public void logAppend(String msg){
		body.append(msg);
		if(!msg.endsWith("\n")){
			body.append("\n");
		}
		getLogUi().setText(body.toString());
	}
	public void logClear(){
		body=new StringBuffer();
		getLogUi().setText(body.toString());
	}
	private javax.swing.JScrollPane getJScrollPane() {
		if(jScrollPane == null) {
			jScrollPane = new javax.swing.JScrollPane();
			jScrollPane.setViewportView(getLogUi());
		}
		return jScrollPane;
	}
	private static void restoreWindowSizeAndLocation(JFrame frame, Settings settings) {
		try {
			int width = Integer.parseInt(settings.getValue(WINDOW_WIDTH));
			int height = Integer.parseInt(settings.getValue(WINDOW_HEIGHT));
			int x = Integer.parseInt(settings.getValue(WINDOW_X));
			int y = Integer.parseInt(settings.getValue(WINDOW_Y));

			frame.setSize(width, height);
			if (isLocationOnScreen(x, y)) {
				frame.setLocation(x, y);
			}
		} catch (NumberFormatException ex) {
			// ignore settings, use the default size and location
		}
	}
	private static boolean isLocationOnScreen(int x, int y) {
		for (GraphicsDevice screen : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
			Rectangle bounds = screen.getDefaultConfiguration().getBounds();
			if (bounds.contains(x, y)) {
				return true;
			}
		}
		return false;
	}
	private javax.swing.JButton getDoValidateBtn() {
		if(doValidateBtn == null) {
			doValidateBtn = new javax.swing.JButton();
			doValidateBtn.setText(rsrc.getString("MainFrame.doValidateButton"));
			doValidateBtn.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SwingWorker worker = new SwingWorker() {
						public Object construct() {
							try {
								boolean ret=new Validator().validate(getGbdbsFile(),getSettings());
                                getLogUi().setCaretPosition(getLogUi().getDocument().getLength());
								Toolkit.getDefaultToolkit().beep();
                                JOptionPane.showMessageDialog(MainFrame.this, ret?Validator.MSG_VALIDATION_DONE:Validator.MSG_VALIDATION_FAILED);                                   
							} catch (Exception ex) {
								Logger.getInstance().logSchwarz(rsrc.getString("MainFrame.generalError"),ex);
							}
							return null;
						}
					};
					worker.start();
				}
			});
		}
		return doValidateBtn;
	}
	private javax.swing.JButton getDoGbdbsFileSelBtn() {
		if(doGbdbsFileSelBtn == null) {
			doGbdbsFileSelBtn = new javax.swing.JButton();
			doGbdbsFileSelBtn.setText("...");
			doGbdbsFileSelBtn.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
                    FileChooser fileDialog =  new FileChooser();
                    fileDialog.setCurrentDirectory(new File(getWorkingDirectory()));
                    fileDialog.setDialogTitle(rsrc.getString("MainFrame.gbdbsFileChooserTitle"));
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(rsrc.getString("MainFrame.gbdbsFileFilter"), "xml");
                    fileDialog.setFileFilter(filter);
					if (fileDialog.showOpenDialog(MainFrame.this) == FileChooser.APPROVE_OPTION) {
						setWorkingDirectory(fileDialog.getCurrentDirectory().getAbsolutePath());
						File selectedFile = fileDialog.getSelectedFile();
						setGbdbsFile(selectedFile.getAbsoluteFile());
					}				
				}
			});
		}
		return doGbdbsFileSelBtn;
	}
	private javax.swing.JButton getDoLogFileSelBtn() {
		if(doLogFileSelBtn == null) {
			doLogFileSelBtn = new javax.swing.JButton();
			doLogFileSelBtn.setText("...");
			doLogFileSelBtn.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					String file=getLogFile();
					FileChooser fileDialog =  new FileChooser(file);
					fileDialog.setCurrentDirectory(new File(getWorkingDirectory()));
					fileDialog.setDialogTitle(rsrc.getString("MainFrame.logFileChooserTitle"));
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(rsrc.getString("MainFrame.logFileFilter"), "log","txt");
                    fileDialog.setFileFilter(filter);

					if (fileDialog.showSaveDialog(MainFrame.this) == FileChooser.APPROVE_OPTION) {
						setWorkingDirectory(fileDialog.getCurrentDirectory().getAbsolutePath());
						file=fileDialog.getSelectedFile().getAbsolutePath();
						setLogFile(file);
					}					
				}
			});
		}
		return doLogFileSelBtn;
	}
	private javax.swing.JButton getDoXmllogFileSelBtn() {
		if(doXmllogFileSelBtn == null) {
			doXmllogFileSelBtn = new javax.swing.JButton();
			doXmllogFileSelBtn.setText("...");
			doXmllogFileSelBtn.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					String file=getLogFile();
					FileChooser fileDialog =  new FileChooser(file);
					fileDialog.setCurrentDirectory(new File(getWorkingDirectory()));
					fileDialog.setDialogTitle(rsrc.getString("MainFrame.xmllogFileChooserTitle"));
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(rsrc.getString("MainFrame.xmllogFileFilter"), "xml");
                    fileDialog.setFileFilter(filter);

					if (fileDialog.showSaveDialog(MainFrame.this) == FileChooser.APPROVE_OPTION) {
						setWorkingDirectory(fileDialog.getCurrentDirectory().getAbsolutePath());
						file=fileDialog.getSelectedFile().getAbsolutePath();
						setXmllogFile(file);
					}					
				}
			});
		}
		return doXmllogFileSelBtn;
	}
	private java.lang.String getWorkingDirectory() {
		String wd=settings.getValue(Main.WORKING_DIRECTORY);
		if(wd==null){
			wd=new File(".").getAbsolutePath();
		}
		return wd;
	}
	private void setWorkingDirectory(java.lang.String workingDirectory) {
		settings.setValue(Main.WORKING_DIRECTORY, workingDirectory);
	}
}
