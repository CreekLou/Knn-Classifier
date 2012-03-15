/* args[0] -> Training File Name
   args[1] -> Test File Name
   args[2] -> K-Value
   args[3] -> Metric Type

   Uncomment all comments beginning with '#' if you want to see the 'processing' when running the program, but note that it increases time cost.
*/

import java.util.*;
import java.io.*;

public class knn{

	public static List<Instance> trainingData = new ArrayList<Instance>();
	public static List<Instance> testData = new ArrayList<Instance>();
	public static TreeMap<Double,Integer> metricData = new TreeMap<Double,Integer>();
	public static Map<String,ArrayList<Instance>> clusters = new HashMap<String,ArrayList<Instance>>();
	public static void main(String[] args){

		final long startTime = System.nanoTime();
		/*trainingData is an ArrayList of type Instance (Check instance.java). Each entry of trainingData corresponds to one instance of the training file. See the comments in instance.java for more info about ArrayList constructors. We can tweak the initial capcity of 'trainingData' & 'testData' to optimum value depending on our results. */
		readData(args[0], trainingData, 0);	
		System.out.println("TD SIZE: "+trainingData.size());
		normalizeNEW(trainingData,Instance.meanTRN,Instance.stdDevTRN);	
		//normalize(trainingData,Instance.maxTRN,Instance.minTRN);
		bootstrap(5);

		readData(args[1], testData, 1);	
		normalizeNEW(testData,Instance.meanTST,Instance.stdDevTST);
		//normalize(testData,Instance.maxTST,Instance.minTST);		

	
		System.out.println("There are "+clusters.size()+" clusters.");
		System.out.println("New Size: "+trainingData.size());

		final int kValue = Integer.parseInt(args[2]);
		final int dMetric = Integer.parseInt(args[3]);
		final String fName = args[0].substring(0,args[0].lastIndexOf(".train"))+"_prediction_file.txt";
		try{
			final BufferedWriter writer = new BufferedWriter(new FileWriter(fName));
			if(dMetric == 0){
                euclideanDistance(kValue,dMetric,writer);
            }else{
                //cosineSimilarity(kValue,dMetric,writer);
            }
			writer.close();
			final long estimatedTime = System.nanoTime() - startTime;
			System.out.println("Time Cost: "+estimatedTime/(Math.pow(10,9))+" seconds.");
		}
		catch(IOException e){
			System.out.println(e.getMessage());
		}

	}

	public static void standardDeviation(List<Instance> tData, Map<Integer,Double> mean, Map<Integer,Double> stdDev){
		int dataSize = tData.size(); double temp;
		for(Map.Entry<Integer,Double> entry: mean.entrySet()){
			entry.setValue(entry.getValue()/dataSize);
		}
		int currentIndex; Instance curInstance;
		double curValue;

		for(int i=0;i<dataSize;i++){
			curInstance = tData.get(i);

			for(Map.Entry<Integer,Double> entry: curInstance.parameters.entrySet()){
				int curKey = entry.getKey();
				double curVal = entry.getValue();
				if(stdDev.containsKey(curKey)){
					temp = stdDev.get(curKey);
				}else{
					temp = 0;
				}
				stdDev.put(curKey, (temp+Math.pow((curVal-mean.get(curKey)),2)));
			}
		}
	}

	public static void normalizeNEW(List<Instance> tData, Map<Integer,Double> mean, Map<Integer,Double> stdDev){
		standardDeviation(tData,mean,stdDev); double curVal; int curKey;
		Instance curInstance; int currentIndex; double temp;
		for(int i=0;i<tData.size();i++){
			curInstance = tData.get(i);

			for(Map.Entry<Integer,Double> entry: curInstance.parameters.entrySet()){
				curKey = entry.getKey();
				curVal = entry.getValue();
				//System.out.println("Current Value: "+curVal+" Mean: "+mean.get(curKey));
				temp = (curVal-mean.get(curKey))/Math.sqrt((stdDev.get(curKey)));
				//System.out.println("STDDEV: "+stdDev.get(curKey)+" new value: "+temp);	
				entry.setValue(temp);
			}
		}
	}

	public static void normalize(List<Instance> tData, Map<Integer,Double> max, Map<Integer,Double> min){
		for(int i=0;i<tData.size();i++){
			int currentIndex; double normalizedValue, maxValue, minValue;
			for(Map.Entry<Integer,Double> entry: tData.get(i).parameters.entrySet()){
				currentIndex = entry.getKey();
				maxValue = max.get(currentIndex);
				minValue = min.get(currentIndex);
				if(!(max==min)){
					normalizedValue = (entry.getValue()-minValue)/(maxValue-minValue);
					tData.get(i).parameters.put(entry.getKey(),normalizedValue);
				}
			}			
		}		
	}

