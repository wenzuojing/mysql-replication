#! /bin/sh

source /etc/profile

if [ -z "$JAVA_HOME" ] ; then
	export JAVA_HOME=/usr/local/java
fi


SCRIPT="$0"
while [ -h "$SCRIPT" ] ; do
  ls=`ls -ld "$SCRIPT"`
  # Drop everything prior to ->
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    SCRIPT="$link"
  else
    SCRIPT=`dirname "$SCRIPT"`/"$link"
  fi
done

SERVER_HOME=`dirname "$SCRIPT"`
SERVER_HOME=`cd "$SERVER_HOME" ; cd .. ; pwd`
export SERVER_HOME

LIBDIR=$SERVER_HOME/lib

CLASSPATH=${CLASSPATH}:${SERVER_HOME}/conf

for lib in ${LIBDIR}/*.jar
do
 CLASSPATH=$CLASSPATH:$lib
done

java=$JAVA_HOME/bin/java

JAVA_OPTS="
-Xmx2G
-Xms2G
-XX:PermSize=128M
-XX:MaxPermSize=256M
-XX:+UseConcMarkSweepGC
-XX:+UseParNewGC
-XX:+CMSConcurrentMTEnabled
-XX:+CMSParallelRemarkEnabled
-XX:+UseCMSCompactAtFullCollection
-XX:CMSFullGCsBeforeCompaction=0
-XX:+CMSClassUnloadingEnabled
-XX:LargePageSizeInBytes=128M
-XX:+UseFastAccessorMethods
-XX:+UseCMSInitiatingOccupancyOnly
-XX:CMSInitiatingOccupancyFraction=80
-XX:SoftRefLRUPolicyMSPerMB=0
-XX:+PrintClassHistogram
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-XX:+PrintHeapAtGC
-Xloggc:/data/logs/gc.log
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/data/logs/dump.hprof
"

echo "JAVA_HOME  :$JAVA_HOME"
echo "SERVER_HOME:$SERVER_HOME"
echo "CLASSPATH  :$CLASSPATH"
echo "JAVA_OPTS  :$JAVA_OPTS"

cd $SERVER_HOME

SERVER_PID="$SERVER_HOME/server.pid"

case $1 in
start)
    echo "Starting Server ... "
    if [ -f "$SERVER_PID" ]; then
      if kill -0 `cat "$SERVER_PID"` > /dev/null 2>&1; then
         echo $command already running as process `cat "$SERVER_PID"`.
         exit 0
      fi
    fi
    exec  $java -classpath  $CLASSPATH  $JAVA_OPTS mysql.replication.Bootstrap &
	if [ $? -eq 0 ]
    then
      if /bin/echo -n $! > "$SERVER_PID"
      then
        sleep 1
        echo "STARTED"
      else
        echo "FAILED TO WRITE PID"
        exit 1
      fi
    else
      echo "SERVER DID NOT START"
      exit 1
    fi
;;
restart)
    sh $0 stop
    sleep 30
    sh $0 start
;;
stop)
    echo "Stopping Server  ... "
    if [ ! -f "$SERVER_PID" ]
    then
      echo "no LTS-Admin to started (could not find file $SERVER_PID)"
    else
      kill -9 $(cat "$SERVER_PID")
      rm "$SERVER_PID"
      echo "STOPPED"
    fi
    exit 0
;;
*)
    echo "Usage: $0 {start|stop|restart}" >&2
esac
