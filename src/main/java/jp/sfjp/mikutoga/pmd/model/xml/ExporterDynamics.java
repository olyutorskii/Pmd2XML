/*
 * dynamics xml exporter
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.xml;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import jp.sfjp.mikutoga.corelib.I18nText;
import jp.sfjp.mikutoga.math.MkPos3D;
import jp.sfjp.mikutoga.pmd.Deg3d;
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
import jp.sfjp.mikutoga.xml.ProxyXmlExporter;

/**
 * 剛体力学設定のXMLエクスポーター。
 */
class ExporterDynamics extends ProxyXmlExporter {

    private static final String LEAD_REF = "Ref:";
    private static final String RIGIDBEHAVIOR_COMMENT =
          "Rigid behavior types:\n"
        + "[0 : FOLLOWBONE    : ボーン追従       ]\n"
        + "[1 : ONLYDYNAMICS  : 物理演算         ]\n"
        + "[2 : BONEDDYNAMICS : ボーン位置合わせ ]\n";


    private final ExtraExporter exp;


    /**
     * コンストラクタ。
     * @param delegate 委譲先
     */
    ExporterDynamics(PmdXmlExporter delegate) {
        super(delegate);
        this.exp = new ExtraExporter(delegate);
        return;
    }

    /**
     * 剛体リストを出力する。
     * @param model モデルデータ
     * @throws IOException 出力エラー
     */
    void putRigidList(PmdModel model)
            throws IOException{
        ind().putSimpleSTag(PmdTag.RIGID_LIST.tag()).ln();
        pushNest();

        boolean dumped = false;
        for(RigidInfo rigid : model.getRigidList()){
            if( ! dumped ){
                ln().putBlockComment(RIGIDBEHAVIOR_COMMENT).ln();
            }
            putRigid(rigid);
            dumped = true;
        }

        popNest();
        ind().putETag(PmdTag.RIGID_LIST.tag()).ln(2);

        return;
    }

    /**
     * 個別の剛体情報を出力する。
     * @param rigid 剛体情報
     * @throws IOException 出力エラー
     */
    private void putRigid(RigidInfo rigid) throws IOException{
        I18nText i18nName = rigid.getRigidName();
        String primary = i18nName.getPrimaryText();

        this.exp.putLocalNameComment(i18nName);
        ln();

        ind().putOpenSTag(PmdTag.RIGID.tag()).sp();
        putAttr(PmdAttr.NAME.attr(), primary).sp();
        this.exp.putNumberedIdAttr(PmdAttr.RIGID_ID,
                ExtraExporter.PFX_RIGID, rigid);
        sp();
        putAttr(PmdAttr.BEHAVIOR.attr(),
                rigid.getBehaviorType().name());
        sp().putCloseSTag().ln();
        pushNest();

        this.exp.putI18nName(i18nName);
        putRigidBody(rigid);

        popNest();
        ind().putETag(PmdTag.RIGID.tag()).ln(2);

        return;
    }

    /**
     * 剛体の詳細を出力する。
     * @param rigid 剛体情報
     * @throws IOException 出力エラー
     */
    private void putRigidBody(RigidInfo rigid) throws IOException{
        BoneInfo linkedBone = rigid.getLinkedBone();
        putLinkedBone(linkedBone);

        RigidShape shape = rigid.getRigidShape();
        putRigidShape(shape);

        MkPos3D position = rigid.getPosition();
        ind();
        this.exp.putPosition(position);
        ln();

        Rad3d rotation = rigid.getRotation();
        ind();
        this.exp.putRadRotation(rotation);
        ln();

        DynamicsInfo dynamics = rigid.getDynamicsInfo();
        putDynamics(dynamics);
        ln();

        Collection<RigidGroup> throughGroup = rigid.getThroughGroupColl();
        putThroughRigid(throughGroup);

        return;
    }

    /**
     * 剛体接続ボーンを出力する。
     * @param linkedBone 接続ボーン
     * @throws IOException 出力エラー
     */
    private void putLinkedBone(BoneInfo linkedBone) throws IOException{
        if(linkedBone == null) return;

        ind().putOpenSTag(PmdTag.LINKED_BONE.tag()).sp();
        this.exp.putNumberedIdAttr(PmdAttr.BONE_IDREF,
                ExtraExporter.PFX_BONE,
                linkedBone);
        sp();
        putCloseEmpty();
        sp().putLineComment(LEAD_REF + linkedBone.getBoneName().getText());
        ln(2);

        return;
    }

