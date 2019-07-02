/*
 * pmd-xml exporter
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.xml;

import java.io.IOException;
import jp.sfjp.mikutoga.corelib.I18nText;
import jp.sfjp.mikutoga.pmd.model.PmdModel;
import jp.sfjp.mikutoga.xml.BasicXmlExporter;
import jp.sfjp.mikutoga.xml.SchemaUtil;

/**
 * PMDモーションデータをXMLへエクスポートする。
 */
public class PmdXmlExporter extends BasicXmlExporter{

    private static final String XML_VER = "1.0";
    private static final String XML_ENC = "UTF-8";
    private static final String XML_DECL =
              "<?xml version=\"" + XML_VER
            + "\" encoding=\"" + XML_ENC
            + "\" ?>";

    private static final String XSINS = "xsi";

    private static final String TOP_COMMENT =
              "\u0020\u0020"               + "MikuMikuDance\n"
            + "\u0020\u0020\u0020\u0020" + "model-data(*.pmd) on XML";

    /** 改行文字列 CR。 */
    private static final String CR = "\r";       // 0x0d
    /** 改行文字列 LF。 */
    private static final String LF = "\n";       // 0x0a
    /** 改行文字列 CRLF。 */
    private static final String CRLF = CR + LF;  // 0x0d, 0x0a


    private XmlModelFileType xmlType = XmlModelFileType.XML_130128;

    private String generator = null;

    private final ExporterMaterial materialExporter;
    private final ExporterBone     boneExporter;
    private final ExporterMorph    morphExporter;
    private final ExporterDynamics dynamicsExporter;
    private final ExporterShape    shapeExporter;

    private final ExtraExporter exp;


    /**
     * コンストラクタ。
     */
    public PmdXmlExporter(){
        super();

        this.materialExporter = new ExporterMaterial(this);
        this.boneExporter     = new ExporterBone(this);
        this.morphExporter    = new ExporterMorph(this);
        this.dynamicsExporter = new ExporterDynamics(this);
        this.shapeExporter    = new ExporterShape(this);

        this.exp = new ExtraExporter(this);

        this.boneExporter.setXmlFileType(this.xmlType);

        return;
    }


    /**
     * 出力XMLファイル種別を返す。
     * @return ファイル種別
     */
    public XmlModelFileType getXmlFileType(){
        return this.xmlType;
    }

    /**
     * 出力XMLファイル種別を設定する。
     * @param type ファイル種別
     */
    public void setXmlFileType(XmlModelFileType type){
        switch(type){
        case XML_101009:
        case XML_130128:
            this.xmlType = type;
            break;
        case XML_AUTO:
            this.xmlType = XmlModelFileType.XML_130128;
            break;
        default:
            throw new IllegalArgumentException();
        }

        assert this.xmlType == XmlModelFileType.XML_101009
            || this.xmlType == XmlModelFileType.XML_130128;

        this.boneExporter.setXmlFileType(this.xmlType);

        return;
    }

    /**
     * Generatorメタ情報を設定する。
     * @param generatorArg Generatorメタ情報。表示したくないときはnull
     */
    public void setGenerator(String generatorArg){
        this.generator = generatorArg;
        return;
    }

    /**
     * Generatorメタ情報を返す。
     * @return Generatorメタ情報。表示したくないときはnull
     */
    public String getGenerator(){
        return this.generator;
    }

    /**
     * PMDモデルデータをXML形式で出力する。
     * @param model PMDモデルデータ
     * @param xmlOut XML出力先
     * @throws IOException 出力エラー
     */
    public void putPmdXml(PmdModel model, Appendable xmlOut)
            throws IOException{
        setAppendable(xmlOut);

        try{
            putPmdXmlImpl(model);
        }finally{
            flush();
        }

        return;
    }

    /**
     * PMDモデルデータをXML形式で出力する。
     * @param model PMDモデルデータ
     * @throws IOException 出力エラー
     */
    private void putPmdXmlImpl(PmdModel model) throws IOException{
        putPmdRootOpen(model);

        putModelInfo(model);
        putMetaInfo();

        this.materialExporter.putMaterialList(model);
        this.materialExporter.putToonMap(model);

        this.boneExporter.putBoneList(model);
        this.boneExporter.putBoneGroupList(model);
        this.boneExporter.putIKChainList(model);

        this.morphExporter.putMorphList(model);

        this.dynamicsExporter.putRigidList(model);
        this.dynamicsExporter.putRigidGroupList(model);
        this.dynamicsExporter.putJointList(model);

        this.shapeExporter.putSurfaceGroupList(model);
        this.shapeExporter.putVertexList(model);

        ind().putETag(PmdTag.PMD_MODEL.tag()).ln(2);
        ind().putLineComment("EOF").ln();

        return;
    }

    /**
     * ルートタグ開始を出力する。
     * @param model モデル情報
     * @throws IOException 出力エラー
     */
    private void putPmdRootOpen(PmdModel model)
            throws IOException{
        ind().putRawText(XML_DECL).ln(2);
        ind().putBlockComment(TOP_COMMENT).ln(2);

        I18nText modelName = model.getModelName();
        ind();
        this.exp.putLocalNameComment(modelName);
        ln();

        ind().putOpenSTag(PmdTag.PMD_MODEL.tag()).ln();
        pushNest();

        putPmdRootAttr(model);

        popNest();
        putCloseSTag().ln(2);

        return;
    }

