package com.johannesbrodwall.infrastructure.db;

public interface Retriever<T, U extends Exception> {

    T execute() throws U;

}
