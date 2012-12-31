/*
 * pmd 2 xml converter main entry
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sourceforge.mikutoga.pmd2xml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.channels.FileChannel;
import java.util.Properties;
import jp.sourceforge.mikutoga.parser.MmdFormatException;
import jp.sourceforge.mikutoga.pmd.IllegalPmdDataException;
import jp.sourceforge.mikutoga.xml.TogaXmlException;
import org.xml.sax.SAXException;

/**
 * PMDモデルファイルとXMLとの間で変換を行うアプリケーション。
 */
public final class Pmd2Xml {

    /** 正常系。 */
    public static final int EXIT_OK     = 0;
    /** ファイル入出力に起因するエラー。 */
    public static final int EXIT_FILE   = 1;
    /** XMLフォーマットに起因するエラー。 */
    public static final int EXIT_XML    = 2;
    /** PMDフォーマットに起因するエラー。 */
    public static final int EXIT_PMD    = 3;
    /** 実行環境に起因するエラー。 */
    public static final int EXIT_JREVER = 4;
    /** オプション指定に起因するエラー。 */
    public static final int EXIT_OPT    = 5;
    /** 内部エラー。 */
    public static final int EXIT_INTERN = 6;

    /** アプリ名。 */
    public static final String APPNAME;
    /** バージョン識別子。 */
    public static final String APPVER;
    /** ライセンス種別。 */
    public static final String APPLICENSE;

    private static final Class<?> THISCLASS;

    private static final PrintStream ERROUT = System.err;

    static{
        THISCLASS = Pmd2Xml.class;
        InputStream ver =
                THISCLASS.getResourceAsStream("resources/version.properties");
        Properties verProps = new Properties();
        try{
            try{
                verProps.load(ver);
            }finally{
                ver.close();
            }
        }catch(IOException e){
            throw new ExceptionInInitializerError(e);
        }

        APPNAME    = verProps.getProperty("app.name");
        APPVER     = verProps.getProperty("app.version");
        APPLICENSE = verProps.getProperty("app.license");

        new Pmd2Xml().hashCode();
    }


    /**
     * ダミーコンストラクタ。
     */
    private Pmd2Xml(){
        super();
        assert this.getClass().equals(THISCLASS);
        return;
    }


    /**
     * VMを終了させる。
     * @param code 終了コード
     */
    private static void exit(int code){
        System.exit(code);
        return;
    }

    /**
     * 標準エラー出力へ例外情報出力。
     * @param ex 例外
     * @param dumpStack スタックトレースを出力するならtrue
     */
    private static void errPrintln(Throwable ex, boolean dumpStack){
        String text = ex.toString();
        ERROUT.println(text);

        if(dumpStack){
            ex.printStackTrace(ERROUT);
        }

        return;
    }

    /**
     * 標準エラー出力へ例外情報出力。
     * @param ex 例外
     */
    private static void errPrintln(Throwable ex){
        errPrintln(ex, false);
        return;
    }

    /**
     * 共通エラーメッセージを出力する。
     * @param text 個別メッセージ
     */
    private static void errMsg(String text){
        ERROUT.println("ERROR:");
        ERROUT.println(text);
        ERROUT.println("(-h for help)");
        return;
    }

    /**
     * 入出力エラー処理。
     * 例外を出力してVM終了する。
     * @param ex 例外
     */
    private static void ioError(Throwable ex){
        errPrintln(ex);
        exit(EXIT_FILE);
    }

    /**
     * XML構文エラー処理。
     * 例外を出力してVM終了する。
     * @param ex 例外
     */
    private static void xmlError(Throwable ex){
        errPrintln(ex);
        exit(EXIT_XML);
    }

    /**
     * PMDファイルフォーマットエラー処理。
     * 例外を出力してVM終了する。
     * @param ex 例外
     */
    private static void pmdError(Throwable ex){
        errPrintln(ex, true);
        exit(EXIT_PMD);
    }

    /**
     * 内部エラー処理。
     * 例外を出力してVM終了する。
     * @param ex 例外
     */
    private static void internalError(Throwable ex){
        errPrintln(ex, true);
        exit(EXIT_INTERN);
    }

    /**
     * JREのバージョン判定を行う。
     * 不適切ならVMごと終了。
     */
    private static void checkJRE(){
        Package jrePackage = java.lang.Object.class.getPackage();
        if( ! jrePackage.isCompatibleWith("1.6")){
            ERROUT.println("You need JRE 1.6 or later.");
            exit(EXIT_JREVER);
        }
        return;
    }

