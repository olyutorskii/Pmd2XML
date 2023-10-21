/*
 */

package testdata.pmd130128.charset;

import org.junit.jupiter.api.Test;

import static testdata.CnvAssert.*;

/**
 *
 */
public class CharsetTest {

    static Class<?> THISCLASS = CharsetTest.class;

    public CharsetTest() {
        assert this.getClass() == THISCLASS;
        return;
    }

    @Test
    public void pmd2xml() throws Exception{
        System.out.println("pmd2xml");
        assertPmd2Xml13(THISCLASS, "charset.pmd", "result.xml");
        return;
    }

    @Test
    public void xml2pmd() throws Exception{
        System.out.println("xml2pmd");
        assertXml2Pmd(THISCLASS, "source.xml", "charset.pmd");
        return;
    }

}
