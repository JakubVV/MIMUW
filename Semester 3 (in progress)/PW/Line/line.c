#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>

#include "err.h"

#define BUFFER_SIZE 32

int main(int argc, char* argv[])
{
    int n_children = 5;
    
    if (argc == 2 || argc > 2)
        n_children = atoi(argv[1]);

    if (argc > 2)
        printf("My pid is %d, my parent's pid is %d\n", getpid(), getppid());

    if (n_children > 0) {
        pid_t pid;
        ASSERT_SYS_OK(pid = fork());
        if (pid == 0) {
            int new_n_children = n_children - 1;
            char buffer[BUFFER_SIZE];
            int ret = snprintf(buffer, sizeof buffer, "%d", new_n_children);
            if (ret < 0 || ret >= (int)sizeof(buffer))
                fatal("snprintf failed");
            execl(argv[0], argv[0], buffer, "child", NULL);
        }
        else {
            int stat_val;
            ASSERT_SYS_OK(pid = wait(&stat_val));
            if (WIFEXITED(stat_val))
                printf("Parent: child %d exited with code %d\n", pid, WEXITSTATUS(stat_val));
            else
                printf("Parent: child %d died due to uncaught signal %d.\n", pid, WTERMSIG(stat_val));
        }
    }
    
    printf("My pid is %d, exiting.\n", getpid());

    return 0;
}
