/*
 * xml loader
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.xml;

import java.awt.Color;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import jp.sfjp.mikutoga.corelib.I18nText;
import jp.sfjp.mikutoga.math.MkPos2D;
import jp.sfjp.mikutoga.math.MkPos3D;
import jp.sfjp.mikutoga.math.MkVec3D;
import jp.sfjp.mikutoga.pmd.BoneType;
import jp.sfjp.mikutoga.pmd.Deg3d;
import jp.sfjp.mikutoga.pmd.MorphType;
import jp.sfjp.mikutoga.pmd.Rad3d;
import jp.sfjp.mikutoga.pmd.RigidBehaviorType;
import jp.sfjp.mikutoga.pmd.RigidShapeType;
import jp.sfjp.mikutoga.pmd.TripletRange;
import jp.sfjp.mikutoga.pmd.model.BoneGroup;
import jp.sfjp.mikutoga.pmd.model.BoneInfo;
import jp.sfjp.mikutoga.pmd.model.DynamicsInfo;
import jp.sfjp.mikutoga.pmd.model.IKChain;
import jp.sfjp.mikutoga.pmd.model.JointInfo;
import jp.sfjp.mikutoga.pmd.model.ListUtil;
import jp.sfjp.mikutoga.pmd.model.Material;
import jp.sfjp.mikutoga.pmd.model.MorphPart;
import jp.sfjp.mikutoga.pmd.model.MorphVertex;
import jp.sfjp.mikutoga.pmd.model.PmdModel;
import jp.sfjp.mikutoga.pmd.model.RigidGroup;
import jp.sfjp.mikutoga.pmd.model.RigidInfo;
import jp.sfjp.mikutoga.pmd.model.RigidShape;
import jp.sfjp.mikutoga.pmd.model.ShadeInfo;
import jp.sfjp.mikutoga.pmd.model.Surface;
import jp.sfjp.mikutoga.pmd.model.ToonMap;
import jp.sfjp.mikutoga.pmd.model.Vertex;
import jp.sourceforge.mikutoga.xml.DomNsUtils;
import jp.sourceforge.mikutoga.xml.DomUtils;
import jp.sourceforge.mikutoga.xml.TogaXmlException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * XML形式でのモデルファイルを読み込む。
 */
public class XmlLoader {

    private static final String ERR_INVROOT =
            "invalid root element[{0}]";
    private static final String ERR_UKVER =
            "unknown schema version[{0}]";


    private PmdModel model;

    private final Map<String, Integer> toonIdxMap =
            new HashMap<String, Integer>();
    private final Map<String, BoneInfo> boneMap =
            new HashMap<String, BoneInfo>();
    private final Map<String, Vertex> vertexMap =
            new HashMap<String, Vertex>();
    private final Map<String, List<Surface>> surfaceGroupMap =
            new HashMap<String, List<Surface>>();
    private final Map<String, RigidInfo> rigidMap =
            new HashMap<String, RigidInfo>();
    private final Map<String, RigidGroup> rigidGroupMap =
            new HashMap<String, RigidGroup>();

    private String rootNamespace = Schema130128.NS_PMDXML;
    private XmlModelFileType fileType = XmlModelFileType.XML_AUTO;


    /**
     * コンストラクタ。
     */
    public XmlLoader(){
        super();
        return;
    }


    /**
     * 要素からxsd:string型属性値を読み取る。
     * @param elem 要素
     * @param attrName 属性名
     * @return 文字列
     * @throws TogaXmlException 属性値が見つからなかった。
     */
    private static String getStringAttr(Element elem, String attrName)
            throws TogaXmlException{
        return DomUtils.getStringAttr(elem, attrName);
    }

    /**
     * 要素からxsd:boolean型属性値を読み取る。
     * @param elem 要素
     * @param attrName 属性名
     * @return 真ならtrue
     * @throws TogaXmlException 属性値が見つからなかった。
     */
    private static boolean getBooleanAttr(Element elem, String attrName)
            throws TogaXmlException{
        return DomUtils.getBooleanAttr(elem, attrName);
    }

    /**
     * 要素からxsd:integer型属性値を読み取る。
     * @param elem 要素
     * @param attrName 属性名
     * @return int値
     * @throws TogaXmlException 属性値が見つからなかった。
     */
    private static int getIntegerAttr(Element elem, String attrName)
            throws TogaXmlException{
        return DomUtils.getIntegerAttr(elem, attrName);
    }

    /**
     * 要素からxsd:float型属性値を読み取る。
     * @param elem 要素
     * @param attrName 属性名
     * @return float値
     * @throws TogaXmlException 属性値が見つからなかった。
     */
    private static float getFloatAttr(Element elem, String attrName)
            throws TogaXmlException{
        return DomUtils.getFloatAttr(elem, attrName);
    }

