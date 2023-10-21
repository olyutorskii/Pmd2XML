/*
 * MMD model xml file types.
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.xml;

/**
 * XMLファイルスキーマ種別。
 */
public enum XmlModelFileType {

    /**
     * XMLファイル。
     *
     * <p>読み込み時のスキーマ判別は自動。出力時のスキーマ種別は最新。
     */
    XML_AUTO,

    /**
     * スキーマ
     * http://mikutoga.sourceforge.jp/xml/xsd/pmdxml-101009.xsd
     * で定義されたXMLファイル。
     */
    XML_101009,

    /**
     * スキーマ
     * http://mikutoga.sourceforge.jp/xml/xsd/pmdxml-130128.xsd
     * で定義されたXMLファイル。
     */
    XML_130128,

}
