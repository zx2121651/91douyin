#pragma once
#include <atomic>

namespace douyin {
namespace core {

class SpinLock {
private:
    std::atomic_flag locked = ATOMIC_FLAG_INIT;

public:
    void lock() {
        while (locked.test_and_set(std::memory_order_acquire)) {
            // 降低 CPU 功耗，避免指令流水线拥堵
#if defined(__aarch64__)
            asm volatile("yield");
#elif defined(__x86_64__)
            asm volatile("pause");
#endif
        }
    }

    void unlock() {
        locked.clear(std::memory_order_release);
    }
};

} // namespace core
} // namespace douyin
