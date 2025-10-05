#ifndef MEMORY_TESTS_H
#define MEMORY_TESTS_H

// This struct stores info about memory operations.
typedef struct {
  volatile unsigned call_total;    // number of all function calls
  volatile unsigned call_counter;  // number of allocation calls
  volatile unsigned fail_counter;  // failed allocation number
  volatile unsigned alloc_counter; // number of completed allocations
  volatile unsigned free_counter;  // number of completed frees
  volatile char *function_name;    // failed function name
} memory_test_data_t;

memory_test_data_t * get_memory_test_data(void);

void memory_tests_check(void);

#endif