	public static void bootstrap(int rValue){
		ArrayList<Instance> additions = new ArrayList<Instance>();
		ArrayList<Instance> instances;
		ArrayList<Instance> selectedInstances = new ArrayList<Instance>();	String className; 
		Map.Entry<Integer,Double> outerEntry;
		Map.Entry<Integer,Double> innerEntry;
		Map.Entry<Integer,Double> temp;
		int[] indexes = new int[rValue];

		Distances tempDist = new Distances();

		for(Map.Entry<String,ArrayList<Instance>> cluster: clusters.entrySet()){
			instances = cluster.getValue();
			className = cluster.getKey();

/*			System.out.println("Processing cluster of class "+className+" with "+cluster.getValue().size()+" instances.");
			System.out.println("---------------");
			for(int i=0; i<cluster.getValue().size();i++){
				System.out.println(cluster.getValue().get(i).parameters.entrySet());				
			}
			System.out.println("-----------");*/

			for(int i=0;i<instances.size();i++){

				metricData.clear();

				for(int j=0;j<instances.size();j++){

					Iterator outer = instances.get(i).parameters.entrySet().iterator();
					Iterator inner = instances.get(j).parameters.entrySet().iterator();
					tempDist.distanceValue = 0;
					tempDist.fromInstance = 0;

					do{
						if(!outer.hasNext()){
							if(inner.hasNext()){
								do{
									innerEntry = (Map.Entry)inner.next();
									tempDist.distanceValue += Math.pow(innerEntry.getValue(),2);
								}while(inner.hasNext());
							}
							break;
						}	else if(!inner.hasNext()){
									do{
										outerEntry = (Map.Entry)outer.next();
										tempDist.distanceValue += Math.pow(outerEntry.getValue(),2);
									}while(outer.hasNext());
									break;
						}	else{
								outerEntry = (Map.Entry)outer.next();
								innerEntry = (Map.Entry)inner.next();
								if(innerEntry.getKey()==innerEntry.getKey()){
									tempDist.distanceValue += Math.pow((innerEntry.getValue()-outerEntry.getValue()),2);				
								}else if(innerEntry.getKey()<outerEntry.getKey()){
									tempDist.distanceValue += Math.pow(innerEntry.getValue(),2);
								}else{
									//This implies that the testData has a missing dimension.
								tempDist.distanceValue += Math.pow(outerEntry.getValue(),2);
							}
						}
					}while(outer.hasNext() || inner.hasNext());

					tempDist.distanceValue = Math.sqrt(tempDist.distanceValue);
					tempDist.fromInstance = j; //jth elem in CLuster
			
	                if(!(i==j)){
	                	if(metricData.size()>=rValue){
	                		if(metricData.lastKey()>tempDist.distanceValue){
	                		metricData.remove(metricData.lastKey());
	                		metricData.put(tempDist.distanceValue,tempDist.fromInstance);
		                	}
	                	}else{
	                		metricData.put(tempDist.distanceValue,tempDist.fromInstance);
	                	}
	                }
				}

				//MetricData now stores the smallest 'RValue' distances btween ith element in cluster and corresponding positions of other element in the cluster.

				int z = 0;

				selectedInstances.clear();

				for(Map.Entry<Double,Integer> entry: metricData.entrySet()){
					selectedInstances.add(instances.get(entry.getValue()));
				}

				//System.out.println("Number of selected Instances: "+selectedInstances.size()+);

				Instance newInstance = new Instance(className);

				for(Instance inst: selectedInstances){

					double[] weights = new double[256]; double sum = 0; double newValue; int curKey;
					for(int q=0 ; q<256; q++){
						weights[q] = 1/(rValue+1);
						sum += weights[q];
					}

					Iterator selIndexes = inst.parameters.entrySet().iterator();
					
					while(selIndexes.hasNext()){
						temp = (Map.Entry)selIndexes.next();
						curKey = temp.getKey();
						if(newInstance.parameters.containsKey(curKey)){
							newValue = newInstance.parameters.get(curKey) + (weights[curKey]*temp.getValue())/(sum);
							newInstance.parameters.put(curKey,newValue);
						}else{
							newValue = (weights[curKey]*temp.getValue())/(sum);
							newInstance.parameters.put(curKey,newValue);
						}
					}
				}

				trainingData.add(newInstance);
			}
		}
	}

