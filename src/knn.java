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
	
	public static void main(String[] args){

		final long startTime = System.nanoTime();
		/*trainingData is an ArrayList of type Instance (Check instance.java). Each entry of trainingData corresponds to one instance of the training file. See the comments in instance.java for more info about ArrayList constructors. We can tweak the initial capcity of 'trainingData' & 'testData' to optimum value depending on our results. */
		readData(args[0], trainingData, 0);
		readData(args[1], testData, 1);
		
		final Long fTime = System.nanoTime();
		//System.out.println("File Reading Time: "+(fTime-startTime)/(Math.pow(10,9))+" seconds.");

		normalizeNEW(trainingData,Instance.meanTRN,Instance.stdDevTRN);
		normalizeNEW(testData,Instance.meanTST,Instance.stdDevTST);

		//normalize(trainingData,Instance.maxTRN,Instance.maxTST);
	//	normalize(testData,Instance.maxTST,Instance.maxTST);

		//System.out.println("Normalization Time: "+(System.nanoTime()-fTime)/(Math.pow(10,9))+" seconds.");
		final int kValue = Integer.parseInt(args[2]);
		final int dMetric = Integer.parseInt(args[3]);
		final String fName = args[0].substring(0,args[0].lastIndexOf(".train"))+"_prediction_file.txt";
		try{
			Long eTime = System.nanoTime();
			final BufferedWriter writer = new BufferedWriter(new FileWriter(fName));
			if(dMetric == 0){
                euclideanDistance(kValue,dMetric,writer,trainingData,testData);
            }else{
               cosineSimilarity(kValue,dMetric,writer,trainingData,testData);
            }
			writer.close();
			final long estimatedTime = System.nanoTime() - startTime;
			Long gTime = System.nanoTime();
			System.out.println("Time Cost: "+estimatedTime/(Math.pow(10,9))+" seconds.");
		//	System.out.println("Distance Method Time: "+(eTime-gTime)/(Math.pow(10,9))+" seconds.");
		}
		catch(IOException e){
			System.out.println(e.getMessage());
		}
	}

	/* Method to normalize the data sets */

	public static void standardDeviation(List<Instance> tData, Map<Integer,Double> mean, Map<Integer,Double> stdDev){
		int dataSize = tData.size();
		for(Map.Entry<Integer,Double> entry: mean.entrySet()){
			entry.setValue(entry.getValue()/dataSize);
		}
		int currentIndex; Instance curInstance;
		double curValue;

		for(int i=0;i<dataSize;i++){
			curInstance = tData.get(i);
			for(int j=0;j<curInstance.indexes.size();j++){
				currentIndex = curInstance.indexes.get(j);
				if(stdDev.containsKey(currentIndex)){
					curValue = stdDev.get(currentIndex);					
				}else{
					curValue = 0;
				}
				stdDev.put(currentIndex,(curValue+Math.pow((curInstance.values.get(j)-mean.get(currentIndex)),2)));
			}				
		}
	}

	public static void normalizeNEW(List<Instance> tData, Map<Integer,Double> mean, Map<Integer,Double> stdDev){
		standardDeviation(tData,mean,stdDev);
		Instance curInstance; int currentIndex; double temp;
		for(int i=0;i<tData.size();i++){
			curInstance = tData.get(i);
			for(int j=0;j<curInstance.indexes.size();j++){
				currentIndex = curInstance.indexes.get(j);
				temp = (curInstance.values.get(j)-mean.get(currentIndex))/Math.sqrt((stdDev.get(currentIndex)));
				curInstance.values.set(j,temp);
			}
		}
	}

	public static void normalize(List<Instance> tData, Map<Integer,Double> max, Map<Integer,Double> min){
		int currentIndex; double maxVal,minVal, normalizedValue;
		for(int i=0;i<tData.size();i++){		
			for(int j=0;j<tData.get(i).indexes.size();j++){
				currentIndex = tData.get(i).indexes.get(j);
				maxVal = max.get(currentIndex);
				minVal = min.get(currentIndex);
				if(!(max==min)){
					normalizedValue = (tData.get(i).values.get(j)-minVal)/(maxVal-minVal);
					tData.get(i).values.set(j,normalizedValue);
				}					
			}
		}
		double[] absValues = new double[tData.size()];
		for(int i=0;i<tData.size();i++){
			absValues[i] = 0;
			for(int j=0;j<tData.get(i).indexes.size();j++){
				absValues[i] += Math.pow(tData.get(i).values.get(j),2);
			}
			absValues[i] = Math.sqrt(absValues[i]);
		}
	}

	public static void readData(String fileName, List<Instance> tData, int type){
		try{
			String line; int flag=0; int colPos; Instance instance = new Instance("");
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			while((line=br.readLine())!=null){
				for(String token: line.split("\\s+")){
					if(flag==0){
						instance = new Instance(token);
						flag++;
					}else{
						colPos = token.indexOf(":");
						instance.addTerm(Integer.parseInt(token.substring(0,colPos)),Double.parseDouble(token.substring(colPos+1)),type);				
					}
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

	public static void euclideanDistance(int kValue, int dMetric, BufferedWriter writer, List<Instance> trnData, List<Instance> tstData){
		/* In this method, we are looping over all the test Data Instances, calculating the distance eucledian distance between each of these instances and all the training data instances. These distance values are then stored in an array of objects of type Distance (check instance.java). The distances are then sorted and passed to method 'Classification' with a reference to the test Data ID. */
		float tp = 0; Distances tempDist = new Distances(); double zz; double estTime = 0.0;
        int trdInstanceDimensionSize, tstInstanceDimensionSize; int _tstIndex = 0; int _trnIndex = 0;
		//Iterating over all testData Instances

		for(int a = 0; a<tstData.size();a++){
			//Iterating over all TrainingData Instances
			metricData.clear();
			zz = System.nanoTime();

			for(int b=0; b<trainingData.size();b++){
                tempDist.distanceValue = 0;
                tempDist.fromInstance = 0;

				trdInstanceDimensionSize = trnData.get(b).indexes.size(); //Number of Dimensions of the current trainingData instance
				tstInstanceDimensionSize = tstData.get(a).indexes.size(); //Number of Dimensions of the current testData instance

				/*Loop to calculate the distance between current trainingData instance & testData Instance. i & j keep track of which index we are on in the testData & trainingData instance respectively. */
				for(int i = 0, j = 0; ; ){
					//During each iteration of this for loop, we first check if we have finished traversing through through the testData or trainingData instance
					if(i == tstInstanceDimensionSize){
						if(!(j == trdInstanceDimensionSize)){
							do{
								tempDist.distanceValue += Math.pow(trnData.get(b).values.get(j),2);
								j++;
							}while(j != trdInstanceDimensionSize);
						}
						break;
					}else if(j == trdInstanceDimensionSize){
							do{
								tempDist.distanceValue += Math.pow(tstData.get(a).values.get(i),2);
								i++;
							}while(i != tstInstanceDimensionSize);
							break;
					// The next 'if' statement is reached only if the there are indexes remaining in both the test and training data instances.
					}else{
							_tstIndex = tstData.get(a).indexes.get(i);
							_trnIndex = trnData.get(b).indexes.get(j);
							if(_tstIndex==_trnIndex){
								tempDist.distanceValue += Math.pow((tstData.get(a).values.get(i)-trnData.get(b).values.get(j)),2);
								i++; j++;
					/* The next 'if' statement is reached only if the current indexes of the instances are different and if the testData index is lesser than
					the trainingData index, implying that the training data has a missing dimension */
							}else if(_tstIndex<_trnIndex){
								tempDist.distanceValue += Math.pow(tstData.get(a).values.get(i),2);
								i++;
							}else{
						//This implies that the testData has a missing dimension.
								tempDist.distanceValue += Math.pow(trnData.get(b).values.get(j),2);
								j++;
							}
						}
				}

				if(!(tempDist.distanceValue==0)){
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
			}
		/*//	System.out.println("TEST ID: "+a);
			for(Map.Entry<Double,Integer> entry: metricData.entrySet()){
				System.out.println("Dist: "+entry.getKey()+" from: "+entry.getValue()+" CLass: "+trnData.get(entry.getValue()).instanceClass);
			}*/

			//The classification method returns 1 if the current instance is classified accurately, else returns 0. 'tp' keeps a track of the true positives.
			tp += classification(a,kValue,dMetric,writer,trnData,tstData);
			estTime += (System.nanoTime()-zz)/(Math.pow(10,9));
		}
		System.out.println("Accuracy: "+(tp/tstData.size())*100);
	}

	//Very similar as eucledian distance, except cosine similarity is calculated.
	public static void cosineSimilarity(int kValue, int dMetric, BufferedWriter writer, List<Instance> trnData, List<Instance> tstData){
		float tp = 0; 
		double modTstDataInstance = 0, modTrnDataInstance = 0; //Variables to store the mod values of the instances.
        Similarities tempSim = new Similarities();

		for(int a=0;a<tstData.size();a++){
			metricData.clear();
			for(int i = 0; i<tstData.get(a).indexes.size();i++){
				modTstDataInstance += Math.pow(tstData.get(a).values.get(i),2);
			}
			modTstDataInstance = Math.sqrt(modTstDataInstance);

			for(int b=0; b<trnData.size();b++){
				tempSim.similarityValue = 0;
                tempSim.fromInstance = 0;

				final int trdInstanceDimensionSize = trnData.get(b).indexes.size();
				final int tstInstanceDimensionSize = tstData.get(a).indexes.size();

				for(int i = 0, j = 0; i<tstInstanceDimensionSize && j<trdInstanceDimensionSize; ){
                    int _tstIndex = tstData.get(a).indexes.get(i);
                    int _trnIndex = trnData.get(b).indexes.get(j);
                    if(_tstIndex==_trnIndex){
                    	tempSim.similarityValue += (tstData.get(a).values.get(i))*(trnData.get(b).values.get(j));
                      	i++; j++;
                        continue;
					}else if(_tstIndex<_trnIndex){
						i++; continue;
					}else{
						j++; continue;
					}
				}

				for(int i=0; i<trnData.get(b).indexes.size();i++){
					modTrnDataInstance += Math.pow(trnData.get(b).values.get(i),2);
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
			tp += classification(a,kValue,dMetric,writer,trnData,tstData);
		}

		//System.out.println("**************************************");
		//System.out.println("TP: "+tp+" FP: "+(testData.size()-tp));
		System.out.println("Accuracy: "+(tp/testData.size())*100);
	}

	public static int classification(int testInstanceId, int kValue, int dMetric, BufferedWriter writer, List<Instance> trnData, List<Instance> tstData){
		try{
			String classId;
			double weight = 0;
			//HashMap to store Key,Value pairs where Key - Class Name and Value is weighted distance.
			HashMap<String,Double> classMap = new HashMap<String,Double>(kValue);
			//Loop to populate the 'classMap' with data from the k closest instances.			
		
			for(Map.Entry<Double,Integer> entry: metricData.entrySet()){
				if(dMetric == 0){
/*					System.out.print("Distance: "+entry.getKey()+" between Test Instance "+testInstanceId+" and Training Instance: "+entry.getValue());
					System.out.println(" Class: "+trainingData.get(entry.getValue()).instanceClass);*/
					classId = trainingData.get(entry.getValue()).instanceClass;
					weight = 1/(Math.pow(entry.getKey(),2));/*
					System.out.println("Weight: "+weight);*/
				}else{
					//#System.out.print("Similarity: "+entry.getKey()+" between Test Instance: "+testInstanceId+" and Training Instance: "+entry.getValue());
					//#System.out.println(" Class: "+trainingData.get(entry.getValue()).instanceClass);
					classId = trnData.get(entry.getValue()).instanceClass;
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
			//System.out.println(classMap.entrySet());

			classId = "0"; //temporary variable to store the class Key of the best Class (class with highest weight)
			weight = 0;
			//bestClass is a temporary variable to store the bestClass name and weighted Distance.
			AbstractMap.SimpleEntry<String,Double> bestClass = new AbstractMap.SimpleEntry<String,Double>("0",0.0); 
			for (Map.Entry<String, Double> entry : classMap.entrySet()) {
	 	   		String key = entry.getKey();
	   			double value = entry.getValue();
	   			if(value>=weight){
	   				bestClass = new AbstractMap.SimpleEntry<String,Double>(key,value);
	   				weight = value;
	   			}
	    	}

/*	    	System.out.println("Assigned CLass: "+bestClass.getKey());
	    	System.out.println("Actual Class: "+tstData.get(testInstanceId).instanceClass);*/

	    	writer.write(bestClass.getKey()+"");
	    	writer.newLine();
			
	    	//Checking if the bestClass the model determined is equal to the actual class.
	    	tstData.get(testInstanceId).classifiedClass = (String)bestClass.getKey();
/*	    	System.out.println("Best Class : "+testData.get(testInstanceId).classifiedClass);
	    	System.out.println("Actual Class: "+testData.get(testInstanceId).instanceClass);
			System.out.println("-------------------------------------------------------------------");*/
	    	if(tstData.get(testInstanceId).classifiedClass.equals(tstData.get(testInstanceId).instanceClass)){
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

