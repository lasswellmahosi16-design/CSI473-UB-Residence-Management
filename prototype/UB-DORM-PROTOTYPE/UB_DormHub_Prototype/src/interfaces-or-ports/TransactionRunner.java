package ub.dormhub.ports;

import java.util.function.Supplier;

public interface TransactionRunner {
    <T> T inTransaction(Supplier<T> work);
}
