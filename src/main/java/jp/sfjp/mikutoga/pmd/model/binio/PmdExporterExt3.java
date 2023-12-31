/*
 * model exporter for pmd-file(Ext3)
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.binio;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import jp.sfjp.mikutoga.bin.export.IllegalTextExportException;
import jp.sfjp.mikutoga.pmd.Deg3d;
import jp.sfjp.mikutoga.pmd.IllegalPmdDataException;
import jp.sfjp.mikutoga.pmd.PmdConst;
import jp.sfjp.mikutoga.pmd.Rad3d;
import jp.sfjp.mikutoga.pmd.RigidShapeType;
import jp.sfjp.mikutoga.pmd.TripletRange;
import jp.sfjp.mikutoga.pmd.model.BoneInfo;
import jp.sfjp.mikutoga.pmd.model.DynamicsInfo;
import jp.sfjp.mikutoga.pmd.model.JointInfo;
import jp.sfjp.mikutoga.pmd.model.PmdModel;
import jp.sfjp.mikutoga.pmd.model.RigidGroup;
import jp.sfjp.mikutoga.pmd.model.RigidInfo;
import jp.sfjp.mikutoga.pmd.model.RigidShape;

/**
 * PMDファイルのエクスポーター(拡張3:物理演算対応)。
 *
 * <p>物理演算対応のPMDファイルフォーマットを
 * 使いたい場合はこのエクスポーターを用いて出力せよ。
 */
public class PmdExporterExt3 extends PmdExporterExt2{

    private static final short MASK_FULLCOLLISION = (short) 0xffff;

