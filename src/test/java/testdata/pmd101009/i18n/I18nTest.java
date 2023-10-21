/*
 */

package testdata.pmd101009.i18n;

import org.junit.jupiter.api.Test;

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