	/*Method to read data from files. */
	public static void readData(String fileName, List<Instance> tData, int type){
		try{
			String line; int flag=0; int colPos; Instance instance = new Instance("");
			String className = "";
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			ArrayList<Instance> temp;
			while((line=br.readLine())!=null){
				for(String token: line.split("\\s+")){
					if(flag==0){
						instance = new Instance(token);
						className = token;
						flag++;
					}else{
						colPos = token.indexOf(":");
						instance.addTerm(Integer.parseInt(token.substring(0,colPos)),Double.parseDouble(token.substring(colPos+1)),type);
					}
				}
				if(clusters.containsKey(className)){
					temp = clusters.get(className);
					temp.add(instance);
					clusters.put(className,temp);	
				}else{
					temp = new ArrayList<Instance>();
					temp.add(instance);
					clusters.put(className,temp);
				}
				tData.add(instance);
				flag = 0;
			}
			br.close();
		}
		catch(IOException e){
			System.out.println("File IO Error "+e.getMessage());
		}
	}

	public static void euclideanDistance(int kValue, int dMetric, BufferedWriter writer){
		/* In this method, we are looping over all the test Data Instances, calculating the distance eucledian distance between each of these instances and all the training data instances. These distance values are then stored in an array of objects of type Distance (check instance.java). The distances are then sorted and passed to method 'Classification' with a reference to the test Data ID. */
		float tp = 0; Distances tempDist = new Distances(); 
        Map.Entry<Integer,Double> TSTentry; 
        Map.Entry<Integer,Double>TRNentry;
		//Iterating over all testData Instances

		for(int a = 0; a<testData.size();a++){
			//Iterating over all TrainingData Instances
			metricData.clear();

			for(int b=0; b<trainingData.size();b++){

                tempDist.distanceValue = 0;
                tempDist.fromInstance = 0;

				Iterator TSTIterator = testData.get(a).parameters.entrySet().iterator();
				Iterator TRNIterator = trainingData.get(b).parameters.entrySet().iterator();

				/*Loop to calculate the distance between current trainingData instance & testData Instance. i & j keep track of which index we are on in the testData & trainingData instance respectively. */

				do{
					//During each iteration of this for loop, we first check if we have finished traversing through through the testData or trainingData instance

					if(!TSTIterator.hasNext()){
						if(TRNIterator.hasNext()){
							do{
								TRNentry = (Map.Entry)TRNIterator.next();
								tempDist.distanceValue += Math.pow(TRNentry.getValue(),2);
							}while(TRNIterator.hasNext());
						}
						break;
					}	else if(!TRNIterator.hasNext()){
								do{
									TSTentry = (Map.Entry)TSTIterator.next();
									tempDist.distanceValue += Math.pow(TSTentry.getValue(),2);
								}while(TSTIterator.hasNext());
								break;
					// The next 'if' statement is reached only if the there are indexes remaining in both the test and training data instances.
					}	else{
							TSTentry = (Map.Entry)TSTIterator.next();
							TRNentry = (Map.Entry)TRNIterator.next();
							if(TSTentry.getKey()==TSTentry.getKey()){
								tempDist.distanceValue += Math.pow((TSTentry.getValue()-TRNentry.getValue()),2);		
					/* The next 'if' statement is reached only if the current indexes of the instances are different and if the testData index is lesser thanthe trainingData index, implying that the training data has a missing dimension */				
							}else if(TSTentry.getKey()<TRNentry.getKey()){
								tempDist.distanceValue += Math.pow(TSTentry.getValue(),2);
							}else{
								//This implies that the testData has a missing dimension.
								tempDist.distanceValue += Math.pow(TRNentry.getValue(),2);
							}
					}

				}while(TSTIterator.hasNext() || TRNIterator.hasNext());

				tempDist.distanceValue = Math.sqrt(tempDist.distanceValue);
                tempDist.fromInstance = b;

                if(metricData.size()>=kValue){
                	if(metricData.lastKey()>tempDist.distanceValue){
                		metricData.remove(metricData.lastKey());
                		metricData.put(tempDist.distanceValue,tempDist.fromInstance);
                	}
                }else{
                	metricData.put(tempDist.distanceValue,tempDist.fromInstance);
                }
			}
			//The classification method returns 1 if the current instance is classified accurately, else returns 0. 'tp' keeps a track of the true positives.
			tp += classification(a,kValue,dMetric,writer);
		}
		System.out.println("Accuracy: "+(tp/testData.size())*100);
	}

