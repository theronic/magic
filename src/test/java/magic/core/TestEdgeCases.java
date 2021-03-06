package magic.core;

import static org.junit.Assert.*;

import org.junit.Test;

import magic.Core;
import magic.compiler.EvalResult;
import magic.data.Symbol;
import magic.data.Tuple;
import magic.lang.Context;
import magic.lang.Slot;

public class TestEdgeCases {
	@SuppressWarnings("unchecked")
	private <T> T exec(String code) {
		EvalResult<?> r= Core.eval(code);
		return (T) r.getValue();
	}
	
	@Test public void testDefUnquote() {
		// unquote should create a symbol that can be used with def
		assertEquals((Long)2L,exec("(def ~'a 2) a"));
		assertEquals((Long)2L,exec("(def ~(symbol \"a\") 2) a"));
	}
	
	@Test public void testArgUnquote() {
		// unquote should create a symbol that can be used in parameter vector
		assertEquals((Long)3L,exec(
				"(defn foo [~'a] (+ 2 a)) "
				+ "(foo 1)"));
	}
	
	@Test public void testContextTravel() {
		assertEquals(Tuple.of(1L),exec(
				  "(def a 1) "
				+ "(def ctx *context*)"
				+ "(def a 2)"
				+ "(context ctx)"
				+ "[a]"));
	}
	

	
	@Test public void testVariadicConcat() {
		//Slot<?> cSlot=Core.INITIAL_CONTEXT.getSlot("magic.core/concat");
		//System.out.println(cSlot.getDependencies());
		//System.out.println(cSlot);
		assertTrue(Core.INITIAL_CONTEXT.getDependencies("magic.core/concat").contains(Symbol.create("magic.core/nil?")));
		
		assertEquals(Tuple.of(1L,2L,3L),exec(
				  "(concat [1] [2 3])"));
		assertEquals(Tuple.of(1L,2L,3L,4L,5L,6L,7L,8L,9L,10L),exec(
				  "(concat [1] [2 3] [4 5 6] [7 8 9 10])"));
	}
	
	@Test public void testCapture() {
		Context c=Core.eval("(defn adder [x] (fn [a] (+ a x)))"+
							"(def add2 (adder 2))"+
							"(def add3 (adder 3))").getContext();
		assertEquals(9L,Core.eval(c,"(add2 7)").getValue());
		assertEquals(10L,Core.eval(c,"(add3 7)").getValue());
	}
	
	@Test public void testRecursiveFunction() {
		Context c=Core.eval("(defn fact [a] "
				+ "  (if (<= a 1) 1 (* a (fact (dec a)))))").getContext();
		Slot<?> s=c.getSlot("fact");
		assertEquals(Symbol.createSet("magic.core/fact","magic.core/fn","magic.core/<=","magic.core/if","magic.core/*","magic.core/dec"),s.getDependencies());
		assertEquals(Tuple.of(24L),Core.eval(c,"[(fact 4)]").getValue());
	}
	
	@Test public void testNS() {
		assertEquals("foo.bar",exec(
				  "(ns foo.bar) "
				+ "magic.core/*ns*"));
	}
}
