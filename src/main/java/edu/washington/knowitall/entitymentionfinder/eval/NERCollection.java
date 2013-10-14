package edu.washington.knowitall.entitymentionfinder.eval;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import edu.washington.knowitall.entitymentionfinder.eval.NamedEntity;


public class NERCollection {
	
	private static final NERCollection instance = new NERCollection();
	private static final StanfordNERAnnotator annotator = StanfordNERAnnotator.getInstance();
	
	
	private NERCollection(){
	}
	
	public static NERCollection getInstance(){
		return instance;
	}
	
	public List<NamedEntity> collectNamedEntitiesFromDocument(String docText){
		List<NamedEntity> namedEntities = new ArrayList<NamedEntity>();
		Annotation annotatedDoc = annotator.annotate(docText);
		List<CoreMap> sentences = annotatedDoc.get(CoreAnnotations.SentencesAnnotation.class);
		for(CoreMap sentence: sentences){
			namedEntities.addAll(collectNamedEntitiesFromSentence(sentence,docText));
		}
		return namedEntities;
	}
	
	private List<NamedEntity> collectNamedEntitiesFromSentence(CoreMap sentence, String docText){
		
		int startIndex = sentence.get(TokensAnnotation.class).get(0).beginPosition();
		String sentenceText = sentence.get(CoreAnnotations.TextAnnotation.class);
		List<NamedEntity> namedEntities = new ArrayList<NamedEntity>();
    	List<CoreLabel> sentenceTokenList = new ArrayList<CoreLabel>();
    	List<CoreLabel> relevantTokens = new ArrayList<CoreLabel>();
    	int tokenIndex =0;
    	for(CoreLabel token: sentence.get(TokensAnnotation.class)){
    			String net = token.get(NamedEntityTagAnnotation.class);
    			token.setNER(net);
				token.setIndex(tokenIndex);
    			if( (net.equals("ORGANIZATION"))||
    				(net.equals("LOCATION")) ||
    				(net.equals("PERSON"))
    				){
    				//System.err.println("NEW RELEVANT TOKEN: " + token.originalText() + " INDEX = " + token.index() + " NER = " + token.ner());
    				relevantTokens.add(token);
    			}
    		tokenIndex +=1 ;
    	}
    	
    	List<CoreLabel> namedEntityTokens = new ArrayList<CoreLabel>();
    	//System.err.println("Processing tokens");
    	for(int i =0; i < relevantTokens.size(); i ++){
    		CoreLabel token = relevantTokens.get(i);
    		//append to tokensList if tokenIndices are neighbors, else create a new namedENtity
    		//System.err.println("Token = "+ token.originalText());
    		
    		if(!namedEntityTokens.isEmpty()){
    			//if the next token follows the last one and has the same NER
    			CoreLabel lastToken = namedEntityTokens.get(namedEntityTokens.size()-1);
    			if( (lastToken.index() == (token.index() -1)) &&
    					(lastToken.ner().equals(token.ner())) ){
    				//System.err.println("added to token sequence");
    				namedEntityTokens.add(token);
    			}
    			else{
    				//create new NamedEntity
    				//System.err.println("added old seqyebce to named Entity List");
    				int begOffset = namedEntityTokens.get(0).beginPosition();
    				int endOffset = namedEntityTokens.get(namedEntityTokens.size()-1).endPosition();
    				String rawString = docText.substring(begOffset,endOffset);
    				String nerType = namedEntityTokens.get(0).ner();
    				namedEntities.add(new NamedEntity(begOffset,endOffset,rawString,rawString.replaceAll("\\s+", " "),nerType));
    				//clear tokensList
    				namedEntityTokens = new ArrayList<CoreLabel>();
    				namedEntityTokens.add(token);
    			}
    		}
    		
    		else{
    			//System.err.println("sequence was empty so started new sequence");
    			namedEntityTokens.add(token);
    		}
    	}
		if(!namedEntityTokens.isEmpty()){
			int begOffset = namedEntityTokens.get(0).beginPosition();
			int endOffset = namedEntityTokens.get(namedEntityTokens.size()-1).endPosition();
			String rawString = docText.substring(begOffset,endOffset);
			String nerType = namedEntityTokens.get(0).ner();
			namedEntities.add(new NamedEntity(begOffset,endOffset,rawString,rawString.replaceAll("\\s+", " "),nerType));
			//clear tokensList
			namedEntityTokens = new ArrayList<CoreLabel>();
		}
		
//		System.err.println("Number of named entities is" + namedEntities.size());
//		for(NamedEntity ne : namedEntities){
//			System.err.println(ne.getName());
//		}
		
		return namedEntities;
	}
	
	
	public static void main(String[] args) throws IOException{
		NERCollection nc = new NERCollection();
		
		//read in testCorpus directory and write new .ann files in structured format
		File testCorpus = new File(args[0]);
		if(testCorpus.isDirectory()){
			File[] testDocuments = testCorpus.listFiles();
			for(File testDocument: testDocuments){
				String testDocName = testDocument.getName();
				if(!testDocName.endsWith(".ann")){
					String annoDocName = testDocName + ".ann";
					String annoDocPath = testDocument.getParent()+"/"+annoDocName;
					PrintWriter pw = new PrintWriter(new File(annoDocPath));
					String docText = FileUtils.readFileToString(testDocument);
					List<NamedEntity> namedEntities = nc.collectNamedEntitiesFromDocument(docText);
					//write to .ann file
					pw.write(testDocName+"\n");
					
					for(NamedEntity ne : namedEntities){
						pw.write(ne.getProcessedName() + "\t" + ne.getBegOffset()+":"+ne.getEndOffset() + "\t" + ne.getProcessedName() +"\n");
					}
					pw.close();
				}
			}
		}
	}
}
