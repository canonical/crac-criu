#! /usr/bin/bash

# Pre-requisites
# Runs on Ubuntu 24.10 and above only
# apt install openjdk-17-jdk-headless
# apt install crac-criu

rm -rf logs
rm -rf *.class

mkdir logs

JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
RC=0

$JAVA_HOME/bin/javac *.java > logs/compile.out 2>&1
if [ $? -ne 0 ]; then
    echo "Compilation failed"
    exit 1
fi

TESTS=(\
   ArraySortTest \
   DirectByteBufferTest \
   FileReaderTest \
   StringBufferTest \
   MemoryMappedReadTest \
   MemoryMappedWriteTest \
   SocketAcceptTest \
   EchoClientTest \
   SelectorServerSocketAcceptTest \
   SelectorConnectedSocketTest \
)

for TEST in "${TESTS[@]}"; do
    echo -n "Running $TEST: "
    $JAVA_HOME/bin/java "$TEST" > logs/$TEST.out 2>&1
    if [ $? -eq 0 ] ; then
        echo "PASS"
    else
        echo "FAIL"
	RC=1
    fi
done

exit $RC
