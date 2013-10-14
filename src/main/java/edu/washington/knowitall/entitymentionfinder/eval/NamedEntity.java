package edu.washington.knowitall.entitymentionfinder.eval;

public class NamedEntity {
		private int begOffset;
		private int endOffset;
		private String name;
		private String processedName;
		private String nerType;
		
		NamedEntity(int begOffset, int endOffset, String name, String processedName, String nerType){
			this.begOffset = begOffset;
			this.endOffset = endOffset;
			this.name = name;
			this.processedName = processedName;
			this.nerType = nerType;
		}
		
		String getName(){
			return name;
		}
		
		String getProcessedName(){
			return processedName;
		}
		
		String getNerType(){
			return nerType;
		}
		
		int getBegOffset(){
			return begOffset;
		}
		
		int getEndOffset(){
			return endOffset;
		}
}
