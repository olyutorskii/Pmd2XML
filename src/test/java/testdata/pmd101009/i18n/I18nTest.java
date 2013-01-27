/*
 */

package testdata.pmd101009.i18n;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static testdata.CnvAssert.*;

/**
 *
 */
public class I18nTest {

    static Class<?> THISCLASS = I18nTest.class;

    public I18nTest() {
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
        assertPmd2Xml(THISCLASS, "i18n.pmd", "i18n.xml");
        return;
    }

    @Test
    public void xml2pmd() throws Exception{
        System.out.println("xml2pmd");
        assertXml2Pmd(THISCLASS, "i18n.xml", "i18n.pmd");
        return;
    }

}
