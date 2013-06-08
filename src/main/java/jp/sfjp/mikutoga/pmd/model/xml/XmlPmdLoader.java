/*
 * xml to pmd loader
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.xml;

import java.io.IOException;
import jp.sfjp.mikutoga.pmd.model.PmdModel;
import jp.sfjp.mikutoga.xml.TogaXmlException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * XMLモデルファイルを読み込むためのローダ。
 */
public class XmlPmdLoader {

    private static final String SAXFEATURES_NAMESPACES =
            "http://xml.org/sax/features/namespaces";

    private final XMLReader reader;


    /**
     * コンストラクタ。
     * <p>XMLリーダは名前空間をサポートしていなければならない。
     * @param reader XMLリーダ
     * @throws NullPointerException 引数がnull
     * @throws SAXException 機能不足のXMLリーダが渡された
     */
    public XmlPmdLoader(XMLReader reader)
            throws NullPointerException, SAXException {
        super();

        if(reader == null) throw new NullPointerException();
        if( ! reader.getFeature(SAXFEATURES_NAMESPACES) ){
            throw new SAXException();
        }

        this.reader = reader;

        return;
    }


    /**
     * XMLのパースを開始する。
     * @param source XML入力
     * @return モデルデータ
     * @throws SAXException 構文エラー
     * @throws IOException 入力エラー
     * @throws TogaXmlException 構文エラー
     */
    public PmdModel parse(InputSource source)
            throws SAXException, IOException, TogaXmlException{
        XmlHandler saxHandler = new XmlHandler();
        this.reader.setContentHandler(saxHandler);

        try{
            this.reader.parse(source);
        }catch(SAXException e){
            Throwable cause = e.getCause();
            if(cause instanceof TogaXmlException){
                throw (TogaXmlException) cause;
            }
            throw e;
        }

        return saxHandler.getPmdModel();
    }

}
