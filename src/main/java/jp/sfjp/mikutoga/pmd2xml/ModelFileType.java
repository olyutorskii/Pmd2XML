/*
 * MMD model file types.
 *
 * License : The MIT License
 * Copyright(c) 2012 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd2xml;

import jp.sfjp.mikutoga.pmd.model.xml.XmlModelFileType;

/**
 * モデルファイル種別。
 */
public enum ModelFileType {

    /**
     * 不明。
     */
    NONE,

    /**
     * MikuMikuDance ver7 前後で読み書きが可能なPMDファイル。
     */
    PMD,

    /**
     * XMLファイル。
     *
     * <p>読み込み時のスキーマ判別は自動。
     *
     * <p>書き込み時のスキーマは最新。
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

    ;


    /**
     * コンストラクタ。
     */
    private ModelFileType(){
        return;
    }

    /**
     * ファイル種別をXMLファイル種別に変換する。
     *
     * <p>未定義の場合はXML_AUTOを返す。
     *
     * @return XMLファイル種別
     */
    public XmlModelFileType toXmlType(){
        XmlModelFileType result;

        switch(this){
        case XML_101009:
            result = XmlModelFileType.XML_101009;
            break;
        case XML_130128:
            result = XmlModelFileType.XML_130128;
            break;
        case XML_AUTO:
            result = XmlModelFileType.XML_AUTO;
            break;
        default:
            result = XmlModelFileType.XML_AUTO;
            break;
        }

        return result;
    }

    /**
     * ファイル種別がXMLか判定する。
     *
     * @return XMLならtrue
     */
    public boolean isXml(){
        boolean result;

        switch(this){
        case XML_101009:
        case XML_130128:
        case XML_AUTO:
            result = true;
            break;
        default:
            result = false;
            break;
        }

        return result;
    }

    /**
     * ファイル種別がPMDか判定する。
     *
     * @return PMDならtrue
     */
    public boolean isPmd(){
        boolean result;

        switch(this){
        case PMD:
            result = true;
            break;
        default:
            result = false;
            break;
        }

        return result;
    }

}
