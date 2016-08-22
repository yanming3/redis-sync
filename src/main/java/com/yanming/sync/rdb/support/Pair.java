package com.yanming.sync.rdb.support;

/**
 * Created by allan on 16/5/9.
 */
public class Pair<A, B> {
    private A one;
    private B two;

    public Pair(A one, B two) {
        this.one = one;
        this.two = two;
    }

    public A getOne() {
        return one;
    }

    public void setOne(A one) {
        this.one = one;
    }

    public B getTwo() {
        return two;
    }

    public void setTwo(B two) {
        this.two = two;
    }
}
