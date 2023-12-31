/*
 * model exporter for pmd-file
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.binio;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jp.sfjp.mikutoga.bin.export.BinaryExporter;
import jp.sfjp.mikutoga.bin.export.IllegalTextExportException;
import jp.sfjp.mikutoga.math.MkPos2D;
import jp.sfjp.mikutoga.math.MkPos3D;
import jp.sfjp.mikutoga.math.MkVec3D;
import jp.sfjp.mikutoga.pmd.BoneType;
import jp.sfjp.mikutoga.pmd.IllegalPmdDataException;
import jp.sfjp.mikutoga.pmd.MorphType;
import jp.sfjp.mikutoga.pmd.PmdConst;
import jp.sfjp.mikutoga.pmd.model.BoneGroup;
import jp.sfjp.mikutoga.pmd.model.BoneInfo;
import jp.sfjp.mikutoga.pmd.model.IKChain;
import jp.sfjp.mikutoga.pmd.model.Material;
import jp.sfjp.mikutoga.pmd.model.MorphPart;
import jp.sfjp.mikutoga.pmd.model.MorphVertex;
import jp.sfjp.mikutoga.pmd.model.PmdModel;
import jp.sfjp.mikutoga.pmd.model.SerialNumbered;
import jp.sfjp.mikutoga.pmd.model.ShadeInfo;
import jp.sfjp.mikutoga.pmd.model.Surface;
import jp.sfjp.mikutoga.pmd.model.Vertex;

/**
 * PMDファイルのエクスポーター(拡張無し基本フォーマット)。
 *
 * <p>英名対応以降のPMDファイルフォーマットを
 * 使いたくない場合はこのエクスポーターを用いて出力せよ。
 */
public class PmdExporterBase extends BinaryExporter{

    /** 前(親)ボーンが無い場合の便宜的なボーンID。 */
    public static final int NOPREVBONE_ID = 0xffff;
    /** 次(子)ボーンが無い場合の便宜的なボーンID。 */
    public static final int NONEXTBONE_ID = 0x0000;
    /** 影響元IKボーンが無い場合の便宜的なボーンID。 */
    public static final int NOIKBONE_ID = 0x0000;

    private static final byte[] MAGIC_BYTES = {
        (byte)0x50, (byte)0x6d, (byte)0x64,               // "Pmd"
        (byte)0x00, (byte)0x00, (byte)0x80, (byte)0x3f,   // 1.0f
    };

    private static final byte[] NULLFILLER =
        { (byte)0x00 };
    private static final byte[] FDFILLER =
        { (byte)0x00, (byte)0xfd };
    private static final byte[] LFFILLER =
        { (byte)0x0a, (byte)0x00, (byte)0xfd };

    /** 改行文字列 CR。 */
    private static final String CR = "\r";       // 0x0d
    /** 改行文字列 LF。 */
    private static final String LF = "\n";       // 0x0a
    /** 改行文字列 CRLF。 */
    private static final String CRLF = CR + LF;  // 0x0d, 0x0a

    // sRGBカラー情報配列インデックス
    private static final int IDX_RED   = 0;
    private static final int IDX_GREEN = 1;
    private static final int IDX_BLUE  = 2;
    private static final int IDX_ALPHA = 3;

    private static final int TRIANGLE = 3;

    static{
        assert NOPREVBONE_ID > PmdConst.MAX_BONE - 1;
    }


    private float[] rgbaBuf = null;


    /**
     * コンストラクタ。
     *
     * @param stream 出力ストリーム
     * @throws NullPointerException 引数がnull
     */
    public PmdExporterBase(OutputStream stream)
            throws NullPointerException{
        super(stream);
        return;
    }

    /**
     * 改行文字の正規化を行う。
     * CR(0x0d)およびCRLF(0x0d0a)がLF(0x0a)へと正規化される。
     *
     * @param text 文字列
     * @return 正規化の行われた文字列。
     */
    protected static String normalizeBreak(String text){
        String result = text;

        result = result.replace(CRLF, LF);
        result = result.replace(CR, LF);

        return result;
    }

