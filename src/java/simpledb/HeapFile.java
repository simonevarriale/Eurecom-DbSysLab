package simpledb;

import java.io.*;
import java.util.*;
/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see simpledb.HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {
	
	private File file;
	private TupleDesc tuple_desc;
	
    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */
    public HeapFile(File f, TupleDesc td) {
        // some code goes here
    	this.file=f;
    	this.tuple_desc=td;
    	
    }

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here
        return this.file;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere to ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // some code goes here
        return this.file.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
        return this.tuple_desc;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) throws IllegalArgumentException{
        // some code goes here
    	byte [] datas = new byte[BufferPool.getPageSize()];
    	int offset = pid.getPageNumber()*BufferPool.getPageSize();
    	
    	if(pid.getPageNumber() > this.numPages()) {
    		throw new IllegalArgumentException();
    	}
    	
    	
    	try {
    		
    		if(pid.getPageNumber() == this.numPages()){
        		//this.num_pages++;
        		return new HeapPage((HeapPageId) pid, HeapPage.createEmptyPageData());
        	}
    		
    		InputStream stream = new FileInputStream(this.file);
    		stream.skipNBytes(offset); 
    		stream.read(datas);
    		stream.close();
			HeapPage hp = new HeapPage((HeapPageId) pid, datas);
			return hp;
			
		} catch (FileNotFoundException e) {
			
			System.out.println("No such file!");
		}
    	catch (IOException e) {
			
    		throw new IllegalArgumentException();
		}
    	
    	
    	return null;
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for lab1
    	int offset = page.getId().getPageNumber()*BufferPool.getPageSize();
    	  	
    	try {
    		RandomAccessFile stream = new RandomAccessFile(this.file, "rw");
    		stream.seek(offset);
    		stream.write(page.getPageData());
    		stream.close();
		} 
    	
    	catch (IOException e) {
			
    		throw new IOException("Error in writing the page");
		}
    
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // some code goes here
        return (int) (this.file.length()/BufferPool.getPageSize());
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        
        // not necessary for lab1
    	ArrayList<Page> modPages = new ArrayList<>();
    	HeapPageId heappid;
    	HeapPage hp;
        
        for(int i=0; i<this.numPages(); i++) {
        	heappid = new HeapPageId(this.getId(), i);
        	hp = (HeapPage) Database.getBufferPool().getPage(tid, heappid, Permissions.READ_WRITE);
        	if(hp.getNumEmptySlots()>0) {
        		hp.insertTuple(t);
        		hp.markDirty(true, tid);
        		modPages.add(hp);
        		break;
        	}
        }
        
        if(modPages.isEmpty()) {
        	
        	heappid = new HeapPageId(this.getId(), this.numPages());    	
        	hp = (HeapPage) Database.getBufferPool().getPage(tid, heappid, Permissions.READ_WRITE);
        	this.writePage(hp);
        	hp.insertTuple(t);
    		hp.markDirty(true, tid);
    		modPages.add(hp);	
        }
        
        
        return modPages;
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
        // not necessary for lab1
        
        ArrayList<Page> modPages = new ArrayList<>();
    	
    	HeapPage hp = (HeapPage)Database.getBufferPool().getPage(tid, t.getRecordId().getPageId(), Permissions.READ_WRITE);
        hp.deleteTuple(t);
        //hp.markDirty(true, tid);
        modPages.add(hp);
        
        return modPages;
        
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        // some code goes here
        return (DbFileIterator) new HFiterator(this, tid, this.numPages());
    }

}