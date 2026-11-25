package com.example.demo.service.concurrency;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Provides per-maid locking to prevent concurrent double-booking on the same maid.
 */
@Component
public class MaidLockManager {

    private final ConcurrentHashMap<Long, ReentrantLock> locks = new ConcurrentHashMap<>();

    public <T> T executeWithLock(Long maidId, Supplier<T> action) {
        ReentrantLock lock = locks.computeIfAbsent(maidId, id -> new ReentrantLock());
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }
}
