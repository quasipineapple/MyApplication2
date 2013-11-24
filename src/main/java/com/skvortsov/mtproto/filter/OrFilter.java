package com.skvortsov.mtproto.filter;


import com.skvortsov.mtproto.Constructor;
import com.skvortsov.mtproto.filter.ConstructorFilter;

import java.util.ArrayList;
import java.util.List;


public class OrFilter implements ConstructorFilter {


    private List<ConstructorFilter> filters = new ArrayList<ConstructorFilter>();


    public OrFilter() {}

    public OrFilter(ConstructorFilter... filters) {
        if (filters == null) {
            throw new IllegalArgumentException("Parameter cannot be null.");
        }
        for(ConstructorFilter filter : filters) {
            if(filter == null) {
                throw new IllegalArgumentException("Parameter cannot be null.");
            }
            this.filters.add(filter);
        }
    }

    public void addFilter(ConstructorFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Parameter cannot be null.");
        }
        filters.add(filter);
    }

    public boolean accept(Constructor packet) {
        for (ConstructorFilter filter : filters) {
            if (filter.accept(packet)) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return filters.toString();
    }
}
