/*
 */

package testdata;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 使い捨てテスト用ドライバ。
 */
public class TestDriver {

    static{
        assert Test.class != null;
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
     * 使い捨てテスト本体。
     * <p>テストが終わったら必ず元に戻して
     * アノテーション"Test"をコメントアウトするように。
     * @throws Exception テスト失敗
     */
    //@Test
    public void main() throws Exception{
    }

}
