package org.mengyun.tcctransaction.observe;

import org.mengyun.tcctransaction.utils.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nervose.Wu
 * @date 2024/2/23 15:51
 */
public abstract class AbstractObservable<T> implements Observable<T> {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractObservable.class);
    protected final ConcurrentHashSet<Observer<T>> observers = new ConcurrentHashSet<>();

    protected volatile boolean closed = false;

    @Override
    public void addObserver(Observer<T> observer) {
        observers.add(observer);
    }

    @Override
    public void deleteObserver(Observer<T> observer) {
        observers.remove(observer);
    }

    @Override
    public ConcurrentHashSet<Observer<T>> getObservers() {
        return observers;
    }

    @Override
    public void notifyObservers(T message) {

        if (closed) {
            return;
        }
        for (Observer<T> observer : observers) {
            try {
                observer.onMessage(message);
            } catch (Exception ignore) {
                //ignore
            }
        }
    }

    @Override
    public void close() {
        closed = true;
    }
}
