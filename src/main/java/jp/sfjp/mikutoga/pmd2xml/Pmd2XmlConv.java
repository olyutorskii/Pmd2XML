/*
 * pmd 2 xml converter
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd2xml;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import jp.sfjp.mikutoga.bin.parser.MmdFormatException;
import jp.sfjp.mikutoga.pmd.IllegalPmdDataException;
import jp.sfjp.mikutoga.pmd.model.PmdModel;
import jp.sfjp.mikutoga.pmd.model.binio.PmdExporter;
import jp.sfjp.mikutoga.pmd.model.binio.PmdLoader;
import jp.sfjp.mikutoga.pmd.model.xml.PmdXmlExporter;
import jp.sfjp.mikutoga.pmd.model.xml.Schema101009;
import jp.sfjp.mikutoga.pmd.model.xml.Schema130128;
import jp.sfjp.mikutoga.pmd.model.xml.XmlModelFileType;
import jp.sfjp.mikutoga.pmd.model.xml.XmlPmdLoader;
import jp.sfjp.mikutoga.xml.BotherHandler;
import jp.sfjp.mikutoga.xml.LocalXmlResource;
import jp.sfjp.mikutoga.xml.SchemaUtil;
import jp.sfjp.mikutoga.xml.TogaXmlException;
import jp.sfjp.mikutoga.xml.XmlResourceResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * PMD-XML間コンバータ本体。
 */
public class Pmd2XmlConv {

    /** デフォルトエンコーディング。 */
    private static final Charset CS_UTF8 = Charset.forName("UTF-8");


    private ModelFileType inTypes  = ModelFileType.NONE;
    private ModelFileType outTypes = ModelFileType.NONE;
    private String newLine = "\r\n";
    private String generator = null;


    /**
     * コンストラクタ。
     */
    public Pmd2XmlConv(){
        super();
        return;
    }


    /**
     * ドキュメントビルダファクトリを初期化する。
     * @param builderFactory ドキュメントビルダファクトリ
     */
    private static void initBuilderFactory(
            DocumentBuilderFactory builderFactory ){
        builderFactory.setCoalescing(true);
        builderFactory.setExpandEntityReferences(true);
        builderFactory.setIgnoringComments(true);
        builderFactory.setIgnoringElementContentWhitespace(false);
        builderFactory.setNamespaceAware(true);
        builderFactory.setValidating(false);
        builderFactory.setXIncludeAware(false);

//      builderFactory.setFeature(name, value);
//      builderFactory.setAttribute(name, value);

        return;
    }

    /**
     * DOMビルダ生成。
     * @return DOMビルダ
     */
    private DocumentBuilder buildBuilder(){
        XmlResourceResolver resolver = new XmlResourceResolver();

        LocalXmlResource[] schemaArray;
        switch(this.inTypes){
        case XML_101009:
            schemaArray = new LocalXmlResource[]{
                Schema101009.SINGLETON,
            };
            break;
        case XML_130128:
            schemaArray = new LocalXmlResource[]{
                Schema130128.SINGLETON,
            };
            break;
        case XML_AUTO:
            schemaArray = new LocalXmlResource[]{
                Schema101009.SINGLETON,
                Schema130128.SINGLETON,
            };
            break;
        default:
            throw new IllegalStateException();
        }

        Schema schema = SchemaUtil.newSchema(resolver, schemaArray);

        DocumentBuilderFactory builderFactory =
                DocumentBuilderFactory.newInstance();
        initBuilderFactory(builderFactory);
        builderFactory.setSchema(schema);

        DocumentBuilder result;
        try{
            result = builderFactory.newDocumentBuilder();
        }catch(ParserConfigurationException e){
            assert false;
            throw new AssertionError(e);
        }
        result.setEntityResolver(resolver);
        result.setErrorHandler(BotherHandler.HANDLER);

        return result;
    }


    /**
     * 入力ファイル種別を設定する。
     * @param type ファイル種別
     * @throws NullPointerException 引数がnull
     * @throws IllegalArgumentException 具体的な種別を渡さなかった
     */
    public void setInType(ModelFileType type)
            throws NullPointerException, IllegalArgumentException {
        if(type == null) throw new NullPointerException();
        if(type == ModelFileType.NONE) throw new IllegalArgumentException();
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
     * @throws NullPointerException 引数がnull
     * @throws IllegalArgumentException 具体的な種別を渡さなかった
     */
    public void setOutType(ModelFileType type)
            throws NullPointerException, IllegalArgumentException {
        if(type == null) throw new NullPointerException();
        if(type == ModelFileType.NONE) throw new IllegalArgumentException();
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
    public String getNewline(){
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

        if(this.inTypes.isPmd()){
            model = pmdRead(is);
        }else if(this.inTypes.isXml()){
            model = xmlRead(is);
        }else{
            throw new IllegalStateException();
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
        if(this.outTypes.isPmd()){
            pmdOut(model, os);
        }else if(this.outTypes.isXml()){
            xmlOut(model, os);
        }else{
            throw new IllegalStateException();
        }

        return;
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
        PmdLoader loader = new PmdLoader();
        PmdModel model = loader.load(is);
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
        XMLReader reader = XmlInputUtil.buildReader(this.inTypes);
        XmlPmdLoader loader = new XmlPmdLoader(reader);
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
        PmdXmlExporter exporter = new PmdXmlExporter();

        XmlModelFileType xmlType = this.outTypes.toXmlType();
        exporter.setXmlFileType(xmlType);
        exporter.setNewLine(this.newLine);
        exporter.setGenerator(this.generator);

        Writer writer;
        writer = new OutputStreamWriter(ostream, CS_UTF8);
        writer = new BufferedWriter(writer);

        exporter.putPmdXml(model, writer);

        exporter.close();

        return;
    }

}