    /**
     * 要素から日本語Windows用ファイル名を属性値として読み取る。
     * 念のため文字U+00A5は文字U-005Cに変換される。
     * @param elem 要素
     * @param attrName 属性名
     * @return ファイル名
     * @throws TogaXmlException 属性値が見つからなかった。
     */
    private static String getSjisFileNameAttr(Element elem, String attrName)
            throws TogaXmlException{
        return DomUtils.getSjisFileNameAttr(elem, attrName);
    }

    /**
     * brタグで区切られた文字列内容(Mixed content)を
     * 改行付き文字列に変換する。
     * brタグはその出現回数だけ\nに変換される。
     * 生文字列コンテンツ中の\n,\rは削除される。
     * 改行文字以外のホワイトスペースは保持される。
     * @param parent br要素及び文字列コンテンツを含む要素
     * @return 変換された文字列
     */
    private static String getBRedContent(Element parent){
        StringBuilder result = new StringBuilder();

        for(Node node = parent.getFirstChild();
            node != null;
            node = node.getNextSibling() ){

            switch(node.getNodeType()){
            case Node.ELEMENT_NODE:
                Element elem = (Element) node;
                if("br".equals(elem.getTagName())){
                    result.append('\n');
                }
                break;
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
                String content = node.getTextContent();
                content = content.replace("\r", "");
                content = content.replace("\n", "");
                result.append(content);
                break;
            default:
                break;
            }
        }

        return result.toString();
    }


    /**
     * パース中のXMLファイル種別を返す。
     * @return ファイル種別
     */
    private XmlModelFileType getFileType(){
        return this.fileType;
    }

    /**
     * パース中のXMLファイル種別を設定する。
     * @param type 具体的なファイル種別
     */
    private void setFileType(XmlModelFileType type){
        if(   type != XmlModelFileType.XML_101009
           && type != XmlModelFileType.XML_130128 ){
            throw new IllegalArgumentException();
        }
        this.fileType = type;
        return;
    }

    /**
     * ルート要素の名前空間URIを返す。
     * @return 名前空間URI。nullなら名前空間が無いと見なされる
     */
    private String getRootNamespace(){
        return this.rootNamespace;
    }

    /**
     * ルート要素の名前空間URIを設定する。
     * @param name 名前空間URI。nullなら名前空間が無いと見なされる
     */
    private void setRootNamespace(String name){
        this.rootNamespace = name;
        return;
    }

    /**
     * 指定された名前の子要素を1つだけ返す。
     * @param parent 親要素
     * @param tagName 子要素名
     * @return 子要素
     * @throws TogaXmlException 1つも見つからなかった
     */
    private Element getChild(Element parent, String tagName)
            throws TogaXmlException{
        String ns = getRootNamespace();
        Element result = DomNsUtils.getFirstChild(parent, ns, tagName);
        return result;
    }

    /**
     * 親要素が指定された名前の子要素を持つか判定する。
     * @param parent 親要素
     * @param tagName 子要素名
     * @return 指定名の子要素が存在すればtrue
     */
    private boolean hasChild(Element parent, String tagName){
        String ns = getRootNamespace();
        return DomNsUtils.hasChild(parent, ns, tagName);
    }

    /**
     * 指定された名前の子要素のforeachを返す。
     * @param parent 親要素
     * @param childTag 子要素名
     * @return 子要素のforeach
     */
    private Iterable<Element> eachChild(Element parent,
                                         String childTag){
        String ns = getRootNamespace();
        return DomNsUtils.getEachChild(parent, ns, childTag);
    }

    /**
     * 多言語名を取得する。
     * @param baseElement 元要素
     * @param text 多言語名格納先
     * @throws TogaXmlException あるべき属性が存在しない。
     */
    private void buildI18nName(Element baseElement, I18nText text)
            throws TogaXmlException{
        String primaryText;
        primaryText = getStringAttr(baseElement, "name");
        text.setPrimaryText(primaryText);

        for(Element i18nNameElem : eachChild(baseElement, "i18nName")){
            String lang = getStringAttr(i18nNameElem, "lang");
            String name = getStringAttr(i18nNameElem, "name");
            if("en".equals(lang)){
                text.setGlobalText(name);
            }else{
                text.setI18nText(lang, text);
            }
        }

        return;
    }

    /**
     * XMLのパースを開始する。
     * @param builder ドキュメントビルダ
     * @param source XML入力
     * @return モデルデータ
     * @throws SAXException 構文エラー
     * @throws IOException 入力エラー
     * @throws TogaXmlException 構文エラー
     */
    public PmdModel parse(DocumentBuilder builder, InputSource source)
            throws SAXException, IOException, TogaXmlException{
        Document document = builder.parse(source);
        PmdModel result = parse(document);
        return result;
    }

