/*
 * morph listener from XML
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.xml;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import jp.sfjp.mikutoga.corelib.I18nText;
import jp.sfjp.mikutoga.math.MkPos3D;
import jp.sfjp.mikutoga.pmd.MorphType;
import jp.sfjp.mikutoga.pmd.model.ListUtil;
import jp.sfjp.mikutoga.pmd.model.MorphPart;
import jp.sfjp.mikutoga.pmd.model.MorphVertex;

/*
    + morphList
        + morph
            + i18nName
            + morphVertex
*/

/**
 * モーフ関連のXML要素出現イベントを受信する。
 */
class SaxMorphListener extends SaxListener{

    private final RefHelper helper;

    private MorphPart currentMorph = null;


    /**
     * コンストラクタ。
     * @param helper 参照ヘルパ
     */
    SaxMorphListener(RefHelper helper) {
        super();
        this.helper = helper;
        return;
    }


    /**
     * morphListタグ終了の通知を受け取る。
     * 各モーフは0番ではなく1番から採番される。
     * 0番は暗黙のBASEモーフ。
     */
    @CloseXmlMark(PmdTag.MORPH_LIST)
    void closeMorphList(){
        Map<MorphType, List<MorphPart>> morphMap =
                getPmdModel().getMorphMap();
        List<MorphPart> tempList = new LinkedList<>();

        tempList.addAll(morphMap.get(MorphType.EYEBROW));
        tempList.addAll(morphMap.get(MorphType.EYE));
        tempList.addAll(morphMap.get(MorphType.LIP));
        tempList.addAll(morphMap.get(MorphType.EXTRA));

        MorphPart baseDummy = new MorphPart();
        tempList.add(0, baseDummy);  // BASE dummy

        ListUtil.assignIndexedSerial(tempList);

        return;
    }

    /**
     * morphタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.MORPH)
    void openMorph(){
        this.currentMorph = new MorphPart();

        String name = getStringAttr(PmdAttr.NAME);
        I18nText morphName = this.currentMorph.getMorphName();
        morphName.setPrimaryText(name);

        String type = getStringAttr(PmdAttr.TYPE);
        MorphType morphType = MorphType.valueOf(type);
        this.currentMorph.setMorphType(morphType);

        return;
    }

    /**
     * morphタグ終了の通知を受け取る。
     */
    @CloseXmlMark(PmdTag.MORPH)
    void closeMorph(){
        Map<MorphType, List<MorphPart>> morphMap =
                getPmdModel().getMorphMap();

        MorphType morphType = this.currentMorph.getMorphType();
        List<MorphPart> morphList = morphMap.get(morphType);
        morphList.add(this.currentMorph);

        this.currentMorph = null;

        return;
    }

    /**
     * i18nTextタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.I18N_NAME)
    void openI18nText(){
        String lang = getStringAttr(PmdAttr.LANG);
        String name = getStringAttr(PmdAttr.NAME);

        I18nText morphName = this.currentMorph.getMorphName();
        morphName.setI18nText(lang, name);

        return;
    }

    /**
     * morphVertexタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.MORPH_VERTEX)
    void openMorphVertex(){
        MorphVertex morphVertex = new MorphVertex();

        String vertexIdRef = getStringAttr(PmdAttr.VERTEX_IDREF);
        this.helper.addMorphVertexIdRef(morphVertex, vertexIdRef);

        float xOff = getFloatAttr(PmdAttr.XOFF);
        float yOff = getFloatAttr(PmdAttr.YOFF);
        float zOff = getFloatAttr(PmdAttr.ZOFF);
        MkPos3D offset = morphVertex.getOffset();
        offset.setPosition(xOff, yOff, zOff);

        List<MorphVertex> morphVertexList =
                this.currentMorph.getMorphVertexList();
        morphVertexList.add(morphVertex);

        return;
    }

}
