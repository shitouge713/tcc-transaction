package org.mengyun.tcctransaction.observe;

import java.util.Set;

/**
 * @author Nervose.Wu
 * @date 2024/2/23 15:48
 */
public interface Observable<T> {
    void addObserver(Observer<T> observer);

    void deleteObserver(Observer<T> observer);

    Set<Observer<T>> getObservers();

    void notifyObservers(T message);

    void close();
}
