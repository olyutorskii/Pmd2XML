/*
 * material xml exporter
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.xml;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import jp.sfjp.mikutoga.corelib.I18nText;
import jp.sfjp.mikutoga.pmd.model.Material;
import jp.sfjp.mikutoga.pmd.model.PmdModel;
import jp.sfjp.mikutoga.pmd.model.ShadeInfo;
import jp.sfjp.mikutoga.pmd.model.ToonMap;
import jp.sfjp.mikutoga.xml.ProxyXmlExporter;

/**
 * マテリアル設定のXMLエクスポーター。
 */
class ExporterMaterial extends ProxyXmlExporter {

    // sRGBカラー情報配列インデックス
    private static final int IDX_RED   = 0;
    private static final int IDX_GREEN = 1;
    private static final int IDX_BLUE  = 2;
    private static final int IDX_ALPHA = 3;


    private final ExtraExporter exp;


    /**
     * コンストラクタ。
     * @param delegate 委譲先
     */
    ExporterMaterial(PmdXmlExporter delegate) {
        super(delegate);
        this.exp = new ExtraExporter(delegate);
        return;
    }

    /**
     * マテリアル素材一覧を出力する。
     * @param model モデルデータ
     * @throws IOException 出力エラー
     */
    void putMaterialList(PmdModel model)
            throws IOException{
        ind().putSimpleSTag(PmdTag.MATERIAL_LIST.tag()).ln();

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

        ind().putETag(PmdTag.MATERIAL_LIST.tag()).ln(2);

        return;
    }

    /**
     * マテリアル素材情報を出力する。
     * @param material マテリアル素材
     * @param no マテリアル通し番号
     * @throws IOException 出力エラー
     */
    private void putMaterial(Material material, int no)
            throws IOException{
        I18nText name = material.getMaterialName();
        String primary = name.getPrimaryText();
        String local = name.getText();
        if(local != null && local.length() > 0){
            ind().putLineComment(local).ln();
        }

        ind().putOpenSTag(PmdTag.MATERIAL.tag()).sp();
        if(primary != null && primary.length() > 0){
            putAttr(PmdAttr.NAME.attr(), primary).sp();
        }
        String bool;
        if(material.getEdgeAppearance()) bool = "true";
        else                             bool = "false";
        putAttr(PmdAttr.SHOW_EDGE.attr(), bool);
        sp();
        this.exp.putNumberedIdAttr(PmdAttr.SURFACE_GROUP_IDREF,
                          ExtraExporter.PFX_SURFACEGROUP, no);
        sp().putCloseSTag().ln();
        pushNest();

        this.exp.putI18nName(name);

        putPhongShade(material);

        ShadeInfo shade = material.getShadeInfo();

        if(shade.isValidToonIndex()){
            putToon(shade);
        }

        putShadeFile(shade);

        popNest();
        ind().putETag(PmdTag.MATERIAL.tag()).ln(2);

        return;
    }

    /**
     * フォンシェーディング情報を出力する。
     * @param material マテリアル
     * @throws IOException 出力エラー
     */
    private void putPhongShade(Material material)
            throws IOException{
        float[] rgba = null;

        ind().putOpenSTag(PmdTag.DIFFUSE.tag()).sp();
        Color diffuse = material.getDiffuseColor();
        rgba = diffuse.getRGBComponents(rgba);
        putTriColor(rgba);
        putFloatAttr(PmdAttr.ALPHA.attr(), rgba[IDX_ALPHA]).sp();
        putCloseEmpty().ln();

        float shininess = material.getShininess();
        ind().putOpenSTag(PmdTag.SPECULAR.tag()).sp();
        Color specular = material.getSpecularColor();
        rgba = specular.getRGBComponents(rgba);
        putTriColor(rgba);
        putFloatAttr(PmdAttr.SHININESS.attr(), shininess).sp();
        putCloseEmpty().ln();

        ind().putOpenSTag(PmdTag.AMBIENT.tag()).sp();
        Color ambient = material.getAmbientColor();
        rgba = ambient.getRGBComponents(rgba);
        putTriColor(rgba);
        putCloseEmpty().ln();

        return;
    }

