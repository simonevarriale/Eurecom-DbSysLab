package simpledb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import simpledb.Aggregator.Op;

/**
 * Knows how to compute some aggregate over a set of StringFields.
 */
public class StringAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;
    private int gbfield;
    private Type gbfieldtype;
    private int afield;
    private Op what;
    private String afieldName, gbfieldName;
    private HashMap<Field, Integer> count;

    /**
     * Aggregate constructor
     * @param gbfield the 0-based index of the group-by field in the tuple, or NO_GROUPING if there is no grouping
     * @param gbfieldtype the type of the group by field (e.g., Type.INT_TYPE), or null if there is no grouping
     * @param afield the 0-based index of the aggregate field in the tuple
     * @param what aggregation operator to use -- only supports COUNT
     * @throws IllegalArgumentException if what != COUNT
     */

    public StringAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        // some code goes here
    	
    	if(what != Op.COUNT) {
    		throw new IllegalArgumentException("Operation not supported");
    	}
    	
    	this.gbfield = gbfield;
    	this.gbfieldtype = gbfieldtype;
    	this.afield = afield;
    	this.what = what;
    	this.count = new HashMap<>();
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the constructor
     * @param tup the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
        // some code goes here
    	Field groupField;
        Field aggregateField;
        if(this.gbfield != Aggregator.NO_GROUPING) {
            groupField = tup.getField(gbfield);
            gbfieldName = tup.getTupleDesc().getFieldName(gbfield);
        }
        else
            groupField = new IntField(Aggregator.NO_GROUPING);
        
        aggregateField = tup.getField(afield);
        afieldName = tup.getTupleDesc().getFieldName(afield);
        
        if(this.what == Op.COUNT){
        	if(count.containsKey(groupField)) {
        		count.put(groupField, count.get(groupField) + 1);
        	}
        	else count.put(groupField,1);
    		
    	}
    }

    /**
     * Create a OpIterator over group aggregate results.
     *
     * @return a OpIterator whose tuples are the pair (groupVal,
     *   aggregateVal) if using group, or a single (aggregateVal) if no
     *   grouping. The aggregateVal is determined by the type of
     *   aggregate specified in the constructor.
     */
    public OpIterator iterator() {
        // some code goes here
    	Type[] type;
    	String[] field;
    	
    	if(gbfield == Aggregator.NO_GROUPING) {
    		type = new Type[1];
    		field = new String[1];
    		type[0] = /*Type.INT_TYPE*/ null;
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
    	
 
    	for(Map.Entry<Field, Integer> e : count.entrySet()) {
    		
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
    	
    	
    	
    	return new TupleIterator(td, tuples);
    }
    

}
