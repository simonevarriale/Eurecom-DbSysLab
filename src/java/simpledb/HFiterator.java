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
		this.hp = (HeapPage) Database.getBufferPool().getPage(this.tid, pageId, Permissions.READ_ONLY);
		this.it = hp.iterator();
		
	}

	@Override
	public boolean hasNext() throws DbException, TransactionAbortedException {
		
		if (this.it == null) return false;
		
		if(this.page_num < this.maxNPages - 1) {
            if(it.hasNext())
                return true;
            else
                this.fetchNextPage();
        }
		
        return it.hasNext();
	
	}

	@Override
	public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
		
		if(this.hasNext()) {
			return this.it.next();
		}
		
		throw new NoSuchElementException();
	}

	@Override
	public void rewind() throws DbException, TransactionAbortedException {
		this.page_num=0;
		this.close();
		this.open();
	}

	@Override
	public void close() {
		this.page_num=0;
		this.it = null;
	}
	
	public void fetchNextPage() throws TransactionAbortedException, DbException {
        this.page_num++;
        this.pageId = new HeapPageId(this.hf.getId(), this.page_num);
		this.hp = (HeapPage) Database.getBufferPool().getPage(this.tid, pageId, Permissions.READ_ONLY);
		this.it = hp.iterator();
    }

}