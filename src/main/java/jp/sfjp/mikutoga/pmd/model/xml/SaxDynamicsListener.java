/*
 * dynamics listener from XML
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.xml;

import java.util.List;
import jp.sfjp.mikutoga.corelib.I18nText;
import jp.sfjp.mikutoga.math.MkPos3D;
import jp.sfjp.mikutoga.pmd.Deg3d;
import jp.sfjp.mikutoga.pmd.Rad3d;
import jp.sfjp.mikutoga.pmd.RigidBehaviorType;
import jp.sfjp.mikutoga.pmd.RigidShapeType;
import jp.sfjp.mikutoga.pmd.TripletRange;
import jp.sfjp.mikutoga.pmd.model.BoneInfo;
import jp.sfjp.mikutoga.pmd.model.DynamicsInfo;
import jp.sfjp.mikutoga.pmd.model.JointInfo;
import jp.sfjp.mikutoga.pmd.model.ListUtil;
import jp.sfjp.mikutoga.pmd.model.RigidGroup;
import jp.sfjp.mikutoga.pmd.model.RigidInfo;
import jp.sfjp.mikutoga.pmd.model.RigidShape;

/*
    + rigidList
        + rigid
            + i18nName
            + linkedBone
            + rigidShapeSphere
            + rigidShapeBox
            + rigidShapeCapsule
            + position
            + radRotation
            + dynamics
            + throughRigidGroup
    + rigidGroupList
        + rigidGroup
            + rigidGroupMember
    + jointList
        + joint
            + i18nName
            + jointedRigidPair
            + position
            + limitPosition
            + radRotation
            + limitRotation
            + elasticPosition
            + elasticRotation
*/

/**
 * 剛体力学関連のXML要素出現イベントを受信する。
 */
class SaxDynamicsListener extends SaxListener{

    private final RefHelper helper;

    private RigidInfo currentRigid = null;
    private RigidGroup currentRigidGroup = null;
    private JointInfo currentJoint = null;


    /**
     * コンストラクタ。
     * @param helper 参照ヘルパ
     */
    SaxDynamicsListener(RefHelper helper) {
        super();
        this.helper = helper;
        return;
    }


    /**
     * rigidListタグ終了の通知を受け取る。
     */
    @CloseXmlMark(PmdTag.RIGID_LIST)
    void closeRigidList(){
        List<RigidInfo> rigidList = getPmdModel().getRigidList();
        ListUtil.assignIndexedSerial(rigidList);
        return;
    }

    /**
     * rigidタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.RIGID)
    void openRigid(){
        this.currentRigid = new RigidInfo();

        String name = getStringAttr(PmdAttr.NAME);
        I18nText rigidName = this.currentRigid.getRigidName();
        rigidName.setPrimaryText(name);

        String rigidId = getStringAttr(PmdAttr.RIGID_ID);
        this.helper.addRigidId(rigidId, this.currentRigid);

        String behavior = getStringAttr(PmdAttr.BEHAVIOR);
        RigidBehaviorType type = RigidBehaviorType.valueOf(behavior);
        this.currentRigid.setBehaviorType(type);

        return;
    }

    /**
     * rigidタグ終了の通知を受け取る。
     */
    @CloseXmlMark(PmdTag.RIGID)
    void closeRigid(){
        List<RigidInfo> rigidInfoList = getPmdModel().getRigidList();
        rigidInfoList.add(this.currentRigid);

        this.currentRigid = null;

        return;
    }

