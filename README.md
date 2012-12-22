# elnetw (エルナト)
_Twitter client for HITOBASIRA_

## はじめに

elnetwはP3:PeraPeraPrvに少し影響をうけた、Java上で動くTwitterクライアントです。

Linux上で動くことを念頭に置いてますが、Java上で動くのでWindowsでもMac OSでもあるいは他のOSでも動くことが期待されています (サポートはLinuxとWindowsのみです。他のOSはパッチを投げないと修正できません)

開発者の技量と時間不足によりあまり使えるものではありませんので、Linuxなら **mikutter** を使うとか、WindowsやMac OSならわざわざこんなの使わなくても **TweetDeck** や **(Open)Tween** や **Krile** や **YoruFukurou** とかを素直に使うのがいいと思うのですよ。

## 動作環境等

 * OS
     JRE1.6以上がインストールされGUIが利用できること
 * メモリ
     あればたくさん (目安としては空き領域が128MB以上)
 * ストレージ
     あればたくさん (キャッシュを保存するため空き領域が64MBくらいあると幸せになるかもしれません)

### あると幸せになれるかも

 * 高速なインターネット
 * notify-sendコマンド (Ubuntuではlibnotify-binパッケージ)

## ソースコードからの使用方法

### ビルド

    $ git clone https://github.com/turenar/elnetw.git
    $ cd elnetw
    $ mvn install
    $ cd elnetw-launcher; mvn assembly:assembly; cd ..

### 実行

    $ cd elnetw-launcher; bin/elnetw

## 主な機能

なし (簡単なユーザーフィルタ機能はmasterブランチにあります。簡単なクエリフィルタ機能ならfilter-implブランチにあります。)

## FAQ
ろくにFAQも書けない程度の能力（）
### Q.なんでこんなの作ろうと思ったの
A.Linuxで使いやすいのがP3:PeraPeraPrvとmikutterしかなかった。でもP3はストリームが使えないしmikutterはシングルアカウントだったから作ったんだよ。
### Q.この機能はつける気あるの？このバグ直してくれない？
A.今すぐ [Google Code](http://code.google.com/p/turetwcl/issues/list) に行ってissueを作るんだ！
### Q.このissueはいつ実装されるの？
A.気が向いたら。待てなかったらPull Request投げるとか、[@ture7]にパッチ投げてついでに一緒に開発させろむしろ代われ云々言えば泣いて喜ぶよ
### Q.ふざけてんの？
A.はい

## ライセンス・ライブラリ

elnetw は MIT Licence のもとで公開されています。

ライブラリとして、[Twitter4J]、[SLF4J]および[Logback]、[java-getopt]、[twitter-text-java]などを利用しています。

[@ture7]: http://twitter.com/ture7 "ごにょごにょやってるひとのTwitterアカウント"
[Twitter4J]: http://twitter4j.org/ "Twitter4J - A Java library for the Twitter API"
[SLF4J]: http://slf4j.org/ "Simple Logging Facade for Java (SLF4J)"
[Logback]: http://logback.qos.ch/ "Logback"
[java-getopt]: http://www.urbanophile.com/arenn/hacking/download.html#getopt "GNU Getopt - Java port"
[twitter-text-java]: https://github.com/twitter/twitter-text-java "A Java implementation of Twitter's text processing library"
