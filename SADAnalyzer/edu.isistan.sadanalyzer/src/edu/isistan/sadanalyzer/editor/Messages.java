package edu.isistan.sadanalyzer.editor;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author mbasso
 *
 */
public class Messages extends NLS {
	
	private static final String BUNDLE_NAME = "edu.isistan.sadanalyzer.editor.messages";

	public static String SadAnalyzerEditor_Configuration;
	public static String SadAnalyzerEditor_ConfigurationTitle;
	public static String SadAnalyzerEditor_ConfigurationModelTree;
	public static String SadAnalyzerEditor_ConfigurationModelTreeDescription;
	public static String SadAnalyzerEditor_ConfigurationModelTreeAllSection;
	public static String SadAnalyzerEditor_ConfigurationDetail;
	public static String SadAnalyzerEditor_ConfigurationDetailDescription;
	public static String SadAnalyzerEditor_SadSource;
	public static String SadAnalyzerEditor_UIMASadFile;
	public static String SadAnalyzerEditor_ConfigurationRun;
	public static String SadAnalyzerEditor_ConfigurationRunDescription;
	public static String SadAnalyzerEditor_ConfigurationRunButton;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}