package magic.data.impl;

import java.util.List;

import magic.data.APersistentVector;
import magic.data.Vectors;

/**
 * Implements a persistent list that is a subset of an existing tuple
 * utilising the same immutable backing array
 * 
 * @author Mike
 *
 * @param <T>
 */
public final class SubVector<T> extends APersistentVector<T>   {	

	private static final long serialVersionUID = 3559316900529560364L;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final SubVector<?> EMPTY_SUBLIST = new SubVector(Vectors.emptyVector(),0,0);

	private final APersistentVector<T> data;
	private final int offset;
	private final int length;
	
	@SuppressWarnings("unchecked")
	public static <T> SubVector<T> create(List<T> source, int fromIndex, int toIndex) {
		if ((fromIndex<0)||(toIndex>source.size())) throw new IndexOutOfBoundsException();
		int newSize=toIndex-fromIndex;
		if (newSize<=0) {
			if (newSize==0) return (SubVector<T>) SubVector.EMPTY_SUBLIST;
			throw new IllegalArgumentException();
		}
		return createLocal(Vectors.createFromList(source),fromIndex,newSize);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> SubVector<T> create(APersistentVector<T> source, int fromIndex, int toIndex) {
		if ((fromIndex<0)||(toIndex>source.size())) throw new IndexOutOfBoundsException();
		int newSize=toIndex-fromIndex;
		if (newSize<=0) {
			if (newSize==0) return (SubVector<T>) SubVector.EMPTY_SUBLIST;
			throw new IllegalArgumentException();
		}
		if (source instanceof SubVector<?>) {
			SubVector<T> sl=(SubVector<T>)source;
			return createLocal(sl.data,fromIndex+sl.offset,toIndex+sl.offset);
		}
		return createLocal(source,fromIndex,toIndex);
	}
	
	private static <T> SubVector<T> createLocal(APersistentVector<T> source, int fromIndex, int toIndex) {
		return new SubVector<T>(source,fromIndex,toIndex-fromIndex);
	}
	
	@Override
	public int size() {
		return length;
	}
	
	private SubVector(APersistentVector<T> source, int off, int len) {
		data=source;
		offset=off;
		length=len;	
	}
	
	@Override
	public T get(int i) {
		if ((i<0)||(i>=length)) throw new IndexOutOfBoundsException();
		return data.get(i+offset);
	}
	
	@Override
	public SubVector<T> clone() {
		return this;
	}
	
	/**
	 * Special append version for SubList 
	 * Attempts to merge adjacent sublists
	 */
	@SuppressWarnings("unchecked")
	@Override
	public APersistentVector<T> concat(APersistentVector<? extends T> values) {
		if (values instanceof SubVector<?>) {
			SubVector<T> sl=(SubVector<T>)values;
			return concat(sl);
		}
		return super.concat(values);
	}
	
	
	public APersistentVector<T> concat(SubVector<T> sl) {
		if ((data==sl.data)&&((offset+length)==sl.offset)) {
			int newLength=length+sl.length;
			if (newLength==data.size()) return data;
			return new SubVector<T>(data,offset,newLength);
		}
		return super.concat(sl);
	}
	
	@Override
	public APersistentVector<T> subList(int fromIndex, int toIndex) {
		if ((fromIndex<0)||(toIndex>size())) throw new IndexOutOfBoundsException();
		if (fromIndex>=toIndex) {
			if (fromIndex==toIndex) return Vectors.emptyVector();
			throw new IllegalArgumentException();
		}
		if ((fromIndex==0)&&(toIndex==size())) return this;
		return data.subList(offset+fromIndex, offset+toIndex);
	}

	@Override
	public APersistentVector<T> include(T value) {
		int newIndex=offset+length;
		if ((data.size()>newIndex)) {
			if(data.get(newIndex)==value) {
				return create(data,offset,newIndex+1);
			} 
			return data.assocAt(newIndex, value).subList(offset, newIndex+1);
			
		}
		return data.include(value).subList(offset, newIndex+1);
	}

	@Override
	public APersistentVector<T> empty() {
		return Vectors.emptyVector();
	}
}
