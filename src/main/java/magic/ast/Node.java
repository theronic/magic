package magic.ast;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.SourceSection;

import magic.compiler.Result;
import magic.data.APersistentMap;
import magic.data.IPersistentSet;
import magic.data.IPersistentVector;
import magic.data.PersistentHashMap;
import magic.data.Sets;
import magic.data.Symbol;
import magic.lang.Context;
import magic.lang.MagicLanguage;
 
/**
 * Base class for Expressions
 * @author Mike
 *
 */
@NodeInfo(language = "Magic", description = "The abstract base node for all expressions")
public abstract class Node<T> extends RootNode {

	private IPersistentSet<Symbol> deps;

	public Node(IPersistentSet<Symbol> deps) {
		this(MagicLanguage.class,null,null);
		this.deps=deps;
	}
	
	public Node(Class<? extends TruffleLanguage<?>> language, SourceSection sourceSection,
			FrameDescriptor frameDescriptor) {
		super(language, sourceSection, frameDescriptor);
	}

	@SuppressWarnings("unchecked")
	public final T compute(Context c) {
		return compute(c,(PersistentHashMap<Symbol, ?>) PersistentHashMap.EMPTY);
	}

	/**
	 * Computes the value of an expression  node in the given context
	 * Discards and effects on the context
	 * @param c
	 * @param bindings
	 * @return
	 */
	public final T compute(Context c, APersistentMap<Symbol,?> bindings) {
		return eval(c,bindings).getValue();
	}
	
	@Override
	public Object execute(VirtualFrame virtualFrame) {
		throw new UnsupportedOperationException("Can't execute a Magic node without a context");
	}
	
	public T getValue() {
		throw new UnsupportedOperationException("Cannot use getValue() on a non-constant expression");
	}
	
	public IPersistentSet<Symbol> getDependencies() {
		return deps;
	}
	
	protected static IPersistentSet<Symbol> calcDependencies(Node<?> f, Node<?>[] args) {
		IPersistentSet<Symbol> deps=f.getDependencies();
		deps=deps.includeAll(calcDependencies(args));
		return deps;
	}
	
	protected static IPersistentSet<Symbol> calcDependencies(Node<?>[] nodes) {
		IPersistentSet<Symbol> deps=Sets.emptySet();
		for (int i=0; i<nodes.length; i++) {
			deps=deps.includeAll(nodes[i].getDependencies());
		}
		return deps;
	}
	
	protected static <T> IPersistentSet<Symbol> calcDependencies(IPersistentVector<Node<T>> nodes) {
		IPersistentSet<Symbol> deps=Sets.emptySet();
		int n=nodes.size();
		for (int i=0; i<n; i++) {
			deps=deps.includeAll(nodes.get(i).getDependencies());
		}
		return deps;
	}

	/**
	 * Evaluates the node in the given context, returning an updated context and value
	 * @param context
	 * @param bindings 
	 * @return
	 */
	public Result<T> eval(Context context, APersistentMap<Symbol, ?> bindings) {
		throw new UnsupportedOperationException("Cannot compile node of type: "+this.getClass());
	}
}