    /**
     * コンストラクタ。
     *
     * @param stream 出力ストリーム
     * @throws NullPointerException 引数がnull
     */
    public PmdExporterExt3(OutputStream stream)
            throws NullPointerException{
        super(stream);
        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param model {@inheritDoc}
     * @throws IOException {@inheritDoc}
     * @throws IllegalPmdDataException {@inheritDoc}
     */
    @Override
    public void dumpPmdModel(PmdModel model)
            throws IOException, IllegalPmdDataException{
        super.dumpPmdModel(model);

        try{
            dumpRigidList(model);
            dumpJointList(model);
        }catch(IllegalTextExportException e){
            throw new IllegalPmdDataException(e);
        }

        return;
    }

    /**
     * 剛体リストを出力する。
     *
     * @param model モデルデータ
     * @throws IOException 出力エラー
     * @throws IllegalTextExportException 長すぎる剛体名
     */
    private void dumpRigidList(PmdModel model)
            throws IOException, IllegalTextExportException{
        List<RigidInfo> rigidList = model.getRigidList();
        int rigidNum = rigidList.size();
        dumpLeInt(rigidNum);

        for(RigidInfo rigid : rigidList){
            dumpRigid(rigid);
        }

        flush();

        return;
    }

    /**
     * 個別の剛体情報を出力する。
     *
     * @param rigid 剛体
     * @throws IOException 出力エラー
     * @throws IllegalTextExportException 長すぎる剛体名
     */
    private void dumpRigid(RigidInfo rigid)
            throws IOException, IllegalTextExportException{
        String rigidName = rigid.getRigidName().getPrimaryText();
        dumpText(rigidName, PmdConst.MAXBYTES_RIGIDNAME);

        BoneInfo linkedBone = rigid.getLinkedBone();
        if(linkedBone == null){
            dumpLeShort(-1);
        }else{
            dumpLeShort(linkedBone.getSerialNumber());
        }

        RigidGroup group = rigid.getRigidGroup();
        dumpByte(group.getSerialNumber());

        short mask = MASK_FULLCOLLISION;
        for(RigidGroup throughGroup : rigid.getThroughGroupColl()){
            int serialId = throughGroup.getSerialNumber();
            mask &= ~(0x0001 << serialId);
        }
        dumpLeShort(mask);

        dumpRigidShape(rigid.getRigidShape());

        dumpPos3D(rigid.getPosition());
        dumpRad3d(rigid.getRotation());

        dumpDynamics(rigid.getDynamicsInfo());

        dumpByte(rigid.getBehaviorType().encode());

        return;
    }

    /**
     * 剛体形状を出力する。
     *
     * @param shape 剛体形状
     * @throws IOException 出力エラー
     */
    private void dumpRigidShape(RigidShape shape)
            throws IOException{
        RigidShapeType type = shape.getShapeType();
        dumpByte(type.encode());

        float width = shape.getWidth();
        float height = shape.getHeight();
        float depth = shape.getDepth();

        dumpLeFloat(width);
        dumpLeFloat(height);
        dumpLeFloat(depth);

        return;
    }

    /**
     * 力学設定を出力する。
     *
     * @param dynamics 力学設定
     * @throws IOException 出力エラー
     */
    private void dumpDynamics(DynamicsInfo dynamics)
            throws IOException{
        float mass        = dynamics.getMass();
        float dampPos     = dynamics.getDampingPosition();
        float dampRot     = dynamics.getDampingRotation();
        float restitution = dynamics.getRestitution();
        float friction    = dynamics.getFriction();

        dumpLeFloat(mass);
        dumpLeFloat(dampPos);
        dumpLeFloat(dampRot);
        dumpLeFloat(restitution);
        dumpLeFloat(friction);

        return;
    }

    /**
     * ジョイントリストを出力する。
     *
     * @param model モデルデータ
     * @throws IOException 出力エラー
     * @throws IllegalTextExportException 長すぎるジョイント名
     */
    private void dumpJointList(PmdModel model)
            throws IOException, IllegalTextExportException{
        List<JointInfo> jointList = model.getJointList();
        int jointNum = jointList.size();
        dumpLeInt(jointNum);

        for(JointInfo joint : jointList){
            dumpJoint(joint);
        }

        flush();

        return;
    }

    /**
     * 個別のジョイント情報を出力する。
     *
     * @param joint ジョイント
     * @throws IOException 出力エラー
     * @throws IllegalTextExportException 長すぎるジョイント名
     */
    private void dumpJoint(JointInfo joint)
            throws IOException, IllegalTextExportException{
        String jointName = joint.getJointName().getPrimaryText();
        dumpText(jointName, PmdConst.MAXBYTES_JOINTNAME);

        RigidInfo rigidA = joint.getRigidA();
        RigidInfo rigidB = joint.getRigidB();

        dumpLeInt(rigidA.getSerialNumber());
        dumpLeInt(rigidB.getSerialNumber());

        dumpPos3D(joint.getPosition());
        dumpRad3d(joint.getRotation());

        dumpTripletRange(joint.getPositionRange());
        dumpTripletRange(joint.getRotationRange());

        dumpPos3D(joint.getElasticPosition());
        dumpDeg3d(joint.getElasticRotation());

        return;
    }

    /**
     * 3次元範囲制約を出力する。
     *
     * @param range 3次元範囲制約
     * @throws IOException 出力エラー
     */
    protected void dumpTripletRange(TripletRange range) throws IOException{
        float xFrom = range.getXFrom();
        float yFrom = range.getYFrom();
        float zFrom = range.getZFrom();

        dumpLeFloat(xFrom);
        dumpLeFloat(yFrom);
        dumpLeFloat(zFrom);

        float xTo = range.getXTo();
        float yTo = range.getYTo();
        float zTo = range.getZTo();

        dumpLeFloat(xTo);
        dumpLeFloat(yTo);
        dumpLeFloat(zTo);

        return;
    }

    /**
     * ラジアンによる3次元姿勢情報を出力する。
     *
     * @param rad 3次元姿勢情報
     * @throws IOException 出力エラー
     */
    protected void dumpRad3d(Rad3d rad) throws IOException{
        float xVal = rad.getXRad();
        float yVal = rad.getYRad();
        float zVal = rad.getZRad();

        dumpLeFloat(xVal);
        dumpLeFloat(yVal);
        dumpLeFloat(zVal);

        return;
    }

    /**
     * 度数法による3次元姿勢情報を出力する。
     *
     * @param deg 3次元姿勢情報
     * @throws IOException 出力エラー
     */
    protected void dumpDeg3d(Deg3d deg) throws IOException{
        float xVal = deg.getXDeg();
        float yVal = deg.getYDeg();
        float zVal = deg.getZDeg();

        dumpLeFloat(xVal);
        dumpLeFloat(yVal);
        dumpLeFloat(zVal);

        return;
    }

}
