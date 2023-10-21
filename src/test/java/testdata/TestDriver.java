/*
 */

package testdata;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * 使い捨てテスト用ドライバ。
 */
public class TestDriver {

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * 使い捨てテスト本体。
     * <p>テストを行う時は
     * アノテーション"Ignore"をコメントアウト。
     * @throws Exception テスト失敗
     */
    @Test
    @Disabled
    public void main() throws Exception{
        assertTrue(true);
//        fail();
        return;
    }

}
