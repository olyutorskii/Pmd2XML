/*
 * attributes of pmd xml file
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.xml;

/**
 * XML属性名一覧。
 * @see "http://mikutoga.sourceforge.jp/xml/xsd/pmdxml-101009.xsd"
 * @see "http://mikutoga.sourceforge.jp/xml/xsd/pmdxml-130128.xsd"
 *
 */
enum PmdAttr {

    VERSION             ("version"),
    SCHEMA_VERSION      ("schemaVersion"),

    NAME                ("name"),
    LANG                ("lang"),

    CONTENT             ("content"),

    SURFACE_GROUP_IDREF ("surfaceGroupIdRef"),
    R                   ("r"),
    G                   ("g"),
    B                   ("b"),
    ALPHA               ("alpha"),
    SHININESS           ("shininess"),
    TOONFILE_ID         ("toonFileId"),
    TOONFILE_IDREF      ("toonFileIdRef"),
    WINFILE_NAME        ("winFileName"),
    INDEX               ("index"),

    BONE_ID             ("boneId"),
    TYPE                ("type"),
    X                   ("x"),
    Y                   ("y"),
    Z                   ("z"),
    PREV_BONE_IDREF     ("prevBoneIdRef"),
    NEXT_BONE_IDREF     ("nextBoneIdRef"),
    BONE_IDREF          ("boneIdRef"),
    RATIO               ("ratio"),

    IK_BONE_IDREF       ("ikBoneIdRef"),
    RECURSIVE_DEPTH     ("recursiveDepth"),
    WEIGHT              ("weight"),

    VERTEX_IDREF        ("vtxIdRef"),
    XOFF                ("xOff"),
    YOFF                ("yOff"),
    ZOFF                ("zOff"),

    RIGID_ID            ("rigidId"),
    RIGID_IDREF         ("rigidIdRef"),
    BEHAVIOR            ("behavior"),
    RADIUS              ("radius"),
    WIDTH               ("width"),
    HEIGHT              ("height"),
    DEPTH               ("depth"),
    X_RAD               ("xRad"),
    Y_RAD               ("yRad"),
    Z_RAD               ("zRad"),

    MASS                ("mass"),
    DAMPING_POSITION    ("dampingPosition"),
    DAMPING_ROTATION    ("dampingRotation"),
    RESTITUTION         ("restitution"),
    FRICTION            ("friction"),

    RIGID_GROUP_ID      ("rigidGroupId"),
    RIGID_GROUP_IDREF   ("rigidGroupIdRef"),

    RIGID_IDREF_1       ("rigidIdRef1"),
    RIGID_IDREF_2       ("rigidIdRef2"),
    X_FROM              ("xFrom"),
    X_TO                ("xTo"),
    Y_FROM              ("yFrom"),
    Y_TO                ("yTo"),
    Z_FROM              ("zFrom"),
    Z_TO                ("zTo"),
    X_DEG               ("xDeg"),
    Y_DEG               ("yDeg"),
    Z_DEG               ("zDeg"),

    SURFACE_GROUP_ID    ("surfaceGroupId"),
    VERTEX_IDREF_1      ("vtxIdRef1"),
    VERTEX_IDREF_2      ("vtxIdRef2"),
    VERTEX_IDREF_3      ("vtxIdRef3"),

    VERTEX_ID           ("vtxId"),
    SHOW_EDGE           ("showEdge"),
    U                   ("u"),
    V                   ("v"),
    BONE_IDREF_1        ("boneIdRef1"),
    BONE_IDREF_2        ("boneIdRef2"),
    WEIGHT_BALANCE      ("weightBalance"),

    ;

    private final String attrName;


    /**
     * コンストラクタ。
     * @param attrName 属性名
     */
    private PmdAttr(String attrName){
        this.attrName = attrName.intern();
        return;
    }

    /**
     * 属性名を返す。
     * @return 属性名
     */
    String attr(){
        return this.attrName;
    }

}
