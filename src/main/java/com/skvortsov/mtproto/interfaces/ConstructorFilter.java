package com.skvortsov.mtproto.interfaces;

import com.skvortsov.mtproto.Constructor;

/**
 * Created by skvortsov on 10/10/13.
 */
public interface ConstructorFilter {

    public boolean accept(Constructor constructor);

}
