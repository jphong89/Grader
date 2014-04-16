package grader.interaction_logger;

import framework.utils.GradingEnvironment;
import grader.config.ConfigurationManagerSelector;
import grader.modules.ModuleProblemManagerSelector;
import grader.trace.CSVSerializable;
import grader.trace.interaction_logger.InteractionLogEntryAdded;
import grader.trace.interaction_logger.InteractionLogFileCreatedOrLoaded;
import grader.trace.interaction_logger.InteractionLogFolderCreated;
import grader.trace.settings.GraderSettingsEnded;
import grader.trace.stepper.ProjectStepperEnded;
import grader.trace.stepper.ProjectStepperStarted;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import util.misc.Common;
import util.trace.TraceableBus;

public class AnInteractionLogWriter implements InteractionLogWriter {
	File curentLogFile;
	Logger logger;
	public static final String LOG_FILE_PREFIX = "interactionLog";
	String fileName;
	String currentAssignment;
	String currentModule;
	
	PrintWriter out = null;
    BufferedWriter bufWriter;
    PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    final Class[] staticDoNotLogEventsArray = {
    		InteractionLogEntryAdded.class, 
    		InteractionLogFileCreatedOrLoaded.class, 
    		InteractionLogFolderCreated.class
    };    
    Set<Class> doNotLogEventsSet = Common.arrayToSet(staticDoNotLogEventsArray);
    
//    String interactionLogFolder;
    String interactionLogFolder;
    
    GraderSettingsEnded lastGraderSettingsEnded;
    
    public static final String SEPARATOR = "_";
	
	
	public AnInteractionLogWriter() {
//		long currentTime = System.currentTimeMillis();
//		Date currentDate = new Date(currentTime);
////		String monthDay = currentDate.getMonth() + "" + currentDate.getDay();
//		String dateString = currentDate.toString();
//		String[] parts = dateString.split(" ");
//		String suffix = parts[1] + parts[2] + parts[5];
		String suffix = getTimeStampSuffix();
//		interactionLogFolder = 
//				ConfigurationManagerSelector.getConfigurationManager().
//					getStaticConfiguration().getString("grader.logger.interactionLogDirectory"); // + "/" + GradingEnvironment.get().getUserName();
//		
//		File folder = new File(interactionLogFolder);
//		if (!folder.exists()) {
//			folder.mkdirs();
//			InteractionLogFolderCreated.newCase(folder.getAbsolutePath(), this);
//		}
		interactionLogFolder = getOrCreateInteractionFolder();
		String userName = GradingEnvironment.get().getUserName();

		if (userName == null || userName.isEmpty())
			userName = "";
//		else
//			userName = userName + "_";
		
		createOrLoadAppendableFile(getSettingsSuffix());
//		fileName = interactionLogFolder + "/" + userName + LOG_FILE_PREFIX + suffix + ".csv";
//		
//
//		out = null;
//	    
//	    try{
//	        bufWriter =
//	            Files.newBufferedWriter(
//	                Paths.get(fileName),
//	                Charset.forName("UTF8"),
//	                StandardOpenOption.WRITE, 
//	                StandardOpenOption.APPEND,
//	                StandardOpenOption.CREATE);
//	        out = new PrintWriter(bufWriter, true);
//	    }catch(IOException e){
//	    	e.printStackTrace();
//	    	//Oh, no! Failed to create PrintWriter
//	    }
//	    
//	    InteractionLogFileCreatedOrLoaded.newCase(fileName, this);

	}
	
	public static String getOrCreateInteractionFolder() {
		String interactionLogFolder = 
				ConfigurationManagerSelector.getConfigurationManager().
					getStaticConfiguration().getString("grader.logger.interactionLogDirectory"); // + "/" + GradingEnvironment.get().getUserName();
		
		File folder = new File(interactionLogFolder);
		if (!folder.exists()) {
			folder.mkdirs();
			InteractionLogFolderCreated.newCase(folder.getAbsolutePath(), AnInteractionLogWriter.class);
		}
		return interactionLogFolder;
		
	}
	
