/*
 * xml resources for PMD-XML
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.xml;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 101009形式XML各種リソースの定義。
 */
public final class Schema101009{

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

    /** schema URI. */
    public static final URI URI_SCHEMA_PMDXML = URI.create(SCHEMA_PMDXML);
    /** local resource URI. */
    public static final URI RES_SCHEMA_PMDXML;

    private static final Class<?> THISCLASS = Schema101009.class;

    static{
        try{
            RES_SCHEMA_PMDXML =
                    THISCLASS.getResource(LOCAL_SCHEMA_PMDXML).toURI();
        }catch(URISyntaxException e){
            throw new ExceptionInInitializerError(e);
        }
    }


    /**
     * コンストラクタ。
     */
    private Schema101009(){
        assert false;
    }

}
