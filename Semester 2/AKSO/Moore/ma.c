#include "ma.h"
#include "memory_tests.h"
#include <stdio.h>
#include <stdbool.h>
#include <errno.h>
#include <stdlib.h>
#include <string.h>
#include <memory.h>

typedef struct connected { 
    moore_t *au; 
    size_t bit;
} *conn;

struct moore {
    size_t inpBits, outpBits, stateBits;
    uint64_t *inp, *outp, *curState, *stateBuf;
    transition_function_t t;
    output_function_t y;
    struct connected *con;  // Array of structs holding information about automaton connections.
                            // conn[i].au - automaton connected to i-th bit.
                            // conn[i].bit - bit of conn[i].au connected to bit with index i.
                            // conn[i].au == NULL <=> i-th bit is disconnected from any automatons. 
};
static size_t groupsOf64(size_t bits) {
    if (bits % 64 == 0)
	      return bits / 64;
    else
	      return bits / 64 + 1;
}

static void identity(uint64_t *output, const uint64_t *state, size_t m, size_t s) {
    size_t x = m > s ? s : m;
    memcpy(output, state, sizeof(uint64_t) * groupsOf64(x));    
}

void ma_delete(moore_t *a) {
    if (a == NULL)
        return;
    free(a->inp);
	  free(a->outp);
	  free(a->curState);
	  free(a->stateBuf);
	  free(a->con);
    free(a);
}

uint64_t const *ma_get_output(moore_t const *a) {
    if (!a) {
        errno = EINVAL;
        return NULL;
    }
    return a->outp;
}

moore_t *ma_create_full(size_t n, size_t m, size_t s, transition_function_t t,
               output_function_t y, uint64_t const *q) {
    moore_t *a = NULL;
    a = (moore_t*) malloc(sizeof(moore_t));
    bool sizesErr = (m == 0) || (s == 0) || (!t) || (!y) || (!q);
    bool pointerErr = (!a);
    if (sizesErr) {
        free(a);
        errno = EINVAL;
        return NULL;
    } 
    if (pointerErr) {
        free(a);
        errno = ENOMEM;
        return NULL;
    }
    a->inp = (uint64_t*) malloc(sizeof(uint64_t) * groupsOf64(n));
    a->outp = (uint64_t*) malloc(sizeof(uint64_t) * groupsOf64(m));
    a->curState = (uint64_t*) malloc(sizeof(uint64_t) * groupsOf64(s));
    a->stateBuf = (uint64_t*) malloc(sizeof(uint64_t) * groupsOf64(s));
    a->con = (conn) malloc(sizeof(struct connected) * n);
    a->inpBits = n;
    a->outpBits = m;
    a->stateBits = s;
    a->t = t;
    a->y = y;
    if (!a->inp || !a->outp || !a->curState || !a->stateBuf || !a->con) {
        ma_delete(a);
        errno = ENOMEM;
        return NULL;
    }
    for (size_t i = 0; i < n; i++) 
        a->con[i].au = NULL;
    for (size_t i = 0; i < groupsOf64(n); i++)
	      a->inp[i] = (uint64_t) 0;
    // Inner state and output.
    memcpy(a->curState, q, sizeof(uint64_t) * groupsOf64(s));
    a->y(a->outp, a->curState, m, s);
    return a;
}

moore_t *ma_create_simple(size_t n, size_t s, transition_function_t t) {
    if (s == 0 || !t) {
        errno = EINVAL;
        return NULL;
    }
    uint64_t *state = malloc(sizeof(uint64_t) * groupsOf64(s));
    if (!state) {
        errno = ENOMEM;
        return NULL;
    }
    for (size_t i = 0; i < groupsOf64(s); i++)
	      state[i] = (uint64_t) 0; 
    moore_t *a = ma_create_full(n, s, s, t, (output_function_t) identity, state);
    free(state);
    return a;
}

int ma_set_state(moore_t *a, uint64_t const *state) {
    if (!a || !state) {
        errno = EINVAL;
        return -1;
    }
    memcpy(a->curState, state, sizeof(uint64_t) * groupsOf64(a->stateBits));
    a->y(a->outp, a->curState, a->outpBits, a->stateBits);
    return 0;
}

int ma_set_input(moore_t *a, uint64_t const *input) {
    if (!a || a->inpBits == 0 || !input) {
        errno = EINVAL;
        return -1;
    }
    for (size_t i = 0; i < a->inpBits; i++) {
        if (a->con[i].au == NULL) {
            size_t ind1 = i / 64;
            size_t ind2 = i % 64;
            bool value = (input[ind1] >> ind2) & 1;
            // Set bit to 0.
            if (value == 0) 
                a->inp[ind1] = a->inp[ind1] & ~((uint64_t)1 << ind2);
            // Set bit to 1.
            else 
                a->inp[ind1] = a->inp[ind1] | ((uint64_t)1 << ind2);
        }
    }
    return 0;
}

int ma_connect(moore_t *a_in, size_t in, moore_t *a_out, size_t out, size_t num) {
    if (!a_in || !a_out || a_in->inpBits < in + num ||
        a_out->outpBits < out + num || num == 0) { // Error handling.
        errno = EINVAL;
        return -1;
    }
    for (size_t i = 0; i < num; i++) { 
        a_in->con[in + i].au = a_out; // Connect a_out to bit with index in + 1.
        a_in->con[in + i].bit = out + i; // Determine, which bit of a_out is connected. 
    }
    return 0;
}

int ma_disconnect(moore_t *a_in, size_t in, size_t num) {
    if (!a_in || a_in->inpBits < in + num) {
        errno = EINVAL;
        return -1;
    }
    for (size_t i = 0; i < num; i++) {
        a_in->con[in + i].au = NULL;
    }
    return 0;
}

int ma_step(moore_t *at[], size_t num) {
    if (num == 0 || !at) { // Error handling pt.1
        errno = EINVAL;
        return -1;
    }
    for (size_t i = 0; i < num; i++) // Error handling pt.2
        if (at[i] == NULL) {
            errno = EINVAL;
            return -1;
        }
    for (size_t i = 0; i < num; i++) {
        for (size_t k = 0; k < at[i]->inpBits; k++) {
            if (at[i]->con[k].au != NULL ) { // Similiar to ma_set_input, but we edit 
	              moore_t *c = at[i]->con[k].au; // inputs when automatons are connected.
                size_t ind1 = k / 64;
                size_t ind2 = k % 64;
                size_t c_ind1 = at[i]->con[k].bit / 64;
                size_t c_ind2 = at[i]->con[k].bit % 64;
                bool value = (c->outp[c_ind1] >> c_ind2) & 1;
                if (value == 0)
                    at[i]->inp[ind1] = at[i]->inp[ind1] & ~((uint64_t) 1 << ind2);
                else
                    at[i]->inp[ind1] = at[i]->inp[ind1] | ((uint64_t) 1 << ind2);
            }
        }
    }
    // Calculate new inner states of given automatons.
    for (size_t i = 0; i < num; i++) {
        moore_t *a = at[i];
        at[i]->t(a->stateBuf, a->inp, a->curState, a->inpBits, a->stateBits);
    }
        
    // Calculate output signals for every automaton.
    for (size_t i = 0; i < num; i++) {
        moore_t *a = at[i]; 
	      memcpy(a->curState, a->stateBuf, sizeof(uint64_t) * groupsOf64(a->stateBits));
        a->y(a->outp, a->curState, a->outpBits, a->stateBits);
    }
    return 0;
}
