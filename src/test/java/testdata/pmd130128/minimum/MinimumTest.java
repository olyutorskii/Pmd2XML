/*
 */

package testdata.pmd130128.minimum;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static testdata.CnvAssert.*;

/**
 *
 */
public class MinimumTest {

    static Class<?> THISCLASS = MinimumTest.class;

    public MinimumTest() {
        assert this.getClass() == THISCLASS;
        return;
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

    @Test
    public void pmd2xml() throws Exception{
        System.out.println("pmd2xml");
        assertPmd2Xml13(THISCLASS, "minimum.pmd", "minimum.xml");
        return;
    }

    @Test
    public void xml2pmd() throws Exception{
        System.out.println("xml2pmd");
        assertXml2Pmd(THISCLASS, "minimum.xml", "minimum.pmd");
        return;
    }

}
