//package com.basic.benchmark;
//
//import java.sql.Timestamp;
//
///**
// * Created by 79875 on 2017/3/6.
// */
//public class WordCountTupleTask implements Runnable {
//
//    private Long tupplecount;
//
//    private Timestamp timestamp;
//
//    private boolean type;//true为BalancingGrouping策略 false为pkgGrouping策略
//
//    public WordCountTupleTask(boolean type) {
//        this.type = type;
//    }
//
//    public WordCountTupleTask(Timestamp timestamp, Long tupplecount,boolean type){
//        this.timestamp=timestamp;
//        this.tupplecount=tupplecount;
//        this.type=type;
//    }
//
//    public void run() {
//        if(type)
//            DataBaseUtil.insertBalancingTupleCount(timestamp,tupplecount);
//        else
//            DataBaseUtil.insertPKGTupleCount(timestamp,tupplecount, taskid);
//    }
//}
