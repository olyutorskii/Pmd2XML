/*
 * command line argument info
 *
 * License : The MIT License
 * Copyright(c) 2012 MikuToga Partners
 */

package jp.sourceforge.mikutoga.pmd2xml;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import jp.sourceforge.mikutoga.pmd.ModelFileType;

/**
 * コマンドラインオプション情報。
 */
final class OptInfo {

    private static final String EOL_LF = "\n";
    private static final String EOL_CRLF = "\r\n";
    private static final String EOL_DEFAULT = EOL_LF;

    private static final String GENERATOR =
            Pmd2Xml.APPNAME + ' ' + Pmd2Xml.APPVER;

    private static final String HELP_CONSOLE =
              "-h       : put help message\n"
            + "-pmd2xml : convert *.pmd to *.xml\n"
            + "-xml2pmd : convert *.xml to *.pmd\n"
            + "-i file  : specify input file\n"
            + "-o file  : specify output file\n"
            + "-f       : force overwriting\n"
            + "-lf      : use LF as XML-newline (default)\n"
            + "-crlf    : use CR+LF as XML-newline\n"
            + "-gen     : print generator to XML (default)\n"
            + "-nogen   : do not print generator to XML\n"
            ;

    private static final String ERRMSG_UNKNOWN =
            "Unknown option : {0}";
    private static final String ERRMSG_NODIR =
            "You must specify -pmd2xml or -xml2pmd.";
    private static final String ERRMSG_NOINFILE =
            "You must specify input file with -i.";
    private static final String ERRMSG_NOOUTFILE =
            "You must specify output file with -o.";


    private boolean hasError = false;
    private String errMsg = null;

    private boolean needHelp = false;
    private ModelFileType inTypes  = ModelFileType.NONE;
    private ModelFileType outTypes = ModelFileType.NONE;
    private String inFilename = null;
    private String outFilename = null;
    private boolean overwrite = false;
    private String newline = EOL_DEFAULT;
    private String generator = GENERATOR;


    /**
     * コンストラクタ。
     */
    private OptInfo(){
        super();
        return;
    }


    /**
     * コマンドラインを解析する。
     * @param args コマンドライン
     * @return オプション情報
     */
    static OptInfo parseOption(String... args){
        OptInfo result = new OptInfo();

        int argIdx = 0;
        int argLength = args.length;

        argline: while(argIdx < argLength){
            String arg = args[argIdx];

            OptSwitch opt = OptSwitch.find(arg);
            if(opt == null){
                String errMsg = MessageFormat.format(ERRMSG_UNKNOWN, arg);
                result.putErrMsg(errMsg);
                break argline;
            }

            switch(opt){
            case OPT_HELP:
                result.needHelp = true;
                break argline;
            case OPT_FORCE:
                result.overwrite = true;
                break;
            case OPT_PMD2XML:
                result.inTypes  = ModelFileType.PMD;
                result.outTypes = ModelFileType.XML_101009;
                break;
            case OPT_XML2PMD:
                result.inTypes  = ModelFileType.XML_101009;
                result.outTypes = ModelFileType.PMD;
                break;
            case OPT_INFILE:
                argIdx++;
                if(argIdx >= argLength){
                    result.putErrMsg(ERRMSG_NOINFILE);
                    break argline;
                }
                result.inFilename = args[argIdx];
                break;
            case OPT_OUTFILE:
                argIdx++;
                if(argIdx >= argLength){
                    result.putErrMsg(ERRMSG_NOOUTFILE);
                    break argline;
                }
                result.outFilename = args[argIdx];
                break;
            case OPT_LF:
                result.newline = EOL_LF;
                break;
            case OPT_CRLF:
                result.newline = EOL_CRLF;
                break;
            case OPT_GEN:
                result.generator = GENERATOR;
                break;
            case OPT_NOGEN:
                result.generator = null;
                break;
            default:
                assert false;
                String errMsg = MessageFormat.format(ERRMSG_UNKNOWN, arg);
                result.putErrMsg(errMsg);
                break argline;
            }

            if(result.hasError()) return result;

            argIdx++;
        }

        if(result.hasError()) return result;
        if(result.needHelp()) return result;

        checkResult(result);

        return result;
    }

