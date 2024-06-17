package org.mengyun.tcctransaction.observe;

/**
 * @author Nervose.Wu
 * @date 2024/2/23 15:48
 */
public interface Observer<T> {

    void onMessage(T message);
}
