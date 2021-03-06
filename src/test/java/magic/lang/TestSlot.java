package magic.lang;

import static org.junit.Assert.*;

import org.junit.Test;

import magic.ast.Constant;
import magic.data.Symbol;

public class TestSlot {

	@Test public void testConstantSlot() {
		Symbol sym=Symbol.create("foo");
		
		Context c=Context.EMPTY;
		c=c.define(sym, Constant.create(1L));
		
		assertEquals(Long.valueOf(1),c.getValue(sym));
		
		Slot<?> s=c.getSlot(sym);
		assertEquals(Long.valueOf(1),s.getValue());
	}
}
