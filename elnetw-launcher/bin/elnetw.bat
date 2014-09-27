@echo off
title Elnetw Launcher (win)
prompt $S
REM ====== 設定開始 ======
REM  ↓強制的にポータブル状態にするときは1、しないときは0
set TURETWCL_FORCE_PORTABLE=0

REM  ↓javaコマンドに渡す引数
set TURETWCL_JAVA_ARGS=

REM  ↓javaの起動に使用するコマンド。おすすめは"javaw.exe"。起動に失敗するときは"java.exe"を指定して下さい。
set TURETWCL_JAVA_COMMAND=javaw.exe

REM  ↓コマンドプロンプトを閉じないようにするときは1にする。
set TURETWCL_VIEW_LOG=0

REM ===== 設定おわり =====

REM +-----------------------
REM |elnetw Launcher
REM |
REM |ランチャーjarを見付け出して起動させる。
REM +-----------------------

setlocal

if "%TURETWCL_FORCE_PORTABLE%"=="1" (
  set PORTABLE_CFG=true
) else (
  set PORTABLE_CFG=false
)

REM JAVA_HOMEがある
if defined JAVA_HOME goto FoundJhomeENV

REM PATH環境変数の中にjava.exeが存在する
for %%i in (%TURETWCL_JAVA_COMMAND%) do (
    set JAVA_BIN="%%~$PATH:i"
    goto init
)

REM 見つからないときはエラー
echo.
echo ERROR: JAVA_HOME not found in your environment.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation
echo.
goto error



:FoundJhomeENV
if exist "%JAVA_HOME%\bin\%TURETWCL_JAVA_COMMAND%" goto FoundJhomeENVbin

echo.
echo ERROR: JAVA_HOME is set to an invalid directory.
echo JAVA_HOME = "%JAVA_HOME%"
echo Please set the JAVA_HOME environment variable to match the
echo   location of your Java installation
echo  or install JRE (not JDK) to store 'javaw.exe' in your PATH.
echo.
goto error



:FoundJhomeENVbin
set JAVA_BIN=%JAVA_HOME%\bin\%TURETWCL_JAVA_COMMAND%
goto init



:init

REM 現在の作業ディレクトリを保存する
set WORKDIR=%CD%

REM バッチファイルが置いてある場所の上のディレクトリに移動し、ディレクトリパスを保存
cd /d %~dp0..
set BATCH_PARENT_DIR=%CD%
echo %BATCH_PARENT_DIR%
set BATCH_DIR=%~dp0
echo %BATCH_DIR%
cd %WORKDIR%

REM binディレクトリから直接起動させたときは上のディレクトリにする。
if "%BATCH_DIR%"=="%WORKDIR%\" cd ..

REM targetが存在するならポータブル設定
if exist "%BATCH_PARENT_DIR%\target\elnetw-dist.jar" set PORTABLE_CFG=true

if exist "%BATCH_PARENT_DIR%\target\elnetw-dist.jar" (
  set TT_CLASSPATH=%BATCH_PARENT_DIR%\target\elnetw-dist.jar
  goto ttstart
)
if exist "%BATCH_PARENT_DIR%\bin\launcher.jar" (
  set TT_CLASSPATH=%BATCH_PARENT_DIR%\bin\launcher.jar;%BATCH_PARENT_DIR%\bin\library.jar
  goto ttstart
)

REM jarが見つからない
echo.
echo ERROR: Usable jar is not found.
echo Please re-install or, 
echo  if you compile from source, run 'mvn assembly:assembly'
echo   in elnetw-launcher directory.
echo.
goto error

:ttstart
set JAVA_ARGS=-Dconfig.portable=%PORTABLE_CFG% -splash:%BATCH_PARENT_DIR%\bin\splash.png %TURETWCL_JAVA_ARGS%

if "%TURETWCL_VIEW_LOG%"=="1" (
  echo "%JAVA_BIN%" %JAVA_ARGS% -classpath %TT_CLASSPATH% jp.mydns.turenar.launcher.TwitterClientLauncher -L%BATCH_PARENT_DIR%\lib %*
  "%JAVA_BIN%" %JAVA_ARGS% -classpath %TT_CLASSPATH% jp.mydns.turenar.launcher.TwitterClientLauncher -L%BATCH_PARENT_DIR%\lib %*
  pause
) else (
  start "elnetw" "%JAVA_BIN%" %JAVA_ARGS% -classpath %TT_CLASSPATH% jp.mydns.turenar.launcher.TwitterClientLauncher -L %BATCH_PARENT_DIR%\lib %*
)
if not errorlevel 0 goto error
goto end


:error
endlocal
pause
exit /B 1

:end
endlocal
exit /B