    /**
     * 文字列を指定されたバイト長で出力する。
     * 文字列の改行記号はLF(0x0a)に正規化される。
     * エンコード結果がバイト長に満たない場合は
     * 1つの0x00及びそれに続く複数の0xfdがパディングされる。
     *
     * @param text 文字列
     * @param maxByteLength バイト長指定
     * @throws IOException 出力エラー
     * @throws IllegalTextExportException エンコード結果が
     *     指定バイト長をはみ出した。
     */
    protected void dumpText(String text, int maxByteLength)
            throws IOException, IllegalTextExportException{
        dumpFixedW31j(text, maxByteLength, FDFILLER);
        return;
    }

    /**
     * モデルデータをPMDファイル形式で出力する。
     *
     * @param model モデルデータ
     * @throws IOException 出力エラー
     * @throws IllegalPmdDataException モデルデータに不備が発見された
     */
    public void dumpPmdModel(PmdModel model)
            throws IOException, IllegalPmdDataException{
        try{
            dumpBasic(model);
            dumpVertexList(model);
            dumpSurfaceList(model);
            dumpMaterialList(model);
            dumpBoneList(model);
            dumpIKChainList(model);
            dumpMorphList(model);
            dumpMorphGroup(model);
            dumpBoneGroupList(model);
        }catch(IllegalTextExportException e){
            throw new IllegalPmdDataException(e);
        }

        return;
    }

    /**
     * モデル基本情報を出力する。
     *
     * @param model モデルデータ
     * @throws IOException 出力エラー
     * @throws IllegalTextExportException モデル名もしくは説明が長すぎる
     */
    private void dumpBasic(PmdModel model)
            throws IOException, IllegalTextExportException{
        for(int idx = 0; idx < MAGIC_BYTES.length; idx++){
            dumpByte(MAGIC_BYTES[idx]);
        }

        String modelName   = model.getModelName()  .getPrimaryText();
        String description = model.getDescription().getPrimaryText();

        dumpText(modelName, PmdConst.MAXBYTES_MODELNAME);
        dumpText(description, PmdConst.MAXBYTES_MODELDESC);

        flush();

        return;
    }

    /**
     * 頂点リストを出力する。
     *
     * @param model モデルデータ
     * @throws IOException 出力エラー
     */
    private void dumpVertexList(PmdModel model)
            throws IOException{
        List<Vertex> vList = model.getVertexList();

        int vertexNum = vList.size();
        dumpLeInt(vertexNum);

        for(Vertex vertex : vList){
            dumpVertex(vertex);
        }

        flush();

        return;
    }

    /**
     * 個別の頂点データを出力する。
     *
     * @param vertex 頂点
     * @throws IOException 出力エラー
     */
    private void dumpVertex(Vertex vertex)
            throws IOException{
        MkPos3D position = vertex.getPosition();
        dumpPos3D(position);

        MkVec3D normal = vertex.getNormal();
        dumpVec3D(normal);

        MkPos2D uv = vertex.getUVPosition();
        dumpPos2d(uv);

        BoneInfo boneA = vertex.getBoneA();
        BoneInfo boneB = vertex.getBoneB();
        dumpSerialIdAsShort(boneA);
        dumpSerialIdAsShort(boneB);

        int weight = vertex.getWeightA();
        dumpByte((byte)weight);

        byte edgeFlag;
        boolean hasEdge = vertex.getEdgeAppearance();
        if(hasEdge) edgeFlag = 0x00;
        else        edgeFlag = 0x01;
        dumpByte(edgeFlag);

        return;
    }

    /**
     * 面リストを出力する。
     *
     * @param model モデルデータ
     * @throws IOException 出力エラー
     */
    private void dumpSurfaceList(PmdModel model)
            throws IOException{
        int surfaceNum = 0;
        List<Material> materialList = model.getMaterialList();
        for(Material material : materialList){
            surfaceNum += material.getSurfaceList().size();
        }
        dumpLeInt(surfaceNum * TRIANGLE);

        Vertex[] triangle = new Vertex[TRIANGLE];
        for(Material material : materialList){
            for(Surface surface : material){
                surface.getTriangle(triangle);
                dumpLeShort(triangle[0].getSerialNumber());
                dumpLeShort(triangle[1].getSerialNumber());
                dumpLeShort(triangle[2].getSerialNumber());
            }
        }

        flush();

        return;
    }

