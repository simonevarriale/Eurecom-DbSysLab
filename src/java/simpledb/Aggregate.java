package simpledb;

import java.util.*;

import simpledb.Aggregator.Op;

/**
 * The Aggregation operator that computes an aggregate (e.g., sum, avg, max,
 * min). Note that we only support aggregates over a single column, grouped by a
 * single column.
 */
public class Aggregate extends Operator {

    private static final long serialVersionUID = 1L;
    private OpIterator child;
    private int gfield;
    private Type gbfieldtype;
    private int afield;
    private Type afieldtype;
    private Op aop;
    private Aggregator aggregator;
    private OpIterator it;

    /**
     * Constructor.
     * 
     * Implementation hint: depending on the type of afield, you will want to
     * construct an {@link IntegerAggregator} or {@link StringAggregator} to help
     * you with your implementation of readNext().
     * 
     * 
     * @param child
     *            The OpIterator that is feeding us tuples.
     * @param afield
     *            The column over which we are computing an aggregate.
     * @param gfield
     *            The column over which we are grouping the result, or -1 if
     *            there is no grouping
     * @param aop
     *            The aggregation operator to use
     */
    public Aggregate(OpIterator child, int afield, int gfield, Aggregator.Op aop) {
	// some code goes here
    	this.child = child;
    	this.afield = afield;
    	
    	if(gfield == Aggregator.NO_GROUPING) {
    		this.gfield = -1;
    		this.gbfieldtype = null;
    		
    	}
    	else {
    		this.gfield = gfield;
    		this.gbfieldtype = child.getTupleDesc().getFieldType(gfield);
    	}
    	
    	this.aop = aop;
    	
    	this.afieldtype = child.getTupleDesc().getFieldType(afield);
    	
    	
    	if(afieldtype == Type.INT_TYPE) {
    		aggregator = new IntegerAggregator(gfield, gbfieldtype , afield, aop);
    	}
    	else{
    		aggregator = new StringAggregator(gfield, gbfieldtype , afield, aop);
    	}
    	
    }

    /**
     * @return If this aggregate is accompanied by a groupby, return the groupby
     *         field index in the <b>INPUT</b> tuples. If not, return
     *         {@link simpledb.Aggregator#NO_GROUPING}
     * */
    public int groupField() {
	// some code goes here
    	return gfield;
		
    }

    /**
     * @return If this aggregate is accompanied by a group by, return the name
     *         of the groupby field in the <b>OUTPUT</b> tuples. If not, return
     *         null;
     * */
    public String groupFieldName() {
	// some code goes here
    	if(gfield == Aggregator.NO_GROUPING)
            return null;
        else
            return child.getTupleDesc().getFieldName(gfield);
    	
    }

    /**
     * @return the aggregate field
     * */
    public int aggregateField() {
	// some code goes here
    	return afield;
    }

    /**
     * @return return the name of the aggregate field in the <b>OUTPUT</b>
     *         tuples
     * */
    public String aggregateFieldName() {
	// some code goes here
    	
       return child.getTupleDesc().getFieldName(afield);
    }

    /**
     * @return return the aggregate operator
     * */
    public Aggregator.Op aggregateOp() {
	// some code goes here
    	return aop;
    }

    public static String nameOfAggregatorOp(Aggregator.Op aop) {
    	return aop.toString();
    }

    public void open() throws NoSuchElementException, DbException,
	    TransactionAbortedException {
	// some code goes here
    	super.open();
    	child.open();
    	
    	//merging all the tuple of child into the aggregate
    	while(child.hasNext()) {
    		aggregator.mergeTupleIntoGroup(child.next());
    	}
    	
    	//when it finish we open the iterator over the aggregator
    	it = aggregator.iterator();
    	it.open();
    	
    }

    /**
     * Returns the next tuple. If there is a group by field, then the first
     * field is the field by which we are grouping, and the second field is the
     * result of computing the aggregate. If there is no group by field, then
     * the result tuple should contain one field representing the result of the
     * aggregate. Should return null if there are no more tuples.
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
	// some code goes here
    	
    	if(it.hasNext()) {
    		return it.next();
    	}
    	
    	return null;
    }

    public void rewind() throws DbException, TransactionAbortedException {
	// some code goes here
    	child.rewind();
    	it.rewind();
    }

    /**
     * Returns the TupleDesc of this Aggregate. If there is no group by field,
     * this will have one field - the aggregate column. If there is a group by
     * field, the first field will be the group by field, and the second will be
     * the aggregate value column.
     * 
     * The name of an aggregate column should be informative. For example:
     * "aggName(aop) (child_td.getFieldName(afield))" where aop and afield are
     * given in the constructor, and child_td is the TupleDesc of the child
     * iterator.
     */
    public TupleDesc getTupleDesc() {
	// some code goes here
    	Type[] type;
    	String[] field;
    	
    	if(gfield == -1) {
    		type = new Type[1];
    		field = new String[1];
    		type[0] = null;
            field[0] = child.getTupleDesc().getFieldName(afield);
    	}
    	else {
    		type = new Type[2];
    		field = new String[2];
            type[0] = child.getTupleDesc().getFieldType(gfield);
            field[0] = child.getTupleDesc().getFieldName(gfield);
            type[1] = Type.INT_TYPE;
            field[1] = child.getTupleDesc().getFieldName(afield);
    	}
    	
    	return new TupleDesc(type,field);
    }

    public void close() {
	// some code goes here
    	super.close();
    	child.close();
    	it.close();
    }

    @Override
    public OpIterator[] getChildren() {
	// some code goes here
    	return new OpIterator[] {child};
    }

    @Override
    public void setChildren(OpIterator[] children) {
	// some code goes here
    	this.child = children[0];
    }
    
}