    /**
     * 剛体形状を出力する。
     * @param shape 剛体形状
     * @throws IOException 出力エラー
     */
    private void putRigidShape(RigidShape shape) throws IOException{
        RigidShapeType type = shape.getShapeType();

        switch(type){
        case BOX:
            ind().putOpenSTag(PmdTag.RIGID_SHAPE_BOX.tag()).sp();
            putFloatAttr(PmdAttr.WIDTH.attr(),
                    shape.getWidth()).sp();
            putFloatAttr(PmdAttr.HEIGHT.attr(),
                    shape.getHeight()).sp();
            putFloatAttr(PmdAttr.DEPTH.attr(),
                    shape.getDepth()).sp();
            break;
        case SPHERE:
            ind().putOpenSTag(PmdTag.RIGID_SHAPE_SPHERE.tag()).sp();
            putFloatAttr(PmdAttr.RADIUS.attr(),
                    shape.getRadius()).sp();
            break;
        case CAPSULE:
            ind().putOpenSTag(PmdTag.RIGID_SHAPE_CAPSULE.tag()).sp();
            putFloatAttr(PmdAttr.HEIGHT.attr(),
                    shape.getHeight()).sp();
            putFloatAttr(PmdAttr.RADIUS.attr(),
                    shape.getRadius()).sp();
            break;
        default:
            assert false;
            throw new AssertionError();
        }

        putCloseEmpty().ln();

        return;
    }

    /**
     * 力学設定を出力する。
     * @param dynamics 力学設定
     * @throws IOException 出力エラー
     */
    private void putDynamics(DynamicsInfo dynamics)
            throws IOException{
        ind().putOpenSTag(PmdTag.DYNAMICS.tag()).ln();
        pushNest();
        ind().putFloatAttr(PmdAttr.MASS.attr(),
                dynamics.getMass()).ln();
        ind().putFloatAttr(PmdAttr.DAMPING_POSITION.attr(),
                dynamics.getDampingPosition()).ln();
        ind().putFloatAttr(PmdAttr.DAMPING_ROTATION.attr(),
                dynamics.getDampingRotation()).ln();
        ind().putFloatAttr(PmdAttr.RESTITUTION.attr(),
                dynamics.getRestitution()).ln();
        ind().putFloatAttr(PmdAttr.FRICTION.attr(),
                dynamics.getFriction()).ln();
        popNest();
        ind().putCloseEmpty().ln();

        return;
    }

    /**
     * 通過剛体グループを出力する。
     * @param groupColl 通過剛体グループ群
     * @throws IOException 出力エラー
     */
    private void putThroughRigid(Collection<RigidGroup> groupColl)
            throws IOException{
        for(RigidGroup group : groupColl){
            ind().putOpenSTag(PmdTag.THROUGH_RIGID_GROUP.tag()).sp();
            this.exp
                    .putNumberedIdAttr(PmdAttr.RIGID_GROUP_IDREF,
                              ExtraExporter.PFX_RIGIDGROUP,
                              group.getSerialNumber() + 1);
            sp();
            putCloseEmpty().ln();
        }

        return;
    }

    /**
     * 剛体グループリストを出力する。
     * @param model モデルデータ
     * @throws IOException 出力エラー
     */
    void putRigidGroupList(PmdModel model)
            throws IOException{
        ind().putSimpleSTag(PmdTag.RIGID_GROUP_LIST.tag()).ln(2);
        pushNest();

        boolean singleLast = false;
        for(RigidGroup group : model.getRigidGroupList()){
            List<RigidInfo> rigidList = group.getRigidList();
            if(singleLast &&  ! rigidList.isEmpty()){
                ln();
            }
            ind().putOpenSTag(PmdTag.RIGID_GROUP.tag()).sp();
            this.exp.putNumberedIdAttr(PmdAttr.RIGID_GROUP_ID,
                              ExtraExporter.PFX_RIGIDGROUP,
                              group.getSerialNumber() + 1);
            sp();
            if(rigidList.isEmpty()){
                putCloseEmpty().ln();
                singleLast = true;
                continue;
            }
            putCloseSTag().ln();
            pushNest();

            for(RigidInfo rigid : rigidList){
                ind().putOpenSTag(PmdTag.RIGID_GROUP_MEMBER.tag()).sp();
                this.exp.putNumberedIdAttr(PmdAttr.RIGID_IDREF,
                        ExtraExporter.PFX_RIGID, rigid);
                sp();
                putCloseEmpty();
                sp();
                putLineComment(LEAD_REF + rigid.getRigidName().getText());
                ln();
            }

            popNest();
            ind().putETag(PmdTag.RIGID_GROUP.tag()).ln(2);
            singleLast = false;
        }

        if(singleLast){
            ln();
        }

        popNest();
        ind().putETag(PmdTag.RIGID_GROUP_LIST.tag()).ln(2);

        return;
    }

    /**
     * ジョイントリストを出力する。
     * @param model モデルデータ
     * @throws IOException 出力エラー
     */
    void putJointList(PmdModel model)
            throws IOException{
        ind().putSimpleSTag(PmdTag.JOINT_LIST.tag()).ln();

        pushNest();
        boolean dumped = false;
        List<JointInfo> jointList = model.getJointList();
        for(JointInfo joint : jointList){
            if( ! dumped ) ln();
            putJoint(joint);
            dumped = true;
        }
        popNest();

        ind().putETag(PmdTag.JOINT_LIST.tag()).ln(2);

        return;
    }

