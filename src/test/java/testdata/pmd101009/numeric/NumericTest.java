/*
 */

package testdata.pmd101009.numeric;

import org.junit.jupiter.api.Test;

import static testdata.CnvAssert.*;

/**
 *
 */
public class NumericTest {

    static Class<?> THISCLASS = NumericTest.class;

    public NumericTest() {
        assert this.getClass() == THISCLASS;
        return;
    }

    @Test
    public void pmd2xml() throws Exception{
        System.out.println("pmd2xml");
        assertPmd2Xml(THISCLASS, "numeric.pmd", "result.xml");
        return;
    }

    @Test
    public void xml2pmd() throws Exception{
        System.out.println("xml2pmd");
        assertXml2Pmd(THISCLASS, "source.xml", "numeric.pmd");
        return;
    }

}
