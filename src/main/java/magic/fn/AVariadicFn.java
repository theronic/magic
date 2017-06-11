package magic.fn;

/**
 * Abstract base class for variadic functions.
 * 
 * May support any number of different arity, subclasses should override hasArity(...)
 * in order to specify which arities are available.
 * 
 * @author Mike
 *
 * @param <T>
 */
public abstract class AVariadicFn<T> extends AFn<T> implements IVariadicFn<T> {

	@Override
	public boolean hasArity(int i) {
		return true;
	}
	
	@Override
	public boolean isFixedArity() {
		return false;
	}
}