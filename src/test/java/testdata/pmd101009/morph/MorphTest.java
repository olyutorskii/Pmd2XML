/*
 */

package testdata.pmd101009.morph;

import org.junit.jupiter.api.Test;

import static testdata.CnvAssert.*;

/**
 *
 */
public class MorphTest {

    static Class<?> THISCLASS = MorphTest.class;

    public MorphTest() {
        assert this.getClass() == THISCLASS;
        return;
    }

    @Test
    public void pmd2xml() throws Exception{
        System.out.println("pmd2xml");
        assertPmd2Xml(THISCLASS, "allmorph.pmd", "allmorph.xml");
        return;
    }

    @Test
    public void xml2pmd() throws Exception{
        System.out.println("xml2pmd");
        assertXml2Pmd(THISCLASS, "allmorph.xml", "allmorph.pmd");
        return;
    }

}
