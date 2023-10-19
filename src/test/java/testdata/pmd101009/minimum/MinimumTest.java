/*
 */

package testdata.pmd101009.minimum;

import org.junit.jupiter.api.Test;

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

    @Test
    public void pmd2xml() throws Exception{
        System.out.println("pmd2xml");
        assertPmd2Xml(THISCLASS, "minimum.pmd", "minimum.xml");
        return;
    }

    @Test
    public void xml2pmd() throws Exception{
        System.out.println("xml2pmd");
        assertXml2Pmd(THISCLASS, "minimum.xml", "minimum.pmd");
        return;
    }

}
