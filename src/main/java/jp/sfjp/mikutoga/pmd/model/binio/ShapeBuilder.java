/*
 * building shape information
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.binio;

import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;
import jp.sfjp.mikutoga.bin.parser.ParseStage;
import jp.sfjp.mikutoga.math.MkPos2D;
import jp.sfjp.mikutoga.math.MkPos3D;
import jp.sfjp.mikutoga.math.MkVec3D;
import jp.sfjp.mikutoga.pmd.model.BoneInfo;
import jp.sfjp.mikutoga.pmd.model.ListUtil;
import jp.sfjp.mikutoga.pmd.model.PmdModel;
import jp.sfjp.mikutoga.pmd.model.Surface;
import jp.sfjp.mikutoga.pmd.model.Vertex;
import jp.sfjp.mikutoga.pmd.parser.PmdShapeHandler;

/**
 * モデル形状に関する通知をパーサから受け取る。
 */
class ShapeBuilder implements PmdShapeHandler {

    private final List<Vertex> vertexList;
    private final List<BoneInfo> boneList;
    private final List<Surface> surfaceList;

    private Iterator<Vertex> vertexIt;
    private Vertex currentVertex = null;

    private Iterator<Surface> surfaceIt;
    private Surface currentSurface = null;

    /**
     * コンストラクタ。
     * @param model モデル
     */
    ShapeBuilder(PmdModel model){
        super();

        this.vertexList  = model.getVertexList();
        this.boneList    = model.getBoneList();
        this.surfaceList = model.getSurfaceList();

        assert this.vertexList instanceof RandomAccess;
        assert this.surfaceList instanceof RandomAccess;
        assert this.boneList instanceof RandomAccess;

        return;
    }

    /**
     * ボーンリスト上にボーンを用意する。
     * すでに指定位置にボーンがあればなにもしない。
     * @param id 0から始まるボーン番号
     * @return 用意されたボーン
     */
    private BoneInfo prepareBone(int id){
        ListUtil.extendList(this.boneList, id + 1);
        BoneInfo bone = this.boneList.get(id);
        if(bone == null){
            bone = new BoneInfo();
            bone.setSerialNumber(id);
            this.boneList.set(id, bone);
        }
        return bone;
    }

    /**
     * {@inheritDoc}
     * @param stage {@inheritDoc}
     * @param loops {@inheritDoc}
     */
    @Override
    public void loopStart(ParseStage stage, int loops){
        if(stage == PmdShapeHandler.VERTEX_LIST){
            ListUtil.prepareDefConsList(this.vertexList, Vertex.class, loops);
            ListUtil.assignIndexedSerial(this.vertexList);

            this.vertexIt = this.vertexList.iterator();
            if(this.vertexIt.hasNext()){
                this.currentVertex = this.vertexIt.next();
            }
        }else if(stage == PmdShapeHandler.SURFACE_LIST){
            ListUtil.prepareDefConsList(this.surfaceList,
                                        Surface.class, loops );
            ListUtil.assignIndexedSerial(this.surfaceList);

            this.surfaceIt = this.surfaceList.iterator();
            if(this.surfaceIt.hasNext()){
                this.currentSurface = this.surfaceIt.next();
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
        if(stage == PmdShapeHandler.VERTEX_LIST){
            if(this.vertexIt.hasNext()){
                this.currentVertex = this.vertexIt.next();
            }
        }else if(stage == PmdShapeHandler.SURFACE_LIST){
            if(this.surfaceIt.hasNext()){
                this.currentSurface = this.surfaceIt.next();
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
     * @param xPos {@inheritDoc}
     * @param yPos {@inheritDoc}
     * @param zPos {@inheritDoc}
     */
    @Override
    public void pmdVertexPosition(float xPos, float yPos, float zPos){
        MkPos3D position = this.currentVertex.getPosition();
        position.setXpos(xPos);
        position.setYpos(yPos);
        position.setZpos(zPos);
        return;
    }

    /**
     * {@inheritDoc}
     * @param xVec {@inheritDoc}
     * @param yVec {@inheritDoc}
     * @param zVec {@inheritDoc}
     */
    @Override
    public void pmdVertexNormal(float xVec, float yVec, float zVec){
        MkVec3D normal = this.currentVertex.getNormal();
        normal.setXVal(xVec);
        normal.setYVal(yVec);
        normal.setZVal(zVec);
        return;
    }

    /**
     * {@inheritDoc}
     * @param uVal {@inheritDoc}
     * @param vVal {@inheritDoc}
     */
    @Override
    public void pmdVertexUV(float uVal, float vVal){
        MkPos2D uv = this.currentVertex.getUVPosition();
        uv.setXpos(uVal);
        uv.setYpos(vVal);
        return;
    }

    /**
     * {@inheritDoc}
     * @param boneId1 {@inheritDoc}
     * @param boneId2 {@inheritDoc}
     * @param weightForB1 {@inheritDoc}
     */
    @Override
    public void pmdVertexWeight(int boneId1, int boneId2, int weightForB1){
        BoneInfo bone1 = prepareBone(boneId1);
        BoneInfo bone2 = prepareBone(boneId2);

        this.currentVertex.setBonePair(bone1, bone2);
        this.currentVertex.setWeightA(weightForB1);

        return;
    }

    /**
     * {@inheritDoc}
     * @param hideEdge {@inheritDoc}
     */
    @Override
    public void pmdVertexEdge(boolean hideEdge){
        boolean appearFlag = ! hideEdge;
        this.currentVertex.setEdgeAppearance(appearFlag);
        return;
    }

    /**
     * {@inheritDoc}
     * @param vertexId1 {@inheritDoc}
     * @param vertexId2 {@inheritDoc}
     * @param vertexId3 {@inheritDoc}
     */
    @Override
    public void pmdSurfaceTriangle(int vertexId1,
                                      int vertexId2,
                                      int vertexId3 ){
        Vertex vtx1 = this.vertexList.get(vertexId1);
        Vertex vtx2 = this.vertexList.get(vertexId2);
        Vertex vtx3 = this.vertexList.get(vertexId3);

        this.currentSurface.setTriangle(vtx1, vtx2, vtx3);

        return;
    }

}
