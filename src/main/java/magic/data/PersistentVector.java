package magic.data;

import java.util.List;

/**
 * Persistent Vector constructed using fixed size blocks represented by other persistent lists
 * @author Mike
 *
 * @param <T>
 */
public final class PersistentVector<T> extends APersistentVector<T> {
	private static final long serialVersionUID = 7210896608719053578L;

	protected static final int DEFAULT_SHIFT=Vectors.TUPLE_BUILD_BITS;
	protected static final int SHIFT_STEP=4;
	protected static final int SHIFT_MASK=(1<<SHIFT_STEP)-1;
	
	public static final int BASE_BLOCKSIZE = 1<<DEFAULT_SHIFT;
	
	/**
	 * The bit shift level for each block
	 */
	private final int shift;
	
	/**
	 * The size of the list
	 */
	private final int size;
	
	/**
	 * The offset into the list
	 */
	private final int offset;
	
	/**
	 * The blocks that comprise the storage for the list
	 * All except the last must be of the correct block size as determined by shift
	 */
	private final APersistentVector<T>[] blocks;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final PersistentVector<?> EMPTY_BLOCKLIST=new PersistentVector(Vectors.NULL_PERSISTENT_LIST_ARRAY,DEFAULT_SHIFT,0,0);
	
	public static <T> PersistentVector<T> create(List<T> list) {
		return create(list,0,list.size());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public PersistentVector<T> empty() {
		return (PersistentVector<T>) EMPTY_BLOCKLIST;
	}
	
	/**
	 * Coerces a List to a BlockList
	 * @param values
	 * @return a BlockList containing the values from the list provided
	 */
	@SuppressWarnings("unchecked")
	public static <T> PersistentVector<T> coerce(List<? extends T> values) {
		if (values instanceof PersistentVector<?>) return (PersistentVector<T>) values;
		int size=values.size();
		if (size==0) return (PersistentVector<T>) EMPTY_BLOCKLIST;
		return create((List<T>)values,0,size);
	}
	
	/**
	 * Create a BlockList using values from the given List
	 * @param list
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	public static <T> PersistentVector<T> create(List<T> list, int fromIndex, int toIndex) {
		int size=toIndex-fromIndex;
		if (size<0) throw new IllegalArgumentException();
		
		int shift=DEFAULT_SHIFT;
		while ((1<<(shift+SHIFT_STEP))<size) {
			shift+=SHIFT_STEP;
		}
		return createLocal(list,fromIndex,toIndex,shift);
	}
	
	/**
	 * Create a blockList using a range of values from the given source array. 
	 * @param list
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> PersistentVector<T> create(T[] list, int fromIndex, int toIndex) {
		int size=toIndex-fromIndex;
		if (size<0) throw new IllegalArgumentException();
		if (size==0) return (PersistentVector<T>) EMPTY_BLOCKLIST;
		
		int shift=DEFAULT_SHIFT;
		while ((1<<(shift+SHIFT_STEP))<size) {
			shift+=SHIFT_STEP;
		}
		return createLocal(list,fromIndex,toIndex,shift);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> PersistentVector<T> createLocal(T[] list, int fromIndex, int toIndex, int shift) {
		if (shift>DEFAULT_SHIFT) {
			int size=toIndex-fromIndex;
			int numBlocks=numBlocks(size,shift);
		
			APersistentVector<T>[] bs=(APersistentVector<T>[]) new APersistentVector<?>[numBlocks];
			for (int i=0; i<(numBlocks-1); i++) {
				bs[i]=createLocal(
						list,
						fromIndex+(i<<shift), 
						fromIndex+((i+1)<<shift),
						shift-SHIFT_STEP);
			}
			bs[numBlocks-1]=createLocal(
					list,
					fromIndex+((numBlocks-1)<<shift), 
					fromIndex+size,
					shift-SHIFT_STEP);
			
			return new PersistentVector<T>(bs,shift,size,0);			
		}
		return createLowestLevel(list,fromIndex, toIndex,DEFAULT_SHIFT);
	}
		
	@SuppressWarnings("unchecked")
	private static <T> PersistentVector<T> createLocal(List<T> list, int fromIndex, int toIndex, int shift) {
		if (shift>DEFAULT_SHIFT) {
			int size=toIndex-fromIndex;
			int numBlocks=numBlocks(size,shift);
		
			APersistentVector<T>[] bs=(APersistentVector<T>[]) new APersistentVector<?>[numBlocks];
			for (int i=0; i<(numBlocks-1); i++) {
				bs[i]=createLocal(
						list,
						fromIndex+(i<<shift), 
						fromIndex+((i+1)<<shift),
						shift-SHIFT_STEP);
			}
			bs[numBlocks-1]=createLocal(
					list,
					fromIndex+((numBlocks-1)<<shift), 
					fromIndex+size,
					shift-SHIFT_STEP);
			
			return new PersistentVector<T>(bs,shift,size,0);			
		}
		return createLowestLevel(list,fromIndex, toIndex,DEFAULT_SHIFT);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> PersistentVector<T> createLowestLevel(List<T> list, int fromIndex, int toIndex,int shift) {
		int size=toIndex-fromIndex;
		int numBlocks=numBlocks(size,shift);
	
		APersistentVector<T>[] bs=(APersistentVector<T>[]) new APersistentVector<?>[numBlocks];
		for (int i=0; i<(numBlocks-1); i++) {
			bs[i]=Vectors.subList(
					list,
					fromIndex+(i<<shift), 
					fromIndex+((i+1)<<shift));
		}
		bs[numBlocks-1]=Vectors.subList(
				list,
				fromIndex+((numBlocks-1)<<shift), 
				fromIndex+size);
	
		return new PersistentVector<T>(bs,shift,size,0);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> PersistentVector<T> createLowestLevel(T[] list, int fromIndex, int toIndex,int shift) {
		int size=toIndex-fromIndex;
		int numBlocks=numBlocks(size,shift);
	
		APersistentVector<T>[] bs=(APersistentVector<T>[]) new APersistentVector<?>[numBlocks];
		for (int i=0; i<(numBlocks-1); i++) {
			bs[i]=Vectors.createFromArray(
					list,
					fromIndex+(i<<shift), 
					fromIndex+((i+1)<<shift));
		}
		bs[numBlocks-1]=Vectors.createFromArray(
				list,
				fromIndex+((numBlocks-1)<<shift), 
				fromIndex+size);
	
		return new PersistentVector<T>(bs,shift,size,0);
	}
	
	private static final int numBlocks(int size, int shift) {
		return 1+((size-1)>>shift);
	}
	
	@SuppressWarnings("unchecked")
	private PersistentVector(APersistentVector<?>[] blocks, int sh, int sz, int off) {
		this.blocks=(APersistentVector<T>[]) blocks;
		shift=sh;
		size=sz;
		offset=off;
	}
	
	@Override
	public T get(int i) {
		if ((i<0)||(i>=size)) throw new IndexOutOfBoundsException();
		int pos=i+offset;
		int bi=blockFor(pos);
		int bpos=pos&((1<<shift)-1);
		return blocks[bi].get(bpos);
	}
	
	@Override
	public int size() {
		return size;
	}
	
	/**
	 * Return the start position of the given block, ignoring offset.
	 * @param blockIndex
	 * @return
	 */
	private int blockStart(int blockIndex) {
		return blockIndex<<shift;
	}
	
	/**
	 * Return the block number for the given position, ignoring offset.
	 * @param 
	 * @return Block number (index into blocks array)
	 */
	private int blockFor(int index) {
		return index>>shift;
	}
	
	/**
	 * Return the size of blocks in this BlockList
	 * @return
	 */
	private int blockSize() {
		return 1<<shift;
	}
	
	@Override
	public APersistentVector<T> subList(int fromIndex, int toIndex) {
		if ((fromIndex<0)||(toIndex>size)) {
			throw new IndexOutOfBoundsException("from: "+fromIndex+" to: " +toIndex+ " with size: "+size+" offset: "+offset+" shift: "+shift);
		}
		if ((fromIndex==0)&&(toIndex==size)) return this;
		if (toIndex==fromIndex) return Vectors.emptyVector();
		if ((fromIndex>toIndex)) {
			throw new IllegalArgumentException("Negative sized subList from: "+fromIndex+" to: " +toIndex);
		}
		
		// see if we can take a subset of a single block
		int fromBlock=blockFor(fromIndex+offset);
		int toBlock=blockFor(toIndex-1+offset);
		if ((fromBlock)==(toBlock)) {
			int blockStart=blockStart(fromBlock);
			return blocks[fromBlock].subList(fromIndex+offset-blockStart, toIndex+offset-blockStart);
		}
		
		return subBlockList(fromIndex,toIndex);
	}
	
	@Override
	public int hashCode() {
		// TODO: fix this looks broken because ignores offset,size
		if (blocks.length==0) return 0;
		int r=blocks[0].hashCode();
		for (int i=1; i<blocks.length; i++) {
			r=Integer.rotateRight(r,blocks[i].size());
			r^=blocks[i].hashCode();
		}
		return r;
	}
	
	/**
	 * Gets a subList as a BlockList with the same shift
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	private PersistentVector<T> subBlockList(int fromIndex, int toIndex) {
		return new PersistentVector<T>(blocks,shift,(toIndex-fromIndex),fromIndex+offset);	
	}

	@SuppressWarnings("unchecked")
	@Override
	public PersistentVector<T> include(T value) {
		int newIx=offset+size; // raw index of new added value
		int newBlock=newIx>>shift;
		int blockLength=blocks.length;
		if (newBlock<blockLength) {
			// conj to last block
			APersistentVector<T>[] newBlocks=blocks.clone();
			newBlocks[newBlock]=blocks[newBlock].include(value);
			return new PersistentVector<T>(newBlocks,shift,size+1,offset);
		} else if ((newIx>>(shift+SHIFT_STEP))==0) {
			// add a final block
			APersistentVector<T>[] newBlocks=new APersistentVector[newBlock+1];
			System.arraycopy(blocks, 0, newBlocks, 0, newBlock);
			newBlocks[newBlock]=Tuple.of(value);
			return new PersistentVector<T>(newBlocks,shift,size+1,offset);
		} else {
			// need to rise one level
			return new PersistentVector<T>(new APersistentVector[]{this,Tuple.of(value)},shift+SHIFT_STEP,size+1,offset);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public PersistentVector<T> concat(APersistentVector<? extends T> src) {
		APersistentVector<T> a= (APersistentVector<T>) src;
		int asize=a.size();
		if (asize==0) return this;
		if (size==0) return PersistentVector.coerce(a);
		int end=offset+size;
		int newEnd=end+asize; // end index of new list
		int newEndBlock=blockFor(newEnd-1);
		int totalBlocks=blockFor(offset+size-1)+1; // num of blocks used (including excess at head due to offset)
		if (newEndBlock<totalBlocks) {
			// concat to last block
			APersistentVector<T>[] newBlocks=blocks.clone();
			APersistentVector<T> tail=blocks[newEndBlock].subList(0,offset+size-blockStart(newEndBlock));
			if (shift<=DEFAULT_SHIFT) {
				// build a Tuple
				newBlocks[newEndBlock]=Tuple.concat(tail, a);
			} else {
				newBlocks[newEndBlock]=tail.concat(a);
			}
			return new PersistentVector<T>(newBlocks,shift,size+asize,offset);
		} else {
			int maxEnd=1<<(shift+SHIFT_STEP); // largest capacity at this level
			if (newEnd<=maxEnd) {
				// add more blocks - will have at least two new
				// we truncate blocks by removing the blocks before the offset
				int offsetBlock=blockFor(offset);
				int numBlocks=totalBlocks-offsetBlock; // number of currently used blocks
				int newOffset=offset-blockStart(offsetBlock);
				int newNumBlocks=newEndBlock-offsetBlock+1; // new total number of blocks
				APersistentVector<T>[] newBlocks=new APersistentVector[newNumBlocks];
				System.arraycopy(blocks, offsetBlock, newBlocks, 0, numBlocks);
				int blockSize=blockSize();
				
				int endBlockOffset=blockSize*blockFor(end-1);
				int split=end-endBlockOffset; // split point in last original block where concat occurs
				
				// update last block in currently used blocks, filling up to blockSize
				APersistentVector<T> oldTail=newBlocks[numBlocks-1].subList(0,split-Math.max(0,offset-endBlockOffset));
				newBlocks[numBlocks-1]=oldTail.concat(a.subList(0, blockSize-split));
				// update inner blocks (if more than 2)
				int innerBlocks=newNumBlocks-numBlocks-1;
				for (int i=0; i<innerBlocks;i++) {
					int aix=(blockSize-split)+i*blockSize;
					newBlocks[numBlocks+i]=a.subList(aix, aix+blockSize);
				}
				// update final block using remaining elements
				int aTailPos=(blockSize-split)+blockSize*innerBlocks;
				newBlocks[newNumBlocks-1]=a.subList(aTailPos, asize);
				return new PersistentVector<T>(newBlocks,shift,size+asize,newOffset);
			} else {
				// need to rise one level
				int aSplit=maxEnd-end; // elements to cut from front of a to complete block
				PersistentVector<T> fullHead=this.concat(a.subList(0, aSplit));
				PersistentVector<T> headList=new PersistentVector<T>(new APersistentVector[]{fullHead},shift+SHIFT_STEP,size+aSplit,offset);
				return headList.concat(a.subList(aSplit, asize));
			}
		}
	}
	
	@Override
	public void validate() {
		if (shift<DEFAULT_SHIFT) throw new Error("Incsufficient shift");
		int blockSize=blockSize();
		int numBlocks=blockFor(offset+size-1)+1;
		for (int i=0; i<numBlocks-1; i++) {
			int bsize=blocks[i].size();
			if (bsize!=blockSize)  throw new Error("Wrong blcok size: "+bsize+" at position "+i);
		}
		APersistentVector<T> lastBlock=blocks[numBlocks-1];
		if (lastBlock.size()<(offset+size-blockStart(numBlocks-1))) {
			throw new Error("Insufficient element is last block");
		}
	}






}
