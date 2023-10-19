/*
 */

package testdata.pmd130128.xml;

import org.junit.jupiter.api.Test;

import static testdata.CnvAssert.*;

/**
 *
 */
public class XmlTest {

    static Class<?> THISCLASS = XmlTest.class;

    public XmlTest() {
        assert this.getClass() == THISCLASS;
        return;
    }

    @Test
    public void xml2pmd() throws Exception{
        System.out.println("xml2pmd");
        assertXml2Pmd(THISCLASS, "namespace.xml", "minimum.pmd");
        return;
    }

}
