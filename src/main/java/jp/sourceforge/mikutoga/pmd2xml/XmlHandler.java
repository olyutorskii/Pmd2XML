/*
 * XML custom error-handler
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sourceforge.mikutoga.pmd2xml;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * 自製エラーハンドラ。
 * 例外を渡されれば即投げる。
 */
final class XmlHandler implements ErrorHandler{

    /**
     * 唯一のシングルトン。
     */
    static final ErrorHandler HANDLER = new XmlHandler();

    /**
     * 隠しコンストラクタ。
     */
    private XmlHandler(){
        super();
        return;
    }

    /**
     * {@inheritDoc}
     * @param exception {@inheritDoc}
     * @throws SAXException {@inheritDoc}
     */
    @Override
    public void error(SAXParseException exception) throws SAXException{
        throw exception;
    }

    /**
     * {@inheritDoc}
     * @param exception {@inheritDoc}
     * @throws SAXException {@inheritDoc}
     */
    @Override
    public void fatalError(SAXParseException exception) throws SAXException{
        throw exception;
    }

    /**
     * {@inheritDoc}
     * @param exception {@inheritDoc}
     * @throws SAXException {@inheritDoc}
     */
    @Override
    public void warning(SAXParseException exception) throws SAXException{
        throw exception;
    }

}
