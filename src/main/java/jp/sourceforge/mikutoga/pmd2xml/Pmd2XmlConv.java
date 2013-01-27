/*
 * pmd 2 xml converter
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sourceforge.mikutoga.pmd2xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import jp.sfjp.mikutoga.pmd.xml101009.Xml101009Exporter;
import jp.sfjp.mikutoga.pmd.xml101009.Xml101009Loader;
import jp.sfjp.mikutoga.pmd.xml101009.Xml101009Resources;
import jp.sourceforge.mikutoga.parser.MmdFormatException;
import jp.sourceforge.mikutoga.pmd.IllegalPmdDataException;
import jp.sourceforge.mikutoga.pmd.ModelFileType;
import jp.sourceforge.mikutoga.pmd.model.PmdModel;
import jp.sourceforge.mikutoga.pmd.model.binio.PmdExporter;
import jp.sourceforge.mikutoga.pmd.model.binio.PmdLoader;
import jp.sourceforge.mikutoga.xml.TogaXmlException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * PMD-XML間コンバータ本体。
 */
public class Pmd2XmlConv {

    private ModelFileType inTypes  = ModelFileType.NONE;
    private ModelFileType outTypes = ModelFileType.NONE;
    private String newLine = "\r\n";
    private String generator = null;

    private final DocumentBuilder builder;


    /**
     * コンストラクタ。
     */
    public Pmd2XmlConv(){
        super();

        try{
            this.builder = Xml101009Resources.newBuilder(XmlHandler.HANDLER);
        }catch(SAXException e){
            throw new AssertionError(e);
        }catch(ParserConfigurationException e){
            throw new AssertionError(e);
        }

        return;
    }


    /**
     * 入力ファイル種別を設定する。
     * @param type ファイル種別
     */
    public void setInType(ModelFileType type){
        if(type == null) throw new NullPointerException();
        this.inTypes = type;
        return;
    }

    /**
     * 入力ファイル種別を返す。
     * @return ファイル種別
     */
    public ModelFileType getInTypes(){
        return this.inTypes;
    }

    /**
     * 出力ファイル種別を設定する。
     * @param type ファイル種別
     */
    public void setOutType(ModelFileType type){
        if(type == null) throw new NullPointerException();
        this.outTypes = type;
        return;
    }

    /**
     * 出力ファイル種別を返す。
     * @return ファイル種別
     */
    public ModelFileType getOutTypes(){
        return this.outTypes;
    }

    /**
     * XML出力用改行文字列を設定する。
     * @param newline 改行文字
     */
    public void setNewline(String newline){
        this.newLine = newline;
        return;
    }

    /**
     * XML出力用改行文字列を返す。
     * @return 改行文字
     */
    public String getNewLine(){
        return this.newLine;
    }

    /**
     * ジェネレータ名を設定する。
     * @param generator ジェネレータ名。表示したくない場合はnull
     */
    public void setGenerator(String generator){
        this.generator = generator;
        return;
    }

    /**
     * ジェネレータ名を返す。
     * @return ジェネレータ名。非表示の場合はnullを返す。
     */
    public String getGenerator(){
        return this.generator;
    }

    /**
     * ファイル変換を行う。
     * @param is 入力ストリーム
     * @param os 出力ストリーム
     * @throws IOException 入力エラー
     * @throws MmdFormatException フォーマットエラー
     * @throws SAXException XMLエラー
     * @throws TogaXmlException XMLエラー
     * @throws IllegalPmdDataException 内部エラー
     */
    public void convert(InputStream is, OutputStream os)
            throws IOException,
                   MmdFormatException,
                   SAXException,
                   TogaXmlException,
                   IllegalPmdDataException {
        PmdModel model = readModel(is);
        writeModel(model, os);
        return;
    }

    /**
     * モデルファイルを読み込む。
     * @param is 入力ストリーム
     * @return モデルデータ
     * @throws IOException 入力エラー
     * @throws MmdFormatException フォーマットエラー
     * @throws SAXException XMLエラー
     * @throws TogaXmlException XMLエラー
     */
    public PmdModel readModel(InputStream is)
            throws IOException,
                   MmdFormatException,
                   SAXException,
                   TogaXmlException {
        PmdModel model = null;
        switch(this.inTypes){
        case PMD:
            model = pmdRead(is);
            break;
        case XML_101009:
            model = xmlRead(is);
            break;
        default:
            assert false;
            break;
        }
        return model;
    }

    /**
     * モデルファイルを出力する。
     * @param model モデルデータ
     * @param os 出力ストリーム
     * @throws IOException 出力エラー
     * @throws IllegalPmdDataException データの不備
     */
    public void writeModel(PmdModel model, OutputStream os)
            throws IOException,
                   IllegalPmdDataException {
        switch(this.outTypes){
        case PMD:
            pmdOut(model, os);
            break;
        case XML_101009:
            xmlOut(model, os);
            break;
        default:
            assert false;
            break;
        }
    }

    /**
     * PMDファイルからモデルデータを読み込む。
     * @param is 入力ストリーム
     * @return モデルデータ
     * @throws IOException 入力エラー
     * @throws MmdFormatException 不正なPMDファイルフォーマット
     */
    private PmdModel pmdRead(InputStream is)
            throws IOException, MmdFormatException{
        PmdLoader loader = new PmdLoader(is);
        PmdModel model = loader.load();
        return model;
    }

    /**
     * XMLファイルからモデルデータを読み込む。
     * @param is 入力ストリーム
     * @return モデルデータ
     * @throws IOException 入力エラー
     * @throws SAXException XML構文エラー
     * @throws TogaXmlException 不正なXMLデータ
     */
    private PmdModel xmlRead(InputStream is)
            throws IOException,
                   SAXException,
                   TogaXmlException {
        InputSource source = new InputSource(is);
        PmdModel result = xmlRead(source);
        return result;
    }

    /**
     * XMLファイルからモデルデータを読み込む。
     * @param source 入力ソース
     * @return モデルデータ
     * @throws IOException 入力エラー
     * @throws SAXException XML構文エラー
     * @throws TogaXmlException 不正なXMLデータ
     */
    private PmdModel xmlRead(InputSource source)
            throws IOException,
                   SAXException,
                   TogaXmlException {
        Xml101009Loader loader = new Xml101009Loader(this.builder);
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
    private void pmdOut(PmdModel model, OutputStream ostream)
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
    private void xmlOut(PmdModel model, OutputStream ostream)
            throws IOException, IllegalPmdDataException{
        Xml101009Exporter exporter = new Xml101009Exporter(ostream);
        exporter.setNewLine(this.newLine);
        exporter.setGenerator(this.generator);
        exporter.putPmdModel(model);
        exporter.close();
        return;
    }

}
