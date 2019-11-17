# JLogger

## DESCRIPTION
This demo shows how to use the JLogger.java program to call
an external program (pslist.exe in this example), and capture
the output to log files using the log4j logging framework.

It is assumed that the user will call the batch script via a
Windows scheduled task, although the Java program does also have
the built-in ability to capture multiple executions of the desired
external program, with a specified delay between each call.

If you want to change the external command being run, you should
only need to create a copy of all these files somewhere, and then
change only the "RunPSList.bat" script.


## INSTALLATION
See the comments at the top of JLogger.java


## COMMENTS
If pslist.exe gives the following error...

```
Processor performance object not found on E1317T
Try running Exctrlst from microsoft.com to repair the performance counters.
```

...then this worked for me (on Windows 8.1) - run this in an Administrator prompt:
`lodctr /r`

**Note:** The "lodctr" command also errored, but it did fix the problem:

```
C:\Windows\system32>lodctr /r
Error: Unable to rebuild performance counter setting from system backup store, error code is 2
C:\Windows\system32>
```

(See http://www.chuonthis.com/tips/2012/05/21/pslist-process-performance-object-not-found-run-exctrlst-from-the-windows-resource-kit-to-repair-the-performance-counters/)


