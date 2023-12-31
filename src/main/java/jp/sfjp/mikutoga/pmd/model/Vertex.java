/*
 * vertex information
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model;

import jp.sfjp.mikutoga.math.MkPos2D;
import jp.sfjp.mikutoga.math.MkPos3D;
import jp.sfjp.mikutoga.math.MkVec3D;

/**
 * 頂点情報。
 */
public class Vertex implements SerialNumbered {

    private static final int MIN_WEIGHT = 0;
    private static final int MAX_WEIGHT = 100;
    private static final int BALANCED   = 50;


    private final MkPos3D position = new MkPos3D();
    private final MkVec3D normal = new MkVec3D();

    private final MkPos2D uvPosition = new MkPos2D();

    private BoneInfo boneA = null;
    private BoneInfo boneB = null;

    private int boneWeight = BALANCED;

    private boolean edgeAppearance = true;

    private int vertexSerialNo = -1;


    /**
     * コンストラクタ。
     */
    public Vertex(){
        super();
        return;
    }


    /**
     * 頂点位置座標を返す。
     *
     * @return 頂点の位置座標
     */
    public MkPos3D getPosition(){
        return this.position;
    }

    /**
     * 法線ベクトルを返す。
     *
     * @return 法線ベクトル
     */
    public MkVec3D getNormal(){
        return this.normal;
    }

    /**
     * UVマップ座標を返す。
     *
     * @return UVマップ情報
     */
    public MkPos2D getUVPosition(){
        return this.uvPosition;
    }

    /**
     * 頂点の属するボーンを設定する。
     *
     * @param boneAArg ボーンA
     * @param boneBArg ボーンB
     * @throws NullPointerException 引数がnull
     */
    public void setBonePair(BoneInfo boneAArg, BoneInfo boneBArg)
            throws NullPointerException{
        if(boneAArg == null || boneBArg == null)
            throw new NullPointerException();

        this.boneA = boneAArg;
        this.boneB = boneBArg;

        return;
    }

    /**
     * ボーンAを返す。
     *
     * @return ボーンA
     */
    public BoneInfo getBoneA(){
        return this.boneA;
    }

    /**
     * ボーンBを返す。
     *
     * @return ボーンB
     */
    public BoneInfo getBoneB(){
        return this.boneB;
    }

    /**
     * ボーンAのウェイト値を設定する。
     *
     * @param weight ウェイト値。0(影響小)-100(影響大)
     * @throws IllegalArgumentException ウェイト値が範囲外
     */
    public void setWeightA(int weight) throws IllegalArgumentException{
        if(    weight < MIN_WEIGHT
            || MAX_WEIGHT < weight ){
            throw new IllegalArgumentException();
        }
        this.boneWeight = weight;
        return;
    }

    /**
     * ボーンBのウェイト値を設定する。
     *
     * @param weight ウェイト値。0(影響小)-100(影響大)
     * @throws IllegalArgumentException ウェイト値が範囲外
     */
    public void setWeightB(int weight) throws IllegalArgumentException{
        setWeightA(MAX_WEIGHT - weight);
        return;
    }

    /**
     * ボーンAのウェイト値を返す。
     *
     * @return ウェイト値
     */
    public int getWeightA(){
        return this.boneWeight;
    }

    /**
     * ボーンBのウェイト値を返す。
     *
     * @return ウェイト値
     */
    public int getWeightB(){
        int result = MAX_WEIGHT - this.boneWeight;
        return result;
    }

    /**
     * ボーンAのウェイト率を返す。
     *
     * @return ウェイト率。0.0(影響小)-1.0(影響大)
     */
    public float getWeightRatioA(){
        return ((float)this.boneWeight) / (float)MAX_WEIGHT;
    }

    /**
     * ボーンBのウェイト率を返す。
     *
     * @return ウェイト率。0.0(影響小)-1.0(影響大)
     */
    public float getWeightRatioB(){
        return ((float)MAX_WEIGHT - (float)this.boneWeight)
                / (float)MAX_WEIGHT;
    }

    /**
     * エッジを表示するか設定する。
     * マテリアル材質単位の設定より優先度は高い。
     *
     * @param show 表示するならtrue
     */
    public void setEdgeAppearance(boolean show){
        this.edgeAppearance = show;
        return;
    }

    /**
     * エッジを表示するか判定する。
     * マテリアル材質単位の設定より優先度は高い。
     *
     * @return 表示するならtrue
     */
    public boolean getEdgeAppearance(){
        return this.edgeAppearance;
    }

    /**
     * {@inheritDoc}
     *
     * @param num {@inheritDoc}
     */
    @Override
    public void setSerialNumber(int num){
        this.vertexSerialNo = num;
        return;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public int getSerialNumber(){
        return this.vertexSerialNo;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String toString(){
        StringBuilder result = new StringBuilder();

        result.append("Vertex(").append(this.vertexSerialNo).append(") ");
        result.append(this.position).append(' ');
        result.append(this.normal).append(' ');
        result.append("UV").append(this.uvPosition).append(' ');

        result.append("[")
              .append(this.boneA.getBoneName())
              .append("<>")
              .append(this.boneB.getBoneName())
              .append("] ");

        result.append("weight=").append(this.boneWeight).append(' ');

        if(this.edgeAppearance) result.append("showEdge");
        else                    result.append("hideEdge");

        return result.toString();
    }

}
