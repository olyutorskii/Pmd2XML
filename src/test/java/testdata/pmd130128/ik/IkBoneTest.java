/*
 */

package testdata.pmd130128.ik;

import org.junit.jupiter.api.Test;

import static testdata.CnvAssert.*;

/**
 *
 */
public class IkBoneTest {

    static Class<?> THISCLASS = IkBoneTest.class;

    public IkBoneTest() {
        assert this.getClass() == THISCLASS;
        return;
    }

    @Test
    public void pmd2xml() throws Exception{
        System.out.println("pmd2xml");
        assertPmd2Xml13(THISCLASS, "ikBone.pmd", "ikBone.xml");
        return;
    }

    @Test
    public void xml2pmd() throws Exception{
        System.out.println("xml2pmd");
        assertXml2Pmd(THISCLASS, "ikBone.xml", "ikBone.pmd");
        return;
    }

}
