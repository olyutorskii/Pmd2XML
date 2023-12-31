/*
 * joint information
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model;

import jp.sfjp.mikutoga.corelib.I18nText;
import jp.sfjp.mikutoga.math.MkPos3D;
import jp.sfjp.mikutoga.pmd.Deg3d;
import jp.sfjp.mikutoga.pmd.Rad3d;
import jp.sfjp.mikutoga.pmd.TripletRange;

/**
 * 剛体間ジョイント情報。
 */
public class JointInfo {

    private final I18nText jointName = new I18nText();

    private RigidInfo rigidA;
    private RigidInfo rigidB;

    private final MkPos3D position = new MkPos3D();
    private final Rad3d rotation = new Rad3d();

    private final MkPos3D elaPosition = new MkPos3D();
    private final Deg3d elaRotation = new Deg3d();

    private final TripletRange posRange = new TripletRange();
    private final TripletRange rotRange = new TripletRange();


    /**
     * コンストラクタ。
     */
    public JointInfo(){
        super();
        return;
    }


    /**
     * ジョイント名を返す。
     * @return ジョイント名
     */
    public I18nText getJointName(){
        return this.jointName;
    }

    /**
     * 連結剛体Aを返す。
     * @return 連結剛体A
     */
    public RigidInfo getRigidA(){
        return this.rigidA;
    }

    /**
     * 連結剛体Bを返す。
     * @return 連結剛体B
     */
    public RigidInfo getRigidB(){
        return this.rigidB;
    }

    /**
     * 連結する剛体を設定する。
     * @param rigidAArg 連結剛体A
     * @param rigidBArg 連結剛体B
     */
    public void setRigidPair(RigidInfo rigidAArg, RigidInfo rigidBArg){
        this.rigidA = rigidAArg;
        this.rigidB = rigidBArg;
        return;
    }

    /**
     * ジョイントの位置を返す。
     * @return ジョイントの位置
     */
    public MkPos3D getPosition(){
        return this.position;
    }

    /**
     * ジョイントの姿勢を返す。
     * @return ジョイントの姿勢
     */
    public Rad3d getRotation(){
        return this.rotation;
    }

    /**
     * ジョイントのバネ位置を返す。
     * @return ジョイントのバネ位置
     */
    public MkPos3D getElasticPosition(){
        return this.elaPosition;
    }

    /**
     * ジョイントのバネ姿勢を返す。
     * @return ジョイントのバネ姿勢
     */
    public Deg3d getElasticRotation(){
        return this.elaRotation;
    }

    /**
     * ジョイントの位置制約を返す。
     * @return ジョイントの位置制約
     */
    public TripletRange getPositionRange(){
        return this.posRange;
    }

    /**
     * ジョイントの姿勢制約を返す。
     * @return ジョイントの姿勢制約
     */
    public TripletRange getRotationRange(){
        return this.rotRange;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String toString(){
        StringBuilder result = new StringBuilder();

        result.append("Joint ");
        result.append(this.jointName);
        result.append("[")
              .append(this.rigidA.getRigidName())
              .append("<=>")
              .append(this.rigidB.getRigidName())
              .append("] ");
        result.append(this.position).append(' ');
        result.append(this.rotation).append(' ');

        result.append("poslim{").append(this.posRange).append("} ");
        result.append("rotlim{").append(this.rotRange).append("} ");

        result.append("ela:").append(this.elaPosition).append(' ');
        result.append("ela:").append(this.elaRotation);

        return result.toString();
    }

}
