/*
 * model exporter for pmd-file(Ext2)
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sourceforge.mikutoga.pmd.model.binio;

import jp.sourceforge.mikutoga.pmd.IllegalPmdDataException;
import java.io.IOException;
import java.io.OutputStream;
import jp.sourceforge.mikutoga.binio.IllegalTextExportException;
import jp.sourceforge.mikutoga.pmd.model.PmdModel;
import jp.sourceforge.mikutoga.pmd.model.ToonMap;
import jp.sourceforge.mikutoga.pmd.parser.PmdLimits;

/**
 * PMDファイルのエクスポーター(拡張2:任意のトゥーンファイル名対応)。
 * <p>
 * 物理演算対応以降のPMDファイルフォーマットを
 * 使いたくない場合はこのエクスポーターを用いて出力せよ。
 */
public class PmdExporterExt2 extends PmdExporterExt1{

    /**
     * コンストラクタ。
     * @param stream 出力ストリーム
     * @throws NullPointerException 引数がnull
     */
    public PmdExporterExt2(OutputStream stream)
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

        try{
            dumpToonMap(model);
        }catch(IllegalTextExportException e){
            throw new IllegalPmdDataException(e);
        }

        return;
    }

    /**
     * 独自トゥーンファイルテーブルを出力する。
     * @param model モデルデータ
     * @throws IOException 出力エラー
     * @throws IllegalPmdTextException トゥーンファイル名が長すぎる
     */
    private void dumpToonMap(PmdModel model)
            throws IOException, IllegalTextExportException{
        ToonMap map = model.getToonMap();

        for(int idx = 0; idx < PmdLimits.TOON_FIXEDNUM; idx++){
            String toonName = map.getIndexedToon(idx);
            if(toonName == null) toonName = "";
            dumpText(toonName, PmdLimits.MAXBYTES_TOONFILENAME);
        }

        flush();

        return;
    }

}
