package com.basic.benchmark;

import com.basic.core.util.MyScheme;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.kafka.*;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.storm.utils.Utils;

import java.util.Arrays;

/**
 * locate com.basic.benchmark
 * Created by tj on 2017/7/14.
 * Submit stormtopology storm jar popularity_awaredifferentiatedscheduling-1.0-SNAPSHOT.jar com.basic.benchmark.partialKeyGroupingTopology partialKeyGroupingTopology testTopic 18
 */
public class partialKeyGroupingTopology {
    public static final String KAFKA_SPOUT_ID ="kafka-spout";
    public static final String WORDCOUNTER_BOLT_ID ="wordcountter-bolt";
    public static final String AGGREGATOR_BOLT_ID= "aggregator-bolt";
    public static final String WORDCOUNTERREPORT_BOLT_ID= "wordcountreport-bolt";
    public static final String TOPOLOGY_NAME= "partialKeyGrouping-topology";

    public static final String WORDCOUNT_STREAM_ID="wordcountstream";
    public static final String TUPLECOUNT_STREAM_ID="tuplecountstream";

    public static void main(String[] args) throws InvalidTopologyException, AuthorizationException, AlreadyAliveException {
        String zks = "root2:2181,root4:2181,root5:2181";
        String topic= args[1];
        String zkRoot = "/stormkafka"; // default zookeeper root configuration for storm
        String id = "partialKeyGrouping";//对应一个应用

        BrokerHosts brokerHosts = new ZkHosts(zks,"/kafka/brokers");
        SpoutConfig spoutConf = new SpoutConfig(brokerHosts, topic, zkRoot, id);
        spoutConf.scheme = new SchemeAsMultiScheme(new MyScheme());
        spoutConf.ignoreZkOffsets = true;
        spoutConf.zkServers = Arrays.asList(new String[] {"root2", "root4", "root5"});
        spoutConf.zkPort = 2181;
        //      spoutConf.startOffsetTime = kafka.api.OffsetRequest.LatestTime();//从最新消息的开始读取
        spoutConf.startOffsetTime = kafka.api.OffsetRequest.EarliestTime();//从最旧的消息开始读取
        KafkaSpout kafkaSpout=new KafkaSpout(spoutConf);

        WordCounterBolt wordCounterBolt=new WordCounterBolt();
        WordCountReportBolt reportBolt=new WordCountReportBolt(false);
        MyAggregatorBolt aggregatorBolt=new MyAggregatorBolt();

        TopologyBuilder builder=new TopologyBuilder();
        Integer numworkers=Integer.valueOf(args[2]);

        builder.setSpout(KAFKA_SPOUT_ID, kafkaSpout, 9);
        builder.setBolt(WORDCOUNTER_BOLT_ID,wordCounterBolt, 36).partialKeyGrouping(KAFKA_SPOUT_ID, new Fields("word"));
        builder.setBolt(AGGREGATOR_BOLT_ID, aggregatorBolt, 36).fieldsGrouping(WORDCOUNTER_BOLT_ID, WORDCOUNT_STREAM_ID,new Fields("word"));

        builder.setBolt(WORDCOUNTERREPORT_BOLT_ID,reportBolt,9).shuffleGrouping(WORDCOUNTER_BOLT_ID,TUPLECOUNT_STREAM_ID);
        //Topology config
        Config config=new Config();
        config.setNumWorkers(numworkers);//config numworkers
        if(args[0].equals("local")){
            LocalCluster localCluster=new LocalCluster();

            localCluster.submitTopology(TOPOLOGY_NAME,config,builder.createTopology());
            Utils.sleep(50*1000);//50s
            localCluster.killTopology(TOPOLOGY_NAME);
            localCluster.shutdown();
        }else {
            StormSubmitter.submitTopology(args[0],config,builder.createTopology());
        }
    }
}
