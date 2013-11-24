package com.skvortsov.mtproto.filter;

import com.skvortsov.mtproto.Constructor;

/**
 * Created by skvortsov on 10/10/13.
 */
public interface ConstructorFilter {

    public boolean accept(Constructor constructor);

}
