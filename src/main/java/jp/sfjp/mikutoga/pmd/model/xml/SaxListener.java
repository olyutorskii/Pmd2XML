/*
 * PMD-SAX element listsner
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.xml;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;
import javax.xml.bind.DatatypeConverter;
import jp.sfjp.mikutoga.pmd.model.PmdModel;
import org.xml.sax.Attributes;

/**
 * XML要素出現の通知受信部の共通実装。
 */
class SaxListener {

    private final Map<PmdTag, Method> openDispatcher;
    private final Map<PmdTag, Method> closeDispatcher;

    private PmdModel pmdModel = null;
    private Attributes currentAttribute = null;


    /**
     * コンストラクタ。
     */
    protected SaxListener(){
        super();

        Class<?> thisClass = this.getClass();
        this.openDispatcher  = getOpenDispatcher(thisClass);
        this.closeDispatcher = getCloseDispatcher(thisClass);

        return;
    }


    /**
     * 指定された注釈がマークされたインスタンスメソッド群を返す。
     * @param klass クラス
     * @param filter 注釈
     * @return インスタンスメソッド群
     */
    private static Collection<Method> filtMethod(Class<?> klass,
                                   Class<? extends Annotation> filter ){
        Collection<Method> result = new LinkedList<Method>();

        for(Method method : klass.getDeclaredMethods()){
            int modifiers = method.getModifiers();
            if(Modifier.isStatic(modifiers)) continue;
            if(Modifier.isPrivate(modifiers)) continue;
            if(method.getParameterTypes().length > 0) continue;

            Annotation anno = method.getAnnotation(filter);
            if(anno == null) continue;

            result.add(method);
        }

        return result;
    }

    /**
     * 注釈でマークされた開始タグ通知用ディスパッチテーブルを返す。
     * @param klass 対象クラス
     * @return ディスパッチテーブル
     */
    private static Map<PmdTag, Method> getOpenDispatcher(Class<?> klass){
        Map<PmdTag, Method> result =
                new EnumMap<PmdTag, Method>(PmdTag.class);

        for(Method method : filtMethod(klass, OpenXmlMark.class)){
            Annotation anno = method.getAnnotation(OpenXmlMark.class);
            OpenXmlMark mark = (OpenXmlMark) anno;
            PmdTag tag = mark.value();
            result.put(tag, method);
        }

        return result;
    }

    /**
     * 注釈でマークされた終了タグ通知用ディスパッチテーブルを返す。
     * @param klass 対象クラス
     * @return ディスパッチテーブル
     */
    private static Map<PmdTag, Method> getCloseDispatcher(Class<?> klass){
        Map<PmdTag, Method> result =
                new EnumMap<PmdTag, Method>(PmdTag.class);

        for(Method method : filtMethod(klass, CloseXmlMark.class)){
            Annotation anno = method.getAnnotation(CloseXmlMark.class);
            CloseXmlMark mark = (CloseXmlMark) anno;
            PmdTag tag = mark.value();
            result.put(tag, method);
        }

        return result;
    }


    /**
     * ディスパッチテーブルに従いディスパッチする。
     * @param map ディスパッチテーブル
     * @param tag タグ種
     * @return ディスパッチが行われなければfalse
     */
    private boolean dispatch(Map<PmdTag, Method> map, PmdTag tag){
        Method method = map.get(tag);
        if(method == null) return false;

        try{
            method.invoke(this);
        }catch(IllegalAccessException ex){
            assert false;
        }catch(InvocationTargetException ex){
            Throwable cause = ex.getTargetException();
            if(cause instanceof RuntimeException){
                throw (RuntimeException) cause;
            }else if(cause instanceof Error){
                throw (Error) cause;
            }
        }

        return true;
    }

    /**
     * 開始タグ登場を通知する。
     * @param tag タグ種別
     * @param attr 属性群
     * @return ディスパッチが行われなければfalse
     */
    boolean openDispatch(PmdTag tag, Attributes attr){
        this.currentAttribute = attr;
        return dispatch(this.openDispatcher, tag);
    }

    /**
     * 終了タグ登場を通知する。
     * @param tag タグ種別
     * @return ディスパッチが行われなければfalse
     */
    boolean closeDispatch(PmdTag tag){
        return dispatch(this.closeDispatcher, tag);
    }

    /**
     * CharData出現の通知。
     * @param ch 文字配列
     * @param start 開始位置
     * @param length 長さ
     */
    void addCharData(char[] ch, int start, int length){
        return;
    }

    /**
     * ビルド対象オブジェクトの登録。
     * @param model ビルド対象オブジェクト
     * @throws NullPointerException 引数がnull
     */
    void setPmdModel(PmdModel model) throws NullPointerException{
        if(model == null) throw new NullPointerException();
        this.pmdModel = model;
        return;
    }

    /**
     * ビルド対象オブジェクトの取得。
     * @return ビルド対象オブジェクト。未登録の場合はnull。
     */
    protected PmdModel getPmdModel(){
        return this.pmdModel;
    }

    /**
     * xsd:string型属性値の読み込み。
     * @param attr 属性名
     * @return 属性値。該当する属性が無ければnull。
     * @see "http://www.w3.org/TR/xmlschema-2/#string"
     */
    protected String getStringAttr(PmdAttr attr){
        String attrName = attr.attr();
        String result = this.currentAttribute.getValue(attrName);
        return result;
    }

    /**
     * xsd:boolean型属性値の読み込み。
     * @param attr 属性名
     * @return 属性値。
     * @throws IllegalArgumentException boolean型表記ではない
     * @see "http://www.w3.org/TR/xmlschema-2/#boolean"
     */
    protected boolean getBooleanAttr(PmdAttr attr)
            throws IllegalArgumentException{
        String attrName = attr.attr();
        String attrVal = this.currentAttribute.getValue(attrName);
        boolean bVal;
        bVal = DatatypeConverter.parseBoolean(attrVal);
        return bVal;
    }

    /**
     * xsd:float型属性値の読み込み。
     * @param attr 属性名
     * @return 属性値。
     * @throws NumberFormatException float型表記ではない
     * @see "http://www.w3.org/TR/xmlschema-2/#float"
     */
    protected float getFloatAttr(PmdAttr attr)
            throws NumberFormatException {
        String attrName = attr.attr();
        String attrVal = this.currentAttribute.getValue(attrName);
        float fVal;
        fVal = DatatypeConverter.parseFloat(attrVal);
        return fVal;
    }

    /**
     * xsd:int型属性値の読み込み。
     * @param attr 属性名
     * @return 属性値。
     * @throws NumberFormatException int型表記ではない
     * @see "http://www.w3.org/TR/xmlschema-2/#int"
     */
    protected int getIntAttr(PmdAttr attr)
            throws NumberFormatException {
        String attrName = attr.attr();
        String attrVal = this.currentAttribute.getValue(attrName);
        int iVal;
        iVal = DatatypeConverter.parseInt(attrVal);
        return iVal;
    }

}
