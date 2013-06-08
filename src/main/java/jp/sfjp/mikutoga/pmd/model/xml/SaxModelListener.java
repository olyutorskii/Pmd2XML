/*
 * model listener from XML
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.xml;

import jp.sfjp.mikutoga.corelib.I18nText;

/*
    + pmdModel
        + i18nName
        + description
        + license
        + credits
        + meta
        .....
*/

/**
 * モデル関連のXML要素出現イベントを受信する。
 */
class SaxModelListener extends SaxListener{

    private String currentLang = null;
    private StringBuilder currentBredTxt = null;


    /**
     * コンストラクタ。
     */
    SaxModelListener() {
        super();
        return;
    }


    /**
     * CharData出現の通知。
     * @param ch 文字配列
     * @param start 開始位置
     * @param length 長さ
     */
    @Override
    void addCharData(char[] ch, int start, int length){
        if(this.currentBredTxt == null) return;

        for(int idx = 0; idx < length; idx++){
            int pos = start + idx;
            char chData = ch[pos];
            if(chData == '\n') continue;
            if(chData == '\r') continue;
            this.currentBredTxt.append(chData);
        }

        return;
    }

    /**
     * pmdModelタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.PMD_MODEL)
    void openPmdModel(){
        I18nText modelName = getPmdModel().getModelName();

        String name = getStringAttr(PmdAttr.NAME);
        modelName.setPrimaryText(name);

        return;
    }

    /**
     * i18nTextタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.I18N_NAME)
    void openI18nText(){
        String lang = getStringAttr(PmdAttr.LANG);
        String name = getStringAttr(PmdAttr.NAME);

        I18nText modelName = getPmdModel().getModelName();
        modelName.setI18nText(lang, name);

        return;
    }

    /**
     * descriptionタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.DESCRIPTION)
    void openDescription(){
        this.currentLang = getStringAttr(PmdAttr.LANG);
        this.currentBredTxt = new StringBuilder();
        return;
    }

    /**
     * descriptionタグ終了の通知を受け取る。
     */
    @CloseXmlMark(PmdTag.DESCRIPTION)
    void closeDescription(){
        String bredText = this.currentBredTxt.toString();

        I18nText desc = getPmdModel().getDescription();
        if(this.currentLang == null){
            desc.setPrimaryText(bredText);
        }else{
            desc.setI18nText(this.currentLang, bredText);
        }

        this.currentLang = null;
        this.currentBredTxt = null;

        return;
    }

    /**
     * brタグ開始の通知を受け取る。
     */
    @OpenXmlMark(PmdTag.BR)
    void openBr(){
        if(this.currentBredTxt == null) return;
        this.currentBredTxt.append('\n');
        return;
    }

}
