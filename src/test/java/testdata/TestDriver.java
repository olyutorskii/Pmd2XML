/*
 */

package testdata;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 使い捨てテスト用ドライバ。
 */
public class TestDriver {

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
     * <p>テストを行う時は
     * アノテーション"Ignore"をコメントアウト。
     * @throws Exception テスト失敗
     */
    @Test
    @Ignore
    public void main() throws Exception{
        assertTrue(true);
//        fail();
        return;
    }

}
