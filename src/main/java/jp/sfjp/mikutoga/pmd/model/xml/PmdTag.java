/*
 * tags of pmd xml file
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.xml;

import java.util.HashMap;
import java.util.Map;

/**
 * XML要素名一覧。
 * @see "http://mikutoga.sourceforge.jp/xml/xsd/pmdxml-101009.xsd"
 * @see "http://mikutoga.sourceforge.jp/xml/xsd/pmdxml-130128.xsd"
 */
enum PmdTag {

    PMD_MODEL           ("pmdModel"),

    BR                  ("br"),
    I18N_NAME           ("i18nName"),
    DESCRIPTION         ("description"),
    LICENSE             ("license"),
    CREDITS             ("credits"),
    META                ("meta"),

    MATERIAL_LIST       ("materialList"),
    MATERIAL            ("material"),
    DIFFUSE             ("diffuse"),
    SPECULAR            ("specular"),
    AMBIENT             ("ambient"),
    TOON                ("toon"),
    TEXTURE_FILE        ("textureFile"),
    SPHEREMAP_FILE      ("spheremapFile"),

    TOON_MAP            ("toonMap"),
    TOON_DEF            ("toonDef"),

    BONE_LIST           ("boneList"),
    BONE                ("bone"),
    POSITION            ("position"),
    IK_BONE             ("ikBone"),
    SOURCE_BONE         ("sourceBone"),
    ROTATION_RATIO      ("rotationRatio"),
    BONE_CHAIN          ("boneChain"),

    BONE_GROUP_LIST     ("boneGroupList"),
    BONE_GROUP          ("boneGroup"),
    BONE_GROUP_MEMBER   ("boneGroupMember"),

    IK_CHAIN_LIST       ("ikChainList"),
    IK_CHAIN            ("ikChain"),
    CHAIN_ORDER         ("chainOrder"),

    MORPH_LIST          ("morphList"),
    MORPH               ("morph"),
    MORPH_VERTEX        ("morphVertex"),

    RIGID_LIST          ("rigidList"),
    RIGID               ("rigid"),
    LINKED_BONE         ("linkedBone"),
    RIGID_SHAPE_SPHERE  ("rigidShapeSphere"),
    RIGID_SHAPE_BOX     ("rigidShapeBox"),
    RIGID_SHAPE_CAPSULE ("rigidShapeCapsule"),
    RAD_ROTATION        ("radRotation"),
    DYNAMICS            ("dynamics"),
    THROUGH_RIGID_GROUP ("throughRigidGroup"),

    RIGID_GROUP_LIST    ("rigidGroupList"),
    RIGID_GROUP         ("rigidGroup"),
    RIGID_GROUP_MEMBER  ("rigidGroupMember"),

    JOINT_LIST          ("jointList"),
    JOINT               ("joint"),
    JOINTED_RIGID_PAIR  ("jointedRigidPair"),
    LIMIT_POSITION      ("limitPosition"),
    LIMIT_ROTATION      ("limitRotation"),
    ELASTIC_POSITION    ("elasticPosition"),
    ELASTIC_ROTATION    ("elasticRotation"),

    SURFACE_GROUP_LIST  ("surfaceGroupList"),
    SURFACE_GROUP       ("surfaceGroup"),
    SURFACE             ("surface"),

    VERTEX_LIST         ("vertexList"),
    VERTEX              ("vertex"),
    NORMAL              ("normal"),
    UV_MAP              ("uvMap"),
    SKINNING            ("skinning"),

    ;

    private static final Map<String, PmdTag> NAME_MAP =
            new HashMap<String, PmdTag>();

    static{
        for(PmdTag tag : values()){
            NAME_MAP.put(tag.tag(), tag);
        }
    }


    private final String tagName;


    /**
     * コンストラクタ。
     * @param tagName 要素名
     */
    private PmdTag(String tagName){
        this.tagName = tagName.intern();
        return;
    }


    /**
     * XML要素名から列挙子を得る。
     * @param name 要素名
     * @return 列挙子。合致する物がなければnull。
     */
    static PmdTag parse(String name){
        PmdTag result;
        result = NAME_MAP.get(name);
        return result;
    }


    /**
     * XML要素名を返す。
     * @return 要素名
     */
    String tag(){
        return this.tagName;
    }

}