    /**
     * マテリアル素材リストを出力する。
     *
     * @param model モデルデータ
     * @throws IOException 出力エラー
     * @throws IllegalTextExportException シェーディングファイル情報が長すぎる
     */
    private void dumpMaterialList(PmdModel model)
            throws IOException, IllegalTextExportException{
        List<Material> materialList = model.getMaterialList();

        int materialNum = materialList.size();
        dumpLeInt(materialNum);

        for(Material material : materialList){
            dumpColorInfo(material);
            dumpShadeInfo(material);
        }

        flush();

        return;
    }

    /**
     * フォンシェーディングの色情報を出力する。
     *
     * @param material マテリアル情報
     * @throws IOException 出力エラー
     */
    private void dumpColorInfo(Material material)
            throws IOException{
        Color diffuse = material.getDiffuseColor();
        this.rgbaBuf = diffuse.getRGBComponents(this.rgbaBuf);
        dumpLeFloat(this.rgbaBuf[IDX_RED]);
        dumpLeFloat(this.rgbaBuf[IDX_GREEN]);
        dumpLeFloat(this.rgbaBuf[IDX_BLUE]);
        dumpLeFloat(this.rgbaBuf[IDX_ALPHA]);

        float shininess = material.getShininess();
        dumpLeFloat(shininess);

        Color specular = material.getSpecularColor();
        this.rgbaBuf = specular.getRGBComponents(this.rgbaBuf);
        dumpLeFloat(this.rgbaBuf[IDX_RED]);
        dumpLeFloat(this.rgbaBuf[IDX_GREEN]);
        dumpLeFloat(this.rgbaBuf[IDX_BLUE]);

        Color ambient = material.getAmbientColor();
        this.rgbaBuf = ambient.getRGBComponents(this.rgbaBuf);
        dumpLeFloat(this.rgbaBuf[IDX_RED]);
        dumpLeFloat(this.rgbaBuf[IDX_GREEN]);
        dumpLeFloat(this.rgbaBuf[IDX_BLUE]);

        return;
    }

    /**
     * シェーディング情報を出力する。
     *
     * @param material マテリアル情報
     * @throws IOException 出力エラー
     * @throws IllegalTextExportException ファイル名が長すぎる
     */
    private void dumpShadeInfo(Material material)
            throws IOException, IllegalTextExportException{
        ShadeInfo shade = material.getShadeInfo();
        int toonIdx = shade.getToonIndex();
        dumpByte(toonIdx);

        boolean showEdge = material.getEdgeAppearance();
        byte edgeFlag;
        if(showEdge) edgeFlag = 0x01;
        else         edgeFlag = 0x00;
        dumpByte(edgeFlag);

        int surfaceNum = material.getSurfaceList().size();
        dumpLeInt(surfaceNum * TRIANGLE);

        dumpShadeFileInfo(shade);

        return;
    }

    /**
     * シェーディングファイル情報を出力する。
     *
     * @param shade シェーディング情報
     * @throws IOException 出力エラー
     * @throws IllegalTextExportException ファイル名が長すぎる
     */
    private void dumpShadeFileInfo(ShadeInfo shade)
            throws IOException, IllegalTextExportException{
        String textureFile   = shade.getTextureFileName();
        String spheremapFile = shade.getSpheremapFileName();

        StringBuilder text = new StringBuilder();
        if(textureFile != null) text.append(textureFile);
        if(spheremapFile != null && spheremapFile.length() > 0){
            text.append('*')
                  .append(spheremapFile);
        }

        byte[] filler;
        if(text.length() <= 0) filler = NULLFILLER;
        else                   filler = FDFILLER;

        dumpFixedW31j(text.toString(),
                      PmdConst.MAXBYTES_TEXTUREFILENAME,
                      filler );

        return;
    }

    /**
     * ボーンリストを出力する。
     *
     * @param model モデルデータ
     * @throws IOException 出力エラー
     * @throws IllegalTextExportException ボーン名が長すぎる
     */
    private void dumpBoneList(PmdModel model)
            throws IOException, IllegalTextExportException{
        List<BoneInfo> boneList = model.getBoneList();

        int boneNum = boneList.size();
        dumpLeShort(boneNum);

        for(BoneInfo bone : boneList){
            dumpBone(bone);
        }

        flush();

        return;
    }

