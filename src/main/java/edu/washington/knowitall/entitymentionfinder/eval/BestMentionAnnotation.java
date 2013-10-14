package edu.washington.knowitall.entitymentionfinder.eval;

public class BestMentionAnnotation {

	private NamedEntity ne;
	private String bestMentionString;
	
	public BestMentionAnnotation(NamedEntity ne, String bestMentionString){
		this.ne = ne;
		this.bestMentionString = bestMentionString;
	}
	
	public boolean annotationDiffers(){
		if(ne.getProcessedName().equals(bestMentionString)){
			return true;
		}
		else{
			return false;
		}
	}
	
	public String getBestMentionName(){
		return bestMentionString;
	}
	
	public NamedEntity getNamedEntity(){
		return ne;
	}
}
