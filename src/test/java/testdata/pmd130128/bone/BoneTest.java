/*
 */

package testdata.pmd130128.bone;

import org.junit.jupiter.api.Test;

import static testdata.CnvAssert.*;

/**
 *
 */
public class BoneTest {

    static Class<?> THISCLASS = BoneTest.class;

    public BoneTest() {
        assert this.getClass() == THISCLASS;
        return;
    }

    @Test
    public void pmd2xml() throws Exception{
        System.out.println("pmd2xml");
        assertPmd2Xml13(THISCLASS, "allbone.pmd", "allbone.xml");
        return;
    }

    @Test
    public void xml2pmd() throws Exception{
        System.out.println("xml2pmd");
        assertXml2Pmd(THISCLASS, "allbone.xml", "allbone.pmd");
        return;
    }

}
