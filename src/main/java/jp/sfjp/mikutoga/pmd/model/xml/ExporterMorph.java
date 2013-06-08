/*
 * morph xml exporter
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.xml;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import jp.sfjp.mikutoga.corelib.I18nText;
import jp.sfjp.mikutoga.math.MkPos3D;
import jp.sfjp.mikutoga.pmd.MorphType;
import jp.sfjp.mikutoga.pmd.model.MorphPart;
import jp.sfjp.mikutoga.pmd.model.MorphVertex;
import jp.sfjp.mikutoga.pmd.model.PmdModel;
import jp.sfjp.mikutoga.pmd.model.Vertex;
import jp.sfjp.mikutoga.xml.ProxyXmlExporter;

/**
 * モーフ定義のXMLエクスポーター。
 */
class ExporterMorph extends ProxyXmlExporter {

    private static final String MORPHTYPE_COMMENT =
          "Morph types:\n"
        + "[1 : EYEBROW : まゆ   ]\n"
        + "[2 : EYE     : 目     ]\n"
        + "[3 : LIP     : リップ ]\n"
        + "[4 : EXTRA   : その他 ]\n";


    private final ExtraExporter exp;


    /**
     * コンストラクタ。
     * @param delegate 委譲先
     */
    ExporterMorph(PmdXmlExporter delegate) {
        super(delegate);
        this.exp = new ExtraExporter(delegate);
        return;
    }

    /**
     * モーフ定義データを出力する。
     * @param model モデルデータ
     * @throws IOException 出力エラー
     */
    void putMorphList(PmdModel model) throws IOException{
        ind().putSimpleSTag(PmdTag.MORPH_LIST.tag()).ln();
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
        ind().putETag(PmdTag.MORPH_LIST.tag()).ln(2);

        return;
    }

    /**
     * 個別のモーフ情報を出力する。
     * @param part モーフ情報
     * @throws IOException 出力エラー
     */
    private void putMorphPart(MorphPart part) throws IOException{
        I18nText i18nName = part.getMorphName();
        String primary = i18nName.getPrimaryText();

        this.exp.putLocalNameComment(i18nName);
        ln();
        ind().putOpenSTag(PmdTag.MORPH.tag()).sp();
        putAttr(PmdAttr.NAME.attr(), primary).sp();
        putAttr(PmdAttr.TYPE.attr(), part.getMorphType().name());
        sp().putCloseSTag();
        ln();
        pushNest();

        this.exp.putI18nName(i18nName);

        for(MorphVertex mvertex : part){
            MkPos3D offset = mvertex.getOffset();
            Vertex base = mvertex.getBaseVertex();

            ind().putOpenSTag(PmdTag.MORPH_VERTEX.tag()).sp();
            this.exp.putNumberedIdAttr(PmdAttr.VERTEX_IDREF,
                    ExtraExporter.PFX_VERTEX, base);
            sp();
            putFloatAttr(PmdAttr.XOFF.attr(),
                    (float) offset.getXpos()).sp();
            putFloatAttr(PmdAttr.YOFF.attr(),
                    (float) offset.getYpos()).sp();
            putFloatAttr(PmdAttr.ZOFF.attr(),
                    (float) offset.getZpos()).sp();
            putCloseEmpty();
            ln();
        }

        popNest();
        ind().putETag(PmdTag.MORPH.tag()).ln(2);

        return;
    }

}
