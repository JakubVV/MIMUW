CC = gcc
CFLAGS = -Wall -Wextra -Wno-implicit-fallthrough -std=gnu17 -fPIC -O2
LDFLAGS = -shared -Wl,--wrap=malloc -Wl,--wrap=calloc -Wl,--wrap=realloc -Wl,--wrap=reallocarray -Wl,--wrap=free -Wl,--wrap=strdup -Wl,--wrap=strndup

OBJS = ma.o memory_tests.o

.PHONY: all clean test

libma.so: $(OBJS)
	$(CC) $(LDFLAGS) -o $@ $(OBJS)

ma.o: ma.c ma.h
	$(CC) $(CFLAGS) -c -o $@ ma.c

memory_tests.o: memory_tests.c memory_tests.h
	$(CC) $(CFLAGS) -c -o $@ memory_tests.c

clean:
	rm -f *.o *.so test

test: libma.so ma_example.c
	$(CC) -o test ma_example.c -L. -lma
	LD_LIBRARY_PATH=. ./test alibaba

