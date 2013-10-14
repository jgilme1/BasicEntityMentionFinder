package edu.washington.knowitall.entitymentionfinder.eval;

import java.util.Properties;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class StanfordNERAnnotator {
	private static final StanfordNERAnnotator instance = new StanfordNERAnnotator();
	
	private StanfordCoreNLP nerPipeline = null;

	private StanfordNERAnnotator(){
		Properties nerProperties = new Properties();
		nerProperties.put("annotators", "tokenize, cleanxml, ssplit, pos, lemma, ner");
		nerProperties.put("clean.allowflawedxml","true");
		nerProperties.put("ner.useSUTime", "false");
		this.nerPipeline = new StanfordCoreNLP(nerProperties);
	}
	
	
	public static StanfordNERAnnotator getInstance(){
		return instance;
	}
	
	
	public Annotation annotate(String text){
		Annotation doc = new Annotation(text);
		nerPipeline.annotate(doc);
		return doc;
	}
}