	//Very similar as eucledian distance, except cosine similarity is calculated.
/*	public static void cosineSimilarity(int kValue, int dMetric, BufferedWriter writer){
		float tp = 0; 
		double modTstDataInstance = 0, modTrnDataInstance = 0; //Variables to store the mod values of the instances.
        Similarities tempSim = new Similarities();

		for(int a=0;a<testData.size();a++){
			metricData.clear();
			for(int i = 0; i<testData.get(a).indexes.size();i++){
				modTstDataInstance += Math.pow(testData.get(a).values.get(i),2);
			}
			modTstDataInstance = Math.sqrt(modTstDataInstance);

			for(int b=0; b<trainingData.size();b++){
				tempSim.similarityValue = 0;
                tempSim.fromInstance = 0;

				final int trdInstanceDimensionSize = trainingData.get(b).indexes.size();
				final int tstInstanceDimensionSize = testData.get(a).indexes.size();

				for(int i = 0, j = 0; i<tstInstanceDimensionSize && j<trdInstanceDimensionSize; ){
                    int _tstIndex = testData.get(a).indexes.get(i);
                    int _trnIndex = trainingData.get(b).indexes.get(j);
                    if(_tstIndex==_trnIndex){
                    	tempSim.similarityValue += (testData.get(a).values.get(i))*(trainingData.get(b).values.get(j));
                      	i++; j++;
                        continue;
					}else if(_tstIndex<_trnIndex){
						i++; continue;
					}else{
						j++; continue;
					}
				}

				for(int i=0; i<trainingData.get(b).indexes.size();i++){
					modTrnDataInstance += Math.pow(trainingData.get(b).values.get(i),2);
				}
				modTrnDataInstance = Math.sqrt(modTrnDataInstance);
				tempSim.similarityValue = (tempSim.similarityValue)/(modTrnDataInstance*modTstDataInstance);
				tempSim.fromInstance = b;

				if(metricData.size()>=kValue){
                	if(metricData.firstKey()<tempSim.similarityValue){
                		metricData.remove(metricData.firstKey());
                		metricData.put(tempSim.similarityValue,tempSim.fromInstance);
                	}
                }else{
                	metricData.put(tempSim.similarityValue,tempSim.fromInstance);
                }
			}

			tp += classification(a,kValue,dMetric,writer);
		}

		//System.out.println("**************************************");
		//System.out.println("TP: "+tp+" FP: "+(testData.size()-tp));
		System.out.println("Accuracy: "+(tp/testData.size())*100);
	}*/

		public static int classification(int testInstanceId, int kValue, int dMetric, BufferedWriter writer){
		try{
			String classId;
			double weight = 0;
			//HashMap to store Key,Value pairs where Key - Class Name and Value is weighted distance
			Map<String,Double> classMap = new HashMap<String,Double>(kValue);
			//Loop to populate the 'classMap' with data from the k closest instances.

			for(Map.Entry<Double,Integer> entry: metricData.entrySet()){
				if(dMetric == 0){
					//#System.out.print("Distance: "+distances[i].distanceValue+" between Test Instance "+testInstanceId+" and Training Instance: "+distances[i].fromInstance);
					//#System.out.println(" Class: "+trainingData.get(distances[i].fromInstance).instanceClass);
					classId = trainingData.get(entry.getValue()).instanceClass;
					weight = 1/(Math.pow(entry.getKey(),2));
					//#System.out.println("Weight: "+weight);
				}else{
					//#System.out.print("Similarity: "+entry.getKey()+" between Test Instance: "+testInstanceId+" and Training Instance: "+entry.getValue());
					//#System.out.println(" Class: "+trainingData.get(entry.getValue()).instanceClass);
					classId = trainingData.get(entry.getValue()).instanceClass;
					weight = entry.getKey(); 
					//#System.out.println("Weight: "+weight);
				}
				if(classMap.containsKey(classId)){
					classMap.put(classId,(classMap.get(classId)+weight));
				}
				else{
					classMap.put(classId,weight);
				}
			}

			//#System.out.println(classMap.entrySet());

			classId = "0"; //temporary variable to store the class Key of the best Class (class with highest weight)
			weight = 0;
			//bestClass is a temporary variable to store the bestClass name and weighted Distance.
			Map.Entry<String,Double> bestClass = new AbstractMap.SimpleEntry<String,Double>("0",0.0); 
			for (Map.Entry<String, Double> entry : classMap.entrySet()) {
	 	   		String key = entry.getKey();
	   			double value = entry.getValue();
	   			if(value>=weight){
	   				bestClass = new AbstractMap.SimpleEntry<String,Double>(key,value);
	   				weight = value;
	   			}
	    	}

	    	writer.write(bestClass.getKey()+"");
	    	writer.newLine();

		    //#System.out.println();
	    	//Checking if the bestClass the model determined is equal to the actual class.
	    	testData.get(testInstanceId).classifiedClass = (String)bestClass.getKey();
	    	//#System.out.println("Best Class : "+testData.get(testInstanceId).classifiedClass);
	    	//#System.out.println("Actual Class: "+testData.get(testInstanceId).instanceClass);
			//#System.out.println("-------------------------------------------------------------------");
	    	if(testData.get(testInstanceId).classifiedClass.equals(testData.get(testInstanceId).instanceClass)){
	    		return 1;
	    	}else{
	    		return 0;
	    	}	
		}
		catch(IOException e){
			System.out.println("Writing Error!");
		}		
		return 0;
	}

}

