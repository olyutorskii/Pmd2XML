/*
 * XML custom error-handler
 *
 * License : The MIT License
 * Copyright(c) 2010 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.xml;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * 自製エラーハンドラ。
 * 例外を渡されれば即投げる。
 */
public final class BotherHandler implements ErrorHandler{

    /**
     * 唯一のシングルトン。
     */
    public static final ErrorHandler HANDLER = new BotherHandler();

    /**
     * 隠しコンストラクタ。
     */
    private BotherHandler(){
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