	public static String getTimeStampSuffix() {
		long currentTime = System.currentTimeMillis();
		Date currentDate = new Date(currentTime);
		String dateString = currentDate.toString();
		String[] parts = dateString.split(" ");
		String suffix = parts[1] + parts[2] + parts[5];
		return suffix;
		
	}
	
	public static final String SETTINGS_SUFFIX = "Settings";
	
	 String getSettingsSuffix() {
		
		return SEPARATOR + SETTINGS_SUFFIX;
		
	}
	 
	 String getAssignmentProblemSuffix() {
		 if (lastGraderSettingsEnded == null) return "";
		 String aModuleName = lastGraderSettingsEnded.getGradingSettingsModel().getCurrentModule();
		 String aProblemName = lastGraderSettingsEnded.getGradingSettingsModel().getCurrentProblem();
		 return SEPARATOR + aModuleName + SEPARATOR + aProblemName;
	 }
	
	 void createOrLoadAppendableFile(String suffix) {
		String userName = GradingEnvironment.get().getUserName();

		if (userName == null || userName.isEmpty())
			userName = "";
//		else
//			userName = userName;
		fileName = interactionLogFolder + "/" + userName + SEPARATOR + LOG_FILE_PREFIX + suffix + ".csv";
		out = null;
	    
	    try{
	        bufWriter =
	            Files.newBufferedWriter(
	                Paths.get(fileName),
	                Charset.forName("UTF8"),
	                StandardOpenOption.WRITE, 
	                StandardOpenOption.APPEND,
	                StandardOpenOption.CREATE);
	        out = new PrintWriter(bufWriter, true);
	    }catch(IOException e){
	    	e.printStackTrace();
	    	//Oh, no! Failed to create PrintWriter
	    }
	    
	    InteractionLogFileCreatedOrLoaded.newCase(fileName, this);
		
	}
	
	public static final String NEW_LINE_REPLACEMENT = "New Line";
	
	@Override
	public void newEvent(Exception aTraceable) {
//		if (aTraceable.getClass().getPackage() != AGraderTracer.class.getPackage()) return;
		if (
				!(aTraceable instanceof CSVSerializable) ||
				doNotLogEventsSet.contains(aTraceable.getClass()))
			return;
		String rawCSVRow = ((CSVSerializable) aTraceable).toCSVRow();
//		String csvRow = rawCSVRow.replaceAll("\n", NEW_LINE_REPLACEMENT);
		String csvRow = rawCSVRow;

		InteractionLogEntryAdded.newCase(fileName, csvRow, this);
		propertyChangeSupport.firePropertyChange("logAddition", null, csvRow);
//		out.println(((CSVSerializable) aTraceable).toCSVRow());
		out.println(csvRow);

		out.flush();
		
		if (aTraceable instanceof GraderSettingsEnded )
			lastGraderSettingsEnded = (GraderSettingsEnded) aTraceable;
		if (aTraceable instanceof ProjectStepperStarted) {
			out.close();
			createOrLoadAppendableFile(getAssignmentProblemSuffix());
			out.println(csvRow);
		}
		if (aTraceable instanceof ProjectStepperEnded) 
			out.close();
		
	}
	@Override
	public void addPropertyChangeListener(PropertyChangeListener aListener) {
		propertyChangeSupport.addPropertyChangeListener(aListener);
		
	}
	@Override
	public Set<Class> getDoNotLogEventsSet() {
		return doNotLogEventsSet;
	}
	@Override
	public void setDoNotLogEventsSet(Set<Class> doNotLogEventsSet) {
		this.doNotLogEventsSet = doNotLogEventsSet;
	}
//	public void startTracing() {
//		
//	}
	@Override
	public String getInteractionLogFolder() {
		return interactionLogFolder;
	}
	public String getCurrentLogFileName() {
		return fileName;
	}
	public void setCurrentLogFileName(String fileName) {
		this.fileName = fileName;
	}
	public static void main (String[] args) {
		new AnInteractionLogWriter();
	}
	

}