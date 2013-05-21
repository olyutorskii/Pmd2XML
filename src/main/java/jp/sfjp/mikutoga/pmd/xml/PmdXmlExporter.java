/*
 * pmd-xml exporter
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.xml;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jp.sfjp.mikutoga.corelib.I18nText;
import jp.sfjp.mikutoga.math.MkPos2D;
import jp.sfjp.mikutoga.math.MkPos3D;
import jp.sfjp.mikutoga.math.MkVec3D;
import jp.sfjp.mikutoga.pmd.BoneType;
import jp.sfjp.mikutoga.pmd.Deg3d;
import jp.sfjp.mikutoga.pmd.MorphType;
import jp.sfjp.mikutoga.pmd.Rad3d;
import jp.sfjp.mikutoga.pmd.RigidShapeType;
import jp.sfjp.mikutoga.pmd.TripletRange;
import jp.sfjp.mikutoga.pmd.model.BoneGroup;
import jp.sfjp.mikutoga.pmd.model.BoneInfo;
import jp.sfjp.mikutoga.pmd.model.DynamicsInfo;
import jp.sfjp.mikutoga.pmd.model.IKChain;
import jp.sfjp.mikutoga.pmd.model.JointInfo;
import jp.sfjp.mikutoga.pmd.model.Material;
import jp.sfjp.mikutoga.pmd.model.MorphPart;
import jp.sfjp.mikutoga.pmd.model.MorphVertex;
import jp.sfjp.mikutoga.pmd.model.PmdModel;
import jp.sfjp.mikutoga.pmd.model.RigidGroup;
import jp.sfjp.mikutoga.pmd.model.RigidInfo;
import jp.sfjp.mikutoga.pmd.model.RigidShape;
import jp.sfjp.mikutoga.pmd.model.SerialNumbered;
import jp.sfjp.mikutoga.pmd.model.ShadeInfo;
import jp.sfjp.mikutoga.pmd.model.Surface;
import jp.sfjp.mikutoga.pmd.model.ToonMap;
import jp.sfjp.mikutoga.pmd.model.Vertex;
import jp.sourceforge.mikutoga.xml.BasicXmlExporter;
import jp.sourceforge.mikutoga.xml.XmlResourceResolver;

/**
 * 101009形式XMLでPMDモデルデータを出力する。
 */
public class PmdXmlExporter extends BasicXmlExporter{

    private static final String TOP_COMMENT =
              "  MikuMikuDance\n"
            + "    model-data(*.pmd) on XML";

    /** 改行文字列 CR。 */
    private static final String CR = "\r";       // 0x0d
    /** 改行文字列 LF。 */
    private static final String LF = "\n";       // 0x0a
    /** 改行文字列 CRLF。 */
    private static final String CRLF = CR + LF;  // 0x0d, 0x0a

    private static final String PFX_SURFACEGROUP = "sg";
    private static final String PFX_TOONFILE = "tf";
    private static final String PFX_VERTEX = "vtx";
    private static final String PFX_BONE = "bn";
    private static final String PFX_RIGID = "rd";
    private static final String PFX_RIGIDGROUP = "rg";

    private static final String BONETYPE_COMMENT =
          "Bone types:\n"
        + "[0 : ROTATE      : Rotate       : 回転           :]\n"
        + "[1 : ROTMOV      : Rotate/Move  : 回転/移動      :]\n"
        + "[2 : IK          : IK           : IK             :]\n"
        + "[3 : UNKNOWN     : Unknown      : 不明           :]\n"
        + "[4 : UNDERIK     : Under IK     : IK影響下(回転) :]\n"
        + "[5 : UNDERROT    : Under rotate : 回転影響下     :]\n"
        + "[6 : IKCONNECTED : IK connected : IK接続先       :]\n"
        + "[7 : HIDDEN      : Hidden       : 非表示         :]\n"
        + "[8 : TWIST       : Twist        : 捩り           :]\n"
        + "[9 : LINKEDROT   : Linked Rotate: 回転連動       :]\n";

    private static final String MORPHTYPE_COMMENT =
          "Morph types:\n"
        + "[1 : EYEBROW : まゆ   ]\n"
        + "[2 : EYE     : 目     ]\n"
        + "[3 : LIP     : リップ ]\n"
        + "[4 : EXTRA   : その他 ]\n";

    private static final String RIGIDBEHAVIOR_COMMENT =
          "Rigid behavior types:\n"
        + "[0 : FOLLOWBONE    : ボーン追従       ]\n"
        + "[1 : ONLYDYNAMICS  : 物理演算         ]\n"
        + "[2 : BONEDDYNAMICS : ボーン位置合わせ ]\n";

    private static final Locale DEF_LOCALE = Locale.JAPANESE;


    private String generator = null;

    private XmlModelFileType xmlType = XmlModelFileType.XML_101009;


