@echo off
title Elnetw Launcher (win)
prompt $S
REM ====== �ݒ�J�n ======
REM  �������I�Ƀ|�[�^�u����Ԃɂ���Ƃ���1�A���Ȃ��Ƃ���0
set TURETWCL_FORCE_PORTABLE=0

REM  ��java�R�}���h�ɓn������
set TURETWCL_JAVA_ARGS=

REM  ��java�̋N���Ɏg�p����R�}���h�B�������߂�"javaw.exe"�B�N���Ɏ��s����Ƃ���"java.exe"���w�肵�ĉ������B
set TURETWCL_JAVA_COMMAND=javaw.exe

REM  ���R�}���h�v�����v�g����Ȃ��悤�ɂ���Ƃ���1�ɂ���B
set TURETWCL_VIEW_LOG=0

REM ===== �ݒ肨��� =====

REM +-----------------------
REM |elnetw Launcher
REM |
REM |�����`���[jar�����t���o���ċN��������B
REM +-----------------------

setlocal

if "%TURETWCL_FORCE_PORTABLE%"=="1" (
  set PORTABLE_CFG=true
) else (
  set PORTABLE_CFG=false
)

REM JAVA_HOME������
if defined JAVA_HOME goto FoundJhomeENV

REM PATH���ϐ��̒���java.exe�����݂���
for %%i in (%TURETWCL_JAVA_COMMAND%) do (
    set JAVA_BIN="%%~$PATH:i"
    goto init
)

REM ������Ȃ��Ƃ��̓G���[
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

REM ���݂̍�ƃf�B���N�g����ۑ�����
set WORKDIR=%CD%

REM �o�b�`�t�@�C�����u���Ă���ꏊ�̏�̃f�B���N�g���Ɉړ����A�f�B���N�g���p�X��ۑ�
cd /d %~dp0..
set BATCH_PARENT_DIR=%CD%
echo %BATCH_PARENT_DIR%
set BATCH_DIR=%~dp0
echo %BATCH_DIR%
cd %WORKDIR%

REM bin�f�B���N�g�����璼�ڋN���������Ƃ��͏�̃f�B���N�g���ɂ���B
if "%BATCH_DIR%"=="%WORKDIR%\" cd ..

REM target�����݂���Ȃ�|�[�^�u���ݒ�
if exist "%BATCH_PARENT_DIR%\target\elnetw-dist.jar" set PORTABLE_CFG=true

if exist "%BATCH_PARENT_DIR%\target\elnetw-dist.jar" (
  set TT_CLASSPATH=%BATCH_PARENT_DIR%\target\elnetw-dist.jar
  goto ttstart
)
if exist "%BATCH_PARENT_DIR%\bin\launcher.jar" (
  set TT_CLASSPATH=%BATCH_PARENT_DIR%\bin\launcher.jar;%BATCH_PARENT_DIR%\bin\library.jar
  goto ttstart
)

REM jar��������Ȃ�
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