    /**
     * オプション整合性の事後検査。
     * @param result オプション情報
     */
    private static void checkResult(OptInfo result){
        if(   result.getInFileType()  == ModelFileType.NONE
           || result.getOutFileType() == ModelFileType.NONE ){
            result.putErrMsg(ERRMSG_NODIR);
            return;
        }

        if(result.getInFilename() == null){
            result.putErrMsg(ERRMSG_NOINFILE);
            return;
        }

        if(result.getOutFilename() == null){
            result.putErrMsg(ERRMSG_NOOUTFILE);
            return;
        }

        return;
    }

    /**
     * コンソール提示用ヘルプ出力文字列を返す。
     * @return オプションヘルプ文字列
     */
    static String getConsoleHelp(){
        return HELP_CONSOLE;
    }


    /**
     * 解析中にエラーが起きたか判定する。
     * @return エラーが起きていればtrue
     */
    boolean hasError(){
        return this.hasError;
    }

    /**
     * エラーメッセージを返す。
     * @return エラーメッセージ。なければnull
     */
    String getErrorMessage(){
        return this.errMsg;
    }

    /**
     * ヘルプ表示が必要か否か判定する。
     * @return 必要ならtrue
     */
    boolean needHelp(){
        return this.needHelp;
    }

    /**
     * 入力ファイル種別を返す。
     * @return 入力ファイル種別
     */
    ModelFileType getInFileType(){
        return this.inTypes;
    }

    /**
     * 出力ファイル種別を返す。
     * @return 出力ファイル種別
     */
    ModelFileType getOutFileType(){
        return this.outTypes;
    }

    /**
     * 入力ファイル名を返す。
     * @return 入力ファイル名
     */
    String getInFilename(){
        return this.inFilename;
    }

    /**
     * 出力ファイル名を返す。
     * @return 出力ファイル名
     */
    String getOutFilename(){
        return this.outFilename;
    }

    /**
     * 上書きモードか否か返す。
     * @return 上書きモードならtrue
     */
    boolean overwriteMode(){
        return this.overwrite;
    }

    /**
     * XML改行文字を返す。
     * @return 改行文字
     */
    String getNewline(){
        return this.newline;
    }

    /**
     * ジェネレータ名を返す。
     * @return ジェネレータ名。表示したくない時はnull
     */
    String getGenerator(){
        return this.generator;
    }

    /**
     * オプション解析エラー情報を設定する。
     * @param txt エラー文字列
     */
    private void putErrMsg(String txt){
        this.hasError = true;
        this.errMsg = txt;
        return;
    }


    /**
     * オプションスイッチ群。
     */
    static enum OptSwitch{
        OPT_HELP    ("-h", "-help", "-?"),
        OPT_XML2PMD ("-xml2pmd"),
        OPT_PMD2XML ("-pmd2xml"),
        OPT_INFILE  ("-i"),
        OPT_OUTFILE ("-o"),
        OPT_FORCE   ("-f"),
        OPT_LF      ("-lf"),
        OPT_CRLF    ("-crlf"),
        OPT_GEN     ("-gen"),
        OPT_NOGEN   ("-nogen"),
        ;


        /**
         * コンストラクタ。
         * @param cmdargs オプションスイッチパターン群
         */
        private OptSwitch(String... cmdargs){
            for(String cmdarg : cmdargs){
                MapHolder.MAP_OPT.put(cmdarg, this);
            }
            return;
        }

        /**
         * パターンに合致するオプションを見つける。
         * @param cmd パターン
         * @return オプション。見つからなければnull
         */
        static OptSwitch find(String cmd){
            OptSwitch result = MapHolder.MAP_OPT.get(cmd);
            return result;
        }


        /**
         * enumコンストラクタからクラス変数にアクセスできない文法を回避。
         */
        private static class MapHolder{
            static final Map<String, OptSwitch> MAP_OPT =
                    new HashMap<String, OptSwitch>();
        }

    }

}
