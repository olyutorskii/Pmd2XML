/*
 * pmd 2 xml converter main entry
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd2xml;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;
import java.util.Properties;
import jp.sfjp.mikutoga.bin.parser.MmdFormatException;
import jp.sfjp.mikutoga.pmd.IllegalPmdDataException;
import jp.sfjp.mikutoga.xml.TogaXmlException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * PMDモデルファイルとXMLとの間で変換を行うアプリケーション。
 */
public final class Pmd2Xml {

    /** 正常系。 */
    public static final int EXIT_OK     = 0;
    /** 内部エラー。 */
    public static final int EXIT_INTERR = 1;
    /** 実行環境に起因するエラー。 */
    public static final int EXIT_ENVERR = 2;
    /** オプション指定に起因するエラー。 */
    public static final int EXIT_OPTERR = 3;
    /** ファイル入出力に起因するエラー。 */
    public static final int EXIT_IOERR  = 4;
    /** XMLフォーマットに起因するエラー。 */
    public static final int EXIT_XMLERR = 5;
    /** PMDフォーマットに起因するエラー。 */
    public static final int EXIT_PMDERR = 6;

    /** アプリ名。 */
    public static final String APPNAME;
    /** バージョン識別子。 */
    public static final String APPVER;
    /** ライセンス種別。 */
    public static final String APPLICENSE;
    /** 開発元URL。 */
    public static final String APPURL;
    /** ジェネレータ名。 */
    public static final String GENERATOR;

    private static final Class<?> THISCLASS;
    private static final String RES_VER = "resources/version.properties";

    private static final PrintStream ERROUT = System.err;
    private static final String MSG_ERR = "ERROR:\n{0}\n(-h for help)";
    private static final String MSG_HELP =
              "{0} {1}\n"
            + "\u0020\u0020License\u0020:\u0020{2}\n"
            + "\u0020\u0020{3}\n";
    private static final String MSG_NOINFILE = "Can't find input file:{0}";
    private static final String MSG_ABNFILE = "{0} is not file.";
    private static final String MSG_OWOUTFILE =
              "{0} already exists.\n"
            + "If you want to overwrite, use -f.";

    private static final String MSG_OLDJRE = "You need JRE {0} or later.";
    private static final String REQUIRED_JRE = "1.8";

