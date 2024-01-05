package simpledb;

import java.io.File;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class HFiterator implements DbFileIterator {
	
	private TransactionId tid;
	private HeapFile hf;
	private int page_num;
	private Iterator<Tuple> it;
	private int maxNPages;
	private HeapPageId pageId;
	private HeapPage hp;
	
	public HFiterator(HeapFile hf, TransactionId tid, int maxNPages) {
        // some code goes here
    	this.tid=tid;
    	this.hf = hf;
    	this.page_num=0;
    	this.it = null;
    	this.maxNPages = maxNPages;
    }
	

	@Override
	public void open() throws DbException, TransactionAbortedException {
		
		this.pageId = new HeapPageId(this.hf.getId(), this.page_num);
		this.hp = (HeapPage) Database.getBufferPool().getPage(this.tid, pageId, Permissions.READ_WRITE);
		this.it = hp.iterator();
		
	}

	@Override
	public boolean hasNext() throws DbException, TransactionAbortedException {
		
		if (this.it == null) return false;
		
		if(this.page_num < this.maxNPages) {
            if(it.hasNext())
                return true;
            else {
            	this.page_num++;
                this.pageId = new HeapPageId(this.hf.getId(), this.page_num);
        		this.hp = (HeapPage) Database.getBufferPool().getPage(this.tid, pageId, Permissions.READ_WRITE);
        		this.it = hp.iterator();
            }
                
        }
		
        return it.hasNext();
	
	}

	@Override
	public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
		
		if (it==null)
            throw new DbException("Iterator not opened yet");
		
		if(this.hasNext()) {
			return this.it.next();
		}
		
		throw new NoSuchElementException();
	}

	@Override
	public void rewind() throws DbException, TransactionAbortedException {
		
		if (it==null)
            throw new DbException("Iterator not opened yet");
		this.close();
		this.open();
	}

	@Override
	public void close() {
		this.page_num=0;
		this.it = null;
		this.pageId = null;
		this.hp = null;
	}
	

}