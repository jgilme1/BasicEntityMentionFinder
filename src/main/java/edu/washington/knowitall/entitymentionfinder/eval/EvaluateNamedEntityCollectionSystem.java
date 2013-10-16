package edu.washington.knowitall.entitymentionfinder.eval;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import edu.washington.knowitall.entitymentionfinder.eval.NamedEntityCollection;
import edu.washington.knowitall.entitymentionfinder.eval.HelperMethods;

public class EvaluateNamedEntityCollectionSystem {
	public static void main(String[] args) throws Exception{
		//run system
		NERCollection nc = NERCollection.getInstance();
		File testCorpusDirectory = new File(args[0]);
		//iterate over annotated file

		if(testCorpusDirectory.isDirectory()){
			int totalNumGuesses =0;
			int totalNumBestMentions = 0;
			int totalNumCorrectGuesses =0;
			int totalAnnotations = 0;
			int correctAnswers =0;
			
			int totalPERBestMentions = 0;
			int totalPERGuesses =0;
			int totalCorrectPERGuesses = 0;
		
			int totalLOCBestMentions = 0;
			int totalLOCGuesses =0;
			int totalCorrectLOCGuesses = 0;
			
			int totalORGBestMentions = 0;
			int totalORGGuesses =0;
			int totalCorrectORGGuesses = 0;
			
			
			
			
			File[] files = testCorpusDirectory.listFiles();
			for(File f: files){
				String fileName = f.getName();
				if(fileName.endsWith(".ann")){
					//load in BestMentionAnnotations
					File textFile = new File(f.getPath().substring(0,f.getPath().length()-4));
					List<NamedEntity> namedEntities = nc.collectNamedEntitiesFromDocument(FileUtils.readFileToString(textFile));
					List<BestMentionAnnotation> labelledBestMentionAnnotations = new ArrayList<BestMentionAnnotation>();
					
					
					//iterate over namedEntities, collecting annotations to form a List<BestMentionAnnotation>
					LineIterator annotationLineIterator = FileUtils.lineIterator(f);
					int namedEntitiesIndex =0;
					while(annotationLineIterator.hasNext()){
						String nextLine = annotationLineIterator.nextLine();
						if(nextLine.contains("\t")){
							String[] values = nextLine.split("\t");
							String processedName = values[0];
							String offsetString = values[1];
							String annotatedName = values[2];
							
							NamedEntity ne = namedEntities.get(namedEntitiesIndex);
							BestMentionAnnotation bma = new BestMentionAnnotation(ne,annotatedName);
							labelledBestMentionAnnotations.add(bma);
							namedEntitiesIndex ++;
						}
					}
					//iterate over namedEntities, finding the best EntityMentionString by our rules
					//create data object for NameEntityCollection
					List<String> locations = new ArrayList<String>();
					List<String> organizations = new ArrayList<String>();
					List<String> people = new ArrayList<String>();

					for(NamedEntity ne : namedEntities){
						if(ne.getNerType().equals("LOCATION")){
							locations.add(ne.getProcessedName());
						}
						else if(ne.getNerType().equals("PERSON")){
							people.add(ne.getProcessedName());

						}
						else if(ne.getNerType().equals("ORGANIZATION")){
							organizations.add(ne.getProcessedName());	
						}
						else{
							throw new Exception ("NER Type must be PERSON, LOCATION, or ORGANIZATION");
						}
					}
					scala.collection.immutable.List<String> scalaLocations = scala.collection.JavaConversions.asScalaIterable(locations).toList();
					scala.collection.immutable.List<String> scalaOrganizations = scala.collection.JavaConversions.asScalaIterable(organizations).toList();
					scala.collection.immutable.List<String> scalaPeople = scala.collection.JavaConversions.asScalaIterable(people).toList();

					NamedEntityCollection nec = new NamedEntityCollection(scalaLocations,scalaOrganizations,scalaPeople);
					
					String outputFileName = textFile.getParent() + "/" + textFile.getName() + ".out";
					File outputFile = new File(outputFileName);
					PrintWriter pw = new PrintWriter(outputFile);
					pw.write(textFile.getName() + "\n");
					
					List<BestMentionAnnotation> systemBestMentionAnnotations = new ArrayList<BestMentionAnnotation>();
					for(NamedEntity ne: namedEntities){
						String bestEntityMentionString = HelperMethods.identifyBestEntityStringByRules(nec, ne.getNerType(), ne.getBegOffset(), ne.getEndOffset(), FileUtils.readFileToString(textFile));
						bestEntityMentionString = bestEntityMentionString.replaceAll("\\s+", " ");
						pw.write(ne.getProcessedName() + "\t" + bestEntityMentionString +"\n");
						
						BestMentionAnnotation bma = new BestMentionAnnotation(ne,bestEntityMentionString);
						systemBestMentionAnnotations.add(bma);
					}
					
					
					//output document level results
					int size = labelledBestMentionAnnotations.size();
					int documentNumGuesses =0;
					int documentNumBestMentions = 0;
					int documentNumCorrectGuesses =0;
					for(int i =0; i < size; i ++){
						BestMentionAnnotation labelledBma = labelledBestMentionAnnotations.get(i);
						BestMentionAnnotation systemBma = systemBestMentionAnnotations.get(i);
						
						NamedEntity labelledNE = labelledBma.getNamedEntity();
						String nerType = labelledNE.getNerType();
						
						String guessedBestMention = systemBma.getBestMentionName();
						String actualBestMention = labelledBma.getBestMentionName();
						String originalMention = labelledBma.getNamedEntity().getProcessedName();
						
						if(!originalMention.equals(actualBestMention)){
							documentNumBestMentions ++;
							
							if(nerType.equals("LOCATION")){
								totalLOCBestMentions++;
							}
							if(nerType.equals("ORGANIZATION")){
								totalORGBestMentions++;
							}
							if(nerType.equals("PERSON")){
								totalPERBestMentions++;
							}
						}
						
						if(!originalMention.equals(guessedBestMention)){
							documentNumGuesses ++;
							if(nerType.equals("LOCATION")){
								totalLOCGuesses++;
							}
							if(nerType.equals("ORGANIZATION")){
								totalORGGuesses++;
							}
							if(nerType.equals("PERSON")){
								totalPERGuesses++;
							}
							if(guessedBestMention.equals(actualBestMention)){
								documentNumCorrectGuesses++;
								if(nerType.equals("LOCATION")){
									totalCorrectLOCGuesses++;
								}
								if(nerType.equals("ORGANIZATION")){
									totalCorrectORGGuesses++;
								}
								if(nerType.equals("PERSON")){
									totalCorrectPERGuesses++;
								}
							}
						}
						
						if(guessedBestMention.equals(actualBestMention)){
							correctAnswers ++;
						}
							
							
						totalAnnotations++;
					}
					
					double precision = documentNumGuesses > 0 ? ((double) documentNumCorrectGuesses) / ((double) documentNumGuesses) : -1.0; 
					double recall = documentNumBestMentions > 0 ? (((double) documentNumCorrectGuesses) / ((double) documentNumBestMentions)) : -1.0;
					pw.write("Number of Best Mentions: " + documentNumBestMentions +"\n");
					pw.write("Number of Best Mention Guesses: " + documentNumGuesses+"\n");
					pw.write("Number of Correct Best Mention Guesses: " + documentNumCorrectGuesses+"\n");
					pw.write("Precision: " + precision+"\n");
					pw.write("Recall: " + recall+"\n");
					pw.close();
					
					totalNumGuesses += documentNumGuesses;
					totalNumCorrectGuesses += documentNumCorrectGuesses;
					totalNumBestMentions += documentNumBestMentions;
				}
			}
			
			PrintWriter pwResults = new PrintWriter(testCorpusDirectory.getPath()+"/results.out");
			double precision = totalNumGuesses > 0 ? ((double) totalNumCorrectGuesses) / ((double) totalNumGuesses) : -1.0; 
			double recall = totalNumBestMentions > 0 ? (((double) totalNumCorrectGuesses) / ((double) totalNumBestMentions)) : -1.0;
			double overallPrecision = ((double)correctAnswers) / ((double)totalAnnotations);
			pwResults.write("Number of Best Mentions: " + totalNumBestMentions+"\n");
			pwResults.write("Number of Best Mention Guesses: " + totalNumGuesses+"\n");
			pwResults.write("Number of Correct Best Mention Guesses: " + totalNumCorrectGuesses+"\n");
			pwResults.write("Precision: " + precision+"\n");
			pwResults.write("Recall: " + recall+"\n");
			pwResults.write("Overall Precision: " + overallPrecision+"\n\n\n");
			
			
			double perPrecision = totalPERGuesses > 0 ? ((double) totalCorrectPERGuesses) / ((double) totalPERGuesses) : -1.0;
			double perRecall = totalPERBestMentions > 0 ? (((double) totalCorrectPERGuesses) / ((double) totalPERBestMentions)) : -1.0;
			pwResults.write("Number of Person Best Mentions: " + totalPERBestMentions + "\n");
			pwResults.write("Number of Person Best Mention Guesses: " + totalPERGuesses + "\n");
			pwResults.write("Number of Correct Person Best Mention Guesses: " + totalCorrectPERGuesses + "\n");
			pwResults.write("PER Precision: " + perPrecision +"\n");
			pwResults.write("PER Recall: " + perRecall + "\n");
			pwResults.write("\n\n\n");
			
			
			double orgPrecision = totalORGGuesses > 0 ? ((double) totalCorrectORGGuesses) / ((double) totalORGGuesses) : -1.0;
			double orgRecall = totalORGBestMentions > 0 ? (((double) totalCorrectORGGuesses) / ((double) totalORGBestMentions)) : -1.0;
			pwResults.write("Number of Organization Best Mentions: " + totalORGBestMentions + "\n");
			pwResults.write("Number of Organization Best Mention Guesses: " + totalORGGuesses + "\n");
			pwResults.write("Number of Correct Organization Best Mention Guesses: " + totalCorrectORGGuesses + "\n");
			pwResults.write("ORG Precision: " + orgPrecision +"\n");
			pwResults.write("ORG Recall: " + orgRecall + "\n");
			pwResults.write("\n\n\n");
			
			
			double locPrecision = totalLOCGuesses > 0 ? ((double) totalCorrectLOCGuesses) / ((double) totalLOCGuesses) : -1.0;
			double locRecall = totalLOCBestMentions > 0 ? (((double) totalCorrectLOCGuesses) / ((double) totalLOCBestMentions)) : -1.0;
			pwResults.write("Number of Location Best Mentions: " + totalLOCBestMentions + "\n");
			pwResults.write("Number of Location Best Mention Guesses: " + totalLOCGuesses + "\n");
			pwResults.write("Number of Correct Location Best Mention Guesses: " + totalCorrectLOCGuesses + "\n");
			pwResults.write("LOC Precision: " + locPrecision +"\n");
			pwResults.write("LOC Recall: " + locRecall + "\n");
			
			
			
			pwResults.close();
			
			//output corpus level performance results
			
		}
	}
}
