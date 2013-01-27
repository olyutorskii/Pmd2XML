/*
 */

package testdata.pmd101009.group;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
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
