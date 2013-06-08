/*
 * bone listener from XML
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.xml;

import java.util.List;
import jp.sfjp.mikutoga.corelib.I18nText;
import jp.sfjp.mikutoga.math.MkPos3D;
import jp.sfjp.mikutoga.pmd.BoneType;
import jp.sfjp.mikutoga.pmd.model.BoneGroup;
import jp.sfjp.mikutoga.pmd.model.BoneInfo;
import jp.sfjp.mikutoga.pmd.model.IKChain;
import jp.sfjp.mikutoga.pmd.model.ListUtil;

/*
    + boneList
        + bone
            + i18nName
            + position
            + rotationRatio
            + ikBone
            + sourceBone
            + boneChain
    + boneGroupList
        + boneGroup
            + i18nName
            + boneGroupMember
    + ikChainList
        + ikChain
            + chainOrder
*/

/**
 * ボーン関連のXML要素出現イベントを受信する。
 */
class SaxBoneListener extends SaxListener{

    private final RefHelper helper;

    private BoneInfo currentBone = null;

    private BoneGroup currentBoneGroup = null;

    private IKChain currentIkChain = null;


    /**
     * コンストラクタ。
     * @param helper 参照ヘルパ
     */
    SaxBoneListener(RefHelper helper) {
        super();
        this.helper = helper;
        return;
    }


    /**
     * boneInfoタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.BONE)
    void openBoneInfo(){
        this.currentBone = new BoneInfo();

        String nameAttr = getStringAttr(PmdAttr.NAME);
        I18nText boneName = this.currentBone.getBoneName();
        boneName.setPrimaryText(nameAttr);

        String boneId = getStringAttr(PmdAttr.BONE_ID);
        this.helper.addBoneId(boneId, this.currentBone);

        String typeAttr = getStringAttr(PmdAttr.TYPE);
        BoneType boneType = BoneType.valueOf(typeAttr);
        this.currentBone.setBoneType(boneType);

        return;
    }

    /**
     * boneInfoタグ終了の通知を受け取る。
     */
    @CloseXmlMark(PmdTag.BONE)
    void closeBoneInfo(){
        List<BoneInfo> boneList = getPmdModel().getBoneList();
        boneList.add(this.currentBone);

        this.currentBone = null;

        return;
    }

    /**
     * i18nTextタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.I18N_NAME)
    void openI18nText(){
        I18nText i18nName;
        if(this.currentBone != null){
            i18nName = this.currentBone.getBoneName();
        }else if(this.currentBoneGroup != null){
            i18nName = this.currentBoneGroup.getGroupName();
        }else{
            return;
        }

        String lang = getStringAttr(PmdAttr.LANG);
        String name = getStringAttr(PmdAttr.NAME);

        i18nName.setI18nText(lang, name);

        return;
    }

    /**
     * boneListタグ終了の通知を受け取る。
     */
    @CloseXmlMark(PmdTag.BONE_LIST)
    void closeBoneList(){
        this.helper.resolveSrcBoneIdRef();
        this.helper.resolveBoneChainIdRef();

        List<BoneInfo> boneList = getPmdModel().getBoneList();
        ListUtil.assignIndexedSerial(boneList);

        return;
    }

    /**
     * positionタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.POSITION)
    void openPosition(){
        float x = getFloatAttr(PmdAttr.X);
        float y = getFloatAttr(PmdAttr.Y);
        float z = getFloatAttr(PmdAttr.Z);

        MkPos3D pos = this.currentBone.getPosition();
        pos.setPosition(x, y, z);

        return;
    }

    /**
     * rotationRatioタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.ROTATION_RATIO)
    void openRotationRatio(){
        int ratio = getIntAttr(PmdAttr.RATIO);
        this.currentBone.setRotationRatio(ratio);
        return;
    }

    /**
     * ikBoneタグ開始の通知を受け取る。
     * ※ 101009版スキーマでしか出現しない。
     */
    @OpenXmlMark(PmdTag.IK_BONE) // 101009 only
    void openIkBone(){
        openSrcBone();
        return;
    }

