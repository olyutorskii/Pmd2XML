/*
 * command line parser
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd2xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * コマンドラインの1オプションとその引数群に相当。
 */
final class CmdLine {

    private OptSwitch opt;
    private List<String> optArgs;


    /**
     * コンストラクタ。
     */
    private CmdLine() {
        super();
        return;
    }


    /**
     * コマンドライン解析を行う。
     *
     * @param args コマンドライン
     * @return 解析されたコマンドライン並び
     */
    static List<CmdLine> parse(String... args){
        List<String> list = Arrays.asList(args);
        return parse(list);
    }

    /**
     * コマンドライン解析を行う。
     *
     * @param argList コマンドライン
     * @return 解析されたコマンドライン並び
     */
    static List<CmdLine> parse(List<String> argList){
        List<CmdLine> result = new LinkedList<>();

        Iterator<String> it = argList.iterator();
        while (it.hasNext()) {
            String arg = it.next();

            CmdLine info = new CmdLine();
            result.add(info);

            info.opt = OptSwitch.parse(arg);

            int exArgNum = 0;
            if (info.opt != null) {
                exArgNum = info.opt.getExArgNum();
            }
            info.optArgs = new ArrayList<>(exArgNum + 1);

            info.optArgs.add(arg);

            for (int argCt = 0; argCt < exArgNum; argCt++) {
                if ( ! it.hasNext()) {
                    break;
                }
                String exarg = it.next();
                info.optArgs.add(exarg);
            }
        }

        return result;
    }


    /**
     * オプション識別子を返す。
     *
     * @return オプション識別子。
     *     オプションを伴わない単純なコマンドライン引数の場合はnullを返す。
     */
    OptSwitch getOptSwitch() {
        return this.opt;
    }

    /**
     * オプションに付随する引数群を返す。
     *
     * @return オプションに付随する引数群。
     *     先頭要素はオプション識別子。
     *     単純なコマンドライン引数の場合は自身が1要素のみを占める。
     */
    List<String> getOptArgs() {
        return this.optArgs;
    }

}
