package grader.project;

import com.thoughtworks.qdox.JavaDocBuilder;

import framework.grading.testing.Feature;
import framework.logging.loggers.FeedbackTextSummaryLogger;
import grader.assignment.GradingFeature;
import grader.execution.AProxyProjectClassLoader;
import grader.execution.MainClassFinder;
import grader.execution.ProjectRunnerSelector;
import grader.execution.ProxyBasedClassesManager;
import grader.execution.ProxyClassLoader;
import grader.file.RootFolderProxy;
import grader.file.filesystem.AFileSystemRootFolderProxy;
import grader.file.zipfile.AZippedRootFolderProxy;
import grader.language.LanguageDependencyManager;
import grader.project.file.ARootCodeFolder;
import grader.project.file.RootCodeFolder;
import grader.project.source.AClassesTextManager;
import grader.project.source.ClassesTextManager;
import grader.project.view.AClassViewManager;
import grader.project.view.ClassViewManager;
import grader.sakai.StudentCodingAssignment;
import grader.sakai.project.SakaiProject;
import grader.trace.execution.MainClassFound;
import grader.trace.execution.MainClassNotFound;
import grader.trace.execution.MainMethodNotFound;
import grader.trace.overall_transcript.OverallTranscriptCleared;
import grader.trace.project.ProjectFolderNotFound;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AProject implements Project {

    public static final String ZIP_SUFFIX = ".zip";
    public static final String DEFAULT_PROJECT_FOLDER = ".";
    public static final String DEFAULT_GRADING_FOLDER = "C:/Users/dewan/Downloads/GraderData";
    public static final String DEFAULT_TRANSCRIPT_FILE_PREFIX = "transcript";
    public static final String DEFAULT_TRANSCRIPT_FILE_SUFFIX = ".txt";
    public static final String DEFAULT_TRASNCRIPT_FILE_NAME = DEFAULT_TRANSCRIPT_FILE_PREFIX + DEFAULT_TRANSCRIPT_FILE_SUFFIX;
//    public static final String PROJECT_DIRECTORY = "D:/dewan_backup/Java/AmandaKaramFinalUpdated/Final";
//    public static final String PROJECT_ZIPPED_DIRECTORY = "D:/dewan_backup/Java/AmandaKaramFinalUpdated.zip";

    String projectFolderName = DEFAULT_PROJECT_FOLDER;
    String gradingProjectFolderName = DEFAULT_GRADING_FOLDER;
    ProxyBasedClassesManager classesManager;
    ClassViewManager classViewManager;
    ClassesTextManager classesTextManager;
    boolean zippedFolder;
    RootCodeFolder rootCodeFolder;
    RootFolderProxy rootFolder;
    ProxyClassLoader proxyClassLoader;
    ClassLoader oldClassLoader;
    String outputFolder = ".";
    String sourceFileName, outputFileName;
    String sourceSuffix = ClassesTextManager.DEFAULT_SOURCES_FILE_SUFFIX;
    String outputSuffix = DEFAULT_TRANSCRIPT_FILE_SUFFIX;
    boolean hasBeenRun, canBeRun = true;
    JavaDocBuilder javaDocBuilder;
    MainClassFinder mainClassFinder;
    Feature currentGradingFeature; // ugly but do not want to change project runner code that has access to project and not grading feature
    String[][] args;
    boolean runChecked;
    StringBuffer currentOutput = new StringBuffer();
    StringBuffer currentInput = new StringBuffer();
//    Map<String, String> processToOutput = new HashMap();
//    Map<String, String> processToInput = new HashMap();
    String[] currentArgs;
//    FileWriter outputFile ;

    protected Class mainClass;
    protected Method mainMethod;
    protected String mainClassName;
    protected String[] inputFiles;
    protected String[] outputFiles;
    boolean noProjectFolder;
//    List<String> nonCompiledClasses = new ArrayList();
    List<String> classNamesThatCouldNotBeCompiled = new ArrayList();

    List<String> classNamesCompiled = new ArrayList();

    static boolean loadClasses = false;

    static boolean compileMissingObjectCode = false;
    static boolean preCompileMissingObjectCode = false;
    static boolean filesCompiled = false;

    static boolean forceCompile = false;

    public AProject(String aProjectFolder, String anOutputFolder, boolean aZippedFolder) {
        init(aProjectFolder, anOutputFolder, aZippedFolder);
    }

    public AProject(StudentCodingAssignment aStudentCodingAssignment) {
        //init(aStudentCodingAssignment.getProjectFolder(), aStudentCodingAssignment.getFeedbackFolder().getAbsoluteName());
        init(aStudentCodingAssignment.getProjectFolder(), aStudentCodingAssignment.getFeedbackFolder().getMixedCaseAbsoluteName());
    }

    public AProject(StudentCodingAssignment aStudentCodingAssignment, String aSourceSuffix, String anOutputSuffix) {
        sourceSuffix = aSourceSuffix;
        outputSuffix = anOutputSuffix;
        //init(aStudentCodingAssignment.getProjectFolder(), aStudentCodingAssignment.getFeedbackFolder().getAbsoluteName());
        init(aStudentCodingAssignment.getProjectFolder(), aStudentCodingAssignment.getFeedbackFolder().getMixedCaseAbsoluteName());
    }

    public String toString() {
        return "(" + projectFolderName + "," + outputFolder + ")";
    }

    public AProject(RootFolderProxy aRootFolder, String anOutputFolder) {
        init(aRootFolder, anOutputFolder);
    }

    @Override
    public String getOutputFolder() {
        return outputFolder;
    }

    @Override
    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    protected MainClassFinder createMainClassFinder() {
//        return new AMainClassFinder();
//    	return JavaMainClassFinderSelector.getMainClassFinder();
        return LanguageDependencyManager.getMainClassFinder();

    }

    public void init(String aProjectFolder, String anOutputFolder, boolean aZippedFolder) {
        if (aZippedFolder) {
            rootFolder = new AZippedRootFolderProxy(aProjectFolder);
        } else {
            rootFolder = new AFileSystemRootFolderProxy(aProjectFolder);
        }
        init(rootFolder, anOutputFolder);
    }

    @Override
    public boolean isNoProjectFolder() {
        return noProjectFolder;
    }

    @Override
    public void setNoProjectFolder(boolean noProjectFolder) {
        this.noProjectFolder = noProjectFolder;
    }

    public void init(RootFolderProxy aRootFolder, String anOutputFolder) {
        outputFolder = anOutputFolder;

        rootFolder = aRootFolder;
        outputFileName = createFullOutputFileName();

        if (aRootFolder == null) {
            setNoProjectFolder(true);
            return;
        } else {

            //projectFolderName = aRootFolder.getAbsoluteName();
            projectFolderName = aRootFolder.getMixedCaseAbsoluteName();
//        if (projectFolderName.contains("bluong"))
//        	System.out.println("bluoing");
//        outputFolder = anOutputFolder;
            try {
                rootCodeFolder = new ARootCodeFolder(rootFolder);
            } catch (ProjectFolderNotFound e) {
            	FeedbackTextSummaryLogger.logNoSrcFolder(this);
                setNoProjectFolder(true);
                return;
            }
            if (AProject.isLoadClasses()) {
                if (rootCodeFolder.hasValidBinaryFolder()) {
                    proxyClassLoader = new AProxyProjectClassLoader(rootCodeFolder);
                } else {
                    proxyClassLoader = new AProxyProjectClassLoader(rootCodeFolder); // create class loader in this case also
                }
            }
            sourceFileName = createFullSourceFileName();
//        outputFileName = createFullOutputFileName();
            classesManager = new AProxyBasedClassesManager();
            mainClassFinder = createMainClassFinder();
        }
    }

    public String createLocalSourceFileName() {
        return classesTextManager.DEFAULT_SOURCES_FILE_PREFIX + sourceSuffix;
    }

    public String createLocalOutputFileName() {
        return DEFAULT_TRANSCRIPT_FILE_PREFIX + outputSuffix;
    }

    public String createFullSourceFileName() {
        return outputFolder + "/" + createLocalSourceFileName();
    }

    public String createFullOutputFileName() {
        return outputFolder + "/" + createLocalOutputFileName();
    }

    boolean madeClassDescriptions;
    List<Class> classesImplicitlyLoaded;

    public List<Class> getImplicitlyLoadedClasses() {
        return classesImplicitlyLoaded;
    }

    public void maybeMakeClassDescriptions() {
    	// earlier it expected class descroptions to be fetched after running
        // but we need the class descriptions to find the main method sometimes
        // so removing this check
//        if (!runChecked && !hasBeenRun)
//            return;
//    	if (!isLoadClasses())
//    		return;
        if (madeClassDescriptions) {
            return;
        }

        makeClassDescriptions();
        madeClassDescriptions = true;

//        try { // Added by Josh: Exceptions can occur when making class descriptions
//            classesManager.makeClassDescriptions(this);
//            classViewManager = new AClassViewManager(classesManager);
//            classesTextManager = new AClassesTextManager(classViewManager);
//            classesTextManager.initializeAllSourcesText();
//            System.out.println("Write sources to:" + sourceFileName);
//            classesTextManager.writeAllSourcesText(sourceFileName);
//            madeClassDescriptions = true;
//        } catch (Exception e) {
//            System.out.println("Error making class descriptions");
//        }
    }

    public void makeClassDescriptions() {
        if (isNoProjectFolder()) {
            return;
        }

        try { // Added by Josh: Exceptions can occur when making class descriptions
            classesManager.makeClassDescriptions(this);
            classViewManager = new AClassViewManager(classesManager);
            classesTextManager = new AClassesTextManager(classViewManager);
            classesTextManager.initializeAllSourcesText();
//            System.out.println("Write sources to:" + sourceFileName);
            classesTextManager.writeAllSourcesText(sourceFileName);
            madeClassDescriptions = true;
        } catch (Exception e) {
            System.out.println("Error making class descriptions");
        }
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public boolean hasBeenRun() {
        return hasBeenRun;
    }

    public void setHasBeenRun(boolean newVal) {
        hasBeenRun = newVal;
        runChecked = true;
        if (hasBeenRun && proxyClassLoader != null) {
            classesImplicitlyLoaded = new ArrayList(proxyClassLoader.getClassesLoaded());
        }
    }

    @Override()
    public boolean setRunParameters(String aMainClassName, String anArgs[][], String[] anInputFiles, String[] anOutputFiles, MainClassFinder aMainClassFinder) {
        args = anArgs;
        try {
            mainClassName = aMainClassName;
            mainClass = proxyClassLoader.loadClass(mainClassName);
            inputFiles = anInputFiles;
            outputFiles = anOutputFiles;
            if (mainClass == null) {
                mainClass = mainClassFinder.mainClass(rootCodeFolder, proxyClassLoader, mainClassName, this);
            }
            if (mainClass == null) {
//                System.out.println("Missing main class:" + mainClassName + " for student:" + getProjectFolderName());
                setCanBeRun(false);
                MainClassNotFound.newCase(mainClassName, getProjectFolderName(), this);
                return false;
            }

            mainMethod = mainClass.getMethod("main", String[].class);
            if (mainMethod == null) {
//                System.out.println("Missing main method:" + "main");
                MainMethodNotFound.newCase(mainClassName, getProjectFolderName(), this);

                setCanBeRun(false);
                return false;
            }
            MainClassFound.newCase(mainClassName, getProjectFolderName(), this);

            return true;
        } catch (Exception e) {
            System.out.println("cannot  run:" + getProjectFolderName());
            setCanBeRun(false);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Thread runProject() {
        try {
            if (!canBeRun()) {
                return null;
            }
            Runnable runnable = ProjectRunnerSelector.createProjectRunner(mainClassName, args, this, inputFiles, outputFiles, mainClass, mainMethod);
            Thread retVal = new Thread(runnable);
            retVal.start();
            System.out.println("started:" + retVal);
            return retVal;
        } catch (Exception e) {
            System.out.println("Could not run:" + getProjectFolderName());
            setCanBeRun(false);
            e.printStackTrace();
            return null;
        }
    }

    public Thread run(String aMainClassName, String[][] anArgs, String[] anInputFiles, String[] anOutputFiles) {
        setRunParameters(aMainClassName, anArgs, anInputFiles, anOutputFiles, mainClassFinder);
        return runProject();
    }

    public AProject(String aProjectFolder, String anOutputFolder) {
        init(aProjectFolder, anOutputFolder, aProjectFolder.endsWith(ZIP_SUFFIX));

    }

    public AProject(String aProjectFolder) {
        init(aProjectFolder, outputFolder, aProjectFolder.endsWith(ZIP_SUFFIX));

    }

    public ProxyClassLoader getClassLoader() {
        return proxyClassLoader;
    }

    public ProxyBasedClassesManager getClassesManager() {
        maybeMakeClassDescriptions();
        return classesManager;
    }

    public void setClassesManager(ProxyBasedClassesManager aClassesManager) {
        this.classesManager = aClassesManager;
    }

    public ClassViewManager getClassViewManager() {
        maybeMakeClassDescriptions();
        return classViewManager;
    }

    public void setClassViewManager(ClassViewManager aClassViewManager) {
        this.classViewManager = aClassViewManager;
    }

    public ClassesTextManager getClassesTextManager() {
        maybeMakeClassDescriptions();
        return classesTextManager;
    }

    public void setClassesTextManager(ClassesTextManager aClassesTextManager) {
        this.classesTextManager = aClassesTextManager;
    }

    public void setProjectFolder(String aProjectFolder) {
        this.projectFolderName = aProjectFolder;
    }

    public String getProjectFolderName() {
        return projectFolderName;
    }

    public RootCodeFolder getRootCodeFolder() {
        return rootCodeFolder;
    }

    @Override
    public String getSourceProjectFolderName() {
        return rootCodeFolder.getSourceProjectFolderName();
    }

    @Override
    public String getSourceFileName() {
        return sourceFileName;
    }

    @Override
    public String getBinaryProjectFolderName() {
        return rootCodeFolder.getBinaryProjectFolderName();
    }

    @Override
    public boolean runChecked() {
        return runChecked;
    }

    @Override
    public void setCanBeRun(boolean newVal) {
        runChecked = true;
        canBeRun = newVal;

    }

    @Override
    public boolean canBeRun() {
        return canBeRun;
    }

    @Override
    public JavaDocBuilder getJavaDocBuilder() {
        if (javaDocBuilder == null) {
            javaDocBuilder = new JavaDocBuilder();
        }
        return javaDocBuilder;
    }

    @Override
    public StringBuffer getCurrentOutput() {
        return currentOutput;
    }

    @Override
    public void clearOutput() {
        currentOutput.setLength(0);
        try {
            FileWriter fileWriter = new FileWriter(new File(outputFileName));
            OverallTranscriptCleared.newCase(null, null, (SakaiProject) this, outputFileName, this);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void setCurrentOutput(StringBuffer currentOutput) {
        this.currentOutput = currentOutput;
    }

    @Override
    public Feature getCurrentGradingFeature() {
        return currentGradingFeature;
    }

    @Override
    public void setCurrentGradingFeature(Feature currentGradingFeature) {
        this.currentGradingFeature = currentGradingFeature;
    }

    @Override
    public String getCurrentInput() {
        return currentInput.toString();
    }

    @Override
    public void setCurrentInput(String aCurrentInput) {
        currentInput.setLength(0);
        appendCurrentInput(aCurrentInput);
    }

    @Override
    public void appendCurrentInput(String aCurrentInput) {
        currentInput.append(aCurrentInput);
    }

//    @Override
//  	public void appendCurrentInput(String aProcess, String aCurrentInput) {
////  		currentInput.append(aCurrentInput);
//    }
    @Override
    public String[] getCurrentArgs() {
        return currentArgs;
    }

    @Override
    public void setCurrentArgs(String[] currentArgs) {
        this.currentArgs = currentArgs;
    }

    @Override
    public String getSourceSuffix() {
        return sourceSuffix;
    }

    @Override
    public boolean hasUnCompiledClasses() {
        // TODO Auto-generated method stub
        return classNamesThatCouldNotBeCompiled.size() > 0;
    }

    @Override
    public List<String> getNonCompiledClasses() {
        return classNamesThatCouldNotBeCompiled;
    }

    @Override
    public void addNonCompiledClass(String newVal) {
        classNamesThatCouldNotBeCompiled.add(newVal);

    }

    @Override
    public boolean hasCompiledClasses() {
        // TODO Auto-generated method stub
        return classNamesCompiled.size() > 0;
    }

    @Override
    public List<String> getCompiledClasses() {
        return classNamesCompiled;
    }

    @Override
    public void addCompiledClass(String newVal) {
        classNamesCompiled.add(newVal);

    }

    public static boolean isLoadClasses() {
        return loadClasses;
    }

    public static void setLoadClasses(boolean makeClassDescriptions) {
        AProject.loadClasses = makeClassDescriptions;
    }

    public static boolean isCompileMissingObjectCode() {
        return compileMissingObjectCode;
    }

    public static void setCompileMissingObjectCode(boolean newVal) {
        AProject.compileMissingObjectCode = newVal;
    }

    public static boolean isForceCompile() {
        return forceCompile;
    }

    public static void setForceCompile(boolean forceCompile) {
        AProject.forceCompile = forceCompile;
    }

    public static boolean isPreCompileMissingObjectCode() {
        return preCompileMissingObjectCode;
    }

    public static void setPrecompileMissingObjectCode(
            boolean preCompileMissingObjectCode) {
        AProject.preCompileMissingObjectCode = preCompileMissingObjectCode;
    }

    public static boolean isFilesCompiled() {
        return filesCompiled;
    }

    public static void setFilesCompiled(boolean filesCompiled) {
        AProject.filesCompiled = filesCompiled;
    }

}
