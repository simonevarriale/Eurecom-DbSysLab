package simpledb;

import java.security.KeyStore.Entry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Knows how to compute some aggregate over a set of IntFields.
 */
public class IntegerAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;
    private int gbfield;
    private Type gbfieldtype;
    private int afield;
    private Op what;
    private String afieldName, gbfieldName;
    private HashMap<Field, Integer> count, avg, sum, min, max;
    /**
     * Aggregate constructor
     * 
     * @param gbfield
     *            the 0-based index of the group-by field in the tuple, or
     *            NO_GROUPING if there is no grouping
     * @param gbfieldtype
     *            the type of the group by field (e.g., Type.INT_TYPE), or null
     *            if there is no grouping
     * @param afield
     *            the 0-based index of the aggregate field in the tuple
     * @param what
     *            the aggregation operator
     */

    public IntegerAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        // some code goes here
    	this.gbfield = gbfield;
    	this.gbfieldtype = gbfieldtype;
    	this.afield = afield;
    	this.what = what;
    	this.count = new HashMap<>();
    	this.avg = new HashMap<>();
    	this.sum = new HashMap<>();
    	this.min = new HashMap<>();
    	this.max = new HashMap<>();
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the
     * constructor
     * 
     * @param tup
     *            the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
        // some code goes here
    	Field groupField;
        IntField aggregateField;
        
        //if there is a group by field
        if(this.gbfield != Aggregator.NO_GROUPING) {
        	
        	//assign gb field and save also the name (useful later for the TupleDesc in Iterator)
            groupField = tup.getField(gbfield);
            gbfieldName = tup.getTupleDesc().getFieldName(gbfield);
        }
        else
        	//otherwise group field is just an IntField
            groupField = new IntField(Aggregator.NO_GROUPING);
        
        //assign the aggregate field and save name for the same reason as before
        aggregateField = (IntField) tup.getField(afield);
        afieldName = tup.getTupleDesc().getFieldName(afield);
        
        
        //updating the map with the aggregate
        if(this.what == Op.COUNT){
        	if(count.containsKey(groupField)) {
        		count.put(groupField, count.get(groupField) + 1);
        	}
        	else count.put(groupField,1);
    		
    	}
    	
        if(this.what == Op.SUM){
    		if(sum.containsKey(groupField)) {
        		sum.put(groupField, sum.get(groupField)+ aggregateField.getValue());
        	}
        	else sum.put(groupField, aggregateField.getValue());
    		
    	}
		
        //in case of avg we need to update both count and sum in order to perform the avg casted to int
        if(this.what == Op.AVG){
        	
        	if(count.containsKey(groupField)) {
        		count.put(groupField, count.get(groupField) + 1);
        	}
        	else count.put(groupField,1);
        	
        	if(sum.containsKey(groupField)) {
        		sum.put(groupField, sum.get(groupField)+ aggregateField.getValue());
        	}
        	else sum.put(groupField, aggregateField.getValue());
        	
			avg.put(groupField, (int) (sum.get(groupField) / count.get(groupField)));

		}
		
        if(this.what == Op.MIN){
			if(min.containsKey(groupField)) {
				min.put(groupField, Math.min( min.get(groupField), aggregateField.getValue()) );
        	}
        	else min.put(groupField, aggregateField.getValue());
		}
		
        if(this.what == Op.MAX){
			if(max.containsKey(groupField)) {
				max.put(groupField, Math.max( max.get(groupField), aggregateField.getValue()) );
        	}
        	else max.put(groupField, aggregateField.getValue());
		}
        
        
            
    	
    }

    /**
     * Create a OpIterator over group aggregate results.
     * 
     * @return a OpIterator whose tuples are the pair (groupVal, aggregateVal)
     *         if using group, or a single (aggregateVal) if no grouping. The
     *         aggregateVal is determined by the type of aggregate specified in
     *         the constructor.
     */
    public OpIterator iterator() {
        // some code goes here
    	Type[] type;
    	String[] field;
    	
    	//assigning the type and field for the TupleDesc
    	if(gbfield == Aggregator.NO_GROUPING) {
    		type = new Type[1];
    		field = new String[1];
    		type[0] = null;
            field[0] = afieldName;
    	}
    	else {
    		type = new Type[2];
    		field = new String[2];
            type[0] = gbfieldtype;
            field[0] = gbfieldName;
            type[1] = Type.INT_TYPE;
            field[1] = afieldName;
    		
    	}
    	
    	TupleDesc td = new TupleDesc(type,field);
    	ArrayList<Tuple> tuples = new ArrayList<Tuple>();
    	
    	//basing on the operation we get the tuples from the map
    	if(what == Op.COUNT){
    		tuples = getTuples(count, td);
    	}
    	else if(what == Op.AVG){
    		tuples = getTuples(avg, td);
    	}
		else if(what == Op.SUM){
			tuples = getTuples(sum, td);
		}
		else if(what == Op.MIN){
			tuples = getTuples(min, td);
		}
		else if(what == Op.MAX){
			tuples = getTuples(max, td);
		}
		
    	 	
    	return new TupleIterator(td, tuples);
    }
    
    private ArrayList<Tuple> getTuples(HashMap<Field, Integer> result, TupleDesc td) {
    	
    	ArrayList<Tuple> tuples = new ArrayList<Tuple>();
    	
    	//scanning the map creating the result
    	for(Map.Entry<Field, Integer> e : result.entrySet()) {
    		
    		Field f = e.getKey();
    		int value = e.getValue();
    		
    		Tuple t = new Tuple(td);
    		
    		if(td.numFields() == 1){
                t.setField(0, new IntField(value));
            }
            else {
                t.setField(0, f);
                t.setField(1, new IntField(value));
            }
            tuples.add(t);
    		
    	}
    	
    	return tuples;
    }

}
