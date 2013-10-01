//package by.muna.util;
package com.tl;

import java.util.HashMap;
import java.util.Map;

public class MapUtil {
    public static class MapAdder<T, S> {
        private Map<T, S> map;

        public MapAdder(Map<T, S> map) {
            this.map = map;
        }

        public MapAdder<T, S> add(T key, S value) {
            this.map.put(key, value);
            return this;
        }

        public Map<T, S> finish() {
            return this.map;
        }
    }

    public static <T, S> MapAdder<T, S> add(Map<T, S> map, T key, S value) {
        return new MapAdder<T, S>(map).add(key, value);
    }

    public static <T, S> MapAdder<T, Object> createObjectHashMap(T key, Object value) {
        return new MapAdder<T, Object>(new HashMap<T, Object>()).add(key, value);
    }
}