    /**
     * XMLのパースを開始する。
     * @param document DOMドキュメント
     * @return モデルデータ
     * @throws TogaXmlException 構文エラー
     */
    public PmdModel parse(Document document)
            throws TogaXmlException{
        this.model = new PmdModel();

        Element pmdModelElem = document.getDocumentElement();
        String namespace = pmdModelElem.getNamespaceURI();
        setRootNamespace(namespace);

        buildBasicInfo(pmdModelElem);

        buildBoneList(pmdModelElem);
        buildVertexList(pmdModelElem);
        buildSurfaceList(pmdModelElem);

        buildToonMap(pmdModelElem);
        buildMaterialList(pmdModelElem);
        buildIkChainList(pmdModelElem);
        buildMorphList(pmdModelElem);
        buildBoneGroupList(pmdModelElem);

        buildRigidList(pmdModelElem);
        buildRigidGroupList(pmdModelElem);
        resolveThroughRigidGroup(pmdModelElem);

        buildJointList(pmdModelElem);

        return this.model;
    }

    /**
     * DOMからモデル基本情報を組み立てる。
     * @param pmdModelElem ルート要素
     * @throws TogaXmlException 構文エラー
     */
    private void buildBasicInfo(Element pmdModelElem)
            throws TogaXmlException{
        if( ! DomNsUtils.hasNsLocalNameElem(pmdModelElem,
                                            getRootNamespace(),
                                            "pmdModel") ){
            String tagName = pmdModelElem.getTagName();
            String msg = MessageFormat.format(ERR_INVROOT, tagName);
            throw new TogaXmlException(msg);
        }

        String version = getStringAttr(pmdModelElem, "schemaVersion");
        if(Schema101009.VER_PMDXML.equals(version)){
            setFileType(XmlModelFileType.XML_101009);
        }else if(Schema130128.VER_PMDXML.equals(version)){
            setFileType(XmlModelFileType.XML_130128);
        }else{
            String msg = MessageFormat.format(ERR_UKVER, version);
            throw new TogaXmlException(msg);
        }

        I18nText modelName = this.model.getModelName();
        buildI18nName(pmdModelElem, modelName);

        String primaryDescription = null;
        String globalDescription = null;
        for(Element descriptionElem :
            eachChild(pmdModelElem, "description")){
            String descriptionText = getBRedContent(descriptionElem);
            if( ! descriptionElem.hasAttribute("lang") ){
                primaryDescription = descriptionText;
            }else{
                String lang = getStringAttr(descriptionElem, "lang");
                if(lang.equals("ja")){
                    primaryDescription = descriptionText;
                }else if(lang.equals("en")){
                    globalDescription = descriptionText;
                }
            }
        }

        I18nText description = this.model.getDescription();
        description.setPrimaryText(primaryDescription);
        description.setGlobalText(globalDescription);

        return;
    }

    /**
     * DOMからボーンリスト情報を組み立てる。
     * @param pmdModelElem ルート要素
     * @throws TogaXmlException 構文エラー
     */
    private void buildBoneList(Element pmdModelElem)
            throws TogaXmlException{
        Element boneListElem = getChild(pmdModelElem, "boneList");

        List<BoneInfo> boneList = this.model.getBoneList();

        for(Element boneElem : eachChild(boneListElem, "bone")){
            BoneInfo boneInfo = new BoneInfo();
            boneList.add(boneInfo);

            I18nText boneName = boneInfo.getBoneName();
            buildI18nName(boneElem, boneName);

            String boneType = getStringAttr(boneElem, "type");
            BoneType type = BoneType.valueOf(boneType);
            boneInfo.setBoneType(type);

            String boneId = getStringAttr(boneElem, "boneId");
            this.boneMap.put(boneId, boneInfo);

            Element positionElem = getChild(boneElem, "position");
            float xPos = getFloatAttr(positionElem, "x");
            float yPos = getFloatAttr(positionElem, "y");
            float zPos = getFloatAttr(positionElem, "z");
            MkPos3D position = boneInfo.getPosition();
            position.setXpos(xPos);
            position.setYpos(yPos);
            position.setZpos(zPos);
        }

        ListUtil.assignIndexedSerial(boneList);

        Iterator<BoneInfo> bit = boneList.iterator();
        for(Element boneElem : eachChild(boneListElem, "bone")){
            BoneInfo boneInfo = bit.next();

            if(hasChild(boneElem, "ikBone")){            // 101009 only
                Element ikBoneElem = getChild(boneElem, "ikBone");
                String ikBoneId = getStringAttr(ikBoneElem, "boneIdRef");
                BoneInfo ikBone = this.boneMap.get(ikBoneId);
                boneInfo.setSrcBone(ikBone);
            }else if(hasChild(boneElem, "sourceBone")){  // 130128 only
                Element srcBoneElem = getChild(boneElem, "sourceBone");
                String srcBoneId = getStringAttr(srcBoneElem, "boneIdRef");
                BoneInfo srcBone = this.boneMap.get(srcBoneId);
                boneInfo.setSrcBone(srcBone);
            }else if(hasChild(boneElem, "rotationRatio")){
                Element rotElem = getChild(boneElem, "rotationRatio");
                int ratio = getIntegerAttr(rotElem, "ratio");
                boneInfo.setRotationRatio(ratio);
            }

            Element boneChainElem = getChild(boneElem, "boneChain");
            if(boneChainElem.hasAttribute("prevBoneIdRef")){
                String prevId = getStringAttr(boneChainElem, "prevBoneIdRef");
                BoneInfo prevBone = this.boneMap.get(prevId);
                boneInfo.setPrevBone(prevBone);
            }
            if(boneChainElem.hasAttribute("nextBoneIdRef")){
                String nextId = getStringAttr(boneChainElem, "nextBoneIdRef");
                BoneInfo nextBone = this.boneMap.get(nextId);
                boneInfo.setNextBone(nextBone);
            }
        }

        return;
    }

    /**
     * DOMから頂点リスト情報を組み立てる。
     * @param pmdModelElem ルート要素
     * @throws TogaXmlException 構文エラー
     */
    private void buildVertexList(Element pmdModelElem)
            throws TogaXmlException{
        Element vertexListElem = getChild(pmdModelElem, "vertexList");

        List<Vertex> vertexList = this.model.getVertexList();

        for(Element vertexElem : eachChild(vertexListElem, "vertex")){
            Vertex vertex = new Vertex();
            vertexList.add(vertex);

            String vertexId = getStringAttr(vertexElem, "vtxId");
            this.vertexMap.put(vertexId, vertex);

            boolean showEdge = getBooleanAttr(vertexElem, "showEdge");
            vertex.setEdgeAppearance(showEdge);

            float xVal;
            float yVal;
            float zVal;

            Element positionElem = getChild(vertexElem, "position");
            xVal = getFloatAttr(positionElem, "x");
            yVal = getFloatAttr(positionElem, "y");
            zVal = getFloatAttr(positionElem, "z");
            MkPos3D position = vertex.getPosition();
            position.setXpos(xVal);
            position.setYpos(yVal);
            position.setZpos(zVal);

            Element normalElem = getChild(vertexElem, "normal");
            xVal = getFloatAttr(normalElem, "x");
            yVal = getFloatAttr(normalElem, "y");
            zVal = getFloatAttr(normalElem, "z");
            MkVec3D normal = vertex.getNormal();
            normal.setXVal(xVal);
            normal.setYVal(yVal);
            normal.setZVal(zVal);

            Element uvElem = getChild(vertexElem, "uvMap");
            float uVal = getFloatAttr(uvElem, "u");
            float vVal = getFloatAttr(uvElem, "v");
            MkPos2D uv = vertex.getUVPosition();
            uv.setXpos(uVal);
            uv.setYpos(vVal);

            Element skinningElem = getChild(vertexElem, "skinning");
            String boneId1 = getStringAttr(skinningElem, "boneIdRef1");
            String boneId2 = getStringAttr(skinningElem, "boneIdRef2");
            int weight = getIntegerAttr(skinningElem, "weightBalance");
            BoneInfo boneA = this.boneMap.get(boneId1);
            BoneInfo boneB = this.boneMap.get(boneId2);
            vertex.setBonePair(boneA, boneB);
            vertex.setWeightA(weight);
        }

        ListUtil.assignIndexedSerial(vertexList);

        return;
    }

    /**
     * DOMからポリゴンリスト情報を組み立てる。
     * @param pmdModelElem ルート要素
     * @throws TogaXmlException 構文エラー
     */
    private void buildSurfaceList(Element pmdModelElem)
            throws TogaXmlException{
        Element surfaceGroupListElem =
                getChild(pmdModelElem, "surfaceGroupList");

        for(Element surfaceGroupElem :
            eachChild(surfaceGroupListElem, "surfaceGroup") ){

            String groupId =
                    getStringAttr(surfaceGroupElem, "surfaceGroupId");
            List<Surface> surfaceList = buildSurface(surfaceGroupElem);

            this.surfaceGroupMap.put(groupId, surfaceList);
        }

        return;
    }

    /**
     * DOMからポリゴン情報を組み立てる。
     * @param surfaceGroupElem surfaceGroup要素
     * @return ポリゴンリスト
     * @throws TogaXmlException 構文エラー
     */
    private List<Surface> buildSurface(Element surfaceGroupElem)
            throws TogaXmlException{
        List<Surface> result = new ArrayList<Surface>();

        for(Element surfaceElem : eachChild(surfaceGroupElem, "surface")){
            Surface surface = new Surface();
            result.add(surface);

            String id1 = getStringAttr(surfaceElem, "vtxIdRef1");
            String id2 = getStringAttr(surfaceElem, "vtxIdRef2");
            String id3 = getStringAttr(surfaceElem, "vtxIdRef3");

            Vertex vertex1 = this.vertexMap.get(id1);
            Vertex vertex2 = this.vertexMap.get(id2);
            Vertex vertex3 = this.vertexMap.get(id3);

            surface.setTriangle(vertex1, vertex2, vertex3);
        }

        return result;
    }

