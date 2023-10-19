/*
 */

package testdata.pmd101009.group;

import org.junit.jupiter.api.Test;

import static testdata.CnvAssert.*;

/**
 *
 */
public class GroupTest {

    static Class<?> THISCLASS = GroupTest.class;

    public GroupTest() {
        assert this.getClass() == THISCLASS;
        return;
    }

    @Test
    public void pmd2xml() throws Exception{
        System.out.println("pmd2xml");
        assertPmd2Xml(THISCLASS, "boneGroup.pmd", "boneGroup.xml");
        return;
    }

    @Test
    public void xml2pmd() throws Exception{
        System.out.println("xml2pmd");
        assertXml2Pmd(THISCLASS, "boneGroup.xml", "boneGroup.pmd");
        return;
    }

}
