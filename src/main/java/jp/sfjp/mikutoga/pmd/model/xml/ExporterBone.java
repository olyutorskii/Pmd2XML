/*
 * bone xml exporter
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.xml;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import jp.sfjp.mikutoga.corelib.I18nText;
import jp.sfjp.mikutoga.math.MkPos3D;
import jp.sfjp.mikutoga.pmd.BoneType;
import jp.sfjp.mikutoga.pmd.model.BoneGroup;
import jp.sfjp.mikutoga.pmd.model.BoneInfo;
import jp.sfjp.mikutoga.pmd.model.IKChain;
import jp.sfjp.mikutoga.pmd.model.PmdModel;
import jp.sfjp.mikutoga.xml.ProxyXmlExporter;

/**
 * ボーン設定のXMLエクスポーター。
 */
class ExporterBone extends ProxyXmlExporter {

    private static final Locale DEF_LOCALE = Locale.JAPANESE;

    private static final String LEAD_REF = "Ref:";
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


    private final ExtraExporter exp;
    private XmlModelFileType xmlType = XmlModelFileType.XML_130128;


    /**
     * コンストラクタ。
     * @param delegate 委譲先
     */
    ExporterBone(PmdXmlExporter delegate) {
        super(delegate);
        this.exp = new ExtraExporter(delegate);
        return;
    }

    /**
     * 出力XMLファイル種別を設定する。
     * @param type ファイル種別
     */
    void setXmlFileType(XmlModelFileType type){
        this.xmlType = type;
        return;
    }

    /**
     * ボーンリストを出力する。
     * @param model モデルデータ
     * @throws IOException 出力エラー
     */
    void putBoneList(PmdModel model)
            throws IOException{
        ind().putSimpleSTag(PmdTag.BONE_LIST.tag()).ln();
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
        ind().putETag(PmdTag.BONE_LIST.tag()).ln(2);

        return;
    }

    /**
     * 個別のボーン情報を出力する。
     * @param bone ボーン情報
     * @throws IOException 出力エラー
     */
    private void putBone(BoneInfo bone)
            throws IOException{
        putBoneComment(bone);

        ind().putOpenSTag(PmdTag.BONE.tag()).sp();
        I18nText i18nName = bone.getBoneName();
        this.exp.putPrimaryNameAttr(PmdAttr.NAME, i18nName);
        sp();
        this.exp.putNumberedIdAttr(PmdAttr.BONE_ID,
                ExtraExporter.PFX_BONE, bone);
        sp();
        BoneType type = bone.getBoneType();
        putAttr(PmdAttr.TYPE.attr(), type.name());
        sp().putCloseSTag().ln();
        pushNest();

        this.exp.putI18nName(i18nName);

        MkPos3D position = bone.getPosition();
        ind();
        this.exp.putPosition(position);
        ln();

        BoneInfo srcBone = bone.getSrcBone();
        if(bone.getBoneType() == BoneType.LINKEDROT){
            putRotationRatio(bone);
        }else if(srcBone != null){
            putSourceBone(srcBone);
        }

        BoneInfo prev = bone.getPrevBone();
        BoneInfo next = bone.getNextBone();
        putBoneChain(prev, next);

        popNest();
        ind().putETag(PmdTag.BONE.tag()).ln(2);

        return;
    }

    /**
     * ボーンコメントを出力する。
     * @param bone ボーン
     * @throws IOException 出力エラー
     */
    private void putBoneComment(BoneInfo bone)
            throws IOException{
        StringBuilder boneComment = new StringBuilder();

        I18nText i18nName = bone.getBoneName();
        String boneName = i18nName.getText();
        if(boneName.isEmpty()){
            boneName = "[NAMELESS]";
        }
        boneComment.append(boneName);

        BoneType type = bone.getBoneType();
        String typeName = type.getGuiName(DEF_LOCALE);
        boneComment.append("\u0020[").append(typeName).append(']');
        ind().putLineComment(boneComment.toString()).ln();

        return;
    }

    /**
     * ボーン回転連動率を出力する。
     * @param bone ボーン
     * @throws IOException 出力エラー
     */
    private void putRotationRatio(BoneInfo bone)
            throws IOException{
        ind().putOpenSTag(PmdTag.ROTATION_RATIO.tag()).sp();
        putIntAttr(PmdAttr.RATIO.attr(),
                bone.getRotationRatio()).sp();
        putCloseEmpty().ln();

        return;
    }

    /**
     * 元ボーン情報を出力する。
     * @param source 元ボーン
     * @throws IOException 出力エラー
     */
    private void putSourceBone(BoneInfo source)
            throws IOException{


        String iktag;
        switch(this.xmlType){
        case XML_101009:
            iktag = PmdTag.IK_BONE.tag();
            break;
        case XML_130128:
            iktag = PmdTag.SOURCE_BONE.tag();
            break;
        default:
            assert false;
            throw new AssertionError();
        }

        ind().putOpenSTag(iktag).sp();
        this.exp.putNumberedIdAttr(PmdAttr.BONE_IDREF,
                ExtraExporter.PFX_BONE, source);
        sp();
        putCloseEmpty().sp();

        String ikBoneName = LEAD_REF + source.getBoneName().getText();
        putLineComment(ikBoneName);
        ln();

        return;
    }

