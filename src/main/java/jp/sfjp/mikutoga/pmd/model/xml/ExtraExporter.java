/*
 * extra xml exporter
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.xml;

import java.io.IOException;
import jp.sfjp.mikutoga.corelib.I18nText;
import jp.sfjp.mikutoga.math.MkPos3D;
import jp.sfjp.mikutoga.pmd.Rad3d;
import jp.sfjp.mikutoga.pmd.model.SerialNumbered;
import jp.sfjp.mikutoga.xml.ProxyXmlExporter;
import jp.sfjp.mikutoga.xml.XmlExporter;

/**
 * XML出力機構の共通部。
 */
class ExtraExporter extends ProxyXmlExporter {

    static final String PFX_SURFACEGROUP = "sg";
    static final String PFX_TOONFILE     = "tf";
    static final String PFX_VERTEX       = "vtx";
    static final String PFX_BONE         = "bn";
    static final String PFX_RIGID        = "rd";
    static final String PFX_RIGIDGROUP   = "rg";

    private static final char CAP_BASIC_LATIN = '\u007f';


    /**
     * コンストラクタ。
     * @param delegate 委譲先
     */
    ExtraExporter(XmlExporter delegate){
        super(delegate);
        return;
    }


    /**
     * 任意の文字列がBasicLatin文字のみから構成されるか判定する。
     * @param seq 文字列
     * @return null、長さ0もしくはBasicLatin文字のみから構成されるならtrue
     */
    static boolean hasOnlyBasicLatin(CharSequence seq){
        if(seq == null) return true;
        int length = seq.length();
        for(int pos = 0; pos < length; pos++){
            char ch = seq.charAt(pos);
            if(ch > CAP_BASIC_LATIN) return false;
        }
        return true;
    }


    /**
     * 文字参照によるエスケープを補佐するためのコメントを出力する。
     * @param seq 文字列
     * @throws IOException 出力エラー
     */
    void putUnescapedComment(CharSequence seq)
            throws IOException{
        if( ! isBasicLatinOnlyOut() ) return;
        if(hasOnlyBasicLatin(seq)) return;
        sp().putLineComment(seq);
        return;
    }

    /**
     * 多言語識別名属性のローカルな名前をコメント出力する。
     * @param name 多言語識別名
     * @throws IOException 出力エラー
     */
    void putLocalNameComment(I18nText name)
            throws IOException{
        String localName = name.getText();
        if(localName.isEmpty()){
            localName = "[NAMELESS]";
        }
        ind().putLineComment(localName);
        return;
    }

    /**
     * 多言語識別名属性のプライマリな名前を出力する。
     * @param attr 属性名
     * @param name 多言語識別名
     * @throws IOException 出力エラー
     */
    void putPrimaryNameAttr(PmdAttr attr, I18nText name)
            throws IOException{
        String attrName = attr.attr();
        String primaryName = name.getPrimaryText();
        putAttr(attrName, primaryName);
        return;
    }

    /**
     * 多言語化された各種識別名を出力する。
     * プライマリ名は出力対象外。
     * @param text 多言語文字列
     * @throws IOException 出力エラー
     */
    void putI18nName(I18nText text) throws IOException{
        for(String lang639 : text.lang639CodeList()){
            if(lang639.equals(I18nText.CODE639_PRIMARY)) continue;
            String name = text.getI18nText(lang639);
            ind().putOpenSTag(PmdTag.I18N_NAME.tag()).sp();
            putAttr(PmdAttr.LANG.attr(), lang639).sp();
            putAttr(PmdAttr.NAME.attr(), name).sp();
            putCloseEmpty();
            putUnescapedComment(name);
            ln();
        }
        return;
    }

    /**
     * 番号付けされたID(IDREF)属性を出力する。
     * @param attr 属性名
     * @param prefix IDプレフィクス
     * @param num 番号
     * @throws IOException 出力エラー
     */
    void putNumberedIdAttr(PmdAttr attr,
                             CharSequence prefix,
                             int num )
            throws IOException{
        String attrName = attr.attr();
        putRawText(attrName).putRawCh('=');
        putRawCh('"');
        putRawText(prefix).putXsdInt(num);
        putRawCh('"');
        return;
    }

    /**
     * 番号付けされたID(IDREF)属性を出力する。
     * @param attr 属性名
     * @param prefix IDプレフィクス
     * @param numbered 番号付けされたオブジェクト
     * @throws IOException 出力エラー
     */
    void putNumberedIdAttr(PmdAttr attr,
                             CharSequence prefix,
                             SerialNumbered numbered )
            throws IOException{
        putNumberedIdAttr(attr, prefix, numbered.getSerialNumber());
        return;
    }

    /**
     * 位置情報を出力する。
     * @param position 位置情報
     * @throws IOException 出力エラー
     */
    void putPosition(MkPos3D position)
            throws IOException{
        putOpenSTag("position").sp();

        putFloatAttr(PmdAttr.X.attr(),
                (float) position.getXpos()).sp();
        putFloatAttr(PmdAttr.Y.attr(),
                (float) position.getYpos()).sp();
        putFloatAttr(PmdAttr.Z.attr(),
                (float) position.getZpos()).sp();

        putCloseEmpty();

        return;
    }

    /**
     * 姿勢情報(ラジアン)を出力する。
     * @param rotation 姿勢情報
     * @throws IOException 出力エラー
     */
    void putRadRotation(Rad3d rotation)
            throws IOException{
        putOpenSTag("radRotation").sp();

        putFloatAttr(PmdAttr.X_RAD.attr(), rotation.getXRad()).sp();
        putFloatAttr(PmdAttr.Y_RAD.attr(), rotation.getYRad()).sp();
        putFloatAttr(PmdAttr.Z_RAD.attr(), rotation.getZRad()).sp();

        putCloseEmpty();

        return;
    }

}
