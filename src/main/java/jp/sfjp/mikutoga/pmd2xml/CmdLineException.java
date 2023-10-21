/*
 * command line exception
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd2xml;

/**
 * コマンドライン引数の不備による異常系。
 */
@SuppressWarnings("serial")
class CmdLineException extends Exception{

    /**
     * コンストラクタ。
     * @param message detail message.
     */
    CmdLineException(String message) {
        super(message);
        return;
    }

}