    /**
     * コンストラクタ。
     */
    public PmdXmlExporter(){
        super();
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
     * 任意の文字列がBasicLatin文字のみから構成されるか判定する。
     * @param seq 文字列
     * @return null、長さ0もしくはBasicLatin文字のみから構成されるならtrue
     */
    public static boolean hasOnlyBasicLatin(CharSequence seq){
        if(seq == null) return true;
        int length = seq.length();
        for(int pos = 0; pos < length; pos++){
            char ch = seq.charAt(pos);
            if(ch > 0x007f) return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     * @throws IOException {@inheritDoc}
     */
    @Override
    public PmdXmlExporter ind() throws IOException{
        super.ind();
        return this;
    }

    /**
     * 文字参照によるエスケープを補佐するためのコメントを出力する。
     * @param seq 文字列
     * @return this本体
     * @throws IOException 出力エラー
     */
    protected PmdXmlExporter putUnescapedComment(CharSequence seq)
            throws IOException{
        if( ! isBasicLatinOnlyOut() ) return this;
        if(hasOnlyBasicLatin(seq)) return this;
        sp().putLineComment(seq);
        return this;
    }

    /**
     * 多言語化された各種識別名を出力する。
     * プライマリ名は出力対象外。
     * @param text 多言語文字列
     * @return this本体
     * @throws IOException 出力エラー
     */
    protected PmdXmlExporter putI18nName(I18nText text) throws IOException{
        for(String lang639 : text.lang639CodeList()){
            if(lang639.equals(I18nText.CODE639_PRIMARY)) continue;
            String name = text.getI18nText(lang639);
            ind().putRawText("<i18nName ");
            putAttr("lang", lang639).sp();
            putAttr("name", name);
            putRawText(" />");
            putUnescapedComment(name);
            ln();
        }
        return this;
    }

    /**
     * 番号付けされたID(IDREF)属性を出力する。
     * @param attrName 属性名
     * @param prefix IDプレフィクス
     * @param num 番号
     * @return this本体
     * @throws IOException 出力エラー
     */
    protected PmdXmlExporter putNumberedIdAttr(CharSequence attrName,
                                                 CharSequence prefix,
                                                 int num )
            throws IOException{
        putRawText(attrName).putRawText("=\"");
        putRawText(prefix).putXsdInt(num);
        putRawCh('"');
        return this;
    }

    /**
     * 番号付けされたID(IDREF)属性を出力する。
     * @param attrName 属性名
     * @param prefix IDプレフィクス
     * @param numbered 番号付けされたオブジェクト
     * @return this本体
     * @throws IOException 出力エラー
     */
    protected PmdXmlExporter putNumberedIdAttr(CharSequence attrName,
                                                 CharSequence prefix,
                                                 SerialNumbered numbered )
            throws IOException{
        putNumberedIdAttr(attrName, prefix, numbered.getSerialNumber());
        return this;
    }

    /**
     * 位置情報を出力する。
     * @param position 位置情報
     * @return this本体
     * @throws IOException 出力エラー
     */
    protected PmdXmlExporter putPosition(MkPos3D position)
            throws IOException{
        putRawText("<position ");
        putFloatAttr("x", (float) position.getXpos()).sp();
        putFloatAttr("y", (float) position.getYpos()).sp();
        putFloatAttr("z", (float) position.getZpos()).sp();
        putRawText("/>");
        return this;
    }

    /**
     * 姿勢情報(ラジアン)を出力する。
     * @param rotation 姿勢情報
     * @return this本体
     * @throws IOException 出力エラー
     */
    protected PmdXmlExporter putRadRotation(Rad3d rotation)
            throws IOException{
        putRawText("<radRotation ");
        putFloatAttr("xRad", rotation.getXRad()).sp();
        putFloatAttr("yRad", rotation.getYRad()).sp();
        putFloatAttr("zRad", rotation.getZRad()).sp();
        putRawText("/>");
        return this;
    }

    /**
     * 多言語識別名属性のローカルな名前をコメント出力する。
     * @param name 多言語識別名
     * @return this本体
     * @throws IOException 出力エラー
     */
    protected PmdXmlExporter putLocalNameComment(I18nText name)
            throws IOException{
        String localName = name.getText();
        if(localName.isEmpty()){
            localName = "[NAMELESS]";
        }
        ind().putLineComment(localName);
        return this;
    }

    /**
     * 多言語識別名属性のプライマリな名前を出力する。
     * @param attrName 属性名
     * @param name 多言語識別名
     * @return this本体
     * @throws IOException 出力エラー
     */
    protected PmdXmlExporter putPrimaryNameAttr(CharSequence attrName,
                                                   I18nText name)
            throws IOException{
        String primaryName = name.getPrimaryText();
        putAttr(attrName, primaryName);
        return this;
    }

    /**
     * PMDモデルデータをXML形式で出力する。
     * @param model PMDモデルデータ
     * @param xmlOut XML出力先
     * @throws IOException 出力エラー
     */
    public void putPmdModel(PmdModel model, Appendable xmlOut)
            throws IOException{
        setAppendable(xmlOut);

        ind().putRawText("<?xml")
                .sp().putAttr("version","1.0")
                .sp().putAttr("encoding","UTF-8")
                .sp().putRawText("?>").ln(2);

        ind().putBlockComment(TOP_COMMENT).ln(2);

        I18nText modelName = model.getModelName();
        ind().putLocalNameComment(modelName).ln();
        ind().putRawText("<pmdModel").ln();
        pushNest();

        String defns;
        String xsduri;
        String version;
        if(this.xmlType == XmlModelFileType.XML_101009){
            defns   = Schema101009.NS_PMDXML;
            xsduri  = Schema101009.SCHEMA_PMDXML;
            version = Schema101009.VER_PMDXML;
        }else if(this.xmlType == XmlModelFileType.XML_130128){
            defns   = Schema130128.NS_PMDXML;
            xsduri  = Schema130128.SCHEMA_PMDXML;
            version = Schema130128.VER_PMDXML;
        }else{
            assert false;
            throw new AssertionError();
        }

        ind().putAttr("xmlns", defns).ln();

        ind().putAttr("xmlns:xsi", XmlResourceResolver.NS_XSD).ln();

        ind().putRawText("xsi:schemaLocation").putRawText("=\"");
        putRawText(defns).ln();
        pushNest();
        ind().putRawText(xsduri).putRawCh('"').ln();
        popNest();

        ind().putAttr("schemaVersion", version).ln(2);
        ind().putPrimaryNameAttr("name", modelName).ln();

        popNest();
        putRawText(">").ln(2);

        putModelInfo(model).flush();
        putMetaInfo(model).flush();
        putMaterialList(model).flush();
        putToonMap(model).flush();
        putBoneList(model).flush();
        putBoneGroupList(model).flush();
        putIKChainList(model).flush();
        putMorphList(model).flush();
        putRigidList(model).flush();
        putRigidGroupList(model).flush();
        putJointList(model).flush();
        putSurfaceGroupList(model).flush();
        putVertexList(model).flush();

        ind().putRawText("</pmdModel>").ln(2);
        ind().putRawText("<!-- EOF -->").ln();

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
        putI18nName(modelName);
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

        ind().putRawText("<description");
        if( ! I18nText.CODE639_PRIMARY.equals(lang639) ){
            sp().putAttr("lang", lang639).sp();
        }
        putRawText(">").ln();

        putBRedContent(text);

        ind().putRawText("</description>").ln();

        if( ! hasOnlyBasicLatin(text) && isBasicLatinOnlyOut() ){
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
    protected BasicXmlExporter putBRedContent(CharSequence content)
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
     * @param model モデルデータ
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putMetaInfo(PmdModel model) throws IOException{
        ind().putRawText("<license>").ln();
        ind().putRawText("</license>").ln(2);

        ind().putRawText("<credits>").ln();
        ind().putRawText("</credits>").ln(2);

        if(this.generator != null){
            ind().putRawText("<meta ");
            putAttr("name", "generator").sp()
                                        .putAttr("content", this.generator);
            putRawText(" />").ln();
        }

        ind().putRawText("<meta ");
        putAttr("name", "siteURL").sp().putAttr("content", "");
        putRawText(" />").ln();
        ind().putRawText("<meta ");
        putAttr("name", "imageURL").sp().putAttr("content", "");
        putRawText(" />").ln(2);

        return this;
    }

    /**
     * マテリアル素材一覧を出力する。
     * @param model モデルデータ
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putMaterialList(PmdModel model)
            throws IOException{
        ind().putRawText("<materialList>").ln();

        pushNest();
        int ct = 0;
        boolean dumped = false;
        List<Material> materialList = model.getMaterialList();
        for(Material material : materialList){
            if( ! dumped ) ln();
            putMaterial(material, ct++);
            dumped = true;
        }
        popNest();

        ind().putRawText("</materialList>").ln(2);

        return this;
    }

    /**
     * マテリアル素材情報を出力する。
     * @param material マテリアル素材
     * @param no マテリアル通し番号
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putMaterial(Material material, int no)
            throws IOException{
        String bool;
        if(material.getEdgeAppearance()) bool = "true";
        else                             bool = "false";
        I18nText name = material.getMaterialName();
        String primary = name.getPrimaryText();
        String local = name.getText();

        if(local != null && local.length() > 0){
            ind().putLineComment(local).ln();
        }
        ind().putRawText("<material ");
        if(primary != null && primary.length() > 0){
            putAttr("name", primary).sp();
        }

        putAttr("showEdge", bool);
        sp();
        putNumberedIdAttr("surfaceGroupIdRef", PFX_SURFACEGROUP, no);
        sp().putRawCh('>').ln();
        pushNest();

        putI18nName(name);

        float[] rgba = new float[4];

        Color diffuse = material.getDiffuseColor();
        diffuse.getRGBComponents(rgba);
        ind().putRawText("<diffuse ");
        putFloatAttr("r", rgba[0]).sp();
        putFloatAttr("g", rgba[1]).sp();
        putFloatAttr("b", rgba[2]).sp();
        putFloatAttr("alpha", rgba[3]).sp();
        putRawText("/>").ln();

        Color specular = material.getSpecularColor();
        specular.getRGBComponents(rgba);
        float shininess = material.getShininess();
        ind().putRawText("<specular ");
        putFloatAttr("r", rgba[0]).sp();
        putFloatAttr("g", rgba[1]).sp();
        putFloatAttr("b", rgba[2]).sp();
        putFloatAttr("shininess", shininess).sp();
        putRawText("/>").ln();

        Color ambient = material.getAmbientColor();
        ambient.getRGBComponents(rgba);
        ind().putRawText("<ambient ");
        putFloatAttr("r", rgba[0]).sp();
        putFloatAttr("g", rgba[1]).sp();
        putFloatAttr("b", rgba[2]).sp();
        putRawText("/>").ln();

        ShadeInfo shade = material.getShadeInfo();
        String textureFileName = shade.getTextureFileName();
        String spheremapFileName = shade.getSpheremapFileName();

        if(shade.isValidToonIndex()){
            ind().putRawText("<toon ");
            int toonIdx = shade.getToonIndex();
            putNumberedIdAttr("toonFileIdRef", PFX_TOONFILE, toonIdx);
            putRawText(" />");
            String toonFileName = shade.getToonFileName();
            if(toonFileName != null && toonFileName.length() > 0){
                sp().putLineComment(toonFileName);
            }
            ln();
        }

        if(textureFileName != null && textureFileName.length() > 0){
            ind().putRawText("<textureFile ");
            putAttr("winFileName", textureFileName);
            putRawText(" />").ln();
        }

        if(spheremapFileName != null && spheremapFileName.length() > 0){
            ind().putRawText("<spheremapFile ");
            putAttr("winFileName", spheremapFileName);
            putRawText(" />").ln();
        }

        popNest();
        ind().putRawText("</material>").ln(2);

        return this;
    }

    /**
     * トゥーンファイルマッピング情報を出力する。
     * @param model モデルデータ
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putToonMap(PmdModel model)
            throws IOException{
        ind().putRawText("<toonMap>").ln();
        pushNest();

        ToonMap map = model.getToonMap();
        for(int index = 0; index <= 9; index++){
            ind().putToon(map, index).ln();
        }

        popNest();
        ind().putRawText("</toonMap>").ln(2);
        return this;
    }

    /**
     * 個別のトゥーンファイル情報を出力する。
     * @param map トゥーンマップ
     * @param index インデックス値
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putToon(ToonMap map, int index)
            throws IOException{
        putRawText("<toonDef ");
        putNumberedIdAttr("toonFileId", PFX_TOONFILE, index).sp();
        putIntAttr("index", index).sp();
        String toonFile = map.getIndexedToon(index);
        putAttr("winFileName", toonFile);
        putRawText(" />");
        putUnescapedComment(toonFile);
        return this;
    }

    /**
     * サーフェイスグループリストを出力する。
     * @param model モデルデータ
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putSurfaceGroupList(PmdModel model)
            throws IOException{
        ind().putRawText("<surfaceGroupList>").ln();

        pushNest();
        int ct = 0;
        boolean dumped = false;
        List<Material> materialList = model.getMaterialList();
        for(Material material : materialList){
            List<Surface> surfaceList = material.getSurfaceList();
            if( ! dumped ) ln();
            putSurfaceList(surfaceList, ct++);
            dumped = true;
        }
        popNest();

        ind().putRawText("</surfaceGroupList>").ln(2);

        return this;
    }

    /**
     * 個別のサーフェイスグループを出力する。
     * @param surfaceList サーフェイスのリスト
     * @param index グループインデックス
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putSurfaceList(List<Surface> surfaceList,
                                              int index)
            throws IOException{
        ind().putRawText("<surfaceGroup ");
        putNumberedIdAttr("surfaceGroupId", PFX_SURFACEGROUP, index);
        sp().putRawText(">").ln();
        pushNest();

        for(Surface surface : surfaceList){
            putSurface(surface);
        }

        popNest();
        ind().putRawText("</surfaceGroup>").ln(2);

        return this;
    }

    /**
     * 個別のサーフェイスを出力する。
     * @param surface サーフェイス
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putSurface(Surface surface)
            throws IOException{
        ind().putRawText("<surface ");

        Vertex vertex1 = surface.getVertex1();
        Vertex vertex2 = surface.getVertex2();
        Vertex vertex3 = surface.getVertex3();

        putNumberedIdAttr("vtxIdRef1", PFX_VERTEX, vertex1).sp();
        putNumberedIdAttr("vtxIdRef2", PFX_VERTEX, vertex2).sp();
        putNumberedIdAttr("vtxIdRef3", PFX_VERTEX, vertex3).sp();

        putRawText("/>").ln();
        return this;
    }

    /**
     * 頂点リストを出力する。
     * @param model モデルデータ
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putVertexList(PmdModel model)
            throws IOException{
        ind().putRawText("<vertexList>").ln();

        pushNest();
        boolean dumped = false;
        List<Vertex> vertexList = model.getVertexList();
        for(Vertex vertex : vertexList){
            if( ! dumped ) ln();
            putVertex(vertex);
            dumped = true;
        }
        popNest();

        ind().putRawText("</vertexList>").ln(2);

        return this;
    }

    /**
     * 個別の頂点情報を出力する。
     * @param vertex 頂点
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putVertex(Vertex vertex)
            throws IOException{
        String bool;
        if(vertex.getEdgeAppearance()) bool = "true";
        else                           bool = "false";

        ind().putRawText("<vertex ");
        putNumberedIdAttr("vtxId", PFX_VERTEX, vertex).sp();
        putAttr("showEdge", bool);
        sp().putRawText(">").ln();
        pushNest();

        MkPos3D position = vertex.getPosition();
        ind().putPosition(position).ln();

        MkVec3D normal = vertex.getNormal();
        ind().putRawText("<normal ");
        putFloatAttr("x", (float) normal.getXVal()).sp();
        putFloatAttr("y", (float) normal.getYVal()).sp();
        putFloatAttr("z", (float) normal.getZVal()).sp();
        putRawText("/>").ln();

        MkPos2D uvPos = vertex.getUVPosition();
        ind().putRawText("<uvMap ");
        putFloatAttr("u", (float) uvPos.getXpos()).sp();
        putFloatAttr("v", (float) uvPos.getYpos()).sp();
        putRawText("/>").ln();

        BoneInfo boneA = vertex.getBoneA();
        BoneInfo boneB = vertex.getBoneB();
        int weight = vertex.getWeightA();
        ind().putRawText("<skinning ");
        putNumberedIdAttr("boneIdRef1", PFX_BONE, boneA).sp();
        putNumberedIdAttr("boneIdRef2", PFX_BONE, boneB).sp();
        putIntAttr("weightBalance", weight).sp();
        putRawText("/>").ln();

        popNest();
        ind().putRawText("</vertex>").ln(2);

        return this;
    }

    /**
     * ボーンリストを出力する。
     * @param model モデルデータ
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putBoneList(PmdModel model)
            throws IOException{
        ind().putRawText("<boneList>").ln();
        pushNest();

        boolean dumped = false;
        for(BoneInfo bone : model.getBoneList()){
            if( ! dumped ){
                ln().putBlockComment(BONETYPE_COMMENT).ln();
            }
            putBone(bone);
            dumped = true;
        }

        popNest();
        ind().putRawText("</boneList>").ln(2);

        return this;
    }

    /**
     * 個別のボーン情報を出力する。
     * @param bone　ボーン情報
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putBone(BoneInfo bone)
            throws IOException{
        I18nText i18nName = bone.getBoneName();
        BoneType type = bone.getBoneType();

        StringBuilder boneComment = new StringBuilder();
        String boneName = i18nName.getText();
        if(boneName.isEmpty()){
            boneName = "[NAMELESS]";
        }
        boneComment.append(boneName);
        String typeName = type.getGuiName(DEF_LOCALE);
        boneComment.append(" [").append(typeName).append(']');
        ind().putLineComment(boneComment.toString()).ln();

        ind().putRawText("<bone ");
        putPrimaryNameAttr("name", i18nName).sp();
        putNumberedIdAttr("boneId", PFX_BONE, bone).sp();
        putAttr("type", type.name());
        sp().putRawText(">").ln();
        pushNest();

        putI18nName(i18nName);

        MkPos3D position = bone.getPosition();
        ind().putPosition(position).ln();

        BoneInfo srcBone = bone.getSrcBone();
        if(bone.getBoneType() == BoneType.LINKEDROT){
            ind().putRawText("<rotationRatio ");
            putIntAttr("ratio", bone.getRotationRatio());
            putRawText(" />").ln();
        }else if(srcBone != null){
            String iktag;
            switch(getXmlFileType()){
            case XML_101009:
                iktag = "<ikBone ";
                break;
            case XML_130128:
                iktag = "<sourceBone ";
                break;
            default:
                assert false;
                throw new AssertionError();
            }
            ind().putRawText(iktag);
            putNumberedIdAttr("boneIdRef", PFX_BONE, srcBone);
            putRawText(" /> ");
            String ikBoneName = "Ref:" + srcBone.getBoneName().getText();
            putLineComment(ikBoneName);
            ln();
        }

        BoneInfo prev = bone.getPrevBone();
        BoneInfo next = bone.getNextBone();

        StringBuilder chainComment = new StringBuilder();
        if(prev != null){
            chainComment.append('[')
                        .append(prev.getBoneName().getPrimaryText())
                        .append(']')
                        .append(" >>#");
        }
        if(next != null){
            if(chainComment.length() <= 0) chainComment.append("#");
            chainComment.append(">> ")
                        .append('[')
                        .append(next.getBoneName().getPrimaryText())
                        .append(']');
        }
        if(chainComment.length() > 0){
            ln();
            ind().putLineComment(chainComment).ln();
        }

        ind().putRawText("<boneChain");
        if(prev != null){
            sp();
            putNumberedIdAttr("prevBoneIdRef", PFX_BONE, prev);
        }
        if(next != null){
            sp();
            putNumberedIdAttr("nextBoneIdRef", PFX_BONE, next);
        }
        putRawText(" />").ln();

        popNest();
        ind().putRawText("</bone>").ln(2);

        return this;
    }

    /**
     * ボーングループリストを出力する。
     * @param model モデルデータ
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putBoneGroupList(PmdModel model)
            throws IOException{
        ind().putRawText("<boneGroupList>").ln();

        pushNest();
        boolean dumped = false;
        List<BoneGroup> groupList = model.getBoneGroupList();
        for(BoneGroup group : groupList){
            if(group.isDefaultBoneGroup()) continue;
            if( ! dumped ) ln();
            putBoneGroup(group);
            dumped = true;
        }
        popNest();

        ind().putRawText("</boneGroupList>").ln(2);

        return this;
    }

    /**
     * 個別のボーングループ情報を出力する。
     * @param group ボーングループ情報
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putBoneGroup(BoneGroup group)
            throws IOException{
        I18nText i18nName = group.getGroupName();

        putLocalNameComment(i18nName).ln();
        ind().putRawText("<boneGroup ");
        putPrimaryNameAttr("name", i18nName);
        sp().putRawText(">").ln();
        pushNest();

        putI18nName(i18nName);

        for(BoneInfo bone : group){
            ind().putRawText("<boneGroupMember ");
            putNumberedIdAttr("boneIdRef", PFX_BONE, bone);
            putRawText(" /> ");
            String boneName = "Ref:" + bone.getBoneName().getText();
            putLineComment(boneName).ln();
        }

        popNest();
        ind().putRawText("</boneGroup>").ln(2);

        return this;
    }

    /**
     * IKチェーンリストを出力する。
     * @param model モデルデータ
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putIKChainList(PmdModel model)
            throws IOException{
        ind().putRawText("<ikChainList>").ln();

        pushNest();
        boolean dumped = false;
        List<IKChain> chainList = model.getIKChainList();
        for(IKChain chain : chainList){
            if( ! dumped ) ln();
            putIKChain(chain);
            dumped = true;
        }
        popNest();

        ind().putRawText("</ikChainList>").ln(2);

        return this;
    }

    /**
     * 個別のIKチェーン情報を出力する。
     * @param chain チェーン情報
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putIKChain(IKChain chain)
            throws IOException{
        int depth = chain.getIKDepth();
        float weight = chain.getIKWeight();
        BoneInfo ikBone = chain.getIkBone();

        ind().putLineComment("Ref:" + ikBone.getBoneName().getText()).ln();
        ind().putRawText("<ikChain ");
        putNumberedIdAttr("ikBoneIdRef", PFX_BONE, ikBone).sp();
        putIntAttr("recursiveDepth", depth).sp();
        putFloatAttr("weight", weight);
        sp().putRawText(">").ln();
        pushNest();

        for(BoneInfo bone : chain){
            ind().putRawText("<chainOrder ");
            putNumberedIdAttr("boneIdRef", PFX_BONE, bone);
            putRawText(" /> ");
            putLineComment("Ref:" + bone.getBoneName().getText());
            ln();
        }

        popNest();
        ind().putRawText("</ikChain>").ln(2);

        return this;
    }

    /**
     * モーフリストを出力する。
     * @param model モデルデータ
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putMorphList(PmdModel model)
            throws IOException{
        ind().putRawText("<morphList>").ln();
        pushNest();

        boolean dumped = false;
        Map<MorphType, List<MorphPart>> morphMap = model.getMorphMap();
        for(MorphType type : MorphType.values()){
            if(type == MorphType.BASE) continue;
            List<MorphPart> partList = morphMap.get(type);
            if(partList == null) continue;
            for(MorphPart part : partList){
                if( ! dumped ){
                    ln().putBlockComment(MORPHTYPE_COMMENT).ln();
                }
                putMorphPart(part);
                dumped = true;
            }
        }

        popNest();
        ind().putRawText("</morphList>").ln(2);

        return this;
    }

    /**
     * 個別のモーフ情報を出力する。
     * @param part モーフ情報
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putMorphPart(MorphPart part)
            throws IOException{
        I18nText i18nName = part.getMorphName();
        String primary = i18nName.getPrimaryText();

        putLocalNameComment(i18nName).ln();
        ind().putRawText("<morph ");
        putAttr("name", primary).sp();
        putAttr("type", part.getMorphType().name());
        sp().putRawText(">");
        ln();
        pushNest();

        putI18nName(i18nName);

        for(MorphVertex mvertex : part){
            MkPos3D offset = mvertex.getOffset();
            Vertex base = mvertex.getBaseVertex();

            ind().putRawText("<morphVertex ");
            putNumberedIdAttr("vtxIdRef", PFX_VERTEX, base).sp();
            putFloatAttr("xOff", (float) offset.getXpos()).sp();
            putFloatAttr("yOff", (float) offset.getYpos()).sp();
            putFloatAttr("zOff", (float) offset.getZpos()).sp();
            putRawText("/>");
            ln();
        }

        popNest();
        ind().putRawText("</morph>").ln(2);

        return this;
    }

    /**
     * 剛体リストを出力する。
     * @param model モデルデータ
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putRigidList(PmdModel model)
            throws IOException{
        ind().putRawText("<rigidList>").ln();
        pushNest();

        boolean dumped = false;
        for(RigidInfo rigid : model.getRigidList()){
            if( ! dumped ){
                ln().putBlockComment(RIGIDBEHAVIOR_COMMENT).ln();
            }
            putRigid(rigid);
            dumped = true;
        }

        popNest();
        ind().putRawText("</rigidList>").ln(2);

        return this;
    }

    /**
     * 個別の剛体情報を出力する。
     * @param rigid 剛体情報
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putRigid(RigidInfo rigid)
            throws IOException{
        BoneInfo linkedBone = rigid.getLinkedBone();
        I18nText i18nName = rigid.getRigidName();
        String primary = i18nName.getPrimaryText();

        putLocalNameComment(i18nName).ln();
        ind().putRawText("<rigid ");
        putAttr("name", primary).sp();
        putNumberedIdAttr("rigidId", PFX_RIGID, rigid).sp();
        putAttr("behavior", rigid.getBehaviorType().name());
        sp().putRawText(">").ln();
        pushNest();

        putI18nName(i18nName);

        if(linkedBone != null){
            ind().putRawText("<linkedBone ");
            putNumberedIdAttr("boneIdRef", PFX_BONE, linkedBone);
            putRawText(" /> ");
            putLineComment("Ref:" + linkedBone.getBoneName().getText());
            ln(2);
        }

        RigidShape shape = rigid.getRigidShape();
        putRigidShape(shape);

        MkPos3D position = rigid.getPosition();
        ind().putPosition(position).ln();

        Rad3d rotation = rigid.getRotation();
        ind().putRadRotation(rotation).ln();

        DynamicsInfo dynamics = rigid.getDynamicsInfo();
        putDynamics(dynamics).ln();

        for(RigidGroup group : rigid.getThroughGroupColl()){
            ind().putRawText("<throughRigidGroup ");
            putNumberedIdAttr("rigidGroupIdRef",
                              PFX_RIGIDGROUP,
                              group.getSerialNumber() + 1).sp();
            putRawText(" />").ln();
        }

        popNest();
        ind().putRawText("</rigid>").ln(2);

        return this;
    }

    /**
     * 剛体形状を出力する。
     * @param shape 剛体形状
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putRigidShape(RigidShape shape)
            throws IOException{
        RigidShapeType type = shape.getShapeType();

        switch(type){
        case BOX:
            ind().putRawText("<rigidShapeBox ");
            putFloatAttr("width", shape.getWidth()).sp();
            putFloatAttr("height", shape.getHeight()).sp();
            putFloatAttr("depth", shape.getDepth()).sp();
            break;
        case SPHERE:
            ind().putRawText("<rigidShapeSphere ");
            putFloatAttr("radius", shape.getRadius()).sp();
            break;
        case CAPSULE:
            ind().putRawText("<rigidShapeCapsule ");
            putFloatAttr("height", shape.getHeight()).sp();
            putFloatAttr("radius", shape.getRadius()).sp();
            break;
        default:
            assert false;
            throw new AssertionError();
        }

        putRawText("/>").ln();

        return this;
    }

    /**
     * 力学設定を出力する。
     * @param dynamics 力学設定
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putDynamics(DynamicsInfo dynamics)
            throws IOException{
        ind().putRawText("<dynamics").ln();
        pushNest();
        ind().putFloatAttr("mass", dynamics.getMass()).ln();
        ind().putFloatAttr("dampingPosition",
                dynamics.getDampingPosition()).ln();
        ind().putFloatAttr("dampingRotation",
                dynamics.getDampingRotation()).ln();
        ind().putFloatAttr("restitution", dynamics.getRestitution()).ln();
        ind().putFloatAttr("friction", dynamics.getFriction()).ln();
        popNest();
        ind().putRawText("/>").ln();

        return this;
    }

    /**
     * 剛体グループリストを出力する。
     * @param model モデルデータ
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putRigidGroupList(PmdModel model)
            throws IOException{
        ind().putRawText("<rigidGroupList>").ln(2);
        pushNest();

        boolean singleLast = false;
        for(RigidGroup group : model.getRigidGroupList()){
            List<RigidInfo> rigidList = group.getRigidList();
            if(singleLast &&  ! rigidList.isEmpty()){
                ln();
            }
            ind().putRawText("<rigidGroup ");
            putNumberedIdAttr("rigidGroupId",
                              PFX_RIGIDGROUP,
                              group.getSerialNumber() + 1);
            if(rigidList.isEmpty()){
                putRawText(" />").ln();
                singleLast = true;
                continue;
            }
            putRawText(" >").ln();
            pushNest();

            for(RigidInfo rigid : rigidList){
                ind().putRawText("<rigidGroupMember ");
                putNumberedIdAttr("rigidIdRef", PFX_RIGID, rigid).sp();
                putRawText("/>");
                sp();
                putLineComment("Ref:" + rigid.getRigidName().getText());
                ln();
            }

            popNest();
            ind().putRawText("</rigidGroup>").ln(2);
            singleLast = false;
        }

        if(singleLast){
            ln();
        }

        popNest();
        ind().putRawText("</rigidGroupList>").ln(2);

        return this;
    }

    /**
     * ジョイントリストを出力する。
     * @param model モデルデータ
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putJointList(PmdModel model)
            throws IOException{
        ind().putRawText("<jointList>").ln();

        pushNest();
        boolean dumped = false;
        List<JointInfo> jointList = model.getJointList();
        for(JointInfo joint : jointList){
            if( ! dumped ) ln();
            putJoint(joint);
            dumped = true;
        }
        popNest();

        ind().putRawText("</jointList>").ln(2);

        return this;
    }

    /**
     * 個別のジョイント情報を出力する。
     * @param joint ジョイント情報
     * @return this本体
     * @throws IOException 出力エラー
     */
    private PmdXmlExporter putJoint(JointInfo joint)
            throws IOException{
        I18nText i18nName = joint.getJointName();

        putLocalNameComment(i18nName).ln();
        ind().putRawText("<joint ");
        putPrimaryNameAttr("name", i18nName);
        sp().putRawText(">").ln();
        pushNest();

        putI18nName(i18nName);

        RigidInfo rigidA = joint.getRigidA();
        RigidInfo rigidB = joint.getRigidB();

        ind();
        putLineComment("[" + rigidA.getRigidName().getText() + "]"
                + " <=> [" + rigidB.getRigidName().getText() + "]");
        ln();

        ind().putRawText("<jointedRigidPair ");
        putNumberedIdAttr("rigidIdRef1", PFX_RIGID, rigidA).sp();
        putNumberedIdAttr("rigidIdRef2", PFX_RIGID, rigidB).sp();
        putRawText("/>").ln(2);

        MkPos3D position = joint.getPosition();
        ind().putPosition(position).ln();

        TripletRange posRange = joint.getPositionRange();
        ind().putRawText("<limitPosition").ln();
        pushNest();
        ind();
        putFloatAttr("xFrom", posRange.getXFrom()).sp();
        putFloatAttr("xTo",   posRange.getXTo()).ln();
        ind();
        putFloatAttr("yFrom", posRange.getYFrom()).sp();
        putFloatAttr("yTo",   posRange.getYTo()).ln();
        ind();
        putFloatAttr("zFrom", posRange.getZFrom()).sp();
        putFloatAttr("zTo",   posRange.getZTo()).ln();
        popNest();
        ind().putRawText("/>").ln(2);

        Rad3d rotation = joint.getRotation();
        ind().putRadRotation(rotation).ln();
        TripletRange rotRange = joint.getRotationRange();
        ind().putRawText("<limitRotation").ln();
        pushNest();
        ind();
        putFloatAttr("xFrom", rotRange.getXFrom()).sp();
        putFloatAttr("xTo",   rotRange.getXTo()).ln();
        ind();
        putFloatAttr("yFrom", rotRange.getYFrom()).sp();
        putFloatAttr("yTo",   rotRange.getYTo()).ln();
        ind();
        putFloatAttr("zFrom", rotRange.getZFrom()).sp();
        putFloatAttr("zTo",   rotRange.getZTo()).ln();
        popNest();
        ind().putRawText("/>").ln(2);

        MkPos3D elaPosition = joint.getElasticPosition();
        ind().putRawText("<elasticPosition ");
        putFloatAttr("x", (float) elaPosition.getXpos()).sp();
        putFloatAttr("y", (float) elaPosition.getYpos()).sp();
        putFloatAttr("z", (float) elaPosition.getZpos()).sp();
        putRawText("/>").ln();

        Deg3d elaRotation = joint.getElasticRotation();
        ind().putRawText("<elasticRotation ");
        putFloatAttr("xDeg", elaRotation.getXDeg()).sp();
        putFloatAttr("yDeg", elaRotation.getYDeg()).sp();
        putFloatAttr("zDeg", elaRotation.getZDeg()).sp();
        putRawText("/>").ln(2);

        popNest();
        ind().putRawText("</joint>").ln(2);

        return this;
    }

}
