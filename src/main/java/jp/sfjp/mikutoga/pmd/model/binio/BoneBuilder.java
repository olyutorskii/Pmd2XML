/*
 * building bone information
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.binio;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import jp.sfjp.mikutoga.bin.parser.ParseStage;
import jp.sfjp.mikutoga.math.MkPos3D;
import jp.sfjp.mikutoga.pmd.BoneType;
import jp.sfjp.mikutoga.pmd.PmdConst;
import jp.sfjp.mikutoga.pmd.model.BoneGroup;
import jp.sfjp.mikutoga.pmd.model.BoneInfo;
import jp.sfjp.mikutoga.pmd.model.IKChain;
import jp.sfjp.mikutoga.pmd.model.ListUtil;
import jp.sfjp.mikutoga.pmd.model.PmdModel;
import jp.sfjp.mikutoga.pmd.parser.PmdBoneHandler;

/**
 * ボーン関係の通知をパーサから受け取る。
 */
class BoneBuilder implements PmdBoneHandler {

    private final List<BoneInfo> boneList;
    private Iterator<BoneInfo> boneIt;
    private BoneInfo currentBone = null;

    private final List<IKChain> ikChainList;
    private Iterator<IKChain> ikChainIt;
    private IKChain currentIkChain = null;

    private final List<BoneGroup> boneGroupList;
    private Iterator<BoneGroup> boneGroupIt;
    private BoneGroup currentBoneGroup = null;

    /**
     * コンストラクタ。
     * @param model モデル
     */
    BoneBuilder(PmdModel model){
        super();

        this.boneList      = model.getBoneList();
        this.ikChainList   = model.getIKChainList();
        this.boneGroupList = model.getBoneGroupList();

        return;
    }

    /**
     * {@inheritDoc}
     * @param stage {@inheritDoc}
     * @param loops {@inheritDoc}
     */
    @Override
    public void loopStart(ParseStage stage, int loops){
        if(stage == PmdBoneHandler.BONE_LIST){
            ListUtil.prepareDefConsList(this.boneList, BoneInfo.class, loops);
            ListUtil.assignIndexedSerial(this.boneList);

            this.boneIt = this.boneList.iterator();
            if(this.boneIt.hasNext()){
                this.currentBone = this.boneIt.next();
            }
        }else if(stage == PmdBoneHandler.IK_LIST){
            ListUtil.prepareDefConsList(this.ikChainList,
                                        IKChain.class,
                                        loops );

            this.ikChainIt = this.ikChainList.iterator();
            if(this.ikChainIt.hasNext()){
                this.currentIkChain = this.ikChainIt.next();
            }
        }else if(stage == PmdBoneHandler.IKCHAIN_LIST){
            assert true; // NOTHING
        }else if(stage == PmdBoneHandler.BONEGROUP_LIST){
            ListUtil.prepareDefConsList(this.boneGroupList,
                                        BoneGroup.class,
                                        loops + 1 );
            ListUtil.assignIndexedSerial(this.boneGroupList);

            this.boneGroupIt = this.boneGroupList.iterator();

            assert this.boneGroupIt.hasNext();
            this.boneGroupIt.next();     // デフォルトボーングループを読み飛ばす

            if(this.boneGroupIt.hasNext()){
                this.currentBoneGroup = this.boneGroupIt.next();
            }
        }else if(stage == PmdBoneHandler.GROUPEDBONE_LIST){
            assert true; // NOTHING
        }else{
            assert false;
            throw new AssertionError();
        }

        return;
    }

    /**
     * {@inheritDoc}
     * @param stage {@inheritDoc}
     */
    @Override
    public void loopNext(ParseStage stage){
        if(stage == PmdBoneHandler.BONE_LIST){
            if(this.boneIt.hasNext()){
                this.currentBone = this.boneIt.next();
            }
        }else if(stage == PmdBoneHandler.IK_LIST){
            if(this.ikChainIt.hasNext()){
                this.currentIkChain = this.ikChainIt.next();
            }
        }else if(stage == PmdBoneHandler.IKCHAIN_LIST){
            assert true; // NOTHING
        }else if(stage == PmdBoneHandler.BONEGROUP_LIST){
            if(this.boneGroupIt.hasNext()){
                this.currentBoneGroup = this.boneGroupIt.next();
            }
        }else if(stage == PmdBoneHandler.GROUPEDBONE_LIST){
            assert true; // NOTHING
        }else{
            assert false;
            throw new AssertionError();
        }
        return;
    }

