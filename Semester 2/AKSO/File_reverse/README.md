# File Reversal Program — `freverse`

## Description

Implement a Linux **assembly** program `freverse` that **reverses the contents of a file** in place.

---

## Program Invocation

```bash
./freverse file
````

Where:

* `file` — name of the file whose contents are to be reversed.

---

## Functional Requirements

* If the file is shorter than **two bytes**, reversing it does **not** change its contents.
* The program **must not have any built-in limitation** on file size — it must correctly handle files **larger than 4 GiB**.
* The program must be **efficient**, avoiding unnecessary memory or I/O overhead.

---

## System Calls

The program **must use only** the following Linux system calls:

```
sys_read
sys_write
sys_open
sys_close
sys_stat
sys_fstat
sys_lseek
sys_mmap
sys_munmap
sys_msync
sys_exit
```

You do **not** need to use all of them, but you **may not** use any other system calls or C library functions.

---

## Error Handling

The program must:

* Validate the correctness of the program call:

  * There must be **exactly one argument** (the filename).
  * The argument must refer to a **valid file**.
* Check the **return values** of all system calls (except `sys_exit`).
* If any of the following occur:

  * No argument provided,
  * More than one argument provided,
  * Invalid argument,
  * Any system call returns an error,

  → The program must **exit with status code `1`**.

---

## Cleanup

* Before exiting (even on error), the program must **explicitly call `sys_close`** on any file it opened.
* The program must ensure proper cleanup of any allocated or mapped resources.

---

## Output

* The program **must not print anything** to the terminal — no messages, no debugging output.

---
