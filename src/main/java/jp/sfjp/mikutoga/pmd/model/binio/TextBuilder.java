/*
 * building text info
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.binio;

import java.util.Iterator;
import java.util.List;
import jp.sfjp.mikutoga.bin.parser.ParseStage;
import jp.sfjp.mikutoga.pmd.model.BoneGroup;
import jp.sfjp.mikutoga.pmd.model.BoneInfo;
import jp.sfjp.mikutoga.pmd.model.MorphPart;
import jp.sfjp.mikutoga.pmd.model.PmdModel;
import jp.sfjp.mikutoga.pmd.parser.PmdBasicHandler;
import jp.sfjp.mikutoga.pmd.parser.PmdEngHandler;

/**
 * テキスト関係の通知をパーサから受け取る。
 */
class TextBuilder implements PmdBasicHandler, PmdEngHandler {

    private final PmdModel model;

    private Iterator<BoneInfo> boneIt;
    private BoneInfo currentBone = null;

    private List<MorphPart> morphPartList;
    private Iterator<MorphPart> morphPartIt;
    private MorphPart currentMorphPart = null;

    private Iterator<BoneGroup> boneGroupIt;
    private BoneGroup currentBoneGroup = null;

    private boolean hasMoreData = false;

    /**
     * コンストラクタ。
     * @param model モデル
     */
    TextBuilder(PmdModel model){
        super();
        this.model = model;
        return;
    }

    /**
     * PMDファイル中の出現順で各モーフを格納するためのリストを設定する。
     * 主な用途はモーフ和英名の突き合わせ作業。
     * @param list モーフ格納リスト
     */
    void setMorphPartList(List<MorphPart> list){
        this.morphPartList = list;
        return;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pmdParseStart(){
        return;
    }

    /**
     * {@inheritDoc}
     * @param hasMoreDataArg {@inheritDoc}
     */
    @Override
    public void pmdParseEnd(boolean hasMoreDataArg){
        this.hasMoreData = hasMoreDataArg;
        return;
    }

    /**
     * {@inheritDoc}
     * @param stage {@inheritDoc}
     * @param loops {@inheritDoc}
     */
    @Override
    public void loopStart(ParseStage stage, int loops){
        if(stage == PmdEngHandler.ENGBONE_LIST){
            this.boneIt = this.model.getBoneList().iterator();
            if(this.boneIt.hasNext()){
                this.currentBone = this.boneIt.next();
            }
        }else if(stage == PmdEngHandler.ENGMORPH_LIST){
            if(this.morphPartList.isEmpty()){
                return;
            }

            this.morphPartIt = this.morphPartList.iterator();

            // 「base」モーフを読み飛ばす
            MorphPart part = this.morphPartIt.next();
            assert part != null;

            if(this.morphPartIt.hasNext()){
                this.currentMorphPart = this.morphPartIt.next();
            }
        }else if(stage == PmdEngHandler.ENGBONEGROUP_LIST){
            this.boneGroupIt = this.model.getBoneGroupList().iterator();

            // デフォルトボーングループを読み飛ばす
            assert this.boneGroupIt.hasNext();
            BoneGroup group = this.boneGroupIt.next();
            assert group != null;

            if(this.boneGroupIt.hasNext()){
                this.currentBoneGroup = this.boneGroupIt.next();
            }
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
        if(stage == PmdEngHandler.ENGBONE_LIST){
            if(this.boneIt.hasNext()){
                this.currentBone = this.boneIt.next();
            }
        }else if(stage == PmdEngHandler.ENGMORPH_LIST){
            if(this.morphPartIt.hasNext()){
                this.currentMorphPart = this.morphPartIt.next();
            }
        }else if(stage == PmdEngHandler.ENGBONEGROUP_LIST){
            if(this.boneGroupIt.hasNext()){
                this.currentBoneGroup = this.boneGroupIt.next();
            }
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
        return;
    }

    /**
     * {@inheritDoc}
     * @param header {@inheritDoc}
     */
    @Override
    public void pmdHeaderInfo(byte[] header){
        return;
    }

    /**
     * {@inheritDoc}
     * @param modelNameArg {@inheritDoc}
     * @param descriptionArg {@inheritDoc}
     */
    @Override
    public void pmdModelInfo(String modelNameArg, String descriptionArg){
        this.model.getModelName()  .setPrimaryText(modelNameArg);
        this.model.getDescription().setPrimaryText(descriptionArg);
        return;
    }

    /**
     * {@inheritDoc}
     * @param hasEnglishInfo {@inheritDoc}
     */
    @Override
    public void pmdEngEnabled(boolean hasEnglishInfo){
        return;
    }

    /**
     * {@inheritDoc}
     * @param modelNameArg {@inheritDoc}
     * @param descriptionArg {@inheritDoc}
     */
    @Override
    public void pmdEngModelInfo(String modelNameArg, String descriptionArg){
        this.model.getModelName()  .setGlobalText(modelNameArg);
        this.model.getDescription().setGlobalText(descriptionArg);
        return;
    }

    /**
     * {@inheritDoc}
     * @param boneName {@inheritDoc}
     */
    @Override
    public void pmdEngBoneInfo(String boneName){
        this.currentBone.getBoneName().setGlobalText(boneName);
        return;
    }

    /**
     * {@inheritDoc}
     * @param morphName {@inheritDoc}
     */
    @Override
    public void pmdEngMorphInfo(String morphName){
        this.currentMorphPart.getMorphName().setGlobalText(morphName);
        return;
    }

    /**
     * {@inheritDoc}
     * @param groupName {@inheritDoc}
     */
    @Override
    public void pmdEngBoneGroupInfo(String groupName){
        this.currentBoneGroup.getGroupName().setGlobalText(groupName);
        return;
    }

    /**
     * 読み残したデータがあるか判定する。
     * @return 読み残したデータがあればtrue
     */
    public boolean hasMoreData(){
        return this.hasMoreData;
    }

}