    /**
     * {@inheritDoc}
     * @param stage {@inheritDoc}
     */
    @Override
    public void loopEnd(ParseStage stage){
        if(stage == PmdBoneHandler.BONE_LIST){
            assert true; // NOTHING
        }else if(stage == PmdBoneHandler.IK_LIST){
            assert true; // NOTHING
        }else if(stage == PmdBoneHandler.IKCHAIN_LIST){
            assert true; // NOTHING
        }else if(stage == PmdBoneHandler.BONEGROUP_LIST){
            assert true; // NOTHING
        }else if(stage == PmdBoneHandler.GROUPEDBONE_LIST){
            pickOrphanBone();
        }else{
            assert false;
            throw new AssertionError();
        }
        return;
    }

    /**
     * 所属グループの無いボーンをデフォルトボーングループへ登録する。
     */
    private void pickOrphanBone(){
        List<BoneInfo> orpahnList = new LinkedList<>();
        orpahnList.addAll(this.boneList);
        for(BoneGroup group : this.boneGroupList){
            orpahnList.removeAll(group.getBoneList());
        }

        BoneGroup defaultGroup = this.boneGroupList.get(0);
        defaultGroup.getBoneList().addAll(orpahnList);

        return;
    }

    /**
     * {@inheritDoc}
     * @param boneName {@inheritDoc}
     * @param boneKind {@inheritDoc}
     */
    @Override
    public void pmdBoneInfo(String boneName, byte boneKind){
        this.currentBone.getBoneName().setPrimaryText(boneName);
        BoneType type = BoneType.decode(boneKind);
        this.currentBone.setBoneType(type);
        return;
    }

    /**
     * {@inheritDoc}
     * @param parentId {@inheritDoc}
     * @param tailId {@inheritDoc}
     * @param srcId {@inheritDoc}
     */
    @Override
    public void pmdBoneLink(int parentId, int tailId, int srcId){
        BoneInfo prevBone = null;
        if(0 <= parentId && parentId < PmdConst.MAX_BONE){
            prevBone = this.boneList.get(parentId);
        }

        BoneInfo nextBone = null;
        if(tailId != 0){
            nextBone = this.boneList.get(tailId);
        }

        BoneInfo srcBone = null;
        if(this.currentBone.getBoneType() == BoneType.LINKEDROT){
            srcBone = null;
            int ratio = srcId;
            this.currentBone.setRotationRatio(ratio);
        }else if(0 < srcId && srcId < PmdConst.MAX_BONE){
            srcBone = this.boneList.get(srcId);
        }

        this.currentBone.setPrevBone(prevBone);
        this.currentBone.setNextBone(nextBone);
        this.currentBone.setSrcBone(srcBone);

        return;
    }

    /**
     * {@inheritDoc}
     * @param xPos {@inheritDoc}
     * @param yPos {@inheritDoc}
     * @param zPos {@inheritDoc}
     */
    @Override
    public void pmdBonePosition(float xPos, float yPos, float zPos){
        MkPos3D position = this.currentBone.getPosition();
        position.setXpos(xPos);
        position.setYpos(yPos);
        position.setZpos(zPos);
        return;
    }

    /**
     * {@inheritDoc}
     * @param boneId {@inheritDoc}
     * @param targetId {@inheritDoc}
     * @param depth {@inheritDoc}
     * @param weight {@inheritDoc}
     */
    @Override
    public void pmdIKInfo(int boneId, int targetId, int depth, float weight){
        BoneInfo bone = this.boneList.get(boneId);
        this.currentIkChain.setIkBone(bone);

        BoneInfo target = this.boneList.get(targetId);
        this.currentIkChain.getChainedBoneList().add(0, target);

        this.currentIkChain.setIKDepth(depth);
        this.currentIkChain.setIKWeight(weight);

        return;
    }

    /**
     * {@inheritDoc}
     * @param childId {@inheritDoc}
     */
    @Override
    public void pmdIKChainInfo(int childId){
        BoneInfo chain = this.boneList.get(childId);
        this.currentIkChain.getChainedBoneList().add(chain);
        return;
    }

    /**
     * {@inheritDoc}
     * @param groupName {@inheritDoc}
     */
    @Override
    public void pmdBoneGroupInfo(String groupName){
        this.currentBoneGroup.getGroupName().setPrimaryText(groupName);
        return;
    }

    /**
     * {@inheritDoc}
     * @param boneId {@inheritDoc}
     * @param groupId {@inheritDoc}
     */
    @Override
    public void pmdGroupedBoneInfo(int boneId, int groupId){
        BoneInfo bone = this.boneList.get(boneId);
        BoneGroup group = this.boneGroupList.get(groupId);
        group.getBoneList().add(bone);
        return;
    }

}
