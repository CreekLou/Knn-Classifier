import java.util.*;
import java.io.*;
import java.lang.Integer;

class Instance{
	
	public String instanceClass; //The original class of the instance as read from file.
	public String classifiedClass; //This is only for testData entries. This stores the value of the class which we assign it during testing.

	/* Each instance has two ArrayLists (Values & Indexes) as its members. Values stores the values of the dimensions as given by <index>:<value> 
	and indexes store the index values. Note that missing dimensions are not being stored. We thought of using either a HashMap or a LinkedHashMap instead of two arraylists, but decided against it for efficiency purposes. Iteration & Random retrieval have better performance better in arraylists. */
	public List<Double> values;
	public List<Integer> indexes;
	/* Stores the min and max value of any particular dimension */
	public static Map<Integer,Double> minTRN = new HashMap<Integer,Double>();
	public static Map<Integer,Double> minTST = new HashMap<Integer,Double>();
	public static Map<Integer,Double> maxTRN = new HashMap<Integer,Double>();
	public static Map<Integer,Double> maxTST = new HashMap<Integer,Double>();
	public static Map<Integer,Double> meanTRN = new HashMap<Integer,Double>();
	public static Map<Integer,Double> stdDevTRN = new HashMap<Integer,Double>();
	public static Map<Integer,Double> meanTST = new HashMap<Integer,Double>();
	public static Map<Integer,Double> stdDevTST = new HashMap<Integer,Double>();

	public Instance(String instanceClass){
		this.instanceClass = instanceClass;
		values = new ArrayList<Double>(); 
		indexes = new ArrayList<Integer>();
		/*Arraylists by default start with an initial capacity of 10 and when a 11th element is added, it increases it's size by 50%. It then proceeds to 
		copy the the entire arraylist to a new one. This is time consuming and inefficient if done at frequent intervals. So it makes sense to initialize 
		the arraylist wiht a much higher capcity and then trim it after adding elements if required. We've initialized 'values' and 'indexes' with the maximum
		dimension size present in the training & test data files (256) as it is not a lot.

		UPDATE: Removing the initial capacity is making no difference to the time cost :-/ */
	}

	public void addTerm(int index, double value, int type){
		indexes.add(index);
		values.add(value);

		if(type == 0){

			if(meanTRN.containsKey(index)){
				meanTRN.put(index,(meanTRN.get(index)+value));
			}else{
				meanTRN.put(index,value);
			}

			if(maxTRN.containsKey(index)){
				if(maxTRN.get(index)<value){
					maxTRN.put(index,value);
				}
			}else{
				maxTRN.put(index,value);
			}

			if(minTRN.containsKey(index)){
				if(minTRN.get(index)>value){
					minTRN.put(index,value);
				}
			}else{
				minTRN.put(index,value);
			}
		}else{

			if(meanTST.containsKey(index)){
				meanTST.put(index,(meanTST.get(index)+value));
			}else{
				meanTST.put(index,value);
			}

			if(maxTST.containsKey(index)){
				if(maxTST.get(index)<value){
					maxTST.put(index,value);
				}
			}else{
				maxTST.put(index,value);
			}

			if(minTST.containsKey(index)){
				if(minTST.get(index)>value){
					minTST.put(index,value);
				}
			}else{
				minTST.put(index,value);
			}
		}
	}
}

class Distances implements Comparable<Distances>{
	double distanceValue;
	int fromInstance;

	public Distances(){
		this.distanceValue = 0;
		this.fromInstance = 0;
	}

	public int compareTo(Distances o){
		if ((this.distanceValue - o.distanceValue)>0)
			return 1;
		else if ((this.distanceValue - o.distanceValue)==0)
			return 0;
		else return -1;
	}
}

class Similarities implements Comparable<Similarities>{
	double similarityValue;
	int fromInstance;

	public Similarities(){
		this.similarityValue = 0;
		this.fromInstance = 0;
	}

	public int compareTo(Similarities o){
		if((this.similarityValue - o.similarityValue)>0)
			return -1;
		else if ((this.similarityValue - o.similarityValue) == 0)
			return 0;
		else return 1;
	}

}