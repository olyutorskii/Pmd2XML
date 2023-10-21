# Pmd2XML #

-----------------------------------------------------------------------


## Pmd2XMLとは ? ##

* **Pmd2XML**は、3D動画制作ツール、MikuMikuDance(MMD)で用いられる
モデルデータファイル(*.pmd)の内容を、XML形式のデータファイルと交換するための
Javaアプリケーションです。
Pmd2XMLは、MikuTogaプロジェクトの派生物として誕生しました。

* MikuMikuDance 3D-CG animation tool was used by a community of Japanese speakers,
so much of Pmd2XML's documentation and comments contain Japanese word.
We plan to provide information in English at some point.

* MikuTogaプロジェクトは、MMDによる3Dアニメーション制作を支援するプログラムの
整備のために発足した、オープンソースプロジェクトです。

* Pmd2XMLは2023年10月頃まで [OSDN][OSDN](旧称 SourceForge.jp)
でホスティングされていました。
OSDNの可用性に関する問題が長期化しているため、GitHubへと移転してきました。

* Pmd2XMLは、同じMikuTogaプロジェクト内のTogaGemライブラリを用いて
開発が進められています。

* PMXモデルファイルは扱えません。


## ビルド方法 ##

* Pmd2XMLはビルドに際して [Maven 3.3.9+](https://maven.apache.org/)
と JDK 1.8+ を要求します。

* Pmd2XMLは[TogaGem][TOGAGEM]などのライブラリに依存しています。
開発時はMaven等を用いてこれらのライブラリを用意してください。

* Mavenを使わずとも `src/main/java/` 配下のソースツリーをコンパイルすることで
ライブラリを構成することが可能です。


## 使い方 ##

例) ※ PMDモデルファイルinput.pmdをXMLファイルoutput.xmlに変換したい場合。

`java -jar pmd2xml-X.X.X.jar -i input.pmd -o output.xml`

例) ※ XMLファイルinput.xmlをPMDモデルファイルoutput.pmdに変換したい場合。

`java -jar pmd2xml-X.X.X.jar -i input.xml -o output.pmd`

オプション詳細は-helpオプションで確認してください。


## ライセンス ##

* Pmd2XML独自のソフトウェア資産には [The MIT License][MIT] が適用されます.


## プロジェクト創設者 ##

* 2010年に [Olyutorskii](https://github.com/olyutorskii) によってプロジェクトが発足しました。


[OSDN]: https://ja.osdn.net
[TOGAGEM]: https://github.com/olyutorskii/TogaGem
[MIT]: https://opensource.org/licenses/MIT


--- EOF ---
