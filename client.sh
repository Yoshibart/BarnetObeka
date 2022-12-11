#Client Script
export JAVA_HOME=(/usr/libexec/java_home)
export PATH=$PATH:$JAVA_HOME/bin
export CLASSPATH=$JAVA_HOME/jre/lib:

javac AuctionClient.java
java -classpath . AuctionClient localhost 1234