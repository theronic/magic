package magic.data;

import java.util.Collection;
import java.util.Iterator;

import magic.Errors;

public class PersistentList<T> extends APersistentList<T> {
 	private static final long serialVersionUID = -3092361115850584058L;

	public static final APersistentList<?> EMPTY = PersistentList.create(Vectors.emptyVector());
	
	private final APersistentVector<T> vector;
	private final int offset;
	private final int size;
	
	@SuppressWarnings("unchecked")
	private PersistentList(APersistentVector<? extends T> source, int offset, int size) {
		this.vector=(APersistentVector<T>) source;
		this.offset=offset;
		this.size=size;	
	}

	
	public static <T> PersistentList<T> create(APersistentVector<? extends T> source, int offset, int size) {
		return new PersistentList<T>(source,offset,size);	
	}
	
	public static <T> PersistentList<T> create(APersistentVector<? extends T> source) {
		return new PersistentList<T>(source,0,source.size());	
	}
	
	public static <T> PersistentList<T> wrap(APersistentVector<? extends T> vs) {
		return new PersistentList<T>(vs,0,vs.size());
	}


	@Override
	public APersistentList<T> concat(APersistentList<? extends T> values) {
		return create(vector.concat(values),offset,size+values.size());
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public APersistentList<T> concat(IPersistentList<? extends T> values) {
		if (values instanceof APersistentList) {
			return concat((APersistentList<T>)values);
		}
		return concat(coerce(values));
	}

	@SuppressWarnings("unchecked")
	public static <T> APersistentList<T> coerce(IPersistentCollection<? extends T> values) {
		if (values instanceof APersistentList) {
			return ((APersistentList<T>)values);
		}	
		return create(Vectors.coerce(values));
	}


	@Override
	public APersistentList<T> concat(Collection<? extends T> values) {
		return create(vector.concat(values),offset,size+values.size());
	}

	@Override
	public APersistentList<T> insert(int index, T value) {
		return create(vector.insert(offset+index,value),offset,size+1);
	}

	@Override
	public APersistentList<T> insertAll(int index, Collection<? extends T> values) {
		return create(vector.insertAll(offset+index,values),offset,size+values.size());
	}

	@Override
	public APersistentList<T> insertAll(int index, IPersistentList<? extends T> values) {
		return create(vector.insertAll(offset+index,values),offset,size+values.size());
	}

	@Override
	public APersistentList<T> copyFrom(int index, IPersistentList<? extends T> values, int srcIndex, int length) {
		return create(vector.copyFrom(offset+index,values,srcIndex,length),offset,size);
	}

	@Override
	public APersistentList<T> include(T value) {
		return create(Tuple.of(value).concat(vector.subList(offset, offset+size)));
	}

	@Override
	public APersistentList<T> deleteAt(int index) {
		return create(vector.deleteAt(index+offset),offset,size-1);
	}

	@Override
	public APersistentList<T> deleteRange(int startIndex, int endIndex) {
		int nDeleted=endIndex=startIndex;
		return create(vector.deleteRange(startIndex+offset,endIndex+offset),offset,size-nDeleted);
	}


	@Override
	public APersistentList<T> update(int index, T value) {
		return create(vector.assocAt(index+offset,value),offset,size);
	}

	@Override
	public T head() {
		if (size==0) throw new IndexOutOfBoundsException(Errors.indexOutOfBounds(0));
		return vector.get(offset);
	}

	@Override
	public APersistentList<T> tail() {
		if (size==0) throw new IllegalArgumentException(Errors.indexOutOfBounds(0));
		return create(vector,offset+1,size-1);
	}

	@Override
	public APersistentList<T> front() {
		return create(vector,offset,size/2);
	}

	@Override
	public APersistentList<T> back() {
		int skip=size/2;
		return create(vector,offset+skip,size-skip);
	}

	@SuppressWarnings("unchecked")
	@Override
	public APersistentList<T> subList(int fromIndex, int toIndex) {
		if ((fromIndex<0)||(toIndex>size)) {
			throw new IndexOutOfBoundsException("from: "+fromIndex+" to: " +toIndex+ " with size: "+size);
		}
		int newSize=toIndex-fromIndex;
		if (newSize==0) return (APersistentList<T>) EMPTY;
		if (newSize<0) {
			throw new IllegalArgumentException("Negative sized subList from: "+fromIndex+" to: " +toIndex);
		}
		if ((fromIndex==0)&&(toIndex==size)) return this;
		return create(vector,offset+fromIndex,newSize);
	}

	@Override
	public T get(int i) {
		if ((i<0)||(i>=size)) throw new IndexOutOfBoundsException(Errors.indexOutOfBounds(i));
		return vector.get(offset+i);
	}

	@Override
	public ISeq<T> seq() {
		if (size==0) return null;
		return null;
	}

	@Override
	public Iterator<T> iterator() {
		return vector.subList(offset, offset+size).iterator();
	}

	@Override
	public int indexOf(Object o) {
		int ix=vector.indexOf(o);
		if (ix<0) return ix;
		return ix-offset;
	}

	@Override
	public int lastIndexOf(Object o) {
		return vector.subList(offset,offset+size).lastIndexOf(o);
	}


	@Override
	public int size() {
		return size;
	}


	@SuppressWarnings("unchecked")
	public static <T> PersistentList<T> of(Object... vals) {
		return (PersistentList<T>) create(Tuple.of(vals));
	}


	public static <T> PersistentList<T> wrap(T[] forms) {
		return create(Tuple.wrap(forms));
	}

	@Override
	public APersistentList<T> assocAt(int index, Object value) {
		if ((index<0)||(index>=size)) throw new IndexOutOfBoundsException("Index: "+index);
		return create(vector.assoc(offset+index, value),offset,size);
	}





}
