/*
 */

package testdata.pmd130128.rigid;

import org.junit.jupiter.api.Test;

import static testdata.CnvAssert.*;

/**
 *
 */
public class RigidTest {

    static Class<?> THISCLASS = RigidTest.class;

    public RigidTest() {
        assert this.getClass() == THISCLASS;
        return;
    }

    @Test
    public void pmd2xml() throws Exception{
        System.out.println("pmd2xml");
        assertPmd2Xml13(THISCLASS, "allrigid.pmd", "allrigid.xml");
        return;
    }

    @Test
    public void xml2pmd() throws Exception{
        System.out.println("xml2pmd");
        assertXml2Pmd(THISCLASS, "allrigid.xml", "allrigid.pmd");
        return;
    }

}
