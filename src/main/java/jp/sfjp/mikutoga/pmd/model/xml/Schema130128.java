/*
 * xml resources for PMD-XML
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.xml;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 130128形式XML各種リソースの定義。
 */
public final class Schema130128{

    /** 名前空間。 */
    public static final String NS_PMDXML =
            "http://mikutoga.sourceforge.jp/xml/ns/pmdxml/130128";
    /** 公開スキーマ。 */
    public static final String SCHEMA_PMDXML =
            "http://mikutoga.sourceforge.jp/xml/xsd/pmdxml-130128.xsd";
    /** 版数。 */
    public static final String VER_PMDXML =
            "130128";
    /** 代替リソースの相対名。 */
    public static final String LOCAL_SCHEMA_PMDXML =
            "resources/pmdxml-130128.xsd";

    public static final URI URI_SCHEMA_PMDXML = URI.create(SCHEMA_PMDXML);
    public static final URI RES_SCHEMA_PMDXML;

    private static final Class<?> THISCLASS = Schema130128.class;

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
    private Schema130128(){
        assert false;
    }

}
