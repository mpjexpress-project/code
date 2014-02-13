set MPJ_HOME=%cd%
echo MPJ_HOME
java -Xmx1024m -cp lib/daemon.jar;. runtime.daemon.MPJDaemon 10000