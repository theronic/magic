package magic.lang;

import magic.ast.Node;
import magic.compiler.AExpander;
import magic.compiler.EvalResult;
import magic.data.APersistentMap;
import magic.data.APersistentSet;
import magic.data.Maps;
import magic.data.Symbol;

/**
 * Represents a "slot" in a magic Context.
 * 
 * A slot features:
 * - An expression stored as a Node
 * - A lazily computed value
 * 
 * @author Mike
 *
 * @param T the Java type of the expression
 */
public class Slot<T> {
	private final Node<T> expression;
	private final APersistentMap<Symbol, Object> bindings;
	private final Context context;
	
	private T value=null;
	private volatile boolean computed=false;
	private final Node<T> compiledExp;
	
	private Slot(Node<T> e, Context context, APersistentMap<Symbol, Object> bindings) {
		this.expression=e;
		this.context=context;
		this.bindings=bindings;
		compiledExp=magic.compiler.Compiler.compileNode(context,expression);

	}
	
	public T getValue() {
		if (computed==false) {
			synchronized (this) {
				if (computed==false) {
					return tryCompute();
				}
			}
		}
		return value;
	}
	
	private T tryCompute() {
//		APersistentSet<Symbol> deps=expression.getDependencies();
//		if (!deps.isEmpty()) {
//			// check slots exist
//			for (Symbol s:deps) {
//				if (c.getSlot(s)==null) throw new UnresolvedException(s);
//			}
//		}
		EvalResult<T> result=compiledExp.eval(context, bindings);
		value=result.getValue();
		computed=true;
		return value;
	}
	
	/**
	 * Gets the compiled Node associated with this Slot
	 * This Node may have unresolved dependencies.
	 * @return
	 */
	public Node<T> getNode() {
		return compiledExp;
	}


	@SuppressWarnings("unchecked")
	public static <T> Slot<T> create(Node<T> exp,Context context) {
		return create(exp,context,(APersistentMap<Symbol, Object>)Maps.EMPTY);
	}

	public static <T> Slot<T> create(Node<T> exp, Context context,APersistentMap<Symbol, Object> bindings) {
		return new Slot<T>(exp,context,bindings);
	}

	public boolean isExpander() {
		return getValue() instanceof AExpander;
	}
	
	public boolean isComputed() {
		return computed;
	}

	public APersistentSet<Symbol> getDependencies() {
		return getNode().getDependencies();
	}

	/**
	 * Invalidates the slot, returning a new slot with no cached values assocaited with the given defining context
	 * @return
	 */
	public Slot<T> invalidate(Context c) {
		return create(expression,c,bindings);
	}
	
	@Override 
	public String toString() {
		return "<Slot exp="+expression+(computed?(" val="+value):"")+">";
	}

}
