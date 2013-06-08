/*
 * shape listener from XML
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.xml;

import java.util.List;
import jp.sfjp.mikutoga.math.MkPos2D;
import jp.sfjp.mikutoga.math.MkPos3D;
import jp.sfjp.mikutoga.math.MkVec3D;
import jp.sfjp.mikutoga.pmd.model.BoneInfo;
import jp.sfjp.mikutoga.pmd.model.ListUtil;
import jp.sfjp.mikutoga.pmd.model.Surface;
import jp.sfjp.mikutoga.pmd.model.Vertex;

/*
    + surfaceGroupList
        + surfaceGroup
            + surface
    + vertexList
        + vertex
            + position
            + normal
            + uvMap
            + skinning
*/

/**
 * 形状関連のXML要素出現イベントを受信する。
 */
class SaxShapeListener extends SaxListener{

    private final RefHelper helper;

    private String currentSurfaceGroupId = null;
    private Vertex currentVertex = null;


    /**
     * コンストラクタ。
     * @param helper 参照ヘルパ
     */
    SaxShapeListener(RefHelper helper) {
        super();
        this.helper = helper;
        return;
    }


    /**
     * surfaceGroupListタグ終了の通知を受け取る。
     */
    @CloseXmlMark(PmdTag.SURFACE_GROUP_LIST)
    void closeSurfaceGroupList(){
        this.helper.resolveMaterialSurfaceGroupId();
        return;
    }

    /**
     * surfaceGroupタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.SURFACE_GROUP)
    void openSurfaceGroup(){
        String surfaceGroupId = getStringAttr(PmdAttr.SURFACE_GROUP_ID);
        this.currentSurfaceGroupId = surfaceGroupId;

        return;
    }

    /**
     * surfaceGroupタグ終了の通知を受け取る。
     */
    @CloseXmlMark(PmdTag.SURFACE_GROUP)
    void closeSurfaceGroup(){
        this.currentSurfaceGroupId = null;
        return;
    }

    /**
     * surfaceタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.SURFACE)
    void openSurface(){
        Surface surface = new Surface();

        String vtxIdRef1 = getStringAttr(PmdAttr.VERTEX_IDREF_1);
        String vtxIdRef2 = getStringAttr(PmdAttr.VERTEX_IDREF_2);
        String vtxIdRef3 = getStringAttr(PmdAttr.VERTEX_IDREF_3);

        this.helper.addSurfaceGroup(this.currentSurfaceGroupId, surface);
        this.helper.addSurfaceVertex(surface,
                                     vtxIdRef1,
                                     vtxIdRef2,
                                     vtxIdRef3 );

        List<Surface> surfaceList = getPmdModel().getSurfaceList();
        surfaceList.add(surface);

        return;
    }

    /**
     * vertexタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.VERTEX)
    void openVertex(){
        this.currentVertex = new Vertex();

        String vtxId     = getStringAttr(PmdAttr.VERTEX_ID);
        boolean showEdge = getBooleanAttr(PmdAttr.SHOW_EDGE);

        this.currentVertex.setEdgeAppearance(showEdge);

        this.helper.addVertexId(vtxId, this.currentVertex);

        return;
    }

    /**
     * vertexタグ終了の通知を受け取る。
     */
    @CloseXmlMark(PmdTag.VERTEX)
    void closeVertex(){
        List<Vertex> vertexList = getPmdModel().getVertexList();

        vertexList.add(this.currentVertex);

        this.currentVertex = null;

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

        MkPos3D pos = this.currentVertex.getPosition();
        pos.setPosition(x, y, z);

        return;
    }

    /**
     * normalタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.NORMAL)
    void openNormal(){
        float x = getFloatAttr(PmdAttr.X);
        float y = getFloatAttr(PmdAttr.Y);
        float z = getFloatAttr(PmdAttr.Z);

        MkVec3D normal = this.currentVertex.getNormal();
        normal.setVector(x, y, z);

        return;
    }

    /**
     * uvMapタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.UV_MAP)
    void openUvMap(){
        float u = getFloatAttr(PmdAttr.U);
        float v = getFloatAttr(PmdAttr.V);

        MkPos2D pos = this.currentVertex.getUVPosition();
        pos.setPosition(u, v);

        return;
    }

    /**
     * skinningタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.SKINNING)
    void openSkinning(){
        String boneIdRef1 = getStringAttr(PmdAttr.BONE_IDREF_1);
        String boneIdRef2 = getStringAttr(PmdAttr.BONE_IDREF_2);
        BoneInfo bone1 = this.helper.findBoneId(boneIdRef1);
        BoneInfo bone2 = this.helper.findBoneId(boneIdRef2);

        int weightBalance = getIntAttr(PmdAttr.WEIGHT_BALANCE);

        this.currentVertex.setBonePair(bone1, bone2);
        this.currentVertex.setWeightA(weightBalance);

        return;
    }

    /**
     * vertexListタグ終了の通知を受け取る。
     */
    @CloseXmlMark(PmdTag.VERTEX_LIST)
    void closeVertexList(){
        this.helper.resolveMorphVertexIdRef();
        this.helper.resolveSurfaceVertexIdRef();

        List<Vertex> vertexList = getPmdModel().getVertexList();
        ListUtil.assignIndexedSerial(vertexList);

        return;
    }

}
