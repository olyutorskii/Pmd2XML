/*
 * bone information
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model;

import jp.sfjp.mikutoga.corelib.I18nText;
import jp.sfjp.mikutoga.math.MkPos3D;
import jp.sfjp.mikutoga.pmd.BoneType;

/**
 * ボーン情報。
 */
public class BoneInfo implements SerialNumbered {

    private static final String NO_BONE = "NONE";

    private final I18nText boneName = new I18nText();
    private BoneType boneType;

    private BoneInfo prevBone;
    private BoneInfo nextBone;
    private BoneInfo srcBone;

    private final MkPos3D position = new MkPos3D();

    private int rotationRatio;

    private int boneSerialNo = -1;


    /**
     * コンストラクタ。
     */
    public BoneInfo(){
        super();
        return;
    }


    /**
     * ボーン名を返す。
     * @return ボーン名
     */
    public I18nText getBoneName(){
        return this.boneName;
    }

    /**
     * ボーン種別を設定する。
     * @param type ボーン種別
     * @throws NullPointerException 引数がnull
     */
    public void setBoneType(BoneType type) throws NullPointerException{
        if(type == null) throw new NullPointerException();
        this.boneType = type;
        return;
    }

    /**
     * ボーン種別を返す。
     * @return ボーン種別
     */
    public BoneType getBoneType(){
        return this.boneType;
    }

    /**
     * 親(前)ボーンを設定する。
     * @param prevBone 前ボーン。ない場合はnullを指定。
     */
    public void setPrevBone(BoneInfo prevBone){
        this.prevBone = prevBone;
        return;
    }

    /**
     * 親(前)ボーンを返す。
     * @return 前ボーン。ない場合はnullを返す。
     */
    public BoneInfo getPrevBone(){
        return this.prevBone;
    }

    /**
     * 子(次)ボーンを設定する。
     * 捩りボーンでは軸方向に位置するボーン、
     * 回転連動ボーンでは影響元ボーン。
     * @param nextBone 次ボーン。ない場合はnullを指定。
     */
    public void setNextBone(BoneInfo nextBone){
        this.nextBone = nextBone;
        return;
    }

    /**
     * 子(次)ボーンを返す。
     * 捩りボーンでは軸方向に位置するボーン、
     * 回転連動ボーンでは影響元ボーン。
     * @return 次ボーン。ない場合はnullを返す。
     */
    public BoneInfo getNextBone(){
        return this.nextBone;
    }

    /**
     * このボーンが影響を受けるIK元、回転元のソースボーンを設定する。
     * @param ikBoneArg ソースボーン。ない場合はnullを指定。
     */
    public void setSrcBone(BoneInfo ikBoneArg){
        this.srcBone = ikBoneArg;
        return;
    }

    /**
     * このボーンが影響を受けるIK元、回転元のソースボーンを返す。
     * @return ソースボーン。ない場合はnull
     */
    public BoneInfo getSrcBone(){
        return this.srcBone;
    }

    /**
     * ボーン位置を返す。
     * @return ボーン位置
     */
    public MkPos3D getPosition(){
        return this.position;
    }

    /**
     * 回転連動の影響度を返す。
     * 回転連動ボーンの場合のみ有効。
     * @return 回転連動の影響度
     */
    public int getRotationRatio(){
        return this.rotationRatio;
    }

    /**
     * 回転連動の影響度を設定する。
     * 回転連動ボーンの場合のみ有効。
     * @param ratio 回転連動の影響度
     */
    public void setRotationRatio(int ratio){
        this.rotationRatio = ratio;
    }

    /**
     * {@inheritDoc}
     * @param num {@inheritDoc}
     */
    @Override
    public void setSerialNumber(int num){
        this.boneSerialNo = num;
        return;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int getSerialNumber(){
        return this.boneSerialNo;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String toString(){
        StringBuilder result = new StringBuilder();

        result.append("Bone")
              .append(this.boneSerialNo)
              .append("(")
              .append(this.boneName.getPrimaryText())
              .append(")");

        result.append(" type=")
              .append(this.boneType);

        result.append(" prev=");
        if(this.prevBone != null) result.append(this.prevBone.getBoneName());
        else                      result.append(NO_BONE);

        result.append(" next=");
        if(this.nextBone != null) result.append(this.nextBone.getBoneName());
        else                      result.append(NO_BONE);

        if(this.boneType == BoneType.LINKEDROT){
            result.append(" rotraio=").append(this.rotationRatio);
        }else{
            result.append(" ik=");
            if(this.srcBone != null){
                result.append(this.srcBone.getBoneName());
            }else{
                result.append(NO_BONE);
            }
        }

        result.append(" ").append(this.position);

        return result.toString();
    }

}
