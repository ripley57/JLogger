@ECHO off 
REM Description:
REM   Run pslist.exe through JLogger.
REM
REM   It is intended that this script be run periodically via a 
REM   Windows scheduled task.
REM
REM Installation:
REM   1. Create a 'script files' directory, e.g. "C:\jlogger_xyz"
REM      That directory needs to contain the following files:
REM         JLogger.java
REM         jlogger.properties
REM         RunPSList.bat
REM         pslist.exe
REM         log4j.jar
REM   2. Edit "DUMP_DIR" below in this script. 
REM      Set this to the location of the 'script files' directory, e.g. "C:\jlogger_xyz"
REM   3. [OPTIONAL] Change "NUM_DUMPS" and "DELAY_SECS" in this script.
REM   4. Run the script no arguments:
REM         RunPSList.bat
REM      (The Java .class file is generated, if it isn't found.)
REM   5. The output log files will be created in the script files directory.
REM
REM   JeremyC 14/11/2019.

REM Java location. You might want to use/change this.
set JAVA_HOME=C:\jdk-8u74-windows-i586
set PATH=%JAVA_HOME%\bin;%PATH%

REM The 'script files' directory.
REM This needs to be set to the location of this directory.
SET DUMP_DIR=C:\temp\jlogger_pslist

REM The command to be run from Java.
set COMMAND_TO_RUN=%DUMP_DIR%\pslist.exe /accepteula

REM Change these if you want. Note: If you are calling this script
REM periodically using a Windows scheduled task, then you probably
REM want to leave "NUM_DUMPS" set to 1, i.e. no consecutive dumps.
set NUM_DUMPS=1
set DELAY_SECS=60

IF NOT EXIST "%DUMP_DIR%" (
echo ERROR: No such directory "%DUMP_DIR%", exiting...
goto :end
)

REM Compile the Java program if required.
IF NOT EXIST "%DUMP_DIR%\JLogger.class" (
javac -cp "%DUMP_DIR%\log4j.jar" "%DUMP_DIR%\JLogger.java"
)

REM Run the Java program.
java.exe -classpath "%DUMP_DIR%\log4j.jar;%DUMP_DIR%" JLogger -v -r "%NUM_DUMPS%" -d "%DELAY_SECS%" -s "%DUMP_DIR%" -c %COMMAND_TO_RUN%
:end
