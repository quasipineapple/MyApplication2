package com.skvortsov.mtproto.filter;

import com.skvortsov.mtproto.Constructor;
import com.skvortsov.mtproto.filter.ConstructorFilter;

/**
 * Created by skvortsov on 10/10/13.
 */
public class ConstructorPredicateFilter implements ConstructorFilter {

    private String constructorPredicate;

    public ConstructorPredicateFilter(String constructorPredicate) {

        if (constructorPredicate == null) {
            throw new IllegalArgumentException("Constructor Predicate cannot be null.");
        }

        this.constructorPredicate = constructorPredicate;
    }

    @Override
    public boolean accept(Constructor constructor) {

        return constructorPredicate.equals(constructor.getPredicate());
    }

    @Override
    public String toString() {
        return "ConstructorPredicateFilter by predicate: " + constructorPredicate;
    }
}