    /**
     * 個別のボーン情報を出力する。
     *
     * @param bone ボーン情報
     * @throws IOException 出力エラー
     * @throws IllegalTextExportException ボーン名が長すぎる
     */
    private void dumpBone(BoneInfo bone)
            throws IOException, IllegalTextExportException{
        String boneName = bone.getBoneName().getPrimaryText();
        dumpText(boneName, PmdConst.MAXBYTES_BONENAME);

        BoneInfo prev = bone.getPrevBone();
        if(prev != null) dumpSerialIdAsShort(prev);
        else             dumpLeShort(NOPREVBONE_ID);

        BoneInfo next = bone.getNextBone();
        if(next != null) dumpSerialIdAsShort(next);
        else             dumpLeShort(NONEXTBONE_ID);

        BoneType type = bone.getBoneType();
        dumpByte(type.encode());

        if(type == BoneType.LINKEDROT){
            int ratio = bone.getRotationRatio();
            dumpLeShort(ratio);
        }else{
            BoneInfo srcBone = bone.getSrcBone();
            if(srcBone != null) dumpSerialIdAsShort(srcBone);
            else                dumpLeShort(NOIKBONE_ID);
        }

        MkPos3D position = bone.getPosition();
        dumpPos3D(position);

        return;
    }

    /**
     * IKチェーンリストを出力する。
     *
     * @param model モデルデータ
     * @throws IOException 出力エラー
     */
    private void dumpIKChainList(PmdModel model)
            throws IOException{
        List<IKChain> ikChainList = model.getIKChainList();

        int ikNum = ikChainList.size();
        dumpLeShort(ikNum);

        for(IKChain chain : ikChainList){
            dumpIKChain(chain);
        }

        flush();

        return;
    }

    /**
     * IKチェーンを出力する。
     *
     * @param chain IKチェーン
     * @throws IOException 出力エラー
     */
    // TODO ボーンリストから自動抽出できる情報ではないのか？
    private void dumpIKChain(IKChain chain)
            throws IOException{
        BoneInfo ikBone = chain.getIkBone();
        dumpSerialIdAsShort(ikBone);

        List<BoneInfo> boneList = chain.getChainedBoneList();

        BoneInfo bone1st = boneList.get(0);
        dumpSerialIdAsShort(bone1st);

        int boneNum = boneList.size();
        dumpByte(boneNum - 1);

        int depth = chain.getIKDepth();
        float weight = chain.getIKWeight();

        dumpLeShort(depth);
        dumpLeFloat(weight);

        for(int idx = 1; idx < boneNum; idx++){ // リストの2番目以降全て
            BoneInfo bone = boneList.get(idx);
            dumpSerialIdAsShort(bone);
        }

        return;
    }

    /**
     * モーフリストを出力する。
     *
     * @param model モデルデータ
     * @throws IOException 出力エラー
     * @throws IllegalTextExportException モーフ名が長すぎる
     */
    private void dumpMorphList(PmdModel model)
            throws IOException, IllegalTextExportException{
        Map<MorphType, List<MorphPart>> morphMap = model.getMorphMap();
        Set<MorphType> typeSet = morphMap.keySet();
        List<MorphPart> morphPartList = new LinkedList<>();

        for(MorphType type : typeSet){
            List<MorphPart> partList = morphMap.get(type);
            if(partList == null) continue;
            morphPartList.addAll(partList);
        }

        int totalMorphPart = morphPartList.size();
        if(totalMorphPart <= 0){
            dumpLeShort(0);
            return;
        }else{
            totalMorphPart++;  // baseの分
            dumpLeShort(totalMorphPart);
        }

        dumpBaseMorph(model);

        for(MorphPart part : morphPartList){
            dumpText(part.getMorphName().getPrimaryText(),
                     PmdConst.MAXBYTES_MORPHNAME );

            List<MorphVertex> morphVertexList = part.getMorphVertexList();
            dumpLeInt(morphVertexList.size());

            dumpByte(part.getMorphType().encode());
            for(MorphVertex morphVertex : morphVertexList){
                dumpLeInt(morphVertex.getSerialNumber());
                dumpPos3D(morphVertex.getOffset());
            }
        }

        flush();

        return;
    }

    /**
     * BASEモーフを出力する。
     *
     * @param model モデルデータ
     * @throws IOException 出力エラー
     * @throws IllegalTextExportException モーフ名が長すぎる
     */
    private void dumpBaseMorph(PmdModel model)
            throws IOException, IllegalTextExportException{
        dumpText("base", PmdConst.MAXBYTES_MORPHNAME);

        List<MorphVertex> mergedMorphVertexList = model.mergeMorphVertex();
        int totalVertex = mergedMorphVertexList.size();
        dumpLeInt(totalVertex);

        dumpByte(MorphType.BASE.encode());
        for(MorphVertex morphVertex : mergedMorphVertexList){
            Vertex baseVertex = morphVertex.getBaseVertex();
            dumpLeInt(baseVertex.getSerialNumber());
            dumpPos3D(baseVertex.getPosition());
        }

        return;
    }

