--------------------------------------------------------------------------------
   タイトル : Terenai Bot System (仮称)
 バージョン : 0.1rc1
   カテゴリ : ユーティリティ？
       種別 : フリーソフト
   動作環境 : JRE1.6が動くところならたぶんどこでも。
 製作・著作 : Turenai Project
     配布元 : http://code.google.co.jp/p/tbotsys/
 ライセンス : MIT License
--------------------------------------------------------------------------------

★注意

　現在開発途中なのでバグが残っている可能性があります。使用は自己責任でお願いします。
　もし、バグを見つけられましたら、Google CodeにIssueとして登録するか、作者にお知らせください。

　このREADMEファイルには、ディレクトリ (フォルダ) 区切りとして"/"を使用しております。
　Windowsの場合には"\"に適宜読み替えてください。

★概要

　Terenai Bot System (以下 tbotsys) は Twitter用の「デスクトップ」ボットです。

★動かし方・アンインストール
　動かすには、シェル (コマンドプロンプト) で次のようにタイプしてください
　　　java -jar tbotsys.jar
　オプションなどは、シェル (コマンドプロンプト) で次のようにタイプして確認してください
　　　java -jar tbotsys.jar --help

　アンインストールの時は、レジストリ等は触ってないので、そのままディレクトリごと削除すればいいと思います

★特徴

  今のところないですね（←ネタではない、と思う

★ファイル
　- tbotsys.cfg.tmpl
      コピーしてtbotsys.cfgとして使用します。
  - tbotsys.cfg
      ボットのデフォルト設定ファイルです。(設定として使用するファイルは実行時オプションで変更できます)
  - data/randpost.txt.tmpl
      コピーしてdata/randpost.txtとして使用します
  - data/randpost.txt
      ランダム投稿する発言候補ファイルです。
  - tbotsys.jar
      プログラム本体です。
  - README.txt
      このファイルです。動作させる前に一度お読みください
  - LICENSE.txt
      ライセンスに関する文章が収録されています。

★使用条件・免責

・tbotsysは、フリーソフトです。利用は自由です。
・tbotsysは、MIT Licence以下で再配布などができます。
　


★引用・転載

・基本MIT License以下で使用できます。
・なにか問題がありましたら作者までお願いします


★連絡先
　Turenai Project
　・Twitter @turenai517
　・http://code.google.co.jp/p/tbotsys/ 


★謝辞
・ライブラリとして、Twitter4J (http://twitter4j.org/)を使用しております。
・ライブラリとして、java-getopt (http://www.urbanophile.com/arenn/hacking/download.html) を使用しております。
