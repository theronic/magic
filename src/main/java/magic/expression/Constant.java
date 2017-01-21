package magic.expression;

import magic.lang.Context;
import magic.lang.Expression;

public class Constant extends Expression {

	private final Object value;
	
	public Constant(Object  value) {
		this.value=value;
	}
	
	@Override
	public Object compute(Context c) {
		return value;
	}

}