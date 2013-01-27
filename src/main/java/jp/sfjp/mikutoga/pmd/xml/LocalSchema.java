/*
 * local xml schema for PMD-XML
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.xml;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import jp.sourceforge.mikutoga.xml.XmlResourceResolver;
import org.xml.sax.SAXException;

/**
 * XML用各種スキーマのローカル参照解決基盤。
 */
public abstract class LocalSchema {

    /**
     * コンストラクタ。
     */
    protected LocalSchema(){
        super();
        return;
    }


    /**
     * XML Schema 用のスキーマファクトリを返す。
     * @return スキーマファクトリ
     */
    private static SchemaFactory newSchemaFactory(){
        SchemaFactory schemaFactory =
                SchemaFactory.newInstance(
                    XMLConstants.W3C_XML_SCHEMA_NS_URI
                );

//      schemaFactory.setFeature(name, value);
//      schemaFactory.setProperty(name, object);

        schemaFactory.setErrorHandler(BotherHandler.HANDLER);

        return schemaFactory;
    }

    /**
     * ローカルスキーマをロードする。
     * <p>任意のリゾルバを指定可能
     * @param resolver リゾルバ
     * @param lscs ローカルスキーマ情報の配列
     * @return スキーマ
     */
    public static Schema newSchema(XmlResourceResolver resolver,
                                    LocalSchema... lscs ){
        List<Source> sourceList = new LinkedList<Source>();
        for(LocalSchema lsc : lscs){
            if(lsc == null) continue;
            lsc.appendToUriMap(resolver);

            Source local = lsc.getLocalSchemaSource();
            if(local == null) continue;
            sourceList.add(local);
        }

        SchemaFactory schemaFactory = newSchemaFactory();
        schemaFactory.setResourceResolver(resolver);

        Source[] sources = new Source[sourceList.size()];
        sourceList.toArray(sources);

        Schema result;
        try{
            if(sources.length <= 0){
                result = schemaFactory.newSchema();
            }else{
                result = schemaFactory.newSchema(sources);
            }
        }catch(SAXException e){   // Build error
            assert false;
            throw new AssertionError(e);
        }

        return result;
    }

    /**
     * オリジナル版スキーマ定義のURIを返す。
     * <p>nullを返す場合は
     * スキーマの自動判定&ダウンロードが求められていると見なされる。
     * <p>このクラスの実装では常にnullを返す。
     * @return オリジナル版スキーマのURL。
     */
    public abstract URI getOriginalSchema();

    /**
     * ローカルリソース版スキーマ定義のURIを返す。
     * <p>nullを返す場合は
     * スキーマの自動判定&ダウンロードが求められていると見なされる。
     * <p>このクラスの実装では常にnullを返す。
     * @return ローカルリソース版スキーマのURL。
     */
    public abstract URI getLocalSchema();

    /**
     * スキーマのSourceを返す。
     * <p>ローカルスキーマのSourceを返す。
     * @return Source 見つからなければnull
     */
    public Source getLocalSchemaSource(){
        URI uri;

        uri = getLocalSchema();
        if(uri == null) return null;

        URL url;
        try{
            url = uri.toURL();
        }catch(MalformedURLException e){      // Build error
            assert false;
            throw new AssertionError(e);
        }

        InputStream is;
        try{
            is = url.openStream();
        }catch(IOException e){              // Build error
            assert false;
            throw new AssertionError(e);
        }
        is = new BufferedInputStream(is);

        Source result = new StreamSource(is);

        return result;
    }

    /**
     * ローカルで解決可能なリソース参照をリゾルバに追加登録する。
     * @param resolver リゾルバ
     */
    public void appendToUriMap(XmlResourceResolver resolver){
        URI original = getOriginalSchema();
        URI local    = getLocalSchema();

        if(original == null) return;

        resolver.putRedirected(original, local);

        return;
    }

}
