AstaViewer (仮)
Twitter Client for 'HITOBASHIRA'
================================
このファイルを読む上での注意
	・このファイルはソースコードからコンパイルした人向けには
	　作られていない箇所があります。配布元のwikiを参照して下さい
	・「ディレクトリ」と「フォルダ」は同じ意味です。また、
	　ディレクトリの区切りは"/"で統一しています。Windows環境の方は
	　適宜"\"に読み替えて下さい。

動作環境等

	OS　　　　　JRE1.6以上がインストールされGUIが使用できるコンピュータ
	メモリ　　　あればたくさん (目安としては空き領域が128MB以上)
	ストレージ　あればたくさん (キャッシュを保存するため空き領域が
	　　　　　　64MBくらいあると幸せになるかもしれません)

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

	bin ディレクトリにある turetwcl (Windows環境の場合は turetwcl.bat)
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
		設定　　　　%userprofile%\.turetwcl
		キャッシュ　%temp%\turetwcl
	その他の環境の場合は
		設定　　　　${HOME}/.turetwcl
		キャッシュ　${HOME}/.cache/turetwcl
	に保存されます。なお、実行ファイルでポータブル設定にした場合は、
	設定については作業ディレクトリ内に保存されます。

ライセンスについて

	このアプリケーションでは、ソースコードには MIT License が
	適用されています。ライセンス全文については doc/license/turetwcl.txt
	を参照して下さい。なお、ドキュメントはCC-BY 3.0 が適用されます。
	CC-BY 3.0については http://creativecommons.org/licenses/by/3.0/
	を参照して下さい。

	このアプリケーションでは、ライブラリとしてオープンソースプロジェクトの
	成果物が使用されております。使用されているライブラリの
	ライセンスについては doc/license/ 下のファイルを参照して下さい。

リンク

	配布元 (turetwcl)
		http://code.google.com/p/turetwcl
	作者のついった
		http://twitter.com/ture7
	ライブラリ等
		http://twitter4j.org/
		http://www.slf4j.org/
		http://logback.qos.ch/
		https://github.com/twitter/twitter-text-java/
		http://www.urbanophile.com/arenn/hacking/getopt/
