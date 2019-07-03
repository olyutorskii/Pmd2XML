/*
 * command line option definition
 *
 * License : The MIT License
 * Copyright(c) 2013 MikuToga Partners
 */

package jp.sfjp.mikutoga.pmd2xml;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * オプションスイッチ群定義。
 */
enum OptSwitch {

    OPT_HELP(    0, "-h", "-help", "-?"),
    OPT_INFILE(  1, "-i"),
    OPT_OUTFILE( 1, "-o"),
    OPT_FORCE(   0, "-f"),
    OPT_NEWLINE( 1, "-nl"),
    OPT_GENOUT(  1, "-genout"),
    OPT_IFORM(   1, "-iform"),
    OPT_OFORM(   1, "-oform"),
    ;

    private static final String HELP_CONSOLE =
              "-h               : put help message\n\n"
            + "-i <file>        : specify input file\n"
            + "-o <file>        : specify output file\n"
            + "-f               : force overwriting\n\n"
            + "-nl <newline>    : specify XML-newline character"
            +                     " (default:lf)\n"
            + "-genout <bool>   : mark generator-name to XML"
            +                     " (default:on)\n\n"
            + "-iform <format>  : specify input format explicitly\n"
            + "-oform <format>  : specify output format explicitly\n\n"
            + "   bool : \"on\" or \"off\""
            +     " or \"true\" or \"false\""
            +      " or \"yes\" or \"no\"\n"
            + "   format : \"pmd\" or \"xml\" or"
            +            " \"xml101009\" or \"xml130128\"\n"
            + "   newline : \"lf\" or \"crlf\"\n"
            ;

    private static final Map<String, OptSwitch> MAP_OPT;

    static{
        Map<String, OptSwitch> map = new HashMap<String, OptSwitch>();

        for(OptSwitch opt : values()){
            for(String cmdarg : opt.cmdopts){
                map.put(cmdarg, opt);
            }
        }

        map = Collections.unmodifiableMap(map);
        MAP_OPT = map;
    }


    private final int exArgNum;
    private final List<String> cmdopts;


    /**
     * コンストラクタ。
     *
     * @param argnum 必要な引数の数
     * @param cmdopts オプションスイッチパターン群
     */
    private OptSwitch(int argnum, String... cmdopts) {
        this.exArgNum = argnum;

        List<String> optlist;
        optlist = Arrays.asList(cmdopts);
        optlist = Collections.unmodifiableList(optlist);
        this.cmdopts = optlist;

        return;
    }


    /**
     * コンソール提示用ヘルプ出力文字列を返す。
     *
     * @return オプションヘルプ文字列
     */
    static String getConsoleHelp(){
        return HELP_CONSOLE;
    }

    /**
     * 文字列に合致するオプションを返す。
     *
     * <p>一つのオプションが複数の表記に合致する場合がある。
     *
     * @param cmd 文字列
     * @return オプション種別。合致する物が見つからなければnull
     */
    static OptSwitch parse(String cmd){
        OptSwitch result = MAP_OPT.get(cmd);
        return result;
    }


    /**
     * 各オプションに後続する引数の数を返す。
     *
     * <p>引数をとらないオプションは0を返す。
     *
     * @return 引数の数
     */
    int getExArgNum(){
        return this.exArgNum;
    }

}
