/*
 */

package jp.sfjp.mikutoga.pmd2xml;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 *
 */
public class OptSwitchTest {

    public OptSwitchTest() {
    }

    /**
     * Test of values method, of class OptSwitch.
     */
    @Test
    public void testValues() {
        System.out.println("values");

        assertEquals(8, OptSwitch.values().length);

        return;
    }

    /**
     * Test of valueOf method, of class OptSwitch.
     */
    @Test
    public void testValueOf() {
        System.out.println("valueOf");

        OptSwitch sw = OptSwitch.valueOf("OPT_HELP");
        assertSame(OptSwitch.OPT_HELP, sw);

        return;
    }

    /**
     * Test of getConsoleHelp method, of class OptSwitch.
     */
    @Test
    public void testGetConsoleHelp() {
        System.out.println("getConsoleHelp");

        String help = OptSwitch.getConsoleHelp();
        assertNotNull(help);

        return;
    }

    /**
     * Test of parse method, of class OptSwitch.
     */
    @Test
    public void testParse() {
        System.out.println("parse");

        OptSwitch sw;

        sw = OptSwitch.parse(null);
        assertNull(sw);
        sw = OptSwitch.parse("");
        assertNull(sw);
        sw = OptSwitch.parse("help");
        assertNull(sw);

        sw = OptSwitch.parse("-h");
        assertSame(OptSwitch.OPT_HELP, sw);
        sw = OptSwitch.parse("-help");
        assertSame(OptSwitch.OPT_HELP, sw);
        sw = OptSwitch.parse("-?");
        assertSame(OptSwitch.OPT_HELP, sw);

        sw = OptSwitch.parse("-i");
        assertSame(OptSwitch.OPT_INFILE, sw);
        sw = OptSwitch.parse("-o");
        assertSame(OptSwitch.OPT_OUTFILE, sw);
        sw = OptSwitch.parse("-f");
        assertSame(OptSwitch.OPT_FORCE, sw);

        sw = OptSwitch.parse("-nl");
        assertSame(OptSwitch.OPT_NEWLINE, sw);
        sw = OptSwitch.parse("-genout");
        assertSame(OptSwitch.OPT_GENOUT, sw);

        sw = OptSwitch.parse("-iform");
        assertSame(OptSwitch.OPT_IFORM, sw);
        sw = OptSwitch.parse("-oform");
        assertSame(OptSwitch.OPT_OFORM, sw);

        return;
    }

    /**
     * Test of getExArgNum method, of class OptSwitch.
     */
    @Test
    public void testGetExArgNum() {
        System.out.println("getExArgNum");

        assertEquals(0, OptSwitch.OPT_HELP.getExArgNum());
        assertEquals(1, OptSwitch.OPT_INFILE.getExArgNum());
        assertEquals(1, OptSwitch.OPT_OUTFILE.getExArgNum());
        assertEquals(0, OptSwitch.OPT_FORCE.getExArgNum());
        assertEquals(1, OptSwitch.OPT_NEWLINE.getExArgNum());
        assertEquals(1, OptSwitch.OPT_GENOUT.getExArgNum());
        assertEquals(1, OptSwitch.OPT_IFORM.getExArgNum());
        assertEquals(1, OptSwitch.OPT_OFORM.getExArgNum());

        return;
    }

}
