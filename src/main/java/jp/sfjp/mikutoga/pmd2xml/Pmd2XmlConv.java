/*
 * pmd 2 xml converter
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd2xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import jp.sfjp.mikutoga.pmd.binio.PmdExporter;
import jp.sfjp.mikutoga.pmd.binio.PmdLoader;
import jp.sfjp.mikutoga.pmd.model.PmdModel;
import jp.sfjp.mikutoga.pmd.xml.BotherHandler;
import jp.sfjp.mikutoga.pmd.xml.LocalSchema;
import jp.sfjp.mikutoga.pmd.xml.Schema101009;
import jp.sfjp.mikutoga.pmd.xml.Schema130128;
import jp.sfjp.mikutoga.pmd.xml.XmlExporter;
import jp.sfjp.mikutoga.pmd.xml.XmlLoader;
import jp.sfjp.mikutoga.pmd.xml.XmlModelFileType;
import jp.sourceforge.mikutoga.parser.MmdFormatException;
import jp.sourceforge.mikutoga.pmd.IllegalPmdDataException;
import jp.sourceforge.mikutoga.xml.TogaXmlException;
import jp.sourceforge.mikutoga.xml.XmlResourceResolver;
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

        Schema schema;

        switch(this.inTypes){
        case XML_101009:
            schema = LocalSchema.newSchema(resolver, new Schema101009());
            break;
        case XML_130128:
            schema = LocalSchema.newSchema(resolver, new Schema130128());
            break;
        case XML_AUTO:
            schema = LocalSchema.newSchema(resolver,
                    new Schema101009(), new Schema130128());
            break;
        default:
            throw new IllegalStateException();
        }

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
     * @throws IllegalArgumentException 具体的な種別を渡さなかった
     */
    public void setInType(ModelFileType type){
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
     * @throws IllegalArgumentException 具体的な種別を渡さなかった
     */
    public void setOutType(ModelFileType type){
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
     * @throws IllegalStateException ファイル種別がまた指定されていない
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
     * @throws IllegalStateException ファイル種別がまた指定されていない
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
        DocumentBuilder builder = buildBuilder();
        XmlLoader loader = new XmlLoader();
        PmdModel model = loader.parse(builder, source);
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
        XmlExporter exporter = new XmlExporter(ostream);

        XmlModelFileType xmlType = this.outTypes.toXmlType();
        exporter.setXmlFileType(xmlType);
        exporter.setNewLine(this.newLine);
        exporter.setGenerator(this.generator);

        exporter.putPmdModel(model);

        exporter.close();

        return;
    }

}
