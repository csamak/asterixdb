WORKING_DIR=$1
HADOOP_VERSION=$2
HDFS_URL=$3
HDFS_PATH=$4
export HADOOP_HOME=$WORKING_DIR/hadoop-$HADOOP_VERSION
echo "$HADOOP_HOME/bin/hadoop fs -rmr $HDFS_URL/$HDFS_PATH"
$HADOOP_HOME/bin/hadoop fs -rmr $HDFS_URL/$HDFS_PATH
