package com.ais.utils;

public interface BiFunc<T1, T2, R> {
  R apply(T1 arg1, T2 arg2);
}
