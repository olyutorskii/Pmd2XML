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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import jp.sourceforge.mikutoga.parser.MmdFormatException;
import jp.sourceforge.mikutoga.pmd.IllegalPmdDataException;
import jp.sourceforge.mikutoga.pmd.model.PmdModel;
import jp.sourceforge.mikutoga.pmd.model.binio.PmdExporter;
import jp.sourceforge.mikutoga.pmd.model.binio.PmdLoader;
import jp.sourceforge.mikutoga.pmd.model.xml.PmdXmlExporter;
import jp.sourceforge.mikutoga.pmd.model.xml.PmdXmlResources;
import jp.sourceforge.mikutoga.pmd.model.xml.Xml2PmdLoader;
import jp.sourceforge.mikutoga.xml.TogaXmlException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * PMDモデルファイルとXMLとの間で変換を行うアプリケーション。
 */
public final class Pmd2Xml {

    private static final Class<?> THISCLASS;
    private static final String APPNAME;
    private static final String APPVER;
    private static final String APPLICENSE;

    static{
        THISCLASS = Pmd2Xml.class;
        InputStream ver =
                THISCLASS.getResourceAsStream("resources/version.properties");
        Properties verProps = new Properties();
        try{
            verProps.load(ver);
        }catch(IOException e){
            throw new ExceptionInInitializerError(e);
        }

        APPNAME    = verProps.getProperty("app.name");
        APPVER     = verProps.getProperty("app.version");
        APPLICENSE = verProps.getProperty("app.license");

        Object dummy = new Pmd2Xml();
    }

    /**
     * 隠しコンストラクタ。
     */
    private Pmd2Xml(){
        super();
        assert this.getClass().equals(THISCLASS);
        return;
    }

    /**
     * Mainエントリ。
     * @param args コマンドパラメータ
     */
    public static void main(String[] args){
        checkJRE();

        String inputFile = null;
        String outputFile = null;
        boolean pmd2xml = false;
        boolean xml2pmd = false;
        boolean force = false;
        int argsLen = args.length;
        for(int argIdx = 0; argIdx < argsLen; argIdx++){
            String arg = args[argIdx];

            if(arg.equals("-h")){
                putHelp();
            }else if(arg.equals("-pmd2xml")){
                pmd2xml = true;
                xml2pmd = false;
            }else if(arg.equals("-xml2pmd")){
                pmd2xml = false;
                xml2pmd = true;
            }else if(arg.equals("-i")){
                if(++argIdx >= argsLen){
                    System.err.println("ERROR:");
                    System.err.println("You need -i argument.");
                    System.err.println("(-h for help)");
                    System.exit(5);
                }
                inputFile = args[argIdx];
            }else if(arg.equals("-o")){
                if(++argIdx >= argsLen){
                    System.err.println("ERROR:");
                    System.err.println("You need -o argument.");
                    System.err.println("(-h for help)");
                    System.exit(5);
                }
                outputFile = args[argIdx];
            }else if(arg.equals("-f")){
                force = true;
            }else{
                System.err.println("ERROR:");
                System.err.println("Unknown option:"+arg);
                System.err.println("(-h for help)");
                System.exit(5);
            }
        }

        if( ( ! pmd2xml) && ( ! xml2pmd) ){
            System.err.println("ERROR:");
            System.err.println("You must specify -pmd2xml or -xml2pmd.");
            System.err.println("(-h for help)");
            System.exit(5);
        }

        if(inputFile == null){
            System.err.println("ERROR:");
            System.err.println("You must specify input file with -i.");
            System.err.println("(-h for help)");
            System.exit(5);
        }

        if(outputFile == null){
            System.err.println("ERROR:");
            System.err.println("You must specify output file with -o.");
            System.err.println("(-h for help)");
            System.exit(5);
        }

        File iFile = new File(inputFile);
        if( (! iFile.exists()) || (! iFile.isFile()) ){
            System.err.println("ERROR:");
            System.err.println("Can't find input file:"
                    + iFile.getAbsolutePath());
            System.err.println("(-h for help)");
            System.exit(1);
        }

        if( ! force ){
            File oFile = new File(outputFile);
            if(oFile.exists()){
                System.err.println("ERROR:");
                System.err.println(oFile.getAbsolutePath()
                        + " already exists.");
                System.err.println("If you want to overwrite, use -f.");
                System.err.println("(-h for help)");
                System.exit(1);
            }
        }else{
            File oFile = new File(outputFile);
            if(oFile.exists()){
                if( ! oFile.isFile()){
                    System.err.println("ERROR:");
                    System.err.println(oFile.getAbsolutePath()
                            + " is not file.");
                    System.err.println("(-h for help)");
                    System.exit(1);
                }
            }
        }

        try{
            if(pmd2xml) pmd2xml(inputFile, outputFile);
            else        xml2pmd(inputFile, outputFile);
        }catch(IOException e){
            ioError(e);
        }catch(ParserConfigurationException e){
            internalError(e);
        }catch(IllegalPmdDataException e){
            internalError(e);
        }catch(MmdFormatException e){
            pmdError(e);
        }catch(TogaXmlException e){
            xmlError(e);
        }catch(SAXException e){
            xmlError(e);
        }

        System.exit(0);

        return;
    }

    /**
     * 入出力エラー処理。
     * 例外を出力してVM終了する。
     * @param ex 例外
     */
    private static void ioError(Throwable ex){
        System.err.println(ex);
        System.exit(1);
    }

    /**
     * XML構文エラー処理。
     * 例外を出力してVM終了する。
     * @param ex 例外
     */
    private static void xmlError(Throwable ex){
        System.err.println(ex);
        System.exit(2);
    }

