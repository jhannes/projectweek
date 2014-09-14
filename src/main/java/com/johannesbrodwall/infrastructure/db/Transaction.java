package com.johannesbrodwall.infrastructure.db;

import java.io.Closeable;

public interface Transaction extends Closeable {

    void setCommit();

}
