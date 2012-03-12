import java.util.*;
import java.io.*;
import java.lang.Integer;

class Instance{
	
	public String instanceClass; //The original class of the instance as read from file.
	public String classifiedClass; //This is only for testData entries. This stores the value of the class which we assign it during testing.

	public Map<Integer,Double> parameters = new LinkedHashMap<Integer,Double>();
	/* Stores the min and max value of any particular dimension */
	public static Map<Integer,Double> min = new HashMap<Integer,Double>();
	public static Map<Integer,Double> max = new HashMap<Integer,Double>();

	public Instance(String instanceClass){
		this.instanceClass = instanceClass;
	}

	public void addTerm(int index, double value){
		parameters.put(index,value);
		
		if(max.containsKey(index)){
			if(max.get(index)<value){
				max.put(index,value);
			}
		}else{
			max.put(index,value);
		}

		if(min.containsKey(index)){
			if(min.get(index)>value){
				min.put(index,value);
			}
		}else{
			min.put(index,value);
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