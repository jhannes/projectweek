package com.johannesbrodwall.infrastructure;

import java.util.Collection;

public interface Repository<T> {

    void deleteAll();
    Collection<T> findAll();
    void insertOrUpdate(T entity);

}
