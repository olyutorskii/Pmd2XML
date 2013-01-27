/*
 * List utilities
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * リスト構造の各種ユーティリティ。
 */
public final class ListUtil {

    /**
     * 隠しコンストラクタ。
     */
    private ListUtil(){
        assert false;
        throw new AssertionError();
    }

    /**
     * リストの出現順にシリアルナンバーを割り振る。
     * シリアルナンバー先頭は0。
     * @param list リスト。
     */
    public static void assignIndexedSerial(
            List<? extends SerialNumbered> list){
        int serial = 0;
        for(SerialNumbered numbered : list){
            numbered.setSerialNumber(serial);
            serial++;
        }

        return;
    }

    /**
     * リストの要素数を拡張する。
     * 追加された要素にはnullが収められる。
     * リストがすでに指定サイズ以上の要素数を持つ場合、何もしない。
     * @param <E> 型
     * @param list リスト
     * @param newSize 新サイズ
     */
    public static <E> void extendList(List<E> list, int newSize){
        int remain = newSize - list.size();
        if(remain <= 0) return;

        for(int ct = 1; ct <= remain; ct++){
            list.add(null);
        }

        assert list.size() == newSize;

        return;
    }

    /**
     * リストのnull要素をデフォルトコンストラクタによるインスタンスで埋める。
     * null要素がなければなにもしない。
     * @param <E> 型
     * @param list リスト
     * @param cons コンストラクタ
     * @return 埋めた数
     */
    public static <E> int fillDefCons(List<E> list,
                                      Constructor<? extends E> cons){
        int result = 0;

        int size = list.size();
        for(int pt = 0; pt < size; pt++){
            E elem = list.get(pt);
            if(elem != null) continue;

            try{
                elem = cons.newInstance();
            }catch(InstantiationException e){
                assert false;
                throw new AssertionError(e);
            }catch(IllegalAccessException e){
                assert false;
                throw new AssertionError(e);
            }catch(InvocationTargetException e){
                Throwable cause = e.getTargetException();
                if(cause instanceof RuntimeException){
                    throw (RuntimeException) cause;
                }
                if(cause instanceof Error){
                    throw (Error) cause;
                }
                assert false;
                throw new AssertionError(e);
            }

            list.set(pt, elem);
            result++;
        }

        return result;
    }

    /**
     * リストのnull要素をデフォルトコンストラクタによるインスタンスで埋める。
     * @param <E> 型
     * @param list リスト
     * @param klass デフォルトコンストラクタの属するクラス
     * @return 埋めた数
     */
    public static <E> int fillDefCons(List<E> list, Class<? extends E> klass){
        Constructor<? extends E> cons;
        try{
            cons = klass.getConstructor();
        }catch(NoSuchMethodException e){
            assert false;
            throw new AssertionError(e);
        }

        return fillDefCons(list, cons);
    }

    /**
     * リスト要素数の拡張とデフォルトコンストラクタによる要素埋めを行う。
     * 追加された要素および既存のnull要素には
     * デフォルトコンストラクタによるインスタンスが収められる。
     * @param <E> 型
     * @param list リスト
     * @param klass デフォルトコンストラクタの属するクラス
     * @param newSize 新サイズ
     * @return 埋めた数。
     */
    public static <E> int prepareDefConsList(List<E> list,
                                             Class<? extends E> klass,
                                             int newSize ){
        extendList(list, newSize);
        int result = fillDefCons(list, klass);
        return result;
    }

}
