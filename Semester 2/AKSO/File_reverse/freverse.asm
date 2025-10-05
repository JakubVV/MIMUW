section .text
global _start

; Macros for x86_64 Linux system functions.
SYS_OPEN    equ 2
SYS_MUNMAP  equ 11
SYS_FSTAT   equ 5
SYS_CLOSE   equ 3
SYS_MMAP    equ 9
SYS_EXIT    equ 60
SYS_MSYNC   equ 26

; Open file flag.
O_RDWR      equ 2

; Macros for mmap.
PROT_READ_WRITE equ 3
MAP_SHARED  equ 1

; Error code.
EXIT_FAIL equ 1

; Struct stat size.
STR_STAT_SIZE equ 144

_start:
    ; Check if argc == 2.
    mov rax, [rsp]          ; Read argc from the stack.
    cmp rax, 2
    jne error

    ; Read the file name.
    mov rdi, [rsp + 16]     ; argv[1]

    ; Open file as read/write.
    mov rax, SYS_OPEN
    mov rsi, O_RDWR         ; Open as read/write.
    mov rdx, 0              ; Don't create a new file.
    syscall
    cmp rax, 0
    jl error                ; Check for errors.
    mov r12, rax            ; r12 - file descriptor.

    ; Read file size using fstat.
    sub rsp, STR_STAT_SIZE  ; Allocate memory for struct stat.
    mov rax, SYS_FSTAT
    mov rdi, r12            ; Descriptor.
    mov rsi, rsp            ; Pointer to struct stat.
    syscall
    cmp rax, 0
    jl error_close          ; Check for errors.

    ; Read file size from struct stat (st_size is on the offset 48).
    mov r13, [rsp + 48]     ; r13 - file size.
    add rsp, STR_STAT_SIZE  ; Restore stack.

    ; If file size < 2, end.
    cmp r13, 2
    jl close_file

    ; Map the file to memory using mmap.
    mov rax, SYS_MMAP
    xor rdi, rdi            ; System chooses address.
    mov rsi, r13            ; Length = file size.
    mov rdx, PROT_READ_WRITE 
    mov r10, MAP_SHARED     
    mov r8, r12             ; Descriptor.
    xor r9, r9              ; Offset = 0.
    syscall
    cmp rax, 0
    jl error_close          ; Check for errors.
    mov r14, rax            ; Mapped address in r14.

    ; Reverse file in memory.
    mov rsi, r14             ; Mapped file's beginning.
    lea rdi, [r14 + r13 - 1] ; Mapped file's end.
    shr r13, 1               ; Number of iterations in the main byte-swapping loop.
_loop:
    test r13, r13
    jz save_file
    mov al, [rsi]           ; Load byte from the beginning.
    mov bl, [rdi]           ; Load byte from the end.
    mov [rsi], bl           ; Save byte from end to beginning.
    mov [rdi], al           ; Save byte from beginning to end.
    ; Change pointers to new addresses and decrease the iterator.
    inc rsi                 
    dec rdi
    dec r13
    jmp _loop

save_file:
    ; Save changes.
    mov rax, SYS_MSYNC
    mov rdi, r14            ; Mapped address.
    mov rsi, r13
    shl rsi, 1              ; Restore size.
    mov rdx, 4              ; Use option MS_SYNC. 
    syscall
    cmp rax, 0
    jl unmap_error          ; Check for errors.

    ; Unmap the memory using SYS_MUNMAP.
    mov rax, SYS_MUNMAP
    mov rdi, r14            ; Mapped address.
    mov rsi, r13
    shl rsi, 1              ; Restore size.
    syscall
    cmp rax, 0
    jl error_close          ; Check for errors.

close_file:
    ; Zamknij plik.
    mov rax, SYS_CLOSE
    mov rdi, r12            ; File descriptor.
    syscall
    cmp rax, 0
    jl error                ; Check for errors.

    ; Exit(0)
    mov rax, SYS_EXIT
    xor rdi, rdi            ; End with success - code 0.
    syscall

unmap_error:
    ; Unmap memory before emergency return (with error).
    mov rax, SYS_MUNMAP
    mov rdi, r14
    mov rsi, r13
    shl rsi, 1              ; Restore size.
    syscall

error_close:
    ; Close file before emergency return (with error).
    mov rax, SYS_CLOSE
    mov rdi, r12
    syscall

error:
    ; Emergency return (code 1).
    mov rax, SYS_EXIT
    mov rdi, EXIT_FAIL
    syscall