    /**
     * DOMからトゥーンマップ情報を組み立てる。
     * @param pmdModelElem ルート要素
     * @throws TogaXmlException 構文エラー
     */
    private void buildToonMap(Element pmdModelElem)
            throws TogaXmlException{
        ToonMap toonMap = this.model.getToonMap();

        Element toonMapElem = getChild(pmdModelElem, "toonMap");

        for(Element toonDefElem : eachChild(toonMapElem, "toonDef")){
            String toonFileId = getStringAttr(toonDefElem, "toonFileId");
            int toonIndex = getIntegerAttr(toonDefElem, "index");
            String toonFile = getSjisFileNameAttr(toonDefElem, "winFileName");

            toonMap.setIndexedToon(toonIndex, toonFile);
            this.toonIdxMap.put(toonFileId, toonIndex);
        }

        return;
    }

    /**
     * DOMからマテリアル情報を組み立てる。
     * @param pmdModelElem ルート要素
     * @throws TogaXmlException 構文エラー
     */
    private void buildMaterialList(Element pmdModelElem)
            throws TogaXmlException{
        Element materialListElem =
                getChild(pmdModelElem, "materialList");

        List<Surface> surfaceList = this.model.getSurfaceList();
        List<Material> materialList = this.model.getMaterialList();

        for(Element materialElem : eachChild(materialListElem, "material")){
            Material material = new Material();
            materialList.add(material);

            material.getShadeInfo().setToonMap(this.model.getToonMap());

            String surfaceGroupId =
                    getStringAttr(materialElem, "surfaceGroupIdRef");
            List<Surface> surfaceGroup =
                    this.surfaceGroupMap.get(surfaceGroupId);
            surfaceList.addAll(surfaceGroup);
            material.getSurfaceList().addAll(surfaceGroup);

            boolean hasEdge = getBooleanAttr(materialElem, "showEdge");
            material.setEdgeAppearance(hasEdge);

            ShadeInfo shadeInfo = material.getShadeInfo();

            int toonIdx;
            if(hasChild(materialElem, "toon")){
                Element toonElem = getChild(materialElem, "toon");
                String toonId = getStringAttr(toonElem, "toonFileIdRef");
                toonIdx = this.toonIdxMap.get(toonId);
            }else{
                toonIdx = 255;
            }
            shadeInfo.setToonIndex(toonIdx);

            if(hasChild(materialElem, "textureFile")){
                Element textureFileElem =
                        getChild(materialElem, "textureFile");
                String textureFile =
                        getSjisFileNameAttr(textureFileElem, "winFileName");
                shadeInfo.setTextureFileName(textureFile);
            }

            if(hasChild(materialElem, "spheremapFile")){
                Element spheremapFileElem =
                        getChild(materialElem, "spheremapFile");
                String spheremapFile =
                        getSjisFileNameAttr(spheremapFileElem, "winFileName");
                shadeInfo.setSpheremapFileName(spheremapFile);
            }

            float red;
            float green;
            float blue;

            Element diffuseElem = getChild(materialElem, "diffuse");
            red   = getFloatAttr(diffuseElem, "r");
            green = getFloatAttr(diffuseElem, "g");
            blue  = getFloatAttr(diffuseElem, "b");
            float alpha = getFloatAttr(diffuseElem, "alpha");
            Color diffuse = new Color(red, green, blue, alpha);
            material.setDiffuseColor(diffuse);

            Element specularElem = getChild(materialElem, "specular");
            red   = getFloatAttr(specularElem, "r");
            green = getFloatAttr(specularElem, "g");
            blue  = getFloatAttr(specularElem, "b");
            float shininess = getFloatAttr(specularElem, "shininess");
            Color specular = new Color(red, green, blue);
            material.setSpecularColor(specular);
            material.setShininess(shininess);

            Element ambientElem = getChild(materialElem, "ambient");
            red   = getFloatAttr(ambientElem, "r");
            green = getFloatAttr(ambientElem, "g");
            blue  = getFloatAttr(ambientElem, "b");
            Color ambient = new Color(red, green, blue);
            material.setAmbientColor(ambient);
        }

        return;
    }

    /**
     * DOMからIKチェーンリスト情報を組み立てる。
     * @param pmdModelElem ルート要素
     * @throws TogaXmlException 構文エラー
     */
    private void buildIkChainList(Element pmdModelElem)
            throws TogaXmlException{
        Element ikChainListElem =
                getChild(pmdModelElem, "ikChainList");

        List<IKChain> ikChainList = this.model.getIKChainList();

        for(Element ikChainElem : eachChild(ikChainListElem, "ikChain")){
            IKChain ikChain = new IKChain();
            ikChainList.add(ikChain);

            String ikBoneIdRef = getStringAttr(ikChainElem, "ikBoneIdRef");
            int rucursiveDepth =
                    getIntegerAttr(ikChainElem, "recursiveDepth");
            float weight = getFloatAttr(ikChainElem, "weight");

            BoneInfo ikBone = this.boneMap.get(ikBoneIdRef);
            ikChain.setIkBone(ikBone);
            ikChain.setIKDepth(rucursiveDepth);
            ikChain.setIKWeight(weight);

            List<BoneInfo> chainList = ikChain.getChainedBoneList();

            for(Element orderElem : eachChild(ikChainElem, "chainOrder")){
                String boneIdRef = getStringAttr(orderElem, "boneIdRef");
                BoneInfo chaindBone = this.boneMap.get(boneIdRef);
                chainList.add(chaindBone);
            }
        }

        return;
    }