    /**
     * ヘルプメッセージを出力してVMを終了させる。
     */
    private static void putHelp(){
        StringBuilder appInfo = new StringBuilder();
        String indent = "  ";

        appInfo.append(APPNAME).append(' ').append(APPVER)
               .append('\n');
        appInfo.append(indent)
               .append("License").append(" : ").append(APPLICENSE)
               .append('\n');
        appInfo.append(indent)
               .append("http://mikutoga.sourceforge.jp/")
               .append('\n');

        ERROUT.println(appInfo.toString());
        ERROUT.println(OptInfo.getConsoleHelp());

        return;
    }

    /**
     * ファイルサイズを0に切り詰める。
     * @param file ファイル
     * @throws IOException 入出力エラー
     */
    private static void trunc(File file) throws IOException{
        if( ! file.exists() ) return;
        if( ! file.isFile() ) return;

        FileOutputStream foStream = new FileOutputStream(file);
        FileChannel channnel = foStream.getChannel();
        channnel.truncate(0);

        channnel.close();
        foStream.close();

        return;
    }

    /**
     * 入力ストリームを準備する。
     * @param fileName 入力ファイル名
     * @return 入力ストリーム
     */
    private static InputStream openInfile(String fileName){
        File inFile = new File(fileName);

        if( (! inFile.exists()) || (! inFile.isFile()) ){
            String absPath = inFile.getAbsolutePath();
            errMsg("Can't find input file:" + absPath);
            exit(EXIT_FILE);
        }

        InputStream is = null;
        try{
            is = new FileInputStream(inFile);
        }catch(FileNotFoundException e){
            ioError(e);
            assert false;
        }

        is = new BufferedInputStream(is);

        return is;
    }

    /**
     * 出力ストリームを準備する。
     * @param fileName 出力ファイル名
     * @param overWrite 頭から上書きして良ければtrue
     * @return 出力ストリーム
     */
    private static OutputStream openOutfile(String fileName,
                                            boolean overWrite) {
        File outFile = new File(fileName);

        if(outFile.exists()){
            String absPath = outFile.getAbsolutePath();
            if( ! outFile.isFile() ){
                String msg = absPath + " is not file.";
                errMsg(msg);
                exit(EXIT_FILE);
            }else if( ! overWrite ){
                String msg =
                          absPath + " already exists.\n"
                        + "If you want to overwrite, use -f.";
                errMsg(msg);
                exit(EXIT_FILE);
            }
        }

        try{
            trunc(outFile);
        }catch(IOException e){
            ioError(e);
        }

        OutputStream os = null;
        try{
            os = new FileOutputStream(outFile);
        }catch(FileNotFoundException e){
            ioError(e);
            assert false;
        }

        os = new BufferedOutputStream(os);

        return os;
    }

    /**
     * Mainエントリ。
     * @param args コマンドパラメータ
     */
    public static void main(String[] args){
        checkJRE();

        Pmd2XmlConv converter = new Pmd2XmlConv();

        OptInfo optInfo = OptInfo.parseOption(args);
        if(optInfo.needHelp()){
            putHelp();
            exit(EXIT_OK);
        }else if(optInfo.hasError()){
            String optErrMsg = optInfo.getErrorMessage();
            errMsg(optErrMsg);
            exit(EXIT_OPT);
        }

        String inputFile = optInfo.getInFilename();
        String outputFile = optInfo.getOutFilename();
        boolean overwrite = optInfo.overwriteMode();

        InputStream  is = openInfile(inputFile);
        OutputStream os = openOutfile(outputFile, overwrite);

        converter.setInType(optInfo.getInFileType());
        converter.setOutType(optInfo.getOutFileType());

        converter.setNewline(optInfo.getNewline());
        converter.setGenerator(optInfo.getGenerator());

        try{
            converter.convert(is, os);
        }catch(IOException e){
            ioError(e);
        }catch(IllegalPmdDataException e){
            internalError(e);
        }catch(MmdFormatException e){
            pmdError(e);
        }catch(TogaXmlException e){
            xmlError(e);
        }catch(SAXException e){
            xmlError(e);
        }

        try{
            is.close();
            try{
                os.close();
            }catch(IOException e){
                ioError(e);
            }
        }catch(IOException e){
            ioError(e);
        }

        exit(EXIT_OK);

        return;
    }

}
