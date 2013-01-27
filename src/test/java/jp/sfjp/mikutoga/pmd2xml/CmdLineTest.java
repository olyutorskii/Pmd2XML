/*
 */

package jp.sfjp.mikutoga.pmd2xml;

import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 */
public class CmdLineTest {

    public CmdLineTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of parse method, of class CmdLine.
     */
    @Test
    public void testParse_StringArr() {
        System.out.println("parse");

        List<CmdLine> list;
        List<String> args;
        CmdLine cmd;

        list = CmdLine.parse();
        assertEquals(0, list.size());

        list = CmdLine.parse("-h", "-nl", "crlf");
        assertEquals(2, list.size());
        cmd = list.get(0);
        assertSame(OptSwitch.OPT_HELP, cmd.getOptSwitch());

        args = cmd.getOptArgs();
        assertEquals(1, args.size());
        assertEquals("-h", args.get(0));

        cmd = list.get(1);
        assertSame(OptSwitch.OPT_NEWLINE, cmd.getOptSwitch());

        args = cmd.getOptArgs();
        assertEquals(2, args.size());
        assertEquals("-nl", args.get(0));
        assertEquals("crlf", args.get(1));

        list = CmdLine.parse("XXX");
        assertEquals(1, list.size());
        cmd = list.get(0);
        assertNull(cmd.getOptSwitch());
        args = cmd.getOptArgs();
        assertEquals(1, args.size());
        assertEquals("XXX", args.get(0));

        return;
    }

    /**
     * Test of parse method, of class CmdLine.
     */
    @Test
    public void testParse_List() {
        System.out.println("parse");

        List<CmdLine> list;
        CmdLine cmd;

        list = CmdLine.parse(Arrays.asList("-h"));
        assertEquals(1, list.size());
        cmd = list.get(0);
        assertSame(OptSwitch.OPT_HELP, cmd.getOptSwitch());

        return;
    }

}
