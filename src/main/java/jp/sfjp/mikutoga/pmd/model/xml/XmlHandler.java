/*
 * xml to pmd SAX Handler
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.xml;

import java.util.EnumMap;
import java.util.Map;
import jp.sfjp.mikutoga.pmd.model.PmdModel;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * XMLモデルファイルパース用SAXハンドラ。
 *
 * <p>下位リスナに各種通知が振り分けられる。
 */
class XmlHandler implements ContentHandler{

    private final Map<PmdTag, SaxListener> listenerMap;
    private SaxListener currentListener = null;

    private PmdModel pmdModel = null;

    private String nspfx = "";
    private String nsuri = null;


    /**
     * コンストラクタ。
     */
    XmlHandler(){
        super();

        RefHelper helper = new RefHelper();
        SaxListener modelListener    = new SaxModelListener();
        SaxListener materialListener = new SaxMaterialListener(helper);
        SaxListener boneListener     = new SaxBoneListener(helper);
        SaxListener morphListener    = new SaxMorphListener(helper);
        SaxListener dynamicsListener = new SaxDynamicsListener(helper);
        SaxListener shapeListener    = new SaxShapeListener(helper);

        this.listenerMap = new EnumMap<PmdTag, SaxListener>(PmdTag.class);
        this.listenerMap.put(PmdTag.PMD_MODEL,          modelListener);
        this.listenerMap.put(PmdTag.MATERIAL_LIST,      materialListener);
        this.listenerMap.put(PmdTag.BONE_LIST,          boneListener);
        this.listenerMap.put(PmdTag.MORPH_LIST,         morphListener);
        this.listenerMap.put(PmdTag.RIGID_LIST,         dynamicsListener);
        this.listenerMap.put(PmdTag.SURFACE_GROUP_LIST, shapeListener);

        return;
    }


    /**
     * ビルド対象のモデルを返す。
     *
     * @return ビルド対象のモデル。ビルド前ならnull
     */
    PmdModel getPmdModel(){
        return this.pmdModel;
    }

    /**
     * {@inheritDoc}
     *
     * @throws SAXException {@inheritDoc}
     */
    @Override
    public void startDocument() throws SAXException{
        this.pmdModel = new PmdModel();

        for(SaxListener listener : this.listenerMap.values()){
            listener.setPmdModel(this.pmdModel);
        }

        return;
    }

    /**
     * {@inheritDoc}
     *
     * @throws SAXException {@inheritDoc}
     */
    @Override
    public void endDocument() throws SAXException{
        assert this.pmdModel != null;
        this.currentListener = null;
        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param prefix {@inheritDoc}
     * @param uri {@inheritDoc}
     * @throws SAXException {@inheritDoc}
     */
    @Override
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        if(    Schema101009.NS_PMDXML.equals(uri)
            || Schema130128.NS_PMDXML.equals(uri) ){
            this.nspfx = prefix;
            this.nsuri = uri;
        }
        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param prefix {@inheritDoc}
     * @throws SAXException {@inheritDoc}
     */
    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        if(prefix.equals(this.nspfx)){
            this.nspfx = "";
            this.nsuri = null;
        }
        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param uri {@inheritDoc}
     * @param localName {@inheritDoc}
     * @param qName {@inheritDoc}
     * @param attr {@inheritDoc}
     * @throws SAXException {@inheritDoc}
     */
    @Override
    public void startElement(String uri,
                               String localName,
                               String qName,
                               Attributes attr)
            throws SAXException {
        if( ! this.nsuri.equals(uri) ) return;

        PmdTag tag = PmdTag.parse(localName);
        if(tag == null) return;

        switchListener(tag);

        if(this.currentListener == null) return;
        this.currentListener.openDispatch(tag, attr);

        return;
    }

    /**
     * タグ出現に従い通知リスナを切り替える。
     *
     * @param tag タグ種別
     */
    private void switchListener(PmdTag tag){
        SaxListener newListener = this.listenerMap.get(tag);
        if(newListener == null) return;

        this.currentListener = newListener;

        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param uri {@inheritDoc}
     * @param localName {@inheritDoc}
     * @param qName {@inheritDoc}
     * @throws SAXException {@inheritDoc}
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if( ! this.nsuri.equals(uri) ) return;

        PmdTag tag = PmdTag.parse(localName);
        if(tag == null) return;

        if(this.currentListener != null){
            this.currentListener.closeDispatch(tag);
        }

        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param locator {@inheritDoc}
     */
    @Override
    public void setDocumentLocator(Locator locator){
        // NOTHING
        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param target {@inheritDoc}
     * @param data {@inheritDoc}
     * @throws SAXException {@inheritDoc}
     */
    @Override
    public void processingInstruction(String target, String data)
            throws SAXException {
        // NOTHING
        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param ch {@inheritDoc}
     * @param start {@inheritDoc}
     * @param length {@inheritDoc}
     * @throws SAXException {@inheritDoc}
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if(this.currentListener == null) return;
        this.currentListener.addCharData(ch, start, length);
        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param ch {@inheritDoc}
     * @param start {@inheritDoc}
     * @param length {@inheritDoc}
     * @throws SAXException {@inheritDoc}
     */
    @Override
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        // NOTHING
        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param name {@inheritDoc}
     * @throws SAXException {@inheritDoc}
     */
    @Override
    public void skippedEntity(String name) throws SAXException{
        // NOTHING
        return;
    }

}
