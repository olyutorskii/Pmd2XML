/*
 */

package jp.sfjp.mikutoga.pmd2xml;

import jp.sfjp.mikutoga.pmd.model.xml.XmlModelFileType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class ModelFileTypeTest {

    public ModelFileTypeTest() {
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

    /**
     * Test of values method, of class ModelFileType.
     */
    @Test
    public void testValues() {
        System.out.println("values");

        assertEquals(5, ModelFileType.values().length);

        return;
    }

    /**
     * Test of toXmlType method, of class ModelFileType.
     */
    @Test
    public void testToXmlType() {
        System.out.println("toXmlType");

        assertSame(XmlModelFileType.XML_AUTO, ModelFileType.NONE.toXmlType());
        assertSame(XmlModelFileType.XML_AUTO, ModelFileType.PMD.toXmlType());
        assertSame(XmlModelFileType.XML_AUTO, ModelFileType.XML_AUTO.toXmlType());
        assertSame(XmlModelFileType.XML_101009, ModelFileType.XML_101009.toXmlType());
        assertSame(XmlModelFileType.XML_130128, ModelFileType.XML_130128.toXmlType());

        return;
    }

    /**
     * Test of isXml method, of class ModelFileType.
     */
    @Test
    public void testIsXml() {
        System.out.println("isXml");

        assertFalse(ModelFileType.NONE.isXml());
        assertFalse(ModelFileType.PMD.isXml());
        assertTrue(ModelFileType.XML_AUTO.isXml());
        assertTrue(ModelFileType.XML_101009.isXml());
        assertTrue(ModelFileType.XML_130128.isXml());

        return;
    }

    /**
     * Test of isPmd method, of class ModelFileType.
     */
    @Test
    public void testIsPmd() {
        System.out.println("isPmd");

        assertFalse(ModelFileType.NONE.isPmd());
        assertTrue(ModelFileType.PMD.isPmd());
        assertFalse(ModelFileType.XML_AUTO.isPmd());
        assertFalse(ModelFileType.XML_101009.isPmd());
        assertFalse(ModelFileType.XML_130128.isPmd());

        return;
    }

}
