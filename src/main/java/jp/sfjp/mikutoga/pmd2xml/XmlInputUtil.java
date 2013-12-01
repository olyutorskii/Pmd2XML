/*
 * xml input utility
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd2xml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import jp.sfjp.mikutoga.pmd.model.xml.Schema101009;
import jp.sfjp.mikutoga.pmd.model.xml.Schema130128;
import jp.sfjp.mikutoga.xml.BotherHandler;
import jp.sfjp.mikutoga.xml.LocalXmlResource;
import jp.sfjp.mikutoga.xml.SchemaUtil;
import jp.sfjp.mikutoga.xml.XmlResourceResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * XML入力に関する各種ユーティリティ。
 */
final class XmlInputUtil {

    /**
     * 隠しコンストラクタ。
     */
    private XmlInputUtil(){
        assert false;
        throw new AssertionError();
    }


    /**
     * 実在ファイルからXML入力ソースを得る。
     * @param file 実在ファイル
     * @return XML入力ソース
     */
    static InputSource fileToSource(File file){
        assert file.exists();

        URI uri = file.toURI();

        URL url;
        try{
            url = uri.toURL();
        }catch(MalformedURLException e){
            // 実在File由来のURLでは起こりえない
            assert false;
            throw new AssertionError(e);
        }

        String systemId = url.toString();

        InputSource source = new InputSource(systemId);

        return source;
    }

    /**
     * InputSourceからInputStreamを得る。
     * <p>入力ソースには、少なくともバイトストリームか
     * URL文字列(SystemId)のいずれかが設定されていなければならない。
     * @param source 入力ソース
     * @return 入力バイトストリーム
     * @throws IllegalArgumentException 入力ソースの設定が足りない。
     * @throws IOException 入力ソースにアクセス不能。
     */
    static InputStream openInputSource(InputSource source)
            throws IllegalArgumentException, IOException{
        InputStream is;

        is = source.getByteStream();

        if(is == null){
            String systemId = source.getSystemId();
            if(systemId == null) throw new IllegalArgumentException();

            URL url = new URL(systemId);
            is = url.openStream();
        }

        is = new BufferedInputStream(is);

        return is;
    }

    /**
     * SAXパーサファクトリを生成する。
     * <ul>
     * <li>XML名前空間機能は有効になる。
     * <li>DTDによる形式検証は無効となる。
     * <li>XIncludeによる差し込み機能は無効となる。
     * </ul>
     * @param schema スキーマ
     * @return ファクトリ
     */
    private static SAXParserFactory buildFactory(Schema schema){
        SAXParserFactory factory = SAXParserFactory.newInstance();

        factory.setNamespaceAware(true);
        factory.setValidating(false);
        factory.setXIncludeAware(false);
//      factory.setFeature(name, value);

        factory.setSchema(schema);

        return factory;
    }

    /**
     * SAXパーサを生成する。
     * @param schema スキーマ
     * @return SAXパーサ
     */
    private static SAXParser buildParser(Schema schema){
        SAXParserFactory factory = buildFactory(schema);

        SAXParser parser;
        try{
            parser = factory.newSAXParser();
        }catch(ParserConfigurationException e){
            assert false;
            throw new AssertionError(e);
        }catch(SAXException e){
            assert false;
            throw new AssertionError(e);
        }

//      parser.setProperty(name, value);

        return parser;
    }

    /**
     * スキーマを生成する。
     * @param resolver リゾルバ
     * @param xmlInType 入力XML種別
     * @return スキーマ
     */
    private static Schema builsSchema(XmlResourceResolver resolver,
                                        ModelFileType xmlInType ){
        LocalXmlResource[] schemaArray;
        switch(xmlInType){
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

        return schema;
    }

    /**
     * XMLリーダを生成する。
     * <p>エラーハンドラには{@link BotherHandler}が指定される。
     * @param xmlInType 入力XML種別
     * @return XMLリーダ
     */
    static XMLReader buildReader(ModelFileType xmlInType){
        XmlResourceResolver resolver = new XmlResourceResolver();

        Schema schema = builsSchema(resolver, xmlInType);

        SAXParser parser = buildParser(schema);

        XMLReader reader;
        try{
            reader = parser.getXMLReader();
        }catch(SAXException e){
            assert false;
            throw new AssertionError(e);
        }

        reader.setEntityResolver(resolver);
        reader.setErrorHandler(BotherHandler.HANDLER);

        return reader;
    }

}
