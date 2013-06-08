/*
 * SAX ID-reference helper
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.xml;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import jp.sfjp.mikutoga.pmd.model.BoneInfo;
import jp.sfjp.mikutoga.pmd.model.Material;
import jp.sfjp.mikutoga.pmd.model.MorphVertex;
import jp.sfjp.mikutoga.pmd.model.RigidGroup;
import jp.sfjp.mikutoga.pmd.model.RigidInfo;
import jp.sfjp.mikutoga.pmd.model.ShadeInfo;
import jp.sfjp.mikutoga.pmd.model.Surface;
import jp.sfjp.mikutoga.pmd.model.Vertex;

/**
 * 各種ID参照解決用の一時的なヘルパ。
 */
class RefHelper {

    // マテリアル関連
    private final List<IdRefHolder<Material>> materialSfcGroupIdRefList =
            new LinkedList<IdRefHolder<Material>>();
    private final List<IdRefHolder<Material>> materialToonIdRefList =
            new LinkedList<IdRefHolder<Material>>();
    private final Map<String, Integer> toonIdxMap =
            new HashMap<String, Integer>();

    // ボーン関連
    private final Map<String, BoneInfo> boneIdMap =
            new HashMap<String, BoneInfo>();
    private final List<IdRefHolder<BoneInfo>> boneChainIdRefList =
            new LinkedList<IdRefHolder<BoneInfo>>();
    private final List<IdRefHolder<BoneInfo>> boneSourceIdRefList =
            new LinkedList<IdRefHolder<BoneInfo>>();

    // モーフ関連
    private final List<IdRefHolder<MorphVertex>> morphVertexIdRefList =
            new LinkedList<IdRefHolder<MorphVertex>>();

    // 剛体関連
    private final Map<String, RigidInfo> rigidIdMap =
            new HashMap<String, RigidInfo>();
    private final Map<String, RigidGroup> rigidGroupIdMap =
            new HashMap<String, RigidGroup>();
    private final List<IdRefHolder<RigidInfo>> thghRigidGroupIdRefList =
            new LinkedList<IdRefHolder<RigidInfo>>();

    // 面関連
    private final Map<String, List<Surface>> surfaceGroupIdMap =
            new HashMap<String, List<Surface>>();
    private final List<IdRefHolder<Surface>> surfaceVertexIdRef =
            new LinkedList<IdRefHolder<Surface>>();

    // 頂点関連
    private final Map<String, Vertex> vertexIdMap =
            new HashMap<String, Vertex>();


    /**
     * コンストラクタ。
     */
    RefHelper(){
        super();
        return;
    }


    /**
     * マテリアルからのサーフェイスグループID参照を登録する。
     * @param material マテリアル
     * @param idRef サーフェイスグループID参照
     */
    void addSurfaceGroupIdRef(Material material, String idRef){
        IdRefHolder<Material> holder =
                new IdRefHolder<Material>(material, idRef);
        this.materialSfcGroupIdRefList.add(holder);

        return;
    }

    /**
     * サーフェイスグループの構成サーフェイスを登録する。
     * @param surfaceGroupId サーフェイスグループID
     * @param surface サーフェイス
     */
    void addSurfaceGroup(String surfaceGroupId, Surface surface){
        List<Surface> surfaceGroup =
                this.surfaceGroupIdMap.get(surfaceGroupId);
        if(surfaceGroup == null){
            surfaceGroup = new LinkedList<Surface>();
            this.surfaceGroupIdMap.put(surfaceGroupId, surfaceGroup);
        }

        surfaceGroup.add(surface);

        return;
    }

    /**
     * マテリアルからのサーフェイスグループID参照を解決する。
     */
    void resolveMaterialSurfaceGroupId(){
        for(IdRefHolder<Material> holder : this.materialSfcGroupIdRefList){
            Material material = holder.getBody();
            String surfaceGroupIdRef = holder.getIdRef();

            List<Surface> surfaceGroup =
                    this.surfaceGroupIdMap.get(surfaceGroupIdRef);

            List<Surface> surfaceList = material.getSurfaceList();
            surfaceList.addAll(surfaceGroup);
        }

        return;
    }

    /**
     * マテリアルからの共有トゥーンファイルID参照を追加登録する。
     * @param material マテリアル
     * @param idRef トゥーンファイルID参照
     */
    void addToonFileIdRef(Material material, String idRef){
        IdRefHolder<Material> holder =
                new IdRefHolder<Material>(material, idRef);
        this.materialToonIdRefList.add(holder);
        return;
    }

    /**
     * 共有トゥーンファイルのインデックスを登録する。
     * @param toonFileId ToonファイルID
     * @param idx 共有Toonインデックス
     */
    void addToonIdx(String toonFileId, int idx){
        this.toonIdxMap.put(toonFileId, idx);
        return;
    }