    /**
     * i18nTextタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.I18N_NAME)
    void openI18nText(){
        I18nText i18nName;
        if(this.currentRigid != null){
            i18nName = this.currentRigid.getRigidName();
        }else if(this.currentJoint != null){
            i18nName = this.currentJoint.getJointName();
        }else{
            assert false;
            throw new AssertionError();
        }

        String lang = getStringAttr(PmdAttr.LANG);
        String name = getStringAttr(PmdAttr.NAME);

        i18nName.setI18nText(lang, name);

        return;
    }

    /**
     * linkedBoneタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.LINKED_BONE)
    void openLinkedBone(){
        String boneIdRef = getStringAttr(PmdAttr.BONE_IDREF);
        BoneInfo linkedBone = this.helper.findBoneId(boneIdRef);
        this.currentRigid.setLinkedBone(linkedBone);

        return;
    }

    /**
     * rigidShapeSphereタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.RIGID_SHAPE_SPHERE)
    void openRigidShapeSphere(){
        RigidShape shape = this.currentRigid.getRigidShape();

        shape.setShapeType(RigidShapeType.SPHERE);

        float radius = getFloatAttr(PmdAttr.RADIUS);
        shape.setRadius(radius);

        return;
    }

    /**
     * rigidShapeBoxタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.RIGID_SHAPE_BOX)
    void openRigidShapeBox(){
        RigidShape shape = this.currentRigid.getRigidShape();

        shape.setShapeType(RigidShapeType.BOX);

        float width  = getFloatAttr(PmdAttr.WIDTH);
        float height = getFloatAttr(PmdAttr.HEIGHT);
        float depth  = getFloatAttr(PmdAttr.DEPTH);

        shape.setWidth(width);
        shape.setHeight(height);
        shape.setDepth(depth);

        return;
    }

    /**
     * rigidShapeCapsuleタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.RIGID_SHAPE_CAPSULE)
    void openRigidShapeCapsule(){
        RigidShape shape = this.currentRigid.getRigidShape();

        shape.setShapeType(RigidShapeType.CAPSULE);

        float height = getFloatAttr(PmdAttr.HEIGHT);
        float radius = getFloatAttr(PmdAttr.RADIUS);

        shape.setHeight(height);
        shape.setRadius(radius);

        return;
    }

    /**
     * positionタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.POSITION)
    void openPosition(){
        MkPos3D pos;
        if(this.currentRigid != null){
            pos = this.currentRigid.getPosition();
        }else if(this.currentJoint != null){
            pos = this.currentJoint.getPosition();
        }else{
            assert false;
            throw new AssertionError();
        }

        float x = getFloatAttr(PmdAttr.X);
        float y = getFloatAttr(PmdAttr.Y);
        float z = getFloatAttr(PmdAttr.Z);

        pos.setPosition(x, y, z);

        return;
    }

    /**
     * radRotationタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.RAD_ROTATION)
    void openRadRotation(){
        Rad3d rad;
        if(this.currentRigid != null){
            rad = this.currentRigid.getRotation();
        }else if(this.currentJoint != null){
            rad = this.currentJoint.getRotation();
        }else{
            assert false;
            throw new AssertionError();
        }

        float xRad = getFloatAttr(PmdAttr.X_RAD);
        float yRad = getFloatAttr(PmdAttr.Y_RAD);
        float zRad = getFloatAttr(PmdAttr.Z_RAD);

        rad.setXRad(xRad);
        rad.setYRad(yRad);
        rad.setZRad(zRad);

        return;
    }

    /**
     * dynamicsタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.DYNAMICS)
    void openDynamics(){

        float mass            = getFloatAttr(PmdAttr.MASS);
        float dampingPosition = getFloatAttr(PmdAttr.DAMPING_POSITION);
        float dampingRotation = getFloatAttr(PmdAttr.DAMPING_ROTATION);
        float restitution     = getFloatAttr(PmdAttr.RESTITUTION);
        float friction        = getFloatAttr(PmdAttr.FRICTION);

        DynamicsInfo dynamics = this.currentRigid.getDynamicsInfo();

        dynamics.setMass(mass);
        dynamics.setDampingPosition(dampingPosition);
        dynamics.setDampingRotation(dampingRotation);
        dynamics.setRestitution(restitution);
        dynamics.setFriction(friction);

        return;
    }

    /**
     * throughRigidGroupタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.THROUGH_RIGID_GROUP)
    void openThroughRigidGroup(){
        String rigidGroupIdRef =
                getStringAttr(PmdAttr.RIGID_GROUP_IDREF);

        this.helper.addThroughRigidGroupIdRef(this.currentRigid,
                                              rigidGroupIdRef );

        return;
    }

    /**
     * rigidGroupタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.RIGID_GROUP)
    void openRigidGroup(){
        RigidGroup rigidGroup = new RigidGroup();
        this.currentRigidGroup = rigidGroup;

        String rigidGroupId = getStringAttr(PmdAttr.RIGID_GROUP_ID);

        this.helper.addRigidGroupId(rigidGroupId, this.currentRigidGroup);

        return;
    }

    /**
     * rigidGroupタグ終了の通知を受け取る。
     */
    @CloseXmlMark(PmdTag.RIGID_GROUP)
    void closeRigidGroup(){
        List<RigidGroup> rigidGroupList = getPmdModel().getRigidGroupList();
        rigidGroupList.add(this.currentRigidGroup);

        this.currentRigidGroup = null;

        return;
    }

