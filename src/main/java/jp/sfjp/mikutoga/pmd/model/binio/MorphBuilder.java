/*
 * building morph information
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.binio;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import jp.sfjp.mikutoga.bin.parser.ParseStage;
import jp.sfjp.mikutoga.math.MkPos3D;
import jp.sfjp.mikutoga.pmd.MorphType;
import jp.sfjp.mikutoga.pmd.model.ListUtil;
import jp.sfjp.mikutoga.pmd.model.MorphPart;
import jp.sfjp.mikutoga.pmd.model.MorphVertex;
import jp.sfjp.mikutoga.pmd.model.PmdModel;
import jp.sfjp.mikutoga.pmd.model.Vertex;
import jp.sfjp.mikutoga.pmd.parser.PmdMorphHandler;

/**
 * モーフ関係の通知をパーサから受け取る。
 */
class MorphBuilder implements PmdMorphHandler {

    private final Map<MorphType, List<MorphPart>> morphMap;

    private List<MorphPart> morphPartList;
    private Iterator<MorphPart> morphPartIt;
    private MorphPart currentMorphPart;
    private final List<Vertex> vertexList;

    private final List<Vertex> morphVertexList = new ArrayList<Vertex>();

    /**
     * コンストラクタ。
     * @param model モデル
     */
    MorphBuilder(PmdModel model){
        super();
        this.vertexList = model.getVertexList();
        this.morphMap = model.getMorphMap();
        return;
    }

    /**
     * PMDファイル中の出現順で各モーフを格納するためのリストを設定する。
     * 主な用途はモーフ英名との突き合わせ作業。
     * @param list モーフ格納リスト
     */
    void setMorphPartList(List<MorphPart> list){
        this.morphPartList = list;
        return;
    }

    /**
     * {@inheritDoc}
     * @param stage {@inheritDoc}
     * @param loops {@inheritDoc}
     */
    @Override
    public void loopStart(ParseStage stage, int loops){
        if(stage == PmdMorphHandler.MORPH_LIST){
            ListUtil.prepareDefConsList(this.morphPartList,
                                        MorphPart.class,
                                        loops );
            ListUtil.assignIndexedSerial(this.morphPartList);

            this.morphPartIt = this.morphPartList.iterator();
            if(this.morphPartIt.hasNext()){
                this.currentMorphPart = this.morphPartIt.next();
            }
        }else if(stage == PmdMorphHandler.MORPHVERTEX_LIST){
            // NOTHING
        }else if(stage == PmdMorphHandler.MORPHORDER_LIST){
            // NOTHING
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
        if(stage == PmdMorphHandler.MORPH_LIST){
            if(this.morphPartIt.hasNext()){
                this.currentMorphPart = this.morphPartIt.next();
            }
        }else if(stage == PmdMorphHandler.MORPHVERTEX_LIST){
            // NOTHING
        }else if(stage == PmdMorphHandler.MORPHORDER_LIST){
            // NOTHING
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
        if(stage == PmdMorphHandler.MORPH_LIST){
            // NOTHING
        }else if(stage == PmdMorphHandler.MORPHVERTEX_LIST){
            // NOTHING
        }else if(stage == PmdMorphHandler.MORPHORDER_LIST){
            // NOTHING
        }else{
            assert false;
            throw new AssertionError();
        }
        return;
    }

    /**
     * {@inheritDoc}
     * @param morphName {@inheritDoc}
     * @param morphType {@inheritDoc}
     */
    @Override
    public void pmdMorphInfo(String morphName, byte morphType){
        this.currentMorphPart.getMorphName().setPrimaryText(morphName);
        MorphType type = MorphType.decode(morphType);
        this.currentMorphPart.setMorphType(type);

        return;
    }

    /**
     * {@inheritDoc}
     * @param serialId {@inheritDoc}
     * @param xPos {@inheritDoc}
     * @param yPos {@inheritDoc}
     * @param zPos {@inheritDoc}
     */
    @Override
    public void pmdMorphVertexInfo(int serialId,
                                   float xPos, float yPos, float zPos){
        MorphVertex morphVertex;
        morphVertex = new MorphVertex();
        MkPos3D position = morphVertex.getOffset();
        position.setXpos(xPos);
        position.setYpos(yPos);
        position.setZpos(zPos);

        Vertex vertex;
        if(this.currentMorphPart.getMorphType().isBase()){
            vertex = this.vertexList.get(serialId);
            this.morphVertexList.add(vertex);
        }else{
            vertex = this.morphVertexList.get(serialId);
        }
        morphVertex.setBaseVertex(vertex);

        this.currentMorphPart.getMorphVertexList().add(morphVertex);

        return;
    }

    /**
     * {@inheritDoc}
     * @param morphId {@inheritDoc}
     */
    @Override
    public void pmdMorphOrderInfo(int morphId){
        MorphPart part = this.morphPartList.get(morphId);
        MorphType type = part.getMorphType();

        List<MorphPart> partList = this.morphMap.get(type);
        if(partList == null){
            partList = new LinkedList<MorphPart>();
            this.morphMap.put(type, partList);
        }
        partList.add(part);

        return;
    }

}
