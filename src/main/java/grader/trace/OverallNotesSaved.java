package grader.trace;

import grader.project.graded.OverviewProjectStepper;
import grader.sakai.project.SakaiProject;
import grader.sakai.project.SakaiProjectDatabase;
import grader.settings.GraderSettingsModel;
import bus.uigen.trace.ConstantsMenuAdditionEnded;
import util.trace.TraceableInfo;

public class OverallNotesSaved extends StepperInfo {
	String overviewNotesFileName;



public OverallNotesSaved(String aMessage,
			SakaiProjectDatabase aSakaiProjectDatabase,
			OverviewProjectStepper aProjectStepper, SakaiProject aProject,
			String anOvervewFileName,
			Object aFinder) {
		super(aMessage, aSakaiProjectDatabase, aProjectStepper, aProject, aFinder);
		overviewNotesFileName = anOvervewFileName;
		// TODO Auto-generated constructor stub
	}

public String getOverviewNotesFileName() {
	return overviewNotesFileName;
}



public void setOverviewNotesFileName(String overviewNotesFileName) {
	this.overviewNotesFileName = overviewNotesFileName;
}

	
	public static OverallNotesSaved newCase(SakaiProjectDatabase aSakaiProjectDatabase, 
			OverviewProjectStepper aProjectStepper, 
			SakaiProject aProject,
			String anOverviewFileName,
			Object aFinder) {
		String aMessage = "Overview Notes Loaded from File:" + anOverviewFileName;
		OverallNotesSaved retVal = new OverallNotesSaved(aMessage, aSakaiProjectDatabase, aProjectStepper, aProject, anOverviewFileName, aFinder);
		retVal.announce();		
		return retVal;
	}
	

}