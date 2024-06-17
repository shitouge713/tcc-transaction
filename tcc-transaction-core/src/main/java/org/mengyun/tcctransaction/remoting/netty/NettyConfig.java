package org.mengyun.tcctransaction.remoting.netty;

/**
 * netty配置操作接口类
 */
public interface NettyConfig {

    int getWorkSelectorThreadSize();

    int getWorkerThreadSize();

    int getSocketBacklog();

    int getSocketRcvBufSize();

    int getSocketSndBufSize();

    int getFrameMaxLength();

    int getRequestProcessThreadSize();

    int getRequestProcessThreadQueueCapacity();


}
