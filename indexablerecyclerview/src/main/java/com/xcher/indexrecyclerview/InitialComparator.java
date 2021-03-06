package com.xcher.indexrecyclerview;

import java.util.Comparator;

/**
 * Created by YoKey on 16/10/14.
 */
class InitialComparator<T extends IndexEntity> implements Comparator<EntityWrapper<T>> {
    @Override
    public int compare(EntityWrapper<T> lhs, EntityWrapper<T> rhs) {
        return lhs.getIndex().compareTo(rhs.getIndex());
    }
}
