/*
 * annotation for beginning XML-element
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd.model.xml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * XML要素開始通知用注釈。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface OpenXmlMark {
    /** タグ指定。 */
    PmdTag value();
}