    /**
     * DOMからモーフリスト情報を組み立てる。
     * @param pmdModelElem ルート要素
     * @throws TogaXmlException 構文エラー
     */
    private void buildMorphList(Element pmdModelElem)
            throws TogaXmlException{
        Element morphListElem =
                getChild(pmdModelElem, "morphList");

        Map<MorphType, List<MorphPart>> morphMap = this.model.getMorphMap();

        for(Element morphElem : eachChild(morphListElem, "morph")){
            MorphPart morphPart = new MorphPart();

            I18nText name = morphPart.getMorphName();
            buildI18nName(morphElem, name);

            String type = getStringAttr(morphElem, "type");
            MorphType morphType = MorphType.valueOf(type);
            morphPart.setMorphType(morphType);

            List<MorphVertex> morphVertexList =
                    morphPart.getMorphVertexList();

            for(Element morphVertexElem
                    : eachChild(morphElem, "morphVertex")){
                String vtxIdRef = getStringAttr(morphVertexElem, "vtxIdRef");
                Vertex baseVertex = this.vertexMap.get(vtxIdRef);
                float xOff = getFloatAttr(morphVertexElem, "xOff");
                float yOff = getFloatAttr(morphVertexElem, "yOff");
                float zOff = getFloatAttr(morphVertexElem, "zOff");

                MorphVertex morphVertex = new MorphVertex();
                morphVertex.setBaseVertex(baseVertex);
                MkPos3D position = morphVertex.getOffset();
                position.setXpos(xOff);
                position.setYpos(yOff);
                position.setZpos(zOff);

                morphVertexList.add(morphVertex);
            }

            morphMap.get(morphType).add(morphPart);
        }

        List<MorphPart> serialList = new LinkedList<MorphPart>();
        MorphPart baseDummy = new MorphPart();
        serialList.add(baseDummy);
        for(MorphPart part : morphMap.get(MorphType.EYEBROW)){
            serialList.add(part);
        }
        for(MorphPart part : morphMap.get(MorphType.EYE)){
            serialList.add(part);
        }
        for(MorphPart part : morphMap.get(MorphType.LIP)){
            serialList.add(part);
        }
        for(MorphPart part : morphMap.get(MorphType.EXTRA)){
            serialList.add(part);
        }
        ListUtil.assignIndexedSerial(serialList);

        return;
    }

    /**
     * DOMからボーングループ情報を組み立てる。
     * @param pmdModelElem ルート要素
     * @throws TogaXmlException 構文エラー
     */
    private void buildBoneGroupList(Element pmdModelElem)
            throws TogaXmlException{
        Element boneGroupListElem =
                getChild(pmdModelElem, "boneGroupList");

        List<BoneGroup> boneGroupList = this.model.getBoneGroupList();
        BoneGroup defaultGroup = new BoneGroup();
        boneGroupList.add(defaultGroup);

        for(Element boneGroupElem
                : eachChild(boneGroupListElem, "boneGroup")){
            BoneGroup group = new BoneGroup();
            boneGroupList.add(group);

            I18nText name = group.getGroupName();
            buildI18nName(boneGroupElem, name);

            for(Element boneGroupMemberElem
                    : eachChild(boneGroupElem, "boneGroupMember")){
                String boneIdRef =
                        getStringAttr(boneGroupMemberElem, "boneIdRef");
                BoneInfo bone = this.boneMap.get(boneIdRef);
                group.getBoneList().add(bone);
            }
        }

        ListUtil.assignIndexedSerial(boneGroupList);

        return;
    }

