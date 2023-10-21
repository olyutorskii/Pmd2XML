/*
 */

package testdata.pmd101009.numeric;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;

import static testdata.CnvAssert.*;
import static org.junit.jupiter.api.condition.JRE.JAVA_18;
import static org.junit.jupiter.api.condition.JRE.JAVA_19;

/**
 *
 */
public class NumericTest {

    static Class<?> THISCLASS = NumericTest.class;

    public NumericTest() {
        assert this.getClass() == THISCLASS;
        return;
    }

    @Test
    @EnabledForJreRange(max = JAVA_18)
    public void pmd2xml() throws Exception{
        System.out.println("pmd2xml");
        assertPmd2Xml(THISCLASS, "numeric.pmd", "result.xml");
        return;
    }

    @Test
    @EnabledForJreRange(min = JAVA_19)
    public void pmd2xml19Later() throws Exception{
        System.out.println("pmd2xml");
        assertPmd2Xml(THISCLASS, "numeric.pmd", "result19Later.xml");
        return;
    }

    @Test
    public void xml2pmd() throws Exception{
        System.out.println("xml2pmd");
        assertXml2Pmd(THISCLASS, "source.xml", "numeric.pmd");
        return;
    }

}
