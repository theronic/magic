package magic.ast;

import magic.compiler.EvalResult;
import magic.data.APersistentMap;
import magic.data.Symbol;
import magic.lang.Context;

public class Define<T> extends Node<T> {

	final Symbol sym;
	final Node<T> exp;

	public Define(Symbol sym, Node<T> exp) {
		super(exp.getDependencies());
		this.sym=sym;
		this.exp=exp;
	}

	public static <T> Define<T> create(Symbol sym, Node<T> exp) {
		return new Define<T>(sym,exp);
	}
	
	@Override
	public EvalResult<T> eval(Context context, APersistentMap<Symbol, Object> bindings) {
		Node<T> specExp=exp.specialiseValues(bindings);
		context=context.define(sym, specExp); 
		return new EvalResult<T>(context,null); // TODO: what should def return??
	}
	
	@Override
	public Node<T> specialiseValues(APersistentMap<Symbol, Object> bindings) {
		Node<T> newExp=exp.specialiseValues(bindings);
		return (exp==newExp)?this:create(sym,newExp);
	}

	@Override
	public Node<T> optimise() {
		Node<T> newExp=exp.optimise();
		return (exp==newExp)?this:create(sym,newExp);
	}
}
