/*
 * xml input utility
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd2xml;

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