    /**
     * マテリアルからの共有トゥーンインデックスの参照問題を解決する。
     */
    void resolveToonIdx(){
        for(IdRefHolder<Material> holder : this.materialToonIdRefList){
            Material material = holder.getBody();
            String toonFileIdRef = holder.getIdRef();
            int toonIdx = this.toonIdxMap.get(toonFileIdRef);

            ShadeInfo shadeInfo = material.getShadeInfo();
            shadeInfo.setToonIndex(toonIdx);
        }

        return;
    }

    /**
     * ボーンIDを登録する。
     * @param boneId ボーンID
     * @param boneInfo ボーン情報
     */
    void addBoneId(String boneId, BoneInfo boneInfo){
        this.boneIdMap.put(boneId, boneInfo);
        return;
    }

    /**
     * ボーンIDを問い合わせる。
     * @param boneId ボーンID
     * @return ボーン情報
     */
    BoneInfo findBoneId(String boneId){
        BoneInfo result = this.boneIdMap.get(boneId);
        return result;
    }

    /**
     * ボーン間チェーン参照情報を登録する。
     * @param bone ボーン情報
     * @param prevBoneIdRef 前ボーンID参照
     * @param nextBoneIdRef 次ボーンID参照
     */
    void addBoneChain(BoneInfo bone,
                        String prevBoneIdRef, String nextBoneIdRef ){
        IdRefHolder<BoneInfo> holder =
                new IdRefHolder<BoneInfo>(bone, prevBoneIdRef, nextBoneIdRef);
        this.boneChainIdRefList.add(holder);
        return;
    }

    /**
     * ボーン間チェーン参照情報を解決する。
     */
    void resolveBoneChainIdRef(){
        for(IdRefHolder<BoneInfo> holder : this.boneChainIdRefList){
            BoneInfo bone = holder.getBody();
            String prevBoneIdRef = holder.getIdRef();
            String nextBoneIdRef = holder.getIdRef2();

            if(prevBoneIdRef != null){
                BoneInfo prevBone = this.boneIdMap.get(prevBoneIdRef);
                bone.setPrevBone(prevBone);
            }

            if(nextBoneIdRef != null){
                BoneInfo nextBone = this.boneIdMap.get(nextBoneIdRef);
                bone.setNextBone(nextBone);
            }
        }

        return;
    }

    /**
     * ボーン情報からのソースボーンID参照を登録する。
     * @param bone ボーン情報
     * @param srcBoneIdRef ソースボーンID参照
     */
    void addSrcBoneIdRef(BoneInfo bone, String srcBoneIdRef){
        IdRefHolder<BoneInfo> holder =
                new IdRefHolder<BoneInfo>(bone, srcBoneIdRef);
        this.boneSourceIdRefList.add(holder);
        return;
    }

    /**
     * ボーン情報からのソースボーンID参照を解決する。
     */
    void resolveSrcBoneIdRef(){
        for(IdRefHolder<BoneInfo> holder : this.boneSourceIdRefList){
            BoneInfo bone = holder.getBody();
            String srcBoneIdRef = holder.getIdRef();
            if(srcBoneIdRef == null) continue;

            BoneInfo srcBone = this.boneIdMap.get(srcBoneIdRef);
            bone.setSrcBone(srcBone);
        }

        return;
    }

    /**
     * モーフ頂点からの頂点ID参照を登録する。
     * @param morphVertex モーフ頂点
     * @param vertexIdRef 頂点ID参照
     */
    void addMorphVertexIdRef(MorphVertex morphVertex, String vertexIdRef){
        IdRefHolder<MorphVertex> holder =
                new IdRefHolder<MorphVertex>(morphVertex, vertexIdRef);
        this.morphVertexIdRefList.add(holder);
        return;
    }

    /**
     * 剛体IDを登録する。
     * @param rigidId 剛体ID
     * @param rigid 剛体情報
     */
    void addRigidId(String rigidId, RigidInfo rigid){
        this.rigidIdMap.put(rigidId, rigid);
        return;
    }

    /**
     * 剛体IDを問い合わせる。
     * @param rigidId 剛体ID
     * @return 剛体情報
     */
    RigidInfo findRigidId(String rigidId){
        RigidInfo result = this.rigidIdMap.get(rigidId);
        return result;
    }

    /**
     * 剛体からの通過剛体グループID参照を登録する。
     * @param rigid 剛体情報
     * @param rigidGroupIdRef 剛体グループID参照
     */
    void addThroughRigidGroupIdRef(RigidInfo rigid, String rigidGroupIdRef){
        IdRefHolder<RigidInfo> holder =
                new IdRefHolder<RigidInfo>(rigid, rigidGroupIdRef);
        this.thghRigidGroupIdRefList.add(holder);
        return;
    }

