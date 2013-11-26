/*
 * xml resources for PMD-XML
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.xml;

import java.net.URI;
import java.net.URISyntaxException;
import jp.sfjp.mikutoga.xml.LocalXmlResource;

/**
 * 101009形式XML各種リソースの定義。
 */
public final class Schema101009 implements LocalXmlResource{

    /** 唯一のシングルトン。 */
    public static final Schema101009 SINGLETON;

    /** 名前空間。 */
    public static final String NS_PMDXML =
            "http://mikutoga.sourceforge.jp/xml/ns/pmdxml/101009";
    /** 公開スキーマ。 */
    public static final String SCHEMA_PMDXML =
            "http://mikutoga.sourceforge.jp/xml/xsd/pmdxml-101009.xsd";
    /** 版数。 */
    public static final String VER_PMDXML =
            "101009";
    /** 代替リソースの相対名。 */
    public static final String LOCAL_SCHEMA_PMDXML =
            "resources/pmdxml-101009.xsd";

    private static final URI URI_SCHEMA_PMDXML = URI.create(SCHEMA_PMDXML);
    private static final URI RES_SCHEMA_PMDXML;

    private static final Class<?> THISCLASS = Schema101009.class;

    static{
        try{
            RES_SCHEMA_PMDXML =
                    THISCLASS.getResource(LOCAL_SCHEMA_PMDXML).toURI();
        }catch(URISyntaxException e){
            throw new ExceptionInInitializerError(e);
        }

        SINGLETON = new Schema101009();
    }


    /**
     * コンストラクタ。
     */
    private Schema101009(){
        super();
        assert this.getClass() == THISCLASS;
        return;
    }


    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     * ※101009版。
     */
    @Override
    public URI getOriginalResource(){
        return URI_SCHEMA_PMDXML;
    }

    /**
     * {@inheritDoc}
     * ※101009版。
     * @return {@inheritDoc}
     */
    @Override
    public URI getLocalResource(){
        return RES_SCHEMA_PMDXML;
    }

}
