/*
 */

package testdata.pmd101009.small;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static testdata.CnvAssert.*;

/**
 *
 */
public class SmallTest {

    static Class<?> THISCLASS = SmallTest.class;

    public SmallTest() {
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
    public void pmd2xmlBone() throws Exception{
        System.out.println("pmd2xmlBone");
        assertPmd2Xml(THISCLASS, "onlybone.pmd", "onlybone.xml");
        return;
    }

    @Test
    public void pmd2xmlTriangle() throws Exception{
        System.out.println("pmd2xmlTriangle");
        assertPmd2Xml(THISCLASS, "onlytriangle.pmd", "onlytriangle.xml");
        return;
    }

    @Test
    public void pmd2xmlMorph() throws Exception{
        System.out.println("pmd2xmlMorph");
        assertPmd2Xml(THISCLASS, "onlymorph.pmd", "onlymorph.xml");
        return;
    }

    @Test
    public void pmd2xmlRigid() throws Exception{
        System.out.println("pmd2xmlRigid");
        assertPmd2Xml(THISCLASS, "onlyrigid.pmd", "onlyrigid.xml");
        return;
    }

    @Test
    public void pmd2xmlJoint() throws Exception{
        System.out.println("pmd2xmlJoint");
        assertPmd2Xml(THISCLASS, "onlyjoint.pmd", "onlyjoint.xml");
        return;
    }

    @Test
    public void xml2pmdBone() throws Exception{
        System.out.println("xml2pmdBone");
        assertXml2Pmd(THISCLASS, "onlybone.xml", "onlybone.pmd");
        return;
    }

    @Test
    public void xml2pmdTriangle() throws Exception{
        System.out.println("xml2pmdTriangle");
        assertXml2Pmd(THISCLASS, "onlytriangle.xml", "onlytriangle.pmd");
        return;
    }

    @Test
    public void xml2pmdMorph() throws Exception{
        System.out.println("xml2pmdMorph");
        assertXml2Pmd(THISCLASS, "onlymorph.xml", "onlymorph.pmd");
        return;
    }

    @Test
    public void xml2pmdRigid() throws Exception{
        System.out.println("xml2pmdRigid");
        assertXml2Pmd(THISCLASS, "onlyrigid.xml", "onlyrigid.pmd");
        return;
    }

    @Test
    public void xml2pmdJoint() throws Exception{
        System.out.println("xml2pmdJoint");
        assertXml2Pmd(THISCLASS, "onlyjoint.xml", "onlyjoint.pmd");
        return;
    }

}