    /**
     * 剛体グループIDを登録する。
     * @param rigidGroupId 剛体グループID
     * @param rigidGroup 剛体グループ
     */
    void addRigidGroupId(String rigidGroupId, RigidGroup rigidGroup){
        this.rigidGroupIdMap.put(rigidGroupId, rigidGroup);
        return;
    }

    /**
     * 剛体グループID参照を解決する。
     */
    void resolveThroughRigidGroupIdRef(){
        for(IdRefHolder<RigidInfo> holder : this.thghRigidGroupIdRefList){
            RigidInfo rigid = holder.getBody();
            String rigidGroupIdRef = holder.getIdRef();
            RigidGroup group = this.rigidGroupIdMap.get(rigidGroupIdRef);

            Collection<RigidGroup> throughGroups =
                    rigid.getThroughGroupColl();
            throughGroups.add(group);
        }

        return;
    }

    /**
     * 三角ポリゴンサーフェイス面からの頂点ID参照を登録する。
     * @param surface surface面
     * @param vtxIdRef1 頂点ID参照その1
     * @param vtxIdRef2 頂点ID参照その2
     * @param vtxIdRef3 頂点ID参照その3
     */
    void addSurfaceVertex(Surface surface,
                            String vtxIdRef1,
                            String vtxIdRef2,
                            String vtxIdRef3 ){
        IdRefHolder<Surface> holder =
                new IdRefHolder<Surface>(surface,
                                         vtxIdRef1,
                                         vtxIdRef2,
                                         vtxIdRef3 );

        this.surfaceVertexIdRef.add(holder);

        return;
    }

    /**
     * 頂点IDを登録する。
     * @param vertexId 頂点ID
     * @param vertex 頂点
     */
    void addVertexId(String vertexId, Vertex vertex){
        this.vertexIdMap.put(vertexId, vertex);
        return;
    }

    /**
     * モーフ頂点からの頂点ID参照を解決する。
     */
    void resolveMorphVertexIdRef(){
        for(IdRefHolder<MorphVertex> holder : this.morphVertexIdRefList){
            MorphVertex morphVertex = holder.getBody();
            String vertexIdRef = holder.getIdRef();
            Vertex vertex = this.vertexIdMap.get(vertexIdRef);

            morphVertex.setBaseVertex(vertex);
        }

        return;
    }

    /**
     * サーフェイスからの頂点ID参照を解決する。
     */
    void resolveSurfaceVertexIdRef(){
        for(IdRefHolder<Surface> holder : this.surfaceVertexIdRef){
            Surface surface = holder.getBody();
            String vertexIdRef1 = holder.getIdRef();
            String vertexIdRef2 = holder.getIdRef2();
            String vertexIdRef3 = holder.getIdRef3();

            Vertex vtx1 = this.vertexIdMap.get(vertexIdRef1);
            Vertex vtx2 = this.vertexIdMap.get(vertexIdRef2);
            Vertex vtx3 = this.vertexIdMap.get(vertexIdRef3);

            surface.setTriangle(vtx1, vtx2, vtx3);
        }

        return;
    }

    /**
     * ID参照解決用一時ホルダ。
     * 必要に応じて参照IDを3つまで持てる。
     * @param <E> ID参照元インスタンス型
     */
    private static final class IdRefHolder<E> {

        private final E body;

        private final String idRef;
        private final String idRef2;
        private final String idRef3;

        /**
         * コンストラクタ。
         * @param body ID参照元インスタンス
         * @param idRef 参照ID
         */
        IdRefHolder(E body, String idRef){
            this(body, idRef, null, null);
            return;
        }

        /**
         * コンストラクタ。
         * @param body ID参照元インスタンス
         * @param idRef 参照ID
         * @param idRef2 参照IDその2
         */
        IdRefHolder(E body, String idRef, String idRef2){
            this(body, idRef, idRef2, null);
            return;
        }

        /**
         * コンストラクタ。
         * @param body ID参照元インスタンス
         * @param idRef  参照ID
         * @param idRef2 参照IDその2
         * @param idRef3 参照IDその3
         */
        IdRefHolder(E body,
                      String idRef,
                      String idRef2,
                      String idRef3 ){
            super();

            this.body = body;

            this.idRef  = idRef;
            this.idRef2 = idRef2;
            this.idRef3 = idRef3;

            return;
        }


        /**
         * ID参照元インスタンスを返す。
         * @return ID参照元インスタンス
         */
        private E getBody(){
            return this.body;
        }

        /**
         * 参照IDを返す。
         * @return 参照ID
         */
        private String getIdRef(){
            return this.idRef;
        }

        /**
         * 参照IDその2を返す。
         * @return 参照ID
         */
        private String getIdRef2(){
            return this.idRef2;
        }

        /**
         * 参照IDその3を返す。
         * @return 参照ID
         */
        private String getIdRef3(){
            return this.idRef3;
        }

    }

}