    /**
     * ルートタグ属性を出力する。
     * @param model モデル情報
     * @throws IOException 出力エラー
     */
    private void putPmdRootAttr(PmdModel model)
            throws IOException{
        String namespace;
        String schemaUrl;
        String schemaVer;

        if(this.xmlType == XmlModelFileType.XML_101009){
            namespace = Schema101009.NS_PMDXML;
            schemaUrl = Schema101009.SCHEMA_PMDXML;
            schemaVer = Schema101009.VER_PMDXML;
        }else if(this.xmlType == XmlModelFileType.XML_130128){
            namespace = Schema130128.NS_PMDXML;
            schemaUrl = Schema130128.SCHEMA_PMDXML;
            schemaVer = Schema130128.VER_PMDXML;
        }else{
            assert false;
            throw new AssertionError();
        }

        ind().putAttr("xmlns", namespace).ln();
        ind().putAttr("xmlns:" + XSINS, SchemaUtil.NS_XSD).ln();

        ind().putRawText(XSINS).putRawText(":schemaLocation=")
             .putRawCh('"');
        putRawText(namespace).ln();
        ind().sp(2).putRawText(schemaUrl)
             .putRawCh('"').ln();

        ind().putAttr(PmdAttr.SCHEMA_VERSION.attr(), schemaVer);
        ln(2);

        I18nText modelName = model.getModelName();
        ind();
        this.exp.putPrimaryNameAttr(PmdAttr.NAME, modelName);
        ln();

        return;
    }

    /**
     * モデル基本情報を出力する。
     * @param model モデル情報
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putModelInfo(PmdModel model)
            throws IOException{
        I18nText modelName = model.getModelName();
        this.exp.putI18nName(modelName);
        ln();

        I18nText description = model.getDescription();
        for(String lang639 : description.lang639CodeList()){
            String descText = description.getI18nText(lang639);
            putDescription(lang639, descText);
            ln();
        }

        return this;
    }

    /**
     * モデル詳細テキストを出力する。
     * @param lang639 言語コード
     * @param content 詳細内容
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putDescription(CharSequence lang639,
                                              CharSequence content)
            throws IOException{
        String text = content.toString();
        text = text.replace(CRLF, LF);
        text = text.replace(CR,   LF);

        ind().putOpenSTag(PmdTag.DESCRIPTION.tag());
        if( ! I18nText.CODE639_PRIMARY.equals(lang639) ){
            sp().putAttr(PmdAttr.LANG.attr(), lang639).sp();
        }
        putCloseSTag().ln();

        putBRedContent(text);

        ind().putETag(PmdTag.DESCRIPTION.tag()).ln();

        if( !    ExtraExporter.hasOnlyBasicLatin(text)
              && isBasicLatinOnlyOut() ){
            putBlockComment(text);
        }

        return this;
    }

    /**
     * break要素を含む要素内容を出力する。
     * 必要に応じてXML定義済み実体文字が割り振られた文字、
     * コントロールコード、および非BasicLatin文字がエスケープされる。
     * \nはbrタグに変換される。
     * @param content 内容
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putBRedContent(CharSequence content)
            throws IOException{
        int length = content.length();

        int startPos = 0;

        for(int idx = 0; idx < length; idx++){
            char ch = content.charAt(idx);
            if(ch == '\n'){
                CharSequence seq = content.subSequence(startPos, idx);
                putContent(seq).putRawText("<br/>").ln();
                startPos = idx + 1;
            }
        }

        if(startPos < length){
            CharSequence seq = content.subSequence(startPos, length);
            putContent(seq).ln();
        }

        return this;
    }

    /**
     * 各種メタ情報を出力する。
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putMetaInfo() throws IOException{
        ind().putSimpleSTag(PmdTag.LICENSE.tag()).ln();
        ind().putETag(PmdTag.LICENSE.tag()).ln(2);

        ind().putSimpleSTag(PmdTag.CREDITS.tag()).ln();
        ind().putETag(PmdTag.CREDITS.tag()).ln(2);

        String genName = getGenerator();
        if(genName != null){
            ind().putOpenSTag(PmdTag.META.tag()).sp();
            putAttr(PmdAttr.NAME.attr(), "generator").sp();
            putAttr(PmdAttr.CONTENT.attr(), genName).sp();
            putCloseEmpty().ln();
        }

        ind().putOpenSTag(PmdTag.META.tag()).sp();
        putAttr(PmdAttr.NAME.attr(), "siteURL").sp();
        putAttr(PmdAttr.CONTENT.attr(), "").sp();
        putCloseEmpty().ln();

        ind().putOpenSTag(PmdTag.META.tag()).sp();
        putAttr(PmdAttr.NAME.attr(), "imageURL").sp();
        putAttr(PmdAttr.CONTENT.attr(), "").sp();
        putCloseEmpty().ln(2);

        return this;
    }

}
