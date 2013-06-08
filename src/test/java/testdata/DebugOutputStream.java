/*
 */

package testdata;

import java.io.IOException;
import java.io.OutputStream;

/**
 * デバッガ監視用出力ストリーム。
 */
public class DebugOutputStream extends OutputStream{

    private final OutputStream os;
    private long offset = 0L;


    /**
     * コンストラクタ。
     * @param os 委譲先出力ストリーム
     */
    public DebugOutputStream(OutputStream os){
        super();
        this.os = os;
        return;
    }


    /**
     * デバッガ用監視場所。
     */
    private void before(){
        return;
    }

    /**
     * デバッガ用監視場所。
     */
    private void after(){
        this.offset++;
        return;
    }

    /**
     * {@inheritDoc}
     * @param b {@inheritDoc}
     * @throws IOException {@inheritDoc}
     */
    @Override
    public void write(int b) throws IOException {
        before();

        this.os.write(b);

        after();

        return;
    }

    /**
     * {@inheritDoc}
     * @throws IOException {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.os.close();
        return;
    }

    /**
     * {@inheritDoc}
     * @throws IOException {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        this.os.flush();
        return;
    }

}