    /**
     * ボーン間チェーン情報を出力する。
     * @param prev 前ボーン
     * @param next 次ボーン
     * @throws IOException 出力エラー
     */
    private void putBoneChain(BoneInfo prev, BoneInfo next)
            throws IOException{
        StringBuilder chainComment = new StringBuilder();

        if(prev != null){
            chainComment.append('[')
                        .append(prev.getBoneName().getPrimaryText())
                        .append("]\u0020>>#");
        }
        if(next != null){
            if(chainComment.length() <= 0) chainComment.append('#');
            chainComment.append(">>\u0020[")
                        .append(next.getBoneName().getPrimaryText())
                        .append(']');
        }
        if(chainComment.length() > 0){
            ln();
            ind().putLineComment(chainComment).ln();
        }

        ind().putOpenSTag(PmdTag.BONE_CHAIN.tag());
        if(prev != null){
            sp();
            this.exp.putNumberedIdAttr(PmdAttr.PREV_BONE_IDREF,
                    ExtraExporter.PFX_BONE, prev);
        }
        if(next != null){
            sp();
            this.exp.putNumberedIdAttr(PmdAttr.NEXT_BONE_IDREF,
                    ExtraExporter.PFX_BONE, next);
        }
        sp().putCloseEmpty().ln();

        return;
    }

    /**
     * ボーングループリストを出力する。
     * @param model モデルデータ
     * @throws IOException 出力エラー
     */
    void putBoneGroupList(PmdModel model)
            throws IOException{
        ind().putSimpleSTag(PmdTag.BONE_GROUP_LIST.tag()).ln();

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

        ind().putETag(PmdTag.BONE_GROUP_LIST.tag()).ln(2);

        return;
    }

    /**
     * 個別のボーングループ情報を出力する。
     * @param group ボーングループ情報
     * @throws IOException 出力エラー
     */
    private void putBoneGroup(BoneGroup group)
            throws IOException{
        I18nText i18nName = group.getGroupName();

        this.exp.putLocalNameComment(i18nName);
        ln();
        ind().putOpenSTag(PmdTag.BONE_GROUP.tag()).sp();
        this.exp.putPrimaryNameAttr(PmdAttr.NAME, i18nName);
        sp().putCloseSTag().ln();
        pushNest();

        this.exp.putI18nName(i18nName);

        for(BoneInfo bone : group){
            ind().putOpenSTag(PmdTag.BONE_GROUP_MEMBER.tag()).sp();
            this.exp.putNumberedIdAttr(PmdAttr.BONE_IDREF,
                    ExtraExporter.PFX_BONE, bone);
            sp();
            putCloseEmpty().sp();
            String boneName = LEAD_REF + bone.getBoneName().getText();
            putLineComment(boneName).ln();
        }

        popNest();
        ind().putETag(PmdTag.BONE_GROUP.tag()).ln(2);

        return;
    }

    /**
     * IKチェーンリストを出力する。
     * @param model モデルデータ
     * @throws IOException 出力エラー
     */
    void putIKChainList(PmdModel model)
            throws IOException{
        ind().putSimpleSTag(PmdTag.IK_CHAIN_LIST.tag()).ln();

        pushNest();
        boolean dumped = false;
        List<IKChain> chainList = model.getIKChainList();
        for(IKChain chain : chainList){
            if( ! dumped ) ln();
            putIKChain(chain);
            dumped = true;
        }
        popNest();

        ind().putETag(PmdTag.IK_CHAIN_LIST.tag()).ln(2);

        return;
    }

    /**
     * 個別のIKチェーン情報を出力する。
     * @param chain チェーン情報
     * @throws IOException 出力エラー
     */
    private void putIKChain(IKChain chain)
            throws IOException{
        int depth = chain.getIKDepth();
        float weight = chain.getIKWeight();
        BoneInfo ikBone = chain.getIkBone();

        ind().putLineComment(LEAD_REF + ikBone.getBoneName().getText()).ln();
        ind().putOpenSTag(PmdTag.IK_CHAIN.tag()).sp();
        this.exp.putNumberedIdAttr(PmdAttr.IK_BONE_IDREF,
                ExtraExporter.PFX_BONE, ikBone);
        sp();
        putIntAttr(PmdAttr.RECURSIVE_DEPTH.attr(), depth).sp();
        putFloatAttr(PmdAttr.WEIGHT.attr(), weight);
        sp().putCloseSTag().ln();
        pushNest();

        for(BoneInfo bone : chain){
            ind().putOpenSTag(PmdTag.CHAIN_ORDER.tag()).sp();
            this.exp.putNumberedIdAttr(PmdAttr.BONE_IDREF,
                    ExtraExporter.PFX_BONE, bone);
            sp();
            putCloseEmpty().sp();
            putLineComment(LEAD_REF + bone.getBoneName().getText());
            ln();
        }

        popNest();
        ind().putETag(PmdTag.IK_CHAIN.tag()).ln(2);

        return;
    }

}
