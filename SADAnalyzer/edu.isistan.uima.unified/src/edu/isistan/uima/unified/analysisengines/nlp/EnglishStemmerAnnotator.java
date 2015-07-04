package edu.isistan.uima.unified.analysisengines.nlp;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.tartarus.snowball.ext.englishStemmer;

public class EnglishStemmerAnnotator  extends StemmerAnnotator {
	protected englishStemmer stemmer;
	
	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);
		stemmer = new englishStemmer();
	}
	
	@Override
	public void destroy() {
		stemmer = null;
		super.destroy();	
	}

	@Override
	protected String getStem(String coveredText) {
		stemmer.setCurrent(coveredText);
		stemmer.stem();
		return stemmer.getCurrent();
	}
}