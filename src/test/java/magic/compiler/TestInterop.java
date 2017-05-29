package magic.compiler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import magic.RT;
import magic.lang.Context;

public class TestInterop {


	@Test public void testInstanceCall() {
		Context c=RT.INITIAL_CONTEXT;
		
		EvalResult<?> r=Compiler.compile(c, 
				"(def len (. \"foo\" length))");
		Context c2=r.getContext();
		//System.out.println(c2.getExpression("f"));
		Object res=c2.getValue("len");
		//System.out.println("<END>");
		assertEquals((Integer)3,res);
	}
	
}