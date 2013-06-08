/*
 * PMD file loader
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.binio;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import jp.sfjp.mikutoga.bin.parser.MmdFormatException;
import jp.sfjp.mikutoga.pmd.model.MorphPart;
import jp.sfjp.mikutoga.pmd.model.PmdModel;
import jp.sfjp.mikutoga.pmd.parser.PmdParser;

/**
 * PMDモデルファイルを読み込むためのローダ。
 */
public class PmdLoader {

    private static final String ERR_TRYLOAD = "try loading first.";
    private static final String ERR_LOADED  = "has been loaded.";


    private boolean loaded = false;
    private boolean hasMoreData = true;


    /**
     * コンストラクタ。
     */
    public PmdLoader(){
        super();
        return;
    }


    /**
     * 正常パース時に読み残したデータがあったか判定する。
     * <p>MMDでの仕様拡張による
     * PMDファイルフォーマットの拡張が行われた場合を想定。
     * @return 読み残したデータがあればtrue
     * @throws IllegalStateException まだパースを試みていない。
     */
    public boolean hasMoreData() throws IllegalStateException{
        if( ! this.loaded ) throw new IllegalStateException(ERR_TRYLOAD);
        return this.hasMoreData;
    }

    /**
     * PMDファイルの読み込みを行いモデル情報を返す。
     * 1インスタンスにつき一度しかロードできない。
     * @param source PMDファイル入力ソース
     * @return モデル情報
     * @throws IOException 入力エラー
     * @throws MmdFormatException PMDファイルフォーマットの異常を検出
     * @throws IllegalStateException このインスタンスで再度のロードを試みた。
     */
    public PmdModel load(InputStream source)
            throws IOException,
                   MmdFormatException,
                   IllegalStateException {
        if(this.loaded) throw new IllegalStateException(ERR_LOADED);

        PmdModel model = new PmdModel();

        PmdParser parser = new PmdParser(source);

        TextBuilder     textBuilder     = new TextBuilder(model);
        ShapeBuilder    shapeBuilder    = new ShapeBuilder(model);
        MaterialBuilder materialBuilder = new MaterialBuilder(model);
        BoneBuilder     boneBuilder     = new BoneBuilder(model);
        MorphBuilder    morphBuilder    = new MorphBuilder(model);
        ToonBuilder     toonBuilder     = new ToonBuilder(model);
        RigidBuilder    rigidBuilder    = new RigidBuilder(model);
        JointBuilder    jointBuilder    = new JointBuilder(model);

        List<MorphPart> morphPartList = new ArrayList<MorphPart>();
        morphBuilder.setMorphPartList(morphPartList);
        textBuilder.setMorphPartList(morphPartList);

        parser.setBasicHandler(textBuilder);
        parser.setShapeHandler(shapeBuilder);
        parser.setMaterialHandler(materialBuilder);
        parser.setBoneHandler(boneBuilder);
        parser.setMorphHandler(morphBuilder);
        parser.setEngHandler(textBuilder);
        parser.setToonHandler(toonBuilder);
        parser.setRigidHandler(rigidBuilder);
        parser.setJointHandler(jointBuilder);

        try{
            parser.parsePmd();
            this.hasMoreData = textBuilder.hasMoreData();
        }finally{
            this.loaded = true;
        }

        return model;
    }

}
