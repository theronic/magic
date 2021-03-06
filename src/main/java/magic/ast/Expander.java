package magic.ast;

import magic.Keywords;
import magic.Symbols;
import magic.Type;
import magic.Types;
import magic.compiler.AExpander;
import magic.compiler.AListExpander;
import magic.compiler.AnalyserException;
import magic.compiler.EvalResult;
import magic.compiler.SourceInfo;
import magic.data.APersistentList;
import magic.data.APersistentMap;
import magic.data.APersistentVector;
import magic.data.Keyword;
import magic.data.Lists;
import magic.data.Maps;
import magic.data.Symbol;
import magic.fn.IFn1;
import magic.lang.Context;

/**
 * AST node representing an expander construction expression 
 *  a.k.a. "(expander [ex [...]] ...)"
 * 
 * @author Mike
 *
 * @param <T>
 */
public class Expander extends BaseForm<AExpander> {

	private final Symbol exSym;
	private final APersistentVector<Symbol> args;
	private final Node<?> body;
  
	@SuppressWarnings("unchecked")
	public Expander(Symbol exSym, APersistentVector<Symbol> args, Node<?> body, APersistentMap<Keyword,Object> meta) {
		super((APersistentList<Node<?>>)(APersistentList<?>)
				Lists.of(Lookup.create(Symbols.EXPANDER),Constant.create(args),body),
				meta);
		this.exSym=exSym;
		this.args=args;
		this.body=body;
	}
	
	@Override
	public Node<AExpander> withMeta(APersistentMap<Keyword, Object> meta) {
		return new Expander(exSym,args,body,meta);
	}
	
	public static Expander create(Symbol exSym, APersistentVector<Symbol> args, Node<?> body,SourceInfo source) {
		APersistentMap<Keyword,Object> meta=Maps.create(Keywords.SOURCE, source);
		meta=meta.assoc(Keywords.DEPS, body.getDependencies().excludeAll(args));
		return new Expander(exSym,args,body,meta);
	}

	@SuppressWarnings("unchecked")
	public static Expander create(Symbol exSym, Vector<Symbol> args, APersistentList<Node<?>> body, SourceInfo source) {
		APersistentVector<Symbol> alist=(APersistentVector<Symbol>) args.toForm();
		return create(exSym,alist,Do.create(body,source),source);
	}

	@Override
	public EvalResult<AExpander> eval(Context context,APersistentMap<Symbol, Object> bindings) {
		// capture lexical bindings excluding exSym and parameters
		bindings=bindings.dissoc(exSym);
		final APersistentMap<Symbol, Object> capturedBindings=bindings.delete(args);
		
		// capture variables defined in the current lexical scope
		Node<?> body=this.body.specialiseValues(capturedBindings);
		
		// System.out.println(body);
		AExpander fn=new AListExpander() {
			private static final long serialVersionUID = -4348972480593384047L;

			@Override
			public Type getReturnType() {
				return body.getType();
			}

			@Override
			public Node<?> expand(Context c, ListForm form, AExpander ex) {
				APersistentMap<Symbol, Object> bnds=capturedBindings;
				bnds=bnds.assoc(exSym, ex);
				int n=form.size();
				int pn=args.size();
				if (n!=pn+1) {
					throw new AnalyserException("Expander expects "+pn+" arguments but got "+n,form);
				}
				for (int i=0; i<pn; i++) {
					bnds=bnds.assoc(args.get(i), form.get(i+1));
				}
				return (Node<?>) body.compute(c,bnds);
			}
		};
		return new EvalResult<AExpander>(context,fn);
	}
	
	@Override
	public Node<? extends AExpander> specialiseValues(APersistentMap<Symbol, Object> bindings) {
		bindings=bindings.delete(args); // hidden by parameter bindings
		bindings=bindings.dissoc(exSym); // hidden by expander binding
		return mapChildren(NodeFunctions.specialiseValues(bindings));
	}
	
	@Override
	public Node<? extends AExpander> optimise() {
		return mapChildren(NodeFunctions.optimise());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Node<? extends AExpander> mapChildren(IFn1<Node<?>, Node<?>> fn) {
		Node<? extends AExpander> newBody=(Node<? extends AExpander>) fn.apply(body);
		return (body==newBody)?this:(Expander) create(exSym,args,newBody,getSourceInfo());
	}

	/**
	 * Returns the type of this `expander` expression, i.e. the a function type returning the type of the body
	 */
	@Override
	public Type getType() {
		return Types.EXPANDER;
	}
	
	@Override
	public String toString() {
		return "(EXPANDER "+args+" "+body+")";
	}

}