    static{
        THISCLASS = Pmd2Xml.class;
        InputStream ver = THISCLASS.getResourceAsStream(RES_VER);
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
        APPURL     = verProps.getProperty("app.url");

        GENERATOR = APPNAME + ' ' + APPVER;

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
     *
     * @param code 終了コード
     * @see java.lang.System#exit(int)
     */
    private static void exit(int code){
        System.exit(code);
        assert false;
        throw new AssertionError();
    }

    /**
     * 共通エラーメッセージを出力する。
     *
     * @param text 個別メッセージ
     */
    private static void errMsg(String text){
        String msg = MessageFormat.format(MSG_ERR, text);
        ERROUT.println(msg);
        return;
    }

    /**
     * 標準エラー出力へ例外情報出力。
     *
     * @param ex 例外
     * @param dumpStack スタックトレースを出力するならtrue
     */
    private static void thPrintln(Throwable ex, boolean dumpStack){
        if(dumpStack){
            ex.printStackTrace(ERROUT);
        }else{
            String text = ex.toString();
            ERROUT.println(text);
        }

        return;
    }

    /**
     * 標準エラー出力へ例外情報出力。
     *
     * @param ex 例外
     */
    private static void thPrintln(Throwable ex){
        thPrintln(ex, false);
        return;
    }

    /**
     * 入出力エラー処理。
     * 例外を出力してVM終了する。
     *
     * @param ex 例外
     */
    private static void ioError(IOException ex){
        thPrintln(ex);
        exit(EXIT_IOERR);
    }

    /**
     * XML構文エラー処理。
     * 例外を出力してVM終了する。
     *
     * @param ex 例外
     */
    private static void xmlError(Throwable ex){
        thPrintln(ex);
        exit(EXIT_XMLERR);
    }

    /**
     * PMDファイルフォーマットエラー処理。
     * 例外を出力してVM終了する。
     *
     * @param ex 例外
     */
    private static void pmdError(MmdFormatException ex){
        thPrintln(ex, true);
        exit(EXIT_PMDERR);
    }

    /**
     * 内部エラー処理。
     * 例外を出力してVM終了する。
     *
     * @param ex 例外
     */
    private static void internalError(Throwable ex){
        thPrintln(ex, true);
        exit(EXIT_INTERR);
    }

    /**
     * JREのバージョン判定を行う。
     * 不適切ならVMごと終了。
     */
    private static void checkJRE(){
        Package jrePackage = java.lang.Object.class.getPackage();
        if( ! jrePackage.isCompatibleWith(REQUIRED_JRE)){
            String msg = MessageFormat.format(MSG_OLDJRE, REQUIRED_JRE);
            ERROUT.println(msg);
            exit(EXIT_ENVERR);
        }
        return;
    }

    /**
     * ヘルプメッセージを出力する。
     */
    private static void putHelp(){
        String msg =
                MessageFormat.format(MSG_HELP,
                APPNAME, APPVER, APPLICENSE, APPURL);
        ERROUT.println(msg);
        ERROUT.println(OptSwitch.getConsoleHelp());
        return;
    }

    /**
     * ファイルサイズを0に切り詰める。
     *
     * <p>ファイルが存在しなければなにもしない。
     *
     * <p>通常ファイルでなければなにもしない。
     *
     * @param file ファイル
     * @throws IOException 入出力エラー
     */
    private static void trunc(File file) throws IOException{
        if( ! file.exists() ) return;
        if( ! file.isFile() ) return;

        if(file.length() <= 0L) return;

        FileOutputStream foStream = new FileOutputStream(file);
        try{
            FileChannel channnel = foStream.getChannel();
            try{
                channnel.truncate(0L);
            }finally{
                channnel.close();
            }
        }finally{
            foStream.close();
        }

        return;
    }

    /**
     * 入力ソースを準備する。
     *
     * <p>入力ファイルが通常ファイルとして存在しなければエラー終了。
     *
     * @param optInfo オプション情報
     * @return 入力ソース
     */
    private static InputSource openInfile(OptInfo optInfo){
        String fileName = optInfo.getInFilename();
        File inFile = new File(fileName);

        if( (! inFile.exists()) || (! inFile.isFile()) ){
            String absPath = inFile.getAbsolutePath();
            String msg = MessageFormat.format(MSG_NOINFILE, absPath);
            errMsg(msg);
            exit(EXIT_IOERR);
        }

        InputSource source = XmlInputUtil.fileToSource(inFile);

        return source;
    }

    /**
     * 出力ストリームを準備する。
     *
     * <p>出力ファイルが通常ファイルでない場合はエラー終了。
     *
     * <p>既存の出力ファイルに上書き指示が伴っていなければエラー終了。
     *
     * @param optInfo オプション情報
     * @return 出力ストリーム
     */
    private static OutputStream openOutfile(OptInfo optInfo){
        String outputFile = optInfo.getOutFilename();
        boolean overwrite = optInfo.overwriteMode();

        File outFile = new File(outputFile);

        if(outFile.exists()){
            String absPath = outFile.getAbsolutePath();
            if( ! outFile.isFile() ){
                String msg = MessageFormat.format(MSG_ABNFILE, absPath);
                errMsg(msg);
                exit(EXIT_IOERR);
            }else if( ! overwrite ){
                String msg = MessageFormat.format(MSG_OWOUTFILE, absPath);
                errMsg(msg);
                exit(EXIT_IOERR);
            }
        }

        try{
            trunc(outFile);
        }catch(IOException e){
            ioError(e);
        }

        OutputStream os;
        try{
            os = new FileOutputStream(outFile);
        }catch(FileNotFoundException e){
            ioError(e);
            assert false;
            throw new AssertionError(e);
        }

        os = new BufferedOutputStream(os);

        return os;
    }

    /**
     * オプション情報に従いコンバータを生成する。
     *
     * @param optInfo オプション情報
     * @return コンバータ
     */
    private static Pmd2XmlConv buildConverter(OptInfo optInfo){
        Pmd2XmlConv converter = new Pmd2XmlConv();

        converter.setInType( optInfo.getInFileType());
        converter.setOutType(optInfo.getOutFileType());

        converter.setNewline(optInfo.getNewline());
        converter.setGenerator(optInfo.getGenerator());

        return converter;
    }

    /**
     * 実際のコンバート作業と異常系処理を行う。
     *
     * <p>異常系が起きた場合、このメソッドは制御を戻さない。
     *
     * @param converter コンバータ
     * @param source 入力ソース
     * @param ostream 出力ストリーム
     */
    private static void doConvert(Pmd2XmlConv converter,
                                   InputSource source,
                                   OutputStream ostream ){
        try{
            converter.convert(source, ostream);
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

        return;
    }

    /**
     * コマンドライン文字列をオプション情報としてパースする。
     *
     * <p>異常系が起きた場合、このメソッドは制御を戻さない。
     *
     * @param args コマンドライン文字列群
     * @return オプション情報
     */
    private static OptInfo parseOption(String[] args){
        OptInfo optInfo;

        try{
            optInfo = OptInfo.parseOption(args);
        }catch(CmdLineException e){
            String optErrMsg = e.getLocalizedMessage();
            errMsg(optErrMsg);
            exit(EXIT_OPTERR);

            assert false;
            throw new AssertionError(e);
        }

        return optInfo;
    }

    /**
     * Mainエントリ。
     *
     * @param args コマンドパラメータ
     */
    public static void main(String[] args){
        checkJRE();

        OptInfo optInfo = parseOption(args);
        if(optInfo.needHelp()){
            putHelp();
            exit(EXIT_OK);
        }

        Pmd2XmlConv converter = buildConverter(optInfo);
        InputSource source = openInfile(optInfo);
        OutputStream ostream = openOutfile(optInfo);

        doConvert(converter, source, ostream);

        try{
            ostream.close();
        }catch(IOException e){
            ioError(e);
        }

        exit(EXIT_OK);

        return;
    }

}