    /**
     * モーフグループを出力する。
     *
     * @param model モデルデータ
     * @throws IOException 出力エラー
     */
    private void dumpMorphGroup(PmdModel model)
            throws IOException{
        Map<MorphType, List<MorphPart>> morphMap = model.getMorphMap();
        Set<MorphType> typeSet = morphMap.keySet();

        int totalMorph = 0;
        for(MorphType type : typeSet){
            List<MorphPart> partList = morphMap.get(type);
            if(partList == null) continue;
            totalMorph += partList.size();
        }
        dumpByte(totalMorph);

        List<MorphType> typeList = new LinkedList<>();
        for(MorphType type : typeSet){
            assert ! type.isBase();
            typeList.add(type);
        }
        Collections.reverse(typeList);  // 一応本家と互換性を

        for(MorphType type : typeList){
            List<MorphPart> partList = morphMap.get(type);
            if(partList == null) continue;
            for(MorphPart part : partList){
                dumpSerialIdAsShort(part);
            }
        }

        flush();

        return;
    }

    /**
     * ボーングループリストを出力する。
     * デフォルトボーングループ内訳は出力されない。
     *
     * @param model モデルデータ
     * @throws IOException 出力エラー
     * @throws IllegalTextExportException ボーングループ名が長すぎる
     */
    private void dumpBoneGroupList(PmdModel model)
            throws IOException, IllegalTextExportException{
        List<BoneGroup> groupList = model.getBoneGroupList();
        int groupNum = groupList.size();
        dumpByte(groupNum - 1);

        int dispBoneNum = 0;
        for(BoneGroup group : groupList){
            if(group.isDefaultBoneGroup()) continue;
            dumpFixedW31j(group.getGroupName().getPrimaryText(),
                          PmdConst.MAXBYTES_BONEGROUPNAME, LFFILLER );
            dispBoneNum += group.getBoneList().size();
        }
        dumpLeInt(dispBoneNum);

        for(BoneGroup group : groupList){
            if(group.isDefaultBoneGroup()) continue;
            for(BoneInfo bone : group){
                dumpSerialIdAsShort(bone);
                int groupId = group.getSerialNumber();
                dumpByte(groupId);
            }
        }

        flush();

        return;
    }

    /**
     * 各種通し番号をshort値で出力する。
     * short値に収まらない上位ビットは捨てられる。
     *
     * @param obj 番号づけられたオブジェクト
     * @throws IOException 出力エラー
     */
    protected void dumpSerialIdAsShort(SerialNumbered obj)
            throws IOException{
        int serialId = obj.getSerialNumber();
        dumpLeShort(serialId);
        return;
    }

    /**
     * 2次元位置情報を出力する。
     *
     * @param position 2次元位置情報
     * @throws IOException 出力エラー
     */
    protected void dumpPos2d(MkPos2D position) throws IOException{
        float xPos = (float) position.getXpos();
        float yPos = (float) position.getYpos();

        dumpLeFloat(xPos);
        dumpLeFloat(yPos);

        return;
    }

    /**
     * 3次元位置情報を出力する。
     *
     * @param position 3次元位置情報
     * @throws IOException 出力エラー
     */
    protected void dumpPos3D(MkPos3D position) throws IOException{
        float xPos = (float) position.getXpos();
        float yPos = (float) position.getYpos();
        float zPos = (float) position.getZpos();

        dumpLeFloat(xPos);
        dumpLeFloat(yPos);
        dumpLeFloat(zPos);

        return;
    }

    /**
     * 3次元ベクトル情報を出力する。
     *
     * @param vector 3次元ベクトル
     * @throws IOException 出力エラー
     */
    protected void dumpVec3D(MkVec3D vector) throws IOException{
        float xVal = (float) vector.getXVal();
        float yVal = (float) vector.getYVal();
        float zVal = (float) vector.getZVal();

        dumpLeFloat(xVal);
        dumpLeFloat(yVal);
        dumpLeFloat(zVal);

        return;
    }

}