    /**
     * 個別のジョイント情報を出力する。
     * @param joint ジョイント情報
     * @throws IOException 出力エラー
     */
    private void putJoint(JointInfo joint)
            throws IOException{
        I18nText i18nName = joint.getJointName();

        this.exp.putLocalNameComment(i18nName);
        ln();
        ind().putOpenSTag(PmdTag.JOINT.tag()).sp();
        this.exp.putPrimaryNameAttr(PmdAttr.NAME, i18nName);
        sp().putCloseSTag().ln();
        pushNest();

        this.exp.putI18nName(i18nName);

        RigidInfo rigidA = joint.getRigidA();
        RigidInfo rigidB = joint.getRigidB();

        ind();
        putLineComment(
                "["
                        + rigidA.getRigidName().getText()
                + "]\u0020<=>\u0020["
                        + rigidB.getRigidName().getText()
                + "]");
        ln();

        ind().putOpenSTag(PmdTag.JOINTED_RIGID_PAIR.tag()).sp();
        this.exp.putNumberedIdAttr(PmdAttr.RIGID_IDREF_1,
                ExtraExporter.PFX_RIGID, rigidA);
        sp();
        this.exp.putNumberedIdAttr(PmdAttr.RIGID_IDREF_2,
                ExtraExporter.PFX_RIGID, rigidB);
        sp();
        putCloseEmpty().ln(2);

        putJointLimit(joint);
        putJointElastic(joint);

        popNest();
        ind().putETag(PmdTag.JOINT.tag()).ln(2);

        return;
    }

    /**
     * ジョイント制限情報を出力する。
     * @param joint ジョイント情報
     * @throws IOException 出力エラー
     */
    private void putJointLimit(JointInfo joint)
            throws IOException{
        MkPos3D position = joint.getPosition();
        ind();
        this.exp.putPosition(position);
        ln();

        ind().putOpenSTag(PmdTag.LIMIT_POSITION.tag()).ln();
        pushNest();
        TripletRange posRange = joint.getPositionRange();
        putTripletRangeAttr(posRange);
        popNest();
        ind().putCloseEmpty().ln(2);

        Rad3d rotation = joint.getRotation();
        ind();
        this.exp.putRadRotation(rotation);
        ln();

        ind().putOpenSTag(PmdTag.LIMIT_ROTATION.tag()).ln();
        pushNest();
        TripletRange rotRange = joint.getRotationRange();
        putTripletRangeAttr(rotRange);
        popNest();
        ind().putCloseEmpty().ln(2);

        return;
    }

    /**
     * XYZ範囲属性を出力する。
     * @param range XYZ範囲
     * @throws IOException 出力エラー
     */
    private void putTripletRangeAttr(TripletRange range)
            throws IOException{
        ind();
        putFloatAttr(PmdAttr.X_FROM.attr(), range.getXFrom()).sp();
        putFloatAttr(PmdAttr.X_TO.attr(),   range.getXTo()  ).ln();

        ind();
        putFloatAttr(PmdAttr.Y_FROM.attr(), range.getYFrom()).sp();
        putFloatAttr(PmdAttr.Y_TO.attr(),   range.getYTo()  ).ln();

        ind();
        putFloatAttr(PmdAttr.Z_FROM.attr(), range.getZFrom()).sp();
        putFloatAttr(PmdAttr.Z_TO.attr(),   range.getZTo()  ).ln();

        return;
    }

    /**
     * ジョイント弾性情報を出力する。
     * @param joint ジョイント情報
     * @throws IOException 出力エラー
     */
    private void putJointElastic(JointInfo joint)
            throws IOException{
        ind().putOpenSTag(PmdTag.ELASTIC_POSITION.tag()).sp();
        MkPos3D elaPosition = joint.getElasticPosition();
        putFloatAttr(PmdAttr.X.attr(),
                (float) elaPosition.getXpos()).sp();
        putFloatAttr(PmdAttr.Y.attr(),
                (float) elaPosition.getYpos()).sp();
        putFloatAttr(PmdAttr.Z.attr(),
                (float) elaPosition.getZpos()).sp();
        putCloseEmpty().ln();

        ind().putOpenSTag(PmdTag.ELASTIC_ROTATION.tag()).sp();
        Deg3d elaRotation = joint.getElasticRotation();
        putFloatAttr(PmdAttr.X_DEG.attr(),
                elaRotation.getXDeg()).sp();
        putFloatAttr(PmdAttr.Y_DEG.attr(),
                elaRotation.getYDeg()).sp();
        putFloatAttr(PmdAttr.Z_DEG.attr(),
                elaRotation.getZDeg()).sp();
        putCloseEmpty().ln(2);

        return;
    }

}
