/*
 * material listener from XML
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.xml;

import java.awt.Color;
import java.util.List;
import jp.sfjp.mikutoga.corelib.I18nText;
import jp.sfjp.mikutoga.pmd.model.Material;
import jp.sfjp.mikutoga.pmd.model.ShadeInfo;
import jp.sfjp.mikutoga.pmd.model.ToonMap;

/*
    + materialList
        + material
            + i18nName
            + diffuse
            + specular
            + ambient
            + toon
            + textureFile
            + spheremapFile
    + toonMap
        + toonDef
*/

/**
 * マテリアル関連のXML要素出現イベントを受信する。
 */
class SaxMaterialListener extends SaxListener{

    private static final char BS_CHAR  = (char) 0x005c;
    private static final char YEN_CHAR = (char) 0x00a5;
    private static final String BS_TXT  = Character.toString(BS_CHAR);
    private static final String YEN_TXT = Character.toString(YEN_CHAR);

    private static final int TOON_IDX_NONE = 255;


    private final RefHelper helper;

    private Material currentMaterial = null;


    /**
     * コンストラクタ。
     * @param helper 参照ヘルパ
     */
    SaxMaterialListener(RefHelper helper) {
        super();
        this.helper = helper;
        return;
    }


    /**
     * 日本語Windows用ファイル名の正規化を行う。
     * 文字U+00A5は文字U-005Cに変換される。
     * @param txt 変換元文字列
     * @return 正規化された文字列
     */
    private static String xferBslash(String txt){
        String result;
        result = txt.replace(YEN_TXT, BS_TXT);
        return result;
    }


    /**
     * materialタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.MATERIAL)
    void openMaterial(){
        this.currentMaterial = new Material();

        String name = getStringAttr(PmdAttr.NAME);
        boolean showEdge = getBooleanAttr(PmdAttr.SHOW_EDGE);
        String surfaceGroupIdRef =
                getStringAttr(PmdAttr.SURFACE_GROUP_IDREF);

        I18nText i18nName = this.currentMaterial.getMaterialName();
        if(name != null){
            i18nName.setPrimaryText(name);
        }

        this.currentMaterial.setEdgeAppearance(showEdge);

        this.currentMaterial.getShadeInfo().setToonIndex(TOON_IDX_NONE);

        this.helper.addSurfaceGroupIdRef(this.currentMaterial,
                                         surfaceGroupIdRef);

        return;
    }

    /**
     * materialタグ終了の通知を受け取る。
     */
    @CloseXmlMark(PmdTag.MATERIAL)
    void closeMaterial(){
        List<Material> materialList = getPmdModel().getMaterialList();
        materialList.add(this.currentMaterial);
        this.currentMaterial = null;

        return;
    }

    /**
     * i18nTextタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.I18N_NAME)
    void openI18nText(){
        String lang = getStringAttr(PmdAttr.LANG);
        String name = getStringAttr(PmdAttr.NAME);

        I18nText materialName = this.currentMaterial.getMaterialName();
        materialName.setI18nText(lang, name);

        return;
    }

    /**
     * diffuseタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.DIFFUSE)
    void openDiffuse(){
        float rCol = getFloatAttr(PmdAttr.R);
        float gCol = getFloatAttr(PmdAttr.G);
        float bCol = getFloatAttr(PmdAttr.B);
        float alpha = getFloatAttr(PmdAttr.ALPHA);

        Color color = new Color(rCol, gCol, bCol, alpha);

        this.currentMaterial.setDiffuseColor(color);

        return;
    }

    /**
     * specularタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.SPECULAR)
    void openSpecular(){
        float rCol = getFloatAttr(PmdAttr.R);
        float gCol = getFloatAttr(PmdAttr.G);
        float bCol = getFloatAttr(PmdAttr.B);

        Color color = new Color(rCol, gCol, bCol);
        float shine = getFloatAttr(PmdAttr.SHININESS);

        this.currentMaterial.setSpecularColor(color);
        this.currentMaterial.setShininess(shine);

        return;
    }

    /**
     * ambientタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.AMBIENT)
    void openAmbient(){
        float rCol = getFloatAttr(PmdAttr.R);
        float gCol = getFloatAttr(PmdAttr.G);
        float bCol = getFloatAttr(PmdAttr.B);

        Color color = new Color(rCol, gCol, bCol);

        this.currentMaterial.setAmbientColor(color);

        return;
    }

    /**
     * toonタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.TOON)
    void openToon(){
        String toonFileIdRef = getStringAttr(PmdAttr.TOONFILE_IDREF);

        this.helper.addToonFileIdRef(this.currentMaterial, toonFileIdRef);

        return;
    }

    /**
     * textureFileタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.TEXTURE_FILE)
    void openTextureFile(){
        String fileName = getStringAttr(PmdAttr.WINFILE_NAME);
        fileName = xferBslash(fileName);

        ShadeInfo shadeInfo = this.currentMaterial.getShadeInfo();
        shadeInfo.setTextureFileName(fileName);

        return;
    }

    /**
     * spheremapFileタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.SPHEREMAP_FILE)
    void openSphemapFile(){
        String fileName = getStringAttr(PmdAttr.WINFILE_NAME);
        fileName = xferBslash(fileName);

        ShadeInfo shadeInfo = this.currentMaterial.getShadeInfo();
        shadeInfo.setSpheremapFileName(fileName);

        return;
    }

    /**
     * toonDefタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.TOON_DEF)
    void openToonDef(){
        String toonFileId = getStringAttr(PmdAttr.TOONFILE_ID);
        int index         = getIntAttr(PmdAttr.INDEX);
        String fileName   = getStringAttr(PmdAttr.WINFILE_NAME);
        fileName = xferBslash(fileName);

        ToonMap toonMap = getPmdModel().getToonMap();
        toonMap.setIndexedToon(index, fileName);

        this.helper.addToonIdx(toonFileId, index);

        return;
    }

    /**
     * toonMapタグ終了の通知を受け取る。
     */
    @CloseXmlMark(PmdTag.TOON_MAP)
    void closeToonMap(){
        this.helper.resolveToonIdx();
        return;
    }

}