    /**
     * DOMから剛体リスト情報を組み立てる。
     * @param pmdModelElem ルート要素
     * @throws TogaXmlException 構文エラー
     */
    private void buildRigidList(Element pmdModelElem)
            throws TogaXmlException{
        Element rigidListElem =
                getChild(pmdModelElem, "rigidList");

        List<RigidInfo> rigidList = this.model.getRigidList();

        for(Element rigidElem : eachChild(rigidListElem, "rigid")){
            RigidInfo rigid = new RigidInfo();
            rigidList.add(rigid);

            I18nText name = rigid.getRigidName();
            buildI18nName(rigidElem, name);

            String behavior = getStringAttr(rigidElem, "behavior");
            RigidBehaviorType type = RigidBehaviorType.valueOf(behavior);
            rigid.setBehaviorType(type);

            String rigidId = getStringAttr(rigidElem, "rigidId");
            this.rigidMap.put(rigidId, rigid);

            if(hasChild(rigidElem, "linkedBone")){
                Element linkedBoneElem = getChild(rigidElem, "linkedBone");
                String boneIdRef = getStringAttr(linkedBoneElem, "boneIdRef");
                BoneInfo linkedBone = this.boneMap.get(boneIdRef);
                rigid.setLinkedBone(linkedBone);
            }

            RigidShape rigidShape = rigid.getRigidShape();
            if(hasChild(rigidElem, "rigidShapeSphere")){
                Element shapeElem =
                        getChild(rigidElem, "rigidShapeSphere");
                float radius = getFloatAttr(shapeElem, "radius");
                rigidShape.setShapeType(RigidShapeType.SPHERE);
                rigidShape.setRadius(radius);
            }
            if(hasChild(rigidElem, "rigidShapeBox")){
                Element shapeElem =
                        getChild(rigidElem, "rigidShapeBox");
                float width  = getFloatAttr(shapeElem, "width");
                float height = getFloatAttr(shapeElem, "height");
                float depth  = getFloatAttr(shapeElem, "depth");
                rigidShape.setShapeType(RigidShapeType.BOX);
                rigidShape.setWidth(width);
                rigidShape.setHeight(height);
                rigidShape.setDepth(depth);
            }
            if(hasChild(rigidElem, "rigidShapeCapsule")){
                Element shapeElem =
                        getChild(rigidElem, "rigidShapeCapsule");
                float height = getFloatAttr(shapeElem, "height");
                float radius = getFloatAttr(shapeElem, "radius");
                rigidShape.setShapeType(RigidShapeType.CAPSULE);
                rigidShape.setHeight(height);
                rigidShape.setRadius(radius);
            }

            float xVal;
            float yVal;
            float zVal;

            Element positionElem = getChild(rigidElem, "position");
            xVal = getFloatAttr(positionElem, "x");
            yVal = getFloatAttr(positionElem, "y");
            zVal = getFloatAttr(positionElem, "z");
            MkPos3D position = rigid.getPosition();
            position.setXpos(xVal);
            position.setYpos(yVal);
            position.setZpos(zVal);

            Element radRotationElem = getChild(rigidElem, "radRotation");
            xVal = getFloatAttr(radRotationElem, "xRad");
            yVal = getFloatAttr(radRotationElem, "yRad");
            zVal = getFloatAttr(radRotationElem, "zRad");
            Rad3d rotation = rigid.getRotation();
            rotation.setXRad(xVal);
            rotation.setYRad(yVal);
            rotation.setZRad(zVal);

            Element dynamicsElem = getChild(rigidElem, "dynamics");
            float mass = getFloatAttr(dynamicsElem, "mass");
            float dampingPosition =
                    getFloatAttr(dynamicsElem, "dampingPosition");
            float dampingRotation =
                    getFloatAttr(dynamicsElem, "dampingRotation");
            float restitution = getFloatAttr(dynamicsElem, "restitution");
            float friction = getFloatAttr(dynamicsElem, "friction");
            DynamicsInfo dynamics = rigid.getDynamicsInfo();
            dynamics.setMass(mass);
            dynamics.setDampingPosition(dampingPosition);
            dynamics.setDampingRotation(dampingRotation);
            dynamics.setRestitution(restitution);
            dynamics.setFriction(friction);
        }

        ListUtil.assignIndexedSerial(rigidList);

        return;
    }

    /**
     * DOMから剛体グループリスト情報を組み立てる。
     * @param pmdModelElem ルート要素
     * @throws TogaXmlException 構文エラー
     */
    private void buildRigidGroupList(Element pmdModelElem)
            throws TogaXmlException{
        Element rigidGroupListElem =
                getChild(pmdModelElem, "rigidGroupList");

        List<RigidGroup> groupList = this.model.getRigidGroupList();

        for(Element rigidGroupElem
                : eachChild(rigidGroupListElem, "rigidGroup")){
            RigidGroup rigidGroup = new RigidGroup();
            groupList.add(rigidGroup);

            String rigidGroupId =
                    getStringAttr(rigidGroupElem, "rigidGroupId");
            this.rigidGroupMap.put(rigidGroupId, rigidGroup);

            for(Element memberElem
                    : eachChild(rigidGroupElem, "rigidGroupMember")){
                String rigidIdRef = getStringAttr(memberElem, "rigidIdRef");
                RigidInfo rigid = this.rigidMap.get(rigidIdRef);
                rigidGroup.getRigidList().add(rigid);
                rigid.setRigidGroup(rigidGroup);
            }
        }

        while(groupList.size() < 16){
            RigidGroup rigidGroup = new RigidGroup();
            groupList.add(rigidGroup);
        }

        ListUtil.assignIndexedSerial(groupList);

        return;
    }

