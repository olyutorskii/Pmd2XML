/*
 * shape xml exporter
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.xml;

import java.io.IOException;
import java.util.List;
import jp.sfjp.mikutoga.math.MkPos2D;
import jp.sfjp.mikutoga.math.MkPos3D;
import jp.sfjp.mikutoga.math.MkVec3D;
import jp.sfjp.mikutoga.pmd.model.BoneInfo;
import jp.sfjp.mikutoga.pmd.model.Material;
import jp.sfjp.mikutoga.pmd.model.PmdModel;
import jp.sfjp.mikutoga.pmd.model.Surface;
import jp.sfjp.mikutoga.pmd.model.Vertex;
import jp.sfjp.mikutoga.xml.ProxyXmlExporter;

/**
 * 形状設定のXMLエクスポーター。
 */
class ExporterShape extends ProxyXmlExporter {

    private final ExtraExporter exp;


    /**
     * コンストラクタ。
     * @param delegate 委譲先
     */
    ExporterShape(PmdXmlExporter delegate) {
        super(delegate);
        this.exp = new ExtraExporter(delegate);
        return;
    }

    /**
     * サーフェイスグループリストを出力する。
     * @param model モデルデータ
     * @throws IOException 出力エラー
     */
    void putSurfaceGroupList(PmdModel model)
            throws IOException{
        ind().putSimpleSTag(PmdTag.SURFACE_GROUP_LIST.tag()).ln();

        pushNest();
        int ct = 0;
        boolean dumped = false;
        List<Material> materialList = model.getMaterialList();
        for(Material material : materialList){
            List<Surface> surfaceList = material.getSurfaceList();
            if( ! dumped ) ln();
            putSurfaceList(surfaceList, ct++);
            dumped = true;
        }
        popNest();

        ind().putETag(PmdTag.SURFACE_GROUP_LIST.tag()).ln(2);

        return;
    }

    /**
     * 個別のサーフェイスグループを出力する。
     * @param surfaceList サーフェイスのリスト
     * @param index グループインデックス
     * @throws IOException 出力エラー
     */
    private void putSurfaceList(List<Surface> surfaceList,
                                              int index)
            throws IOException{
        ind().putOpenSTag(PmdTag.SURFACE_GROUP.tag()).sp();
        this.exp.putNumberedIdAttr(PmdAttr.SURFACE_GROUP_ID,
                          ExtraExporter.PFX_SURFACEGROUP, index);
        sp().putCloseSTag().ln();
        pushNest();

        for(Surface surface : surfaceList){
            putSurface(surface);
        }

        popNest();
        ind().putETag(PmdTag.SURFACE_GROUP.tag()).ln(2);

        return;
    }

    /**
     * 個別のサーフェイスを出力する。
     * @param surface サーフェイス
     * @throws IOException 出力エラー
     */
    private void putSurface(Surface surface)
            throws IOException{
        ind().putOpenSTag(PmdTag.SURFACE.tag()).sp();

        Vertex vertex1 = surface.getVertex1();
        Vertex vertex2 = surface.getVertex2();
        Vertex vertex3 = surface.getVertex3();

        this.exp.putNumberedIdAttr(PmdAttr.VERTEX_IDREF_1,
                          ExtraExporter.PFX_VERTEX, vertex1);
        sp();
        this.exp.putNumberedIdAttr(PmdAttr.VERTEX_IDREF_2,
                          ExtraExporter.PFX_VERTEX, vertex2);
        sp();
        this.exp.putNumberedIdAttr(PmdAttr.VERTEX_IDREF_3,
                          ExtraExporter.PFX_VERTEX, vertex3);
        sp();

        putCloseEmpty().ln();
        return;
    }

    /**
     * 頂点リストを出力する。
     * @param model モデルデータ
     * @throws IOException 出力エラー
     */
    void putVertexList(PmdModel model)
            throws IOException{
        ind().putSimpleSTag(PmdTag.VERTEX_LIST.tag()).ln();

        pushNest();
        boolean dumped = false;
        List<Vertex> vertexList = model.getVertexList();
        for(Vertex vertex : vertexList){
            if( ! dumped ) ln();
            putVertex(vertex);
            dumped = true;
        }
        popNest();

        ind().putETag(PmdTag.VERTEX_LIST.tag()).ln(2);

        return;
    }

    /**
     * 個別の頂点情報を出力する。
     * @param vertex 頂点
     * @throws IOException 出力エラー
     */
    private void putVertex(Vertex vertex)
            throws IOException{
        String bool;
        if(vertex.getEdgeAppearance()) bool = "true";
        else                           bool = "false";

        ind().putOpenSTag(PmdTag.VERTEX.tag()).sp();
        this.exp.putNumberedIdAttr(PmdAttr.VERTEX_ID,
                ExtraExporter.PFX_VERTEX, vertex);
        sp();
        putAttr(PmdAttr.SHOW_EDGE.attr(), bool);
        sp().putCloseSTag().ln();
        pushNest();

        putVertexBody(vertex);

        popNest();
        ind().putETag(PmdTag.VERTEX.tag()).ln(2);

        return;
    }

    /**
     * 頂点情報の詳細を出力する。
     * @param vertex 頂点
     * @throws IOException 出力エラー
     */
    private void putVertexBody(Vertex vertex)
            throws IOException{
        MkPos3D position = vertex.getPosition();
        ind();
        this.exp.putPosition(position);
        ln();

        MkVec3D normal = vertex.getNormal();
        ind().putOpenSTag(PmdTag.NORMAL.tag()).sp();
        putFloatAttr(PmdAttr.X.attr(), (float) normal.getXVal())
                .sp();
        putFloatAttr(PmdAttr.Y.attr(), (float) normal.getYVal())
                .sp();
        putFloatAttr(PmdAttr.Z.attr(), (float) normal.getZVal())
                .sp();
        putCloseEmpty().ln();

        MkPos2D uvPos = vertex.getUVPosition();
        ind().putOpenSTag(PmdTag.UV_MAP.tag()).sp();
        putFloatAttr(PmdAttr.U.attr(), (float) uvPos.getXpos()).sp();
        putFloatAttr(PmdAttr.V.attr(), (float) uvPos.getYpos()).sp();
        putCloseEmpty().ln();

        BoneInfo boneA = vertex.getBoneA();
        BoneInfo boneB = vertex.getBoneB();
        int weight = vertex.getWeightA();
        ind().putOpenSTag(PmdTag.SKINNING.tag()).sp();
        this.exp.putNumberedIdAttr(PmdAttr.BONE_IDREF_1,
                ExtraExporter.PFX_BONE, boneA);
        sp();
        this.exp.putNumberedIdAttr(PmdAttr.BONE_IDREF_2,
                ExtraExporter.PFX_BONE, boneB);
        sp();
        putIntAttr(PmdAttr.WEIGHT_BALANCE.attr(), weight).sp();
        putCloseEmpty().ln();

        return;
    }

}
