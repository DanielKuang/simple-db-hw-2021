package simpledb.storage;

import simpledb.common.Database;
import simpledb.common.DbException;
import simpledb.common.Debug;
import simpledb.common.Permissions;
import simpledb.transaction.TransactionAbortedException;
import simpledb.transaction.TransactionId;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {

    // Heapfile class properties
    private final File f;
    private final TupleDesc td;

    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */
    public HeapFile(File f, TupleDesc td) {
        // some code goes here
        this.f = f;
        this.td = td;
    }

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here
        return this.f;
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
        return this.f.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
        return this.td;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        // some code goes here
        HeapPageId hpid = (HeapPageId) pid;
        byte[] data = new byte[BufferPool.getPageSize()];
        int offset = BufferPool.getPageSize() * pid.getPageNumber();
        try {
            RandomAccessFile disk = new RandomAccessFile(this.f, "r");
            disk.seek(offset);
            disk.read(data);
            disk.close();
            return new HeapPage(hpid, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for lab1
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // some code goes here
        return (int) Math.floor(this.f.length() / BufferPool.getPageSize());
    }

    // see DbFile.java for javadocs
    public List<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        // some code goes here

        // pageIDs start at 0 and go to HeapFile.numPages() - 1
        final List<Tuple> allTups = new ArrayList<>();
        for (int i = 0; i < this.numPages(); i++) {
            try {
                HeapPageId pid = new HeapPageId(this.getId(), i);
                HeapPage page = (HeapPage) Database.getBufferPool().getPage(tid, pid, null);
                Iterator<Tuple> pageIt = page.iterator();
                while (pageIt.hasNext()) {
                    allTups.add(pageIt.next());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        DbFileIterator it = new DbFileIterator() {
            private int indx = 0;
            private boolean isOpen = false;

            @Override
            public boolean hasNext() {
                return indx < allTups.size() && isOpen;
            }

            @Override
            public Tuple next() {
                if (!hasNext()) {
                    if (!isOpen) {
                        throw new NoSuchElementException("Iterator is closed");
                    } else {
                        throw new NoSuchElementException("Ran out of tuples to iterate");
                    }
                }
                return allTups.get(indx++);
            }

            @Override
            public void close() {
                isOpen = false;
            }

            @Override
            public void open() {
                indx = 0;
                isOpen = true;
            }

            @Override
            public void rewind() {
                indx = 0;
            }
        };
        return it;
    }

}

