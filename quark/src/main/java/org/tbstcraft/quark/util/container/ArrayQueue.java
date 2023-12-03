package org.tbstcraft.quark.util.container;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

/**
 * array queue
 * <p>work with inject value from header,poll from last</p>
 *
 * @param <E> container Template class.
 * @author GrassBlock2022
 */
public class ArrayQueue<E> {
    public ArrayList<E> list = new ArrayList<>();
    public HashMap<String, Object> map = new HashMap<>();

    /**
     * get size of container.
     *
     * @return size
     * @see ArrayList
     */
    public int size() {
        return this.list.size();
    }

    /**
     * get is empty from list.
     *
     * @return is empty.
     * @see ArrayList
     */
    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    /**
     * get if contains object.
     *
     * @param e element
     * @return get if contains.
     * @see ArrayList
     */
    public boolean contains(E e) {
        return this.map.containsKey(e.toString());
    }

    /**
     * generate an array of element.
     *
     * @param a array sample.
     * @return get array.
     * @see ArrayList
     */
    public E[] toArray(E[] a) {
        return this.list.toArray(a);
    }


    /**
     * add an object,inject in head.
     *
     * @param e element
     */
    public void add(E e) {
        if (e != null && !this.map.containsKey(e.toString())) {
            if (size() < 0) {
                this.list.clear();
            }
            this.map.put(e.toString(), null);
            this.list.add(0, e);
        }
    }

    /**
     * add some object,by order.
     *
     * @param all list of elements.
     */
    public void addAll(List<E> all) {
        for (E e : all) {
            this.add(e);
        }
    }

    /**
     * poll an object from ending,return null if array is empty
     *
     * @return element
     */
    public E poll() {
        try {
            if (list.size() > 0) {
                E e = this.list.remove(this.list.size() - 1);
                this.map.remove(e.toString());
                return e;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * poll all from list,by order.
     *
     * @param count count
     * @return list.
     */
    public List<E> pollAll(int count) {
        ArrayList<E> returns = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            E e = this.poll();
            if (e != null) {
                returns.add(e);
            }
        }
        return returns;
    }

    /**
     * clear map.
     */
    public void clear() {
        this.list.clear();
    }

    /**
     * remove by prediction
     *
     * @see ArrayList
     */
    public void removeIf(Predicate<E> predicate) {
        for (E e : this.list) {
            if (!predicate.test(e)) {
                continue;
            }
            this.list.remove(e);
            this.map.remove(e.toString());
        }
    }

    /**
     * remove element
     *
     * @param e element
     * @see ArrayList
     */
    public void remove(E e) {
        this.list.remove(e);
        this.map.remove(e.toString());
    }

    /**
     * sort arraylist
     *
     * @param sorter sorter
     * @see ArrayList
     */
    public void sort(Comparator<E> sorter) {
        this.list.sort(sorter);
    }

}
