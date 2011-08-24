/*
 * model exporter for pmd-file(Ext1)
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sourceforge.mikutoga.pmd.model.binio;

import jp.sourceforge.mikutoga.pmd.IllegalPmdDataException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import jp.sourceforge.mikutoga.binio.IllegalTextExportException;
import jp.sourceforge.mikutoga.pmd.MorphType;
import jp.sourceforge.mikutoga.pmd.model.BoneGroup;
import jp.sourceforge.mikutoga.pmd.model.BoneInfo;
import jp.sourceforge.mikutoga.pmd.model.MorphPart;
import jp.sourceforge.mikutoga.pmd.model.PmdModel;
import jp.sourceforge.mikutoga.pmd.parser.PmdLimits;

/**
 * PMDファイルのエクスポーター(拡張1:英名対応)。
 * <p>
 * 任意のトゥーンファイル名対応以降のPMDファイルフォーマットを
 * 使いたくない場合はこのエクスポーターを用いて出力せよ。
 */
public class PmdExporterExt1 extends PmdExporterBase{

    /**
     * コンストラクタ。
     * @param stream 出力ストリーム
     * @throws NullPointerException 引数がnull
     */
    public PmdExporterExt1(OutputStream stream)
            throws NullPointerException{
        super(stream);
        return;
    }

    /**
     * {@inheritDoc}
     * @param model {@inheritDoc}
     * @throws IOException {@inheritDoc}
     * @throws IllegalPmdDataException {@inheritDoc}
     */
    @Override
    public void dumpPmdModel(PmdModel model)
            throws IOException, IllegalPmdDataException{
        super.dumpPmdModel(model);

        dumpGlobalInfo(model);

        return;
    }

    /**
     * 英語名情報を出力する。
     * @param model モデルデータ
     * @throws IOException 出力エラー
     * @throws IllegalPmdTextException 文字列が長すぎる。
     */
    private void dumpGlobalInfo(PmdModel model)
            throws IOException, IllegalPmdDataException{
        boolean hasGlobal = model.hasGlobalText();
        byte globalFlag;
        if(hasGlobal) globalFlag = 0x01;
        else          globalFlag = 0x00;
        dumpByte(globalFlag);

        if(hasGlobal){
            try{
                dumpBasicGlobal(model);
                dumpBoneGlobal(model);
                dumpMorphGlobal(model);
                dumpBoneGroupGlobal(model);
            }catch(IllegalTextExportException e){
                throw new IllegalPmdDataException(e);
            }
        }

        flush();

        return;
    }

    /**
     * モデル基本情報を英語で出力する。
     * @param model モデルデータ
     * @throws IOException 出力エラー
     * @throws IllegalPmdTextException 文字列が長すぎる。
     */
    private void dumpBasicGlobal(PmdModel model)
            throws IOException, IllegalTextExportException{
        String modelName = model.getModelName().getGlobalText();
        if(modelName == null) modelName = "";
        dumpText(modelName, PmdLimits.MAXBYTES_MODELNAME);

        String description = model.getDescription().getGlobalText();
        if(description == null) description = "";
        dumpText(description, PmdLimits.MAXBYTES_MODELDESC);

        flush();
    }

    /**
     * ボーン英語名情報を出力する。
     * @param model モデルデータ
     * @throws IOException 出力エラー
     * @throws IllegalPmdTextException 文字列が長すぎる。
     */
    private void dumpBoneGlobal(PmdModel model)
            throws IOException, IllegalTextExportException{
        for(BoneInfo bone : model.getBoneList()){
            String boneName = bone.getBoneName().getGlobalText();
            if(boneName == null) boneName = "";
            dumpText(boneName, PmdLimits.MAXBYTES_BONENAME);
        }

        flush();
    }

    /**
     * モーフ英語名情報を出力する。
     * @param model モデルデータ
     * @throws IOException 出力エラー
     * @throws IllegalPmdTextException 文字列が長すぎる。
     */
    private void dumpMorphGlobal(PmdModel model)
            throws IOException, IllegalTextExportException{
        Map<MorphType, List<MorphPart>> morphMap = model.getMorphMap();

        for(MorphType type : MorphType.values()){
            if(type.isBase()) continue;
            List<MorphPart> partList = morphMap.get(type);
            if(partList == null) continue;
            for(MorphPart part : partList){
                String morphName = part.getMorphName().getGlobalText();
                if(morphName == null) morphName = "";
                dumpText(morphName, PmdLimits.MAXBYTES_MORPHNAME);
            }
        }

        flush();
    }

    /**
     * ボーングループ英語名情報を出力する。
     * @param model モデルデータ
     * @throws IOException 出力エラー
     * @throws IllegalPmdTextException 文字列が長すぎる
     */
    private void dumpBoneGroupGlobal(PmdModel model)
            throws IOException, IllegalTextExportException{
        for(BoneGroup group : model.getBoneGroupList()){
            if(group.isDefaultBoneGroup()) continue;
            String groupName = group.getGroupName().getGlobalText();
            if(groupName == null) groupName = "";
            dumpText(groupName, PmdLimits.MAXBYTES_BONEGROUPNAME);
        }

        flush();
    }

}
