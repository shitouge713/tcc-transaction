package org.mengyun.tcctransaction.observe;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author Nervose.Wu
 * @date 2024/2/23 16:39
 */
public class ObservableCenter {

    public static final ObservableCenter INSTANCE = new ObservableCenter();

    @SuppressWarnings("rawtypes")
    private final ConcurrentHashMap<ObservableType, Observable> observables = new ConcurrentHashMap<>();

    private ObservableCenter() {
    }

    public <T> void registerObservableIfAbsent(ObservableType observableType, Supplier<Observable<T>> observableSupplier) {
        observables.computeIfAbsent(observableType, key -> observableSupplier.get());
    }

    @SuppressWarnings("unchecked")
    public <T> void registerObserver(ObservableType observableType, Observer<T> observer){
        observables.get(observableType).addObserver(observer);
    }

    @SuppressWarnings("unchecked")
    public <T> boolean publish(ObservableType observableType, T message) {
        if (!observables.containsKey(observableType)) {
            return false;
        }
        observables.get(observableType).notifyObservers(message);
        return true;
    }

    public void close() {
        observables.values().forEach(Observable::close);
    }
}

