package magic.ast;

import magic.Type;
import magic.Types;
import magic.compiler.EvalResult;
import magic.data.APersistentMap;
import magic.data.Symbol;
import magic.lang.Context;

public class Do<T> extends Node<T> {
	private final int nBody;
	private final Node<?>[] body;
	
	public Do(Node<?>[] bodyExprs) {
		super(calcDependencies(bodyExprs));
		nBody=bodyExprs.length;
		body=bodyExprs;
	}

	public static <T> Node<T> create(Node<?>[] body) {
		return new Do<T>(body);
	}
	
	@SuppressWarnings({"unchecked"})
	@Override
	public EvalResult<T> eval(Context context, APersistentMap<Symbol, Object> bindings) {
		int nBody=body.length;
		
		EvalResult<T> r=new EvalResult<>(context,null);
		for (int i=0; i<nBody; i++) {
			r=(EvalResult<T>) body[i].eval(r.getContext(),bindings);
		}
		return r;
	}
	
	@Override
	public Node<T> specialiseValues(APersistentMap<Symbol, Object> bindings) {
		boolean changed=false;
		Node<?>[] newBody=body;
		for (int i=0; i<nBody; i++) {
			Node<?> node=body[i];
			Node<?> newNode=node.specialiseValues(bindings);
			if (node!=newNode) {
				if (!changed) {
					newBody=newBody.clone();
					changed=true;
				}
				newBody[i]=newNode;
			} 
		}
		return (body==newBody)?this:create(newBody);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Node<T> optimise() {
		if (nBody==0) return (Node<T>) Constant.NULL;
		if (nBody==1) return (Node<T>) body[0].optimise();
		boolean changed=false;
		Node<?>[] newBody=body;
		for (int i=0; i<nBody; i++) {
			Node<?> node=body[i];
			Node<?> newNode=node.optimise();
			if (node!=newNode) {
				if (!changed) {
					newBody=newBody.clone();
					changed=true;
				}
				newBody[i]=newNode;
			} 
		}
		return (body==newBody)?this:create(newBody);		
	}
	
	/**
	 * Returns the type of this `do` expression, i.e. the type of the last subexpression
	 */
	@Override
	public Type getType() {
		if (nBody==0) return Types.NULL;
		return body[nBody-1].getType();
	}
	
	@Override
	public String toString() {
		StringBuilder sb= new StringBuilder("(Do ");
		for (int i=0; i<nBody; i++) {
			if (i>0) sb.append(' ');
			sb.append(body[i]);
		}
		sb.append(')');
		return sb.toString();
	}

}
