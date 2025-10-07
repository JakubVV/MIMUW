#ifdef NDEBUG
#undef NDEBUG
#endif

#include "ma.h"
#include "memory_tests.h"
#include <assert.h>
#include <errno.h>
#include <stdbool.h>
#include <stdio.h>
#include <string.h>

#define PASS 0
#define FAIL 1
#define WRONG_TEST 2

#define SIZE(x) (sizeof x / sizeof x[0])

#define ASSERT(f)                        \
  do {                                   \
    if (!(f))                            \
      return FAIL;                       \
  } while (0)

#define V(code, where) (((unsigned long)code) << (3 * where))

void t_one(uint64_t *next_state, uint64_t const *input,
           uint64_t const *old_state, size_t, size_t) {
  next_state[0] = old_state[0] + input[0];
}

void y_one(uint64_t *output, uint64_t const *state, size_t, size_t) {
  output[0] = state[0] + 1;
}

static void t_two(uint64_t *next_state, uint64_t const *input,
                  uint64_t const *old_state, size_t, size_t) {
  next_state[0] = old_state[0] ^ input[0];
}

static int one(void) {
  const uint64_t q1 = 1, x3 = 3, *y;

  moore_t *a = ma_create_full(64, 64, 64, t_one, y_one, &q1);
  assert(a);

  y = ma_get_output(a);
  ASSERT(y != NULL);
  ASSERT(ma_set_input(a, &x3) == 0);
  ASSERT(y[0] == 2);
  ASSERT(ma_step(&a, 1) == 0);
  ASSERT(y[0] == 5);
  ASSERT(ma_step(&a, 1) == 0);
  ASSERT(y[0] == 8);
  ASSERT(ma_set_input(a, &q1) == 0);
  ASSERT(ma_set_state(a, &x3) == 0);
  ASSERT(y[0] == 4);
  ASSERT(ma_step(&a, 1) == 0);
  ASSERT(y[0] == 5);
  ASSERT(ma_step(&a, 1) == 0);
  ASSERT(y[0] == 6);

  ma_delete(a);
  return PASS;
}

static int two(void) {
  uint64_t x = 1;
  const uint64_t *y[2];
  moore_t *a[2];

  a[0] = ma_create_simple(1, 1, t_two);
  a[1] = ma_create_simple(1, 1, t_two);
  assert(a[0]);
  assert(a[1]);

  y[0] = ma_get_output(a[0]);
  y[1] = ma_get_output(a[1]);
  ASSERT(y[0] != NULL);
  ASSERT(y[1] != NULL);

  ASSERT(ma_set_input(a[0], &x) == 0);
  ASSERT(ma_connect(a[1], 0, a[0], 0, 1) == 0);
  ASSERT(y[1][0] == 0 && y[0][0] == 0);

  ASSERT(ma_step(a, 2) == 0);
  ASSERT(y[1][0] == 0 && y[0][0] == 1);

  ASSERT(ma_step(a, 2) == 0);
  ASSERT(y[1][0] == 1 && y[0][0] == 0);

  ASSERT(ma_step(a, 2) == 0);
  ASSERT(y[1][0] == 1 && y[0][0] == 1);

  ASSERT(ma_step(a, 2) == 0);
  ASSERT(y[1][0] == 0 && y[0][0] == 0);
  ASSERT(ma_step(a, 2) == 0);

  ASSERT(y[1][0] == 0 && y[0][0] == 1);

  ASSERT(ma_disconnect(a[1], 0, 1) == 0);
  x = 0;
  ASSERT(ma_set_input(a[1], &x) == 0);
  ASSERT(ma_step(a, 2) == 0);
  ASSERT(y[1][0] == 0 && y[0][0] == 0);
  ASSERT(ma_step(a, 2) == 0);
  ASSERT(y[1][0] == 0 && y[0][0] == 1);

  ma_delete(a[0]);
  ma_delete(a[1]);
  return PASS;
}

static unsigned long alloc_fail_test(void) {
  const uint64_t q1 = 1;
  unsigned long visited = 0;
  moore_t *maf, *mas;

  errno = 0;
  if ((maf = ma_create_full(64, 64, 64, t_one, y_one, &q1)) != NULL)
    visited |= V(1, 0);
  else if (errno == ENOMEM &&
           (maf = ma_create_full(64, 64, 64, t_one, y_one, &q1)) != NULL)
    visited |= V(2, 0);
  else
    return visited |= V(4, 0);

  errno = 0;
  if ((mas = ma_create_simple(1, 1, t_two)) != NULL)
    visited |= V(1, 1);
  else if (errno == ENOMEM && (mas = ma_create_simple(1, 1, t_two)) != NULL)
    visited |= V(2, 1);
  else
    return visited |= V(4, 1); 

  ma_delete(maf);
  ma_delete(mas);

  return visited;
}

static int memory_test(unsigned long (* test_function)(void)) {
  memory_test_data_t *mtd = get_memory_test_data();

  unsigned fail = 0, pass = 0;
  mtd->call_total = 0;
  mtd->fail_counter = 1;
  while (fail < 3 && pass < 3) {
    mtd->call_counter = 0;
    mtd->alloc_counter = 0;
    mtd->free_counter = 0;
    mtd->function_name = NULL;
    unsigned long visited_points = test_function();
    if (mtd->alloc_counter != mtd->free_counter ||
        (visited_points & 0444444444444444444444UL) != 0) {
      fprintf(stderr,
              "fail_counter %u, alloc_counter %u, free_counter %u, "
              "function_name %s, visited_point %lo\n",
              mtd->fail_counter, mtd->alloc_counter, mtd->free_counter,
              mtd->function_name, visited_points);
      ++fail;
    }
    if (mtd->function_name == NULL)
      ++pass;
    else
      pass = 0;
    mtd->fail_counter++;
  }

  return mtd->call_total > 0 && fail == 0 ? PASS : FAIL;
}

static int memory(void) {
  memory_tests_check();
  return memory_test(alloc_fail_test);
}

typedef struct {
  char const *name;
  int (*function)(void);
} test_list_t;

#define TEST(t) {#t, t}

static const test_list_t test_list[] = {
  TEST(one),
  TEST(two),
  TEST(memory),
};

static int do_test(int (*function)(void)) {
  int result = function();
  puts("quite long magic string");
  return result;
}

int main(int argc, char *argv[]) {
  if (argc == 2)
    for (size_t i = 0; i < SIZE(test_list); ++i)
      if (strcmp(argv[1], test_list[i].name) == 0)
        return do_test(test_list[i].function);

  fprintf(stderr, "Usage:\n%s test_name\n", argv[0]);
  return WRONG_TEST;
}
