package grader.trace.assignment_data;

import grader.trace.file.FileInfo;
import grader.trace.file.SerializableFileInfo;

public class InputFileFound extends FileInfo {

	public InputFileFound(String aMessage, String aFileName,
			Object aFinder) {
		super(aMessage, aFileName, aFinder);
	}
	
	public static InputFileFound newCase(String aFileName,
			Object aFinder) {
		String aMessage =  "Binary folder identified: " + aFileName;
		InputFileFound retVal = new InputFileFound(aMessage, aFileName, aFinder);
		retVal.announce();
		return retVal;
	}

}
