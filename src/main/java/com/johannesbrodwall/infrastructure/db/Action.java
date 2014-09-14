package com.johannesbrodwall.infrastructure.db;

public interface Action<T extends Exception> {

    void execute() throws T;

}
