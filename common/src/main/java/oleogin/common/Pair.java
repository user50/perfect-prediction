package oleogin.common;

import java.io.Serializable;

public class Pair<T,R> implements Serializable {

    private T first;
    private R second;

    public Pair(T first, R second) {
        this.first = first;
        this.second = second;
    }

    public Pair() {
    }

    public T getFirst() {
        return first;
    }

    public R getSecond() {
        return second;
    }

    public void setFirst(T first) {
        this.first = first;
    }

    public void setSecond(R second) {
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair<?, ?> pair = (Pair<?, ?>) o;

        if (!first.equals(pair.first)) return false;
        return second.equals(pair.second);
    }

    @Override
    public int hashCode() {
        int result = first.hashCode();
        result = 31 * result + second.hashCode();
        return result;
    }
}