    /**
     * PMDファイルフォーマットエラー処理。
     * 例外を出力してVM終了する。
     * @param ex 例外
     */
    private static void pmdError(Throwable ex){
        System.err.println(ex);
        ex.printStackTrace(System.err);
        System.exit(3);
    }

    /**
     * 内部エラー処理。
     * 例外を出力してVM終了する。
     * @param ex 例外
     */
    private static void internalError(Throwable ex){
        System.err.println(ex);
        ex.printStackTrace(System.err);
        System.exit(4);
    }

    /**
     * JREのバージョン判定を行う。
     * 不適切ならVMごと終了。
     */
    private static void checkJRE(){
        Package jrePackage = java.lang.Object.class.getPackage();
        if( ! jrePackage.isCompatibleWith("1.6")){
            System.err.println("You need JRE 1.6 or later.");
            System.exit(4);
        }
        return;
    }

    /**
     * ヘルプメッセージを出力してVMを終了させる。
     */
    private static void putHelp(){
        System.err.println(APPNAME + ' ' + APPVER );
        System.err.println("  License : " + APPLICENSE);
        System.err.println("  http://mikutoga.sourceforge.jp/");
        System.err.println();
        System.err.println("-h       : put help massage");
        System.err.println("-pmd2xml : convert *.pmd to *.xml");
        System.err.println("-xml2pmd : convert *.xml to *.pmd");
        System.err.println("-i file  : specify input file");
        System.err.println("-o file  : specify output file");
        System.err.println("-f       : force overwriting");
        System.exit(0);
        return;
    }

    /**
     * PMD->XML変換を行う。
     * @param inputFile 入力ファイル名
     * @param outputFile 出力ファイル名
     * @throws IOException 入出力エラー
     * @throws MmdFormatException 不正なPMDファイル
     * @throws IllegalPmdDataException 不正なモデルデータ
     */
    private static void pmd2xml(String inputFile, String outputFile)
            throws IOException, MmdFormatException, IllegalPmdDataException{
        File iFile = new File(inputFile);
        InputStream is = new FileInputStream(iFile);
        is = new BufferedInputStream(is);
        PmdModel model = pmdRead(is);
        is.close();

        File oFile = new File(outputFile);
        trunc(oFile);
        OutputStream ostream;
        ostream = new FileOutputStream(oFile, false);
        ostream = new BufferedOutputStream(ostream);
        xmlOut(model, ostream);
        ostream.close();

        return;
    }

    /**
     * XML->PMD変換を行う。
     * @param inputFile 入力ファイル名
     * @param outputFile 出力ファイル名
     * @throws IOException 入出力エラー
     * @throws ParserConfigurationException XML構成のエラー
     * @throws SAXException 不正なXMLファイル
     * @throws TogaXmlException 不正なXMLファイル
     * @throws IllegalPmdDataException 不正なPMDモデルデータ
     */
    private static void xml2pmd(String inputFile, String outputFile)
            throws IOException,
                   ParserConfigurationException,
                   SAXException,
                   TogaXmlException,
                   IllegalPmdDataException {
        File iFile = new File(inputFile);
        InputStream is = new FileInputStream(iFile);
        is = new BufferedInputStream(is);
        InputSource source = new InputSource(is);
        PmdModel model = xmlRead(source);
        is.close();

        File oFile = new File(outputFile);
        trunc(oFile);
        OutputStream ostream;
        ostream = new FileOutputStream(oFile, false);
        ostream = new BufferedOutputStream(ostream);
        pmdOut(model, ostream);
        ostream.close();

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
     * PMDファイルからモデルデータを読み込む。
     * @param is 入力ストリーム
     * @return モデルデータ
     * @throws IOException 入力エラー
     * @throws MmdFormatException 不正なPMDファイルフォーマット
     */
    private static PmdModel pmdRead(InputStream is)
            throws IOException, MmdFormatException{
        PmdLoader loader = new PmdLoader(is);

        PmdModel model = loader.load();

        return model;
    }

    /**
     * XMLファイルからモデルデータを読み込む。
     * @param source 入力ソース
     * @return モデルデータ
     * @throws IOException 入力エラー
     * @throws ParserConfigurationException XML構成エラー
     * @throws SAXException XML構文エラー
     * @throws TogaXmlException 不正なXMLデータ
     */
    private static PmdModel xmlRead(InputSource source)
            throws IOException,
                   ParserConfigurationException,
                   SAXException,
                   TogaXmlException {
        DocumentBuilder builder =
                PmdXmlResources.newBuilder(XmlHandler.HANDLER);
        Xml2PmdLoader loader = new Xml2PmdLoader(builder);

        PmdModel model = loader.parse(source);

        return model;
    }

    /**
     * モデルデータをPMDファイルに出力する。
     * @param model モデルデータ
     * @param ostream 出力ストリーム
     * @throws IOException 出力エラー
     * @throws IllegalPmdDataException 不正なモデルデータ
     */
    private static void pmdOut(PmdModel model, OutputStream ostream)
            throws IOException, IllegalPmdDataException{
        PmdExporter exporter = new PmdExporter(ostream);
        exporter.dumpPmdModel(model);
        ostream.close();
        return;
    }

    /**
     * モデルデータをXMLファイルに出力する。
     * @param model モデルデータ
     * @param ostream 出力ストリーム
     * @throws IOException 出力エラー
     * @throws IllegalPmdDataException 不正なモデルデータ
     */
    private static void xmlOut(PmdModel model, OutputStream ostream)
            throws IOException, IllegalPmdDataException{
        PmdXmlExporter exporter = new PmdXmlExporter(ostream);
        exporter.setNewLine("\r\n");
        exporter.setGenerator(APPNAME + ' ' + APPVER);
        exporter.putPmdModel(model);
        exporter.close();
        return;
    }

}