    /**
     * rigidGroupMemberタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.RIGID_GROUP_MEMBER)
    void openRigidGroupMember(){
        String rigidIdRef = getStringAttr(PmdAttr.RIGID_IDREF);

        RigidInfo member = this.helper.findRigidId(rigidIdRef);

        List<RigidInfo> memberList = this.currentRigidGroup.getRigidList();
        memberList.add(member);
        member.setRigidGroup(this.currentRigidGroup);

        return;
    }

    /**
     * rigidGroupListタグ終了の通知を受け取る。
     * 剛体グループ総数が定員に満たない場合は自動追加される。
     */
    @CloseXmlMark(PmdTag.RIGID_GROUP_LIST)
    void closeRigidGroupList(){

        this.helper.resolveThroughRigidGroupIdRef();

        List<RigidGroup> rigidGroupList = getPmdModel().getRigidGroupList();

        while(rigidGroupList.size() < RigidGroup.MAX_RIGID_GROUP){
            RigidGroup group = new RigidGroup();
            rigidGroupList.add(group);
        }

        ListUtil.assignIndexedSerial(rigidGroupList);

        return;
    }

    /**
     * jointタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.JOINT)
    void openJoint(){
        this.currentJoint = new JointInfo();

        String name = getStringAttr(PmdAttr.NAME);
        I18nText jointName = this.currentJoint.getJointName();
        jointName.setPrimaryText(name);

        return;
    }

    /**
     * jointタグ終了の通知を受け取る。
     */
    @CloseXmlMark(PmdTag.JOINT)
    void closeJoint(){
        List<JointInfo> jointList = getPmdModel().getJointList();
        jointList.add(this.currentJoint);

        this.currentJoint = null;

        return;
    }

    /**
     * jointedRigidPairタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.JOINTED_RIGID_PAIR)
    void openJointedRigidPair(){
        String rigidIdRef1 = getStringAttr(PmdAttr.RIGID_IDREF_1);
        String rigidIdRef2 = getStringAttr(PmdAttr.RIGID_IDREF_2);

        RigidInfo rigidA = this.helper.findRigidId(rigidIdRef1);
        RigidInfo rigidB = this.helper.findRigidId(rigidIdRef2);

        this.currentJoint.setRigidPair(rigidA, rigidB);

        return;
    }

    /**
     * limitPositionタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.LIMIT_POSITION)
    void openLimitPosition(){
        float xFrom = getFloatAttr(PmdAttr.X_FROM);
        float xTo   = getFloatAttr(PmdAttr.X_TO);

        float yFrom = getFloatAttr(PmdAttr.Y_FROM);
        float yTo   = getFloatAttr(PmdAttr.Y_TO);

        float zFrom = getFloatAttr(PmdAttr.Z_FROM);
        float zTo   = getFloatAttr(PmdAttr.Z_TO);

        TripletRange limitPos = this.currentJoint.getPositionRange();
        limitPos.setXRange(xFrom, xTo);
        limitPos.setYRange(yFrom, yTo);
        limitPos.setZRange(zFrom, zTo);

        return;
    }

    /**
     * limitRotationタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.LIMIT_ROTATION)
    void openLimitRotation(){
        float xFrom = getFloatAttr(PmdAttr.X_FROM);
        float xTo   = getFloatAttr(PmdAttr.X_TO);

        float yFrom = getFloatAttr(PmdAttr.Y_FROM);
        float yTo   = getFloatAttr(PmdAttr.Y_TO);

        float zFrom = getFloatAttr(PmdAttr.Z_FROM);
        float zTo   = getFloatAttr(PmdAttr.Z_TO);

        TripletRange limitRot = this.currentJoint.getRotationRange();
        limitRot.setXRange(xFrom, xTo);
        limitRot.setYRange(yFrom, yTo);
        limitRot.setZRange(zFrom, zTo);

        return;
    }

    /**
     * elasticPositionタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.ELASTIC_POSITION)
    void openElasticPosition(){
        float x = getFloatAttr(PmdAttr.X);
        float y = getFloatAttr(PmdAttr.Y);
        float z = getFloatAttr(PmdAttr.Z);

        MkPos3D pos = this.currentJoint.getElasticPosition();
        pos.setPosition(x, y, z);

        return;
    }

    /**
     * elasticRotationタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.ELASTIC_ROTATION)
    void openElasticRotation(){
        float xDeg = getFloatAttr(PmdAttr.X_DEG);
        float yDeg = getFloatAttr(PmdAttr.Y_DEG);
        float zDeg = getFloatAttr(PmdAttr.Z_DEG);

        Deg3d rot = this.currentJoint.getElasticRotation();
        rot.setXDeg(xDeg);
        rot.setYDeg(yDeg);
        rot.setZDeg(zDeg);

        return;
    }

}