    /**
     * sourceBoneタグ開始の通知を受け取る。
     * ※ 130128版スキーマでしか出現しない。
     */
    @OpenXmlMark(PmdTag.SOURCE_BONE) // 130128 only
    void openSrcBone(){
        String boneIdRef = getStringAttr(PmdAttr.BONE_IDREF);
        this.helper.addSrcBoneIdRef(this.currentBone, boneIdRef);
        return;
    }

    /**
     * boneChainタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.BONE_CHAIN)
    void openBoneChain(){
        String prevBoneIdRef = getStringAttr(PmdAttr.PREV_BONE_IDREF);
        String nextBoneIdRef = getStringAttr(PmdAttr.NEXT_BONE_IDREF);

        this.helper.addBoneChain(this.currentBone,
                                 prevBoneIdRef, nextBoneIdRef );

        return;
    }

    /**
     * boneGroupListタグ開始の通知を受け取る。
     * 暗黙のデフォルトボーングループが無条件に格納される。
     */
    @OpenXmlMark(PmdTag.BONE_GROUP_LIST)
    void openBoneGroupList(){
        BoneGroup defaultBoneGroup = new BoneGroup();
        defaultBoneGroup.setSerialNumber(0);

        List<BoneGroup> boneGroupList =
                this.getPmdModel().getBoneGroupList();
        boneGroupList.add(defaultBoneGroup);

        return;
    }

    /**
     * boneGroupListタグ終了の通知を受け取る。
     */
    @CloseXmlMark(PmdTag.BONE_GROUP_LIST)
    void closeBoneGroupList(){
        List<BoneGroup> boneGroupList = getPmdModel().getBoneGroupList();
        ListUtil.assignIndexedSerial(boneGroupList);
        return;
    }

    /**
     * boneGroupタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.BONE_GROUP)
    void openBoneGroup(){
        this.currentBoneGroup = new BoneGroup();

        String nameAttr = getStringAttr(PmdAttr.NAME);
        I18nText groupName = this.currentBoneGroup.getGroupName();
        groupName.setPrimaryText(nameAttr);

        return;
    }

    /**
     * boneGroupタグ終了の通知を受け取る。
     */
    @CloseXmlMark(PmdTag.BONE_GROUP)
    void closeBoneGroup(){
        List<BoneGroup> boneGroupList = getPmdModel().getBoneGroupList();
        boneGroupList.add(this.currentBoneGroup);

        this.currentBoneGroup = null;

        return;
    }

    /**
     * boneGroupMemberタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.BONE_GROUP_MEMBER)
    void openBoneGroupMember(){
        String boneIdRef = getStringAttr(PmdAttr.BONE_IDREF);

        BoneInfo bone = this.helper.findBoneId(boneIdRef);

        List<BoneInfo> boneList = this.currentBoneGroup.getBoneList();
        boneList.add(bone);

        return;
    }

    /**
     * ikChainタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.IK_CHAIN)
    void openIkChain(){
        this.currentIkChain = new IKChain();

        String ikBoneIdRef = getStringAttr(PmdAttr.IK_BONE_IDREF);
        BoneInfo bone = this.helper.findBoneId(ikBoneIdRef);
        this.currentIkChain.setIkBone(bone);

        int depth = getIntAttr(PmdAttr.RECURSIVE_DEPTH);
        this.currentIkChain.setIKDepth(depth);

        float weight = getFloatAttr(PmdAttr.WEIGHT);
        this.currentIkChain.setIKWeight(weight);

        return;
    }

    /**
     * ikChainタグ終了の通知を受け取る。
     */
    @CloseXmlMark(PmdTag.IK_CHAIN)
    void closeIkChain(){
        List<IKChain> ikChainList = getPmdModel().getIKChainList();
        ikChainList.add(this.currentIkChain);

        this.currentIkChain = null;

        return;
    }

    /**
     * chainOrderタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.CHAIN_ORDER)
    void openChainOrder(){
        String boneIdRef = getStringAttr(PmdAttr.BONE_IDREF);
        BoneInfo bone = this.helper.findBoneId(boneIdRef);

        List<BoneInfo> chainList = this.currentIkChain.getChainedBoneList();
        chainList.add(bone);

        return;
    }

}
