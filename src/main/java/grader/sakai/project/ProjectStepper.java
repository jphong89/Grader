package grader.sakai.project;

import util.annotations.ComponentWidth;
import util.annotations.Row;
import util.annotations.Visible;
import util.misc.ClearanceManager;
import util.models.LabelBeanModel;
import util.models.PropertyListenerRegisterer;
import grader.assignment.GradingFeature;
import grader.assignment.GradingFeatureList;
import grader.project.Project;
import grader.settings.navigation.NavigationSetter;

import java.beans.PropertyChangeListener;


public interface ProjectStepper  extends ClearanceManager, PropertyListenerRegisterer, PropertyChangeListener{
	public boolean setProject(SakaiProject newVal) ;
	
	public void output();
	
	public void sources() ;
	public double getScore() ;
	public void setScore(double newVal) ;
	public  void waitForClearance() ;
	public SakaiProjectDatabase getProjectDatabase() ;

	public void setProjectDatabase(SakaiProjectDatabase aProjectDatabase) ;
	public void setOnyen(String anOnyen) throws MissingOnyenException ;
	public boolean setProject(String anOnyen) ;
	public boolean isAutoRun() ;
    public void setAutoRun(boolean newVal);
    public void autoRun() ;
    public boolean hasMoreSteps();
	
	public void setHasMoreSteps(boolean newVal);
    public SakaiProject getProject();
    boolean runProjectsInteractively();
    public void configureNavigationList();

	boolean preDone();

	void done();

//	String getNavigationFilter();
//
//	void setNavigationFilter(String newVal);

	boolean preGetGradingFeatures();

	boolean preAutoGrade();

	void autoGrade();

	GradingFeatureList getGradingFeatures();

	boolean isAllGraded();

	boolean preNext();

	void next();

	boolean prePrevious();

	void previous();

	boolean preRunProjectsInteractively();

	boolean move(boolean forward);
	public boolean isAutoAutoGrade() ;
    public void setAutoAutoGrade(boolean newVal) ;
    public void autoAutoGrade() ;

	void setFrame(Object aFrame);

	Object getFrame();

	LabelBeanModel getPhoto();

	String getFeedback();

	String getTranscript();

	NavigationSetter getNavigationSetter();

	void validate();

	boolean runProjectsInteractively(String aGoToOnyen) throws MissingOnyenException;

	void setName(String newVal);

	String getName();

	String getOnyen();

	void setOverallNotes(String newVal);

	String getOverallNotes();

	void internalSetOnyen(String anOnyen) throws MissingOnyenException;

	boolean isProceedWhenDone();

	void toggleProceedWhenDone();

	void internalSetMultiplier(double newValue);

	void setComputedFeedback();

	void setStoredFeedback();

	void setStoredOutput();

	GradingFeature getSelectedGradingFeature();

	void internalSetNotes(String newVal);

	void internalSetResult(String newVal);

	void internalSetAutoNotes(String newVal);

	void internalSetComments(String newVal);

	void setColors();

	boolean isChanged();

	void setChanged(boolean changed);

	void setComputedScore();

	boolean isSettingUpProject();

	void setSettingUpProject(boolean settingUpProject);

	boolean shouldVisit();

	void setInternalScore(double newVal);

	void setMultiplierColor();

	void setScoreColor();

	void setOverallNotesColor();

	boolean runAttempted();

	int getCurrentOnyenIndex();

	void setCurrentOnyenIndex(int currentOnyenIndex);

	int getFilteredOnyenIndex();

	void setFilteredOnyenIndex(int filteredOnyenIndex);
	

}
