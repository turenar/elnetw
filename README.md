# elnetw (エルナト)
_Twitter client for HITOBASIRA_

## はじめに

elnetwはP3:PeraPeraPrvに少し影響をうけた、Java上で動くTwitterクライアントです。

Linux上で動くことを念頭に置いてますが、Java上で動くのでWindowsでもMac OSでもあるいは他のOSでも動くことが期待されています (サポートはLinuxとWindowsのみです。他のOSはパッチを投げないと修正できません)

開発者の技量と時間不足によりあまり使えるものではありませんので、Linuxなら **mikutter** を使うとか、WindowsやMac OSならわざわざこんなの使わなくても **TweetDeck** や **(Open)Tween** や **Krile** や **YoruFukurou** とかを素直に使うのがいいと思うのですよ。

## 動作環境等

 * OS
     JRE1.7以上がインストールされGUIが利用できること
 * メモリ
     あればたくさん (目安としては空き領域が128MB以上)
 * ストレージ
     あればたくさん (キャッシュを保存するため空き領域が64MBくらいあるとよい)

### あると幸せになれるかも

 * 高速なインターネット
 * notify-sendコマンド (Linuxのみ？: Ubuntuではlibnotify-binパッケージ)
 * java-gnome (Linuxのみ: gtk.jarへの(ハードorシンボリック)リンクを~/.elnetw/libに作成してください)
 * zenityコマンド(Linuxのみ)

## パッケージのダウンロード

[公式版リリース]とか[開発版リリース]をどうぞ。

[公式版リリース]: http://sourceforge.jp/users/turenar/pf/elnetw/files/?id=2305
[開発版リリース]: http://sourceforge.jp/users/turenar/pf/elnetw/files/?id=2303

## ソースコードからの使用方法

### ビルド

    $ git clone https://github.com/turenar/elnetw.git
    $ cd elnetw
    $ mvn install

### 実行

    $ elnetw-launcher/bin/elnetw

## 主な機能

 * クエリフィルタ
 * ブロック中のユーザーのオートミュート

## FAQ
ろくにFAQも書けない程度の能力（）
### Q.なんでこんなの作ろうと思ったの
A.Linuxで使いやすいのがP3:PeraPeraPrvとmikutterしかなかった。でもP3はストリームが使えないしmikutterはシングルアカウントだったから作ったんだよ。
### Q.なんで elnetw なの？
A.おうし座の [エルナト星](http://ja.wikipedia.org/wiki/%E3%82%A8%E3%83%AB%E3%83%8A%E3%83%88) からとったの。え？だったら elnatwじゃないの、って？なななな何を言ってるのかよくわからないな！！！！ (誤字をやっちゃったんです許してください)
### Q.この機能はつける気あるの？このバグ直してくれない？
A.今すぐissueを作るんだ！バグっぽいときは、そのバグが再現する手順を書いてネ☆バグの確認に時間がかかるとやる気がなくなっちゃうからね
### Q.このissueはいつ実装されるの？
A.気が向いたら。待てなかったらPull Request投げるとか、[@ture7]にパッチ投げてついでに一緒に開発させろむしろ代われ云々言えば泣いて喜ぶよ
### Q.コーディング規約とかあるの？
A.タブ文字を使用する以外は特に規約とか作る気ないです。IDEも気に入ってるもの使ってくれていいです。
### Q.クライアント名がださいんだけど
A. http://dev.twitter.com からアプリを作って、設定からConsumer(Secret)Keyを設定すれば via 部分が変わるよ！再認証を忘れずに！
### Q.FAQがみにくいんだけど
A.パッチをくれ
### Q.ふざけてんの？
A.当然だ

## ライセンス・ライブラリ

elnetw は MIT Licence のもとで公開されています。

ライブラリとして、[Twitter4J]、[SLF4J]および[Logback]、[base64]、[htmlparser]、[json]、[twitter-text-java]などを利用しています。

[@ture7]: http://twitter.com/ture7 "ごにょごにょやってるひとのTwitterアカウント"
[Twitter4J]: http://twitter4j.org/ "Twitter4J - A Java library for the Twitter API"
[SLF4J]: http://slf4j.org/ "Simple Logging Facade for Java (SLF4J)"
[Logback]: http://logback.qos.ch/ "Logback"
[twitter-text-java]: https://github.com/twitter/twitter-text-java "A Java implementation of Twitter's text processing library"
[htmlparser]: http://about.validator.nu/htmlparser/ "The Validator.nu HTML Parser"
[base64]: http://iharder.sourceforge.net/current/java/base64/ "Base64: Public Domain Base64 Encoder/Decoder"
[json]: http://www.json.org/java/index.html "JSON in Java"