    /**
     * DOM内の剛体衝突情報を解決する。
     * @param pmdModelElem ルート要素
     * @throws TogaXmlException 構文エラー
     */
    private void resolveThroughRigidGroup(Element pmdModelElem)
            throws TogaXmlException{
        Element rigidListElem =
                getChild(pmdModelElem, "rigidList");

        List<RigidInfo> rigidList = this.model.getRigidList();

        Iterator<RigidInfo> rit = rigidList.iterator();
        for(Element rigidElem : eachChild(rigidListElem, "rigid")){
            RigidInfo rigid = rit.next();
            for(Element groupElem
                    : eachChild(rigidElem, "throughRigidGroup")){
                String groupId = getStringAttr(groupElem, "rigidGroupIdRef");
                RigidGroup group = this.rigidGroupMap.get(groupId);
                rigid.getThroughGroupColl().add(group);
            }
        }

        return;
    }

    /**
     * DOMからジョイントリストを組み立てる。
     * @param pmdModelElem ルート要素
     * @throws TogaXmlException 構文エラー
     */
    private void buildJointList(Element pmdModelElem)
            throws TogaXmlException{
        Element jointListElem =
                getChild(pmdModelElem, "jointList");

        List<JointInfo> jointList = this.model.getJointList();

        for(Element jointElem : eachChild(jointListElem, "joint")){
            JointInfo joint = new JointInfo();
            jointList.add(joint);

            I18nText name = joint.getJointName();
            buildI18nName(jointElem, name);

            Element rigidPairElem = getChild(jointElem, "jointedRigidPair");
            String rigidIdRef1 = getStringAttr(rigidPairElem, "rigidIdRef1");
            String rigidIdRef2 = getStringAttr(rigidPairElem, "rigidIdRef2");
            RigidInfo rigid1 = this.rigidMap.get(rigidIdRef1);
            RigidInfo rigid2 = this.rigidMap.get(rigidIdRef2);
            joint.setRigidPair(rigid1, rigid2);

            float xVal;
            float yVal;
            float zVal;
            float xFrom;
            float xTo;
            float yFrom;
            float yTo;
            float zFrom;
            float zTo;

            MkPos3D position = joint.getPosition();
            Element positionElem = getChild(jointElem, "position");
            xVal = getFloatAttr(positionElem, "x");
            yVal = getFloatAttr(positionElem, "y");
            zVal = getFloatAttr(positionElem, "z");
            position.setXpos(xVal);
            position.setYpos(yVal);
            position.setZpos(zVal);

            TripletRange limitPosition = joint.getPositionRange();
            Element limitPositionElem = getChild(jointElem, "limitPosition");
            xFrom = getFloatAttr(limitPositionElem, "xFrom");
            xTo   = getFloatAttr(limitPositionElem, "xTo");
            yFrom = getFloatAttr(limitPositionElem, "yFrom");
            yTo   = getFloatAttr(limitPositionElem, "yTo");
            zFrom = getFloatAttr(limitPositionElem, "zFrom");
            zTo   = getFloatAttr(limitPositionElem, "zTo");
            limitPosition.setXRange(xFrom, xTo);
            limitPosition.setYRange(yFrom, yTo);
            limitPosition.setZRange(zFrom, zTo);

            Rad3d rotation = joint.getRotation();
            Element rotationElem = getChild(jointElem, "radRotation");
            xVal = getFloatAttr(rotationElem, "xRad");
            yVal = getFloatAttr(rotationElem, "yRad");
            zVal = getFloatAttr(rotationElem, "zRad");
            rotation.setXRad(xVal);
            rotation.setYRad(yVal);
            rotation.setZRad(zVal);

            TripletRange limitRotation = joint.getRotationRange();
            Element limitRotationElem = getChild(jointElem, "limitRotation");
            xFrom = getFloatAttr(limitRotationElem, "xFrom");
            xTo   = getFloatAttr(limitRotationElem, "xTo");
            yFrom = getFloatAttr(limitRotationElem, "yFrom");
            yTo   = getFloatAttr(limitRotationElem, "yTo");
            zFrom = getFloatAttr(limitRotationElem, "zFrom");
            zTo   = getFloatAttr(limitRotationElem, "zTo");
            limitRotation.setXRange(xFrom, xTo);
            limitRotation.setYRange(yFrom, yTo);
            limitRotation.setZRange(zFrom, zTo);

            MkPos3D elasticPosition = joint.getElasticPosition();
            Element elasticPositionElem =
                    getChild(jointElem, "elasticPosition");
            xVal = getFloatAttr(elasticPositionElem, "x");
            yVal = getFloatAttr(elasticPositionElem, "y");
            zVal = getFloatAttr(elasticPositionElem, "z");
            elasticPosition.setXpos(xVal);
            elasticPosition.setYpos(yVal);
            elasticPosition.setZpos(zVal);

            Deg3d elasticRotation = joint.getElasticRotation();
            Element elasticRotationElem =
                    getChild(jointElem, "elasticRotation");
            xVal = getFloatAttr(elasticRotationElem, "xDeg");
            yVal = getFloatAttr(elasticRotationElem, "yDeg");
            zVal = getFloatAttr(elasticRotationElem, "zDeg");
            elasticRotation.setXDeg(xVal);
            elasticRotation.setYDeg(yVal);
            elasticRotation.setZDeg(zVal);
        }

        return;
    }

}
