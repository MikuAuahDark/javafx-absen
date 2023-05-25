package id.npad93.p10;

import java.util.LinkedList;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class Util {
    public static class Observer<T> implements ObservableValue<T> {
        private Callable<T> getter;
        private LinkedList<ChangeListener<? super T>> callbacks;

        private Observer(Callable<T> getter) {
            this.getter = getter;
            callbacks = new LinkedList<>();
        }

        @Override
        public void addListener(InvalidationListener listener) {
            // Not support lazy evaluation
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            // Not support lazy evaluation
        }

        @Override
        public void addListener(ChangeListener<? super T> listener) {
            callbacks.add(listener);
        }

        @Override
        public void removeListener(ChangeListener<? super T> listener) {
            callbacks.remove(listener);
        }

        void fireChange(T oldValue, T newValue) {
            for (ChangeListener<? super T> cb : callbacks) {
                cb.changed(this, oldValue, newValue);
            }
        }

        @Override
        public T getValue() {
            try {
                return getter.call();
            } catch (Exception e) {
                return null;
            }
        }
    }

    static class ObserverMapper<T> extends WeakHashMap<Observer<T>, Callable<T>> {
        public ObserverMapper() {
            super();
        }
    }

    public static <T> Observer<T> makeObservable(Callable<T> getter) {
        return new Observer<T>(getter);
    }

    public static String escapeCSV(String str) {
        if (str.indexOf(',') != -1) {
            return "\"" + str.replace("\"", "\\\"") + "\"";
        } else {
            return str;
        }
    }
}
