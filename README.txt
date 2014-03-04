elnetw (エルナト)
Twitter Client for 'HITOBASHIRA'
================================
このファイルを読む上での注意
	・このファイルはソースコードからコンパイルした人向けには
	　作られていない箇所があります。配布元のwikiを参照して下さい
	・「ディレクトリ」と「フォルダ」は同じ意味です。また、
	　ディレクトリの区切りは"/"で統一しています。Windows環境の方は
	　適宜"\"に読み替えて下さい。

動作環境等

	OS　　　　　JRE1.7以上がインストールされGUIが使用できるコンピュータ
	メモリ　　　あればたくさん (目安としては空き領域が128MB以上)
	ストレージ　あればたくさん (キャッシュを保存するため空き領域が
	　　　　　　64MBくらいあると気持ちよくなれるかも)

	あると幸せになれるかも:
		* 高速なインターネット
		* notify-sendコマンド (ubuntuではlibnotify-binパッケージ)

インストール・アンインストール

	インストールは、ダウンロードしたファイルを展開することにより
	使用できるようになります。

	アンインストールは、展開により作成されたファイルを削除するなり
	ごみ箱に突っ込むなりで対処して下さい。
	レジストリ等はいじったりしてませんが、気になる場合は設定ファイルを
	削除して下さい。

使用について

	bin ディレクトリにある elnetw (Windows環境の場合は elnetw.bat)
	を実行して下さい。

	(Windows) UACの制限が掛かるフォルダ内では正常に動作するかが
		よくテストされていません。UACのかかるフォルダ (Program Files等)
		以外にファイルを展開するようにお願い致します。なお、
		UACにより正常に動かない場合は @ture7 までバグ報告をおねがいします。
	(Windows) ショートカットを作成する場合は、
		作業フォルダが"展開したフォルダ"になってることを確認して下さい。
	(Linux)	  ハードリンクではなくシンボリックリンクを
		作成するようにして下さい。

設定ファイル・キャッシュについて

	Windows環境の場合は
		設定　　　　%userprofile%\.elnetw
		キャッシュ　%temp%\elnetw
	その他の環境の場合は
		設定　　　　${HOME}/.elnetw
		キャッシュ　${HOME}/.cache/elnetw
	に保存されます。変更はアプリケーション引数として
		-Delnetw.home=<設定フォルダ> -Delnetw.cache.dir=<キャッシュフォルダ>
	を指定してください。


ライセンスについて

	このアプリケーションでは、MIT License が
	適用されています。ライセンス全文については doc/license/elnetw.txt
	を参照して下さい。

	このアプリケーションでは、ライブラリとしてオープンソースプロジェクトの
	成果物が使用されております。使用されているライブラリの
	ライセンスについては doc/license/ 下のファイルを参照して下さい。

リンク

	配布元 (elnetw)
		https://github.com/turenar/elnetw
		http://sourceforge.jp/users/turenar/pf/elnetw/
	作者のついった
		http://twitter.com/ture7
	ライブラリ等
		http://twitter4j.org/
		http://www.slf4j.org/
		http://logback.qos.ch/
		https://github.com/twitter/twitter-text-java/
		http://www.urbanophile.com/arenn/hacking/getopt/
