/*
 */

package jp.sfjp.mikutoga.pmd2xml;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class OptInfoTest {

    public OptInfoTest() {
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
     * Test of parseOption method, of class OptInfo.
     */
    @Test
    public void testParseOption() throws Exception {
        System.out.println("parseOption");

        OptInfo info;

        info = OptInfo.parseOption("-i", "ifile.xml", "-o", "ofile.pmd");
        assertFalse(info.needHelp());
        assertSame(ModelFileType.XML_AUTO, info.getInFileType());
        assertSame(ModelFileType.PMD, info.getOutFileType());
        assertEquals("ifile.xml", info.getInFilename());
        assertEquals("ofile.pmd", info.getOutFilename());
        assertFalse(info.overwriteMode());
        assertEquals("\n", info.getNewline());
        assertNotNull(info.getGenerator());

        info = OptInfo.parseOption("-i", "ifile.pmd", "-o", "ofile.xml");
        assertSame(ModelFileType.PMD, info.getInFileType());
        assertSame(ModelFileType.XML_AUTO, info.getOutFileType());

        info = OptInfo.parseOption("-i", "ifile.xml", "-o", "ofile.pmd", "-f");
        assertTrue(info.overwriteMode());

        info = OptInfo.parseOption("-i", "ifile.xml", "-o", "ofile.pmd",
                "-nl", "crlf");
        assertEquals("\r\n", info.getNewline());

        info = OptInfo.parseOption("-i", "ifile.xml", "-o", "ofile.pmd",
                "-genout", "off");
        assertNull(info.getGenerator());

        return;
    }

    /**
     * Test of needHelp method, of class OptInfo.
     */
    @Test
    public void testNeedHelp() throws Exception{
        System.out.println("needHelp");

        OptInfo info;

        info = OptInfo.parseOption("-h");
        assertTrue(info.needHelp());

        return;
    }

}
