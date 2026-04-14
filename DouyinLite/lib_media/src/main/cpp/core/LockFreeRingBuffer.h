#pragma once
#include <atomic>
#include <cstdint>
#include <cstdlib>
#include <cstring>
#include <algorithm>

namespace douyin {
namespace core {

// 强制内存对齐到 64 字节，彻底杜绝读写指针在 L1 Cache 中的伪共享现象
struct alignas(64) LockFreeRingBuffer {
    uint8_t* data;
    size_t capacity;

    alignas(64) std::atomic<size_t> write_pos;
    alignas(64) std::atomic<size_t> read_pos;

    LockFreeRingBuffer(size_t cap) : capacity(cap), write_pos(0), read_pos(0) {
        // 使用 posix_memalign 保证数据块的起始地址也在 64 字节边界 (兼容更多系统)
        void* ptr = nullptr;
        if (posix_memalign(&ptr, 64, capacity) == 0) {
            data = static_cast<uint8_t*>(ptr);
        } else {
            data = nullptr;
        }
    }

    ~LockFreeRingBuffer() {
        if (data) free(data);
    }

    // 核心写入，必须使用 memory_order_release
    size_t PushData(const uint8_t* in_data, size_t len) {
        size_t current_read = read_pos.load(std::memory_order_acquire);
        size_t current_write = write_pos.load(std::memory_order_relaxed);
        size_t available_space = capacity - (current_write - current_read);

        if (available_space < len) return 0; // 空间不足，抛弃或等待

        size_t offset = current_write % capacity;
        size_t first_chunk = std::min(len, capacity - offset);

        memcpy(data + offset, in_data, first_chunk);
        if (first_chunk < len) {
            memcpy(data, in_data + first_chunk, len - first_chunk); // 绕回环
        }

        write_pos.fetch_add(len, std::memory_order_release); // 发布数据
        return len;
    }
};

} // namespace core
} // namespace douyin
