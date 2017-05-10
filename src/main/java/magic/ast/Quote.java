package magic.ast;

import magic.RT;
import magic.Type;
import magic.compiler.EvalResult;
import magic.data.APersistentMap;
import magic.data.APersistentSet;
import magic.data.APersistentVector;
import magic.data.Maps;
import magic.data.Symbol;
import magic.data.Tuple;
import magic.lang.Context;
import magic.type.JavaType;

/**
 * AST node representing a quoted form
 * 
 * This may possibly include unquoted elements, which are analysed to produce AST replacement nodes
 * @author Mike
 *
 */
public class Quote extends Node<Object> {

	private final boolean syntaxQuote;
	private final Object form;
	private final APersistentMap<APersistentVector<Object>,Node<Object>> unquotes;

	public Quote(Object form, boolean syntaxQuote, APersistentSet<Symbol> symbolSet, APersistentMap<APersistentVector<Object>,Node<Object>> unquotes) {
		super (symbolSet);
		this.unquotes=unquotes;
		this.syntaxQuote=syntaxQuote;
		this.form=form;
	}

	@SuppressWarnings("unchecked")
	public static Quote create(Object form, boolean syntaxQuote, APersistentSet<Symbol> symbolSet) {
		// TODO: identify unquotes
		return new Quote(form,syntaxQuote,symbolSet,(APersistentMap<APersistentVector<Object>, Node<Object>>) Maps.EMPTY);
	}
	
	@Override
	public EvalResult<Object> eval(Context context, APersistentMap<Symbol, Object> bindings) {
		if (unquotes.size()==0) {}
			
		return (EvalResult<Object>) EvalResult.create(context, form);
	}
	
	public boolean isSyntaxQuote() {
		return syntaxQuote;
	}
	
	@Override
	public Type getType() {
		Node<Object> topLevelUnquote=unquotes.get(Tuple.EMPTY);
		if (topLevelUnquote!=null) return topLevelUnquote.getType();
		return JavaType.create(form.getClass());
	}

	@Override
	public Node<Object> specialiseValues(APersistentMap<Symbol, Object> bindings) {
		// TODO: specialise unquotes 
		return this;
	}

	@Override
	public Node<Object> optimise() {
		if (unquotes.size()==0) return Constant.create(form);
		// TODO: optimise unquotes into constants?
		return this;
	}
	
	@Override
	public String toString() {
		return (syntaxQuote?"(Syntax-Quote ":"(Quote ")+RT.toString(form)+")";
	}

}