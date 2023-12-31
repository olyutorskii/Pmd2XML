/*
 * toon file mapping
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * インデックス化されたトゥーンファイル構成。
 * 既存のトゥーンファイル構成と異なるトゥーンファイル名を用いることが可能。
 * <p>デフォルトのトゥーンファイル構成。
 * <ul>
 * <li>0x00:toon01.bmp
 * <li>0x01:toon02.bmp
 * <li>.....
 * <li>0x09:toon10.bmp
 * <li>0xff:toon0.bmp
 * </ul>
 */
public class ToonMap {

    /** カスタムトゥーンファイルの総数。 */
    public static final int MAX_CUSTOM_TOON = 10;

    private static final Map<Integer, String> DEF_TOONMAP;

    private static final int IDX_SP = 0xff;  // 特殊トゥーンのインデックス

    static{
        Map<Integer, String> map = new TreeMap<>();

        int idx = 0x00;
        map.put(idx++, "toon01.bmp");
        map.put(idx++, "toon02.bmp");
        map.put(idx++, "toon03.bmp");
        map.put(idx++, "toon04.bmp");
        map.put(idx++, "toon05.bmp");
        map.put(idx++, "toon06.bmp");
        map.put(idx++, "toon07.bmp");
        map.put(idx++, "toon08.bmp");
        map.put(idx++, "toon09.bmp");
        map.put(idx++, "toon10.bmp");
        map.put(IDX_SP, "toon0.bmp");

        assert idx == MAX_CUSTOM_TOON;

        DEF_TOONMAP = Collections.unmodifiableMap(map);

        assert DEF_TOONMAP.size() == MAX_CUSTOM_TOON + 1;
    }


    private final Map<Integer, String> toonDefMap =
            new TreeMap<>(DEF_TOONMAP);


    /**
     * コンストラクタ。
     */
    public ToonMap(){
        super();
        return;
    }


    /**
     * 指定したインデックス値に対応したトゥーンファイル名を返す。
     * @param idx インデックス値
     * @return トゥーンファイル名。該当するものがなければnull
     */
    public String getIndexedToon(int idx){
        String result = this.toonDefMap.get(idx);
        return result;
    }

    /**
     * 指定したインデックス値にトゥーンファイル名を設定する。
     * @param idx インデックス値
     * @param toonFileName トゥーンフィル名
     * @throws NullPointerException トゥーンファイル名がnull
     */
    public void setIndexedToon(int idx, String toonFileName)
            throws NullPointerException{
        if(toonFileName == null) throw new NullPointerException();
        this.toonDefMap.put(idx, toonFileName);
        return;
    }

    /**
     * このトゥーンファイル構成が
     * デフォルトのトゥーンファイル構成と等しいか判定する。
     * @return 等しければtrue
     */
    public boolean isDefaultMap(){
        boolean result = this.toonDefMap.equals(DEF_TOONMAP);
        return result;
    }

    /**
     * 指定インデックスのトゥーンファイル名がデフォルトと等しいか判定する。
     * @param idx インデックス
     * @return デフォルトと等しければtrue。
     */
    public boolean isDefaultToon(int idx){
        String thisToon = this.toonDefMap.get(idx);
        if(thisToon == null) return false;

        String defToon = DEF_TOONMAP.get(idx);

        boolean result = thisToon.equals(defToon);

        return result;
    }

    /**
     * このトゥーンファイル構成をデフォルト構成内容でリセットする。
     */
    public void resetDefaultMap(){
        this.toonDefMap.clear();
        this.toonDefMap.putAll(DEF_TOONMAP);
        return;
    }

    /**
     * 指定インデックスのトゥーンファイル名を
     * デフォルトのトゥーンファイル名にリセットする。
     * @param idx インデックス値
     */
    public void resetIndexedToon(int idx){
        String toonFile = DEF_TOONMAP.get(idx);
        this.toonDefMap.put(idx, toonFile);
        return;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String toString(){
        StringBuilder result = new StringBuilder();

        boolean dumped = false;
        for(Map.Entry<Integer, String> entry : this.toonDefMap.entrySet()){
            Integer idx = entry.getKey();
            String toonFile = entry.getValue();

            if(dumped) result.append(", ");
            result.append('(').append(idx).append(')');
            result.append(toonFile);
            dumped = true;
        }

        return result.toString();
    }

}
