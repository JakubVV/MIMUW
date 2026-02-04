#include <stdio.h>
#include <pthread.h>
#include <unistd.h>
#include <stdbool.h>
#include <stdatomic.h>

const long counter = 50000000;

long x = 0;

// 1, 2
_Atomic int lock = 0;



void critical_section(void) {
    long y;
    y = x;
    y = y + 1;
    x = y;
}

void local_section(void) {
}

void entry_protocol(int nr) {
    (void)nr; 
    
    // 1
   // int expected = 0;
    //while (!atomic_compare_exchange_weak_explicit(&lock, &expected, 1, memory_order_acquire, memory_order_relaxed)) {
    //    expected = 0;
    //}
    
    // 2
    while (atomic_exchange_explicit(&lock, 1, memory_order_acquire) != 0) {
        // empty
    }
}

void exit_protocol(int nr) {
    (void)nr;  
    
    // 1, 2
    atomic_store_explicit(&lock, 0, memory_order_release);
}

void* th(void* arg) {
    int nr = *(int*)arg;
    for (long i = 0; i < counter; i++) {
        local_section();
        entry_protocol(nr);
        critical_section();
        exit_protocol(nr);
    }
    return NULL;
}

void* monitor(void* arg) {
    (void)arg; // Unused argument
    long prev = 0;
    while (1) {
        prev = x;
        sleep(2);
        if (prev == x)
            printf("Deadlock! lock = %d\n", atomic_load_explicit(&lock, memory_order_relaxed));
        else
            printf("monitor: %ld\n", x);
    }
}

int main() {
    printf("main() starts\n");
    pthread_t monitor_th;
    pthread_t t1, t2;

    pthread_create(&monitor_th, NULL, monitor, NULL);
    pthread_create(&t1, NULL, th, (void*)&(int){0});
    pthread_create(&t2, NULL, th, (void*)&(int){1});

    pthread_join(t1, NULL);
    pthread_join(t2, NULL);

    printf("main() completes: %ld\n", x);

    return 0;
}