    /**
     * 三原色属性を出力する。
     * @param rgb カラー情報
     * @throws IOException 出力エラー
     */
    private void putTriColor(float[] rgb)
            throws IOException{
        putFloatAttr(PmdAttr.R.attr(), rgb[IDX_RED]  ).sp();
        putFloatAttr(PmdAttr.G.attr(), rgb[IDX_GREEN]).sp();
        putFloatAttr(PmdAttr.B.attr(), rgb[IDX_BLUE] ).sp();

        return;
    }

    /**
     * マテリアルのトゥーン情報を出力する。
     * @param shade シェーディング情報
     * @throws IOException 出力エラー
     */
    private void putToon(ShadeInfo shade)
            throws IOException{
        ind().putOpenSTag(PmdTag.TOON.tag()).sp();

        int toonIdx = shade.getToonIndex();
        this.exp.putNumberedIdAttr(PmdAttr.TOONFILE_IDREF,
                          ExtraExporter.PFX_TOONFILE, toonIdx);
        sp();

        putCloseEmpty();

        String toonFileName = shade.getToonFileName();
        if(toonFileName != null && toonFileName.length() > 0){
            sp().putLineComment(toonFileName);
        }
        ln();

        return;
    }

    /**
     * シェーディングファイル情報を出力する。
     * @param shade シェーディング情報
     * @throws IOException 出力エラー
     */
    private void putShadeFile(ShadeInfo shade)
            throws IOException{
        String textureFileName   = shade.getTextureFileName();
        String spheremapFileName = shade.getSpheremapFileName();

        if(textureFileName != null && textureFileName.length() > 0){
            ind().putOpenSTag(PmdTag.TEXTURE_FILE.tag()).sp();
            putAttr(PmdAttr.WINFILE_NAME.attr(),
                    textureFileName).sp();
            putCloseEmpty().ln();
        }

        if(spheremapFileName != null && spheremapFileName.length() > 0){
            ind().putOpenSTag(PmdTag.SPHEREMAP_FILE.tag()).sp();
            putAttr(PmdAttr.WINFILE_NAME.attr(),
                    spheremapFileName).sp();
            putCloseEmpty().ln();
        }

        return;
    }

    /**
     * トゥーンファイルマッピング情報を出力する。
     * @param model モデルデータ
     * @throws IOException 出力エラー
     */
    void putToonMap(PmdModel model)
            throws IOException{
        ind().putSimpleSTag(PmdTag.TOON_MAP.tag()).ln();
        pushNest();

        ToonMap map = model.getToonMap();
        for(int index = 0; index < ToonMap.MAX_CUSTOM_TOON; index++){
            ind();
            putToonDef(map, index);
            ln();
        }

        popNest();
        ind().putETag(PmdTag.TOON_MAP.tag()).ln(2);
        return;
    }

    /**
     * 個別のトゥーンファイル情報を出力する。
     * @param map トゥーンマップ
     * @param index インデックス値
     * @throws IOException 出力エラー
     */
    private void putToonDef(ToonMap map, int index)
            throws IOException{
        putOpenSTag(PmdTag.TOON_DEF.tag()).sp();
        this.exp.putNumberedIdAttr(PmdAttr.TOONFILE_ID,
                ExtraExporter.PFX_TOONFILE, index);
        sp();
        putIntAttr(PmdAttr.INDEX.attr(), index).sp();
        String toonFile = map.getIndexedToon(index);
        putAttr(PmdAttr.WINFILE_NAME.attr(), toonFile).sp();
        putCloseEmpty();
        this.exp.putUnescapedComment(toonFile);
        return;
    }

}
