; Implementation of ninv using Knuth's Algorithm D.
; Objective: calculate floor(2^n / x)
extern malloc
extern free
extern calloc

section .text
global ninv

clz32:
    bsr eax, edi             ; EAX = position of highest set bit (0-31)
    xor eax, 31              ; clz = 31 XOR highest_bit_position
    ret

conv64to32:
    test rdx, rdx            ; check if k != 0
    jz .conv64_done          ; if k == 0, end
.conv64_loop:
    mov rax, [rsi]           ; rax = s[i] (64-bit word)
    mov [rdi], eax           ; d[2*i] = lower 32 bity
    shr rax, 32              ; rax = upper 32 bity
    mov [rdi+4], eax         ; d[2*i+1] = upper 32 bity
    add rsi, 8               ; s++
    add rdi, 8               ; d += 2 (32-bit words)
    dec rdx                  ; k--
    jnz .conv64_loop         ; continue if k != 0
.conv64_done:
    ret

conv32to64:
    test rdx, rdx           ; check if k != 0
    jz .conv32_done         ; if k == 0, end
.conv32_loop:
    mov eax, [rsi]          ; eax = s[2*i] (lower 32 bits)
    mov ecx, [rsi+4]        ; ecx = s[2*i+1] (upper 32 bits)
    shl rcx, 32             ; rcx = upper 32 bits << 32
    or rax, rcx             ; rax = (s[2*i+1] << 32) | s[2*i]
    mov [rdi], rax          ; d[i] = complex 64-bit word
    add rsi, 8              ; s += 2 (32-bit words)
    add rdi, 8              ; d++
    dec rdx                 ; k--
    jnz .conv32_loop        ; continue if k != 0
.conv32_done:
    ret

add_back:
    test rcx, rcx            ; check if t != 0
    jz .done                 ; if t == 0, end
    lea r10, [rdi+rsi*4]     ; r10 = &u[j]
    mov r11, rdx             ; r11 = v  (save pointer v)
    xor r8d, r8d             ; carry = 0 (r8d = 0)
.loop:
    mov eax, [r10]           ; eax = u[j+i]  (zero-extends to rax)
    mov edx, [r11]           ; edx = v[i]     (zero-extends to rdx)
    add rax, rdx             ; rax = u + v
    add rax, r8              ; rax = u + v + carry
    mov [r10], eax           ; save lower 32 bits back to u[j + i]
    shr rax, 32              ; rax = sum >> 32  (0 or 1)
    mov r8d, eax             ; carry = (uint32_t)(sum >> 32)
    add r10, 4               ; u_ptr += 4
    add r11, 4               ; v_ptr += 4
    dec rcx                  ; t--
    jnz .loop                ; continue if t != 0
    add dword [r10], r8d     ; now r10 points to u[j+t], add carry (0/1)
.done:
    ret

sub_mul:
    push rbx
    push r12
    mov r11, rdx            ; v
    mov r12, rcx            ; t
    ; RDI = uint32_t *u
    ; RSI = size_t j
    ; R8D = uint32_t q
    xor r10, r10            ; carry = 0 (64-bit)
    xor rbx, rbx            ; borrow = 0 (32-bit)
    xor r9, r9              ; i = 0
.loop:
    cmp r9, r12
    jge .final_borrow
    ; prod = (uint64_t)v[i] * q + carry
    xor rax, rax
    mov eax, [r11 + r9*4]  ; eax = v[i]
    mul r8                 ; rdx:rax = v[i] * q
    add rax, r10           ; rax = prod + carry
    ; carry = prod >> 32;
    mov r10, rax           ; prod in r10
    shr r10, 32            ; new carry
    ; p = (uint32_t)prod (lower 32 bits)
    ; need = (uint64_t)p + borrow
    xor rdx, rdx
    mov edx, eax           ; edx = p (lower 32 bits of prod)
    add rdx, rbx           ; need = p + borrow (64-bit)
    ; ui = u[j + i]
    mov rcx, rsi           ; rcx = j
    add rcx, r9            ; rcx = j + i
    xor rax, rax
    mov eax, [rdi + rcx*4] ; rax = ui = u[j+i] (extend to 64-bit)
    ; borrow = (uint32_t)(ui < need) - compare 64-bit values
    cmp rax, rdx
    setb bl                ; bl = 1 if ui < need, 0 otherwise
    movzx rbx, bl          
    ; u[j + i] = ui - (uint32_t)need
    mov eax, [rdi + rcx*4] ; load ui as 32-bit.
    sub eax, edx           
    mov [rdi + rcx*4], eax
    inc r9
    jmp .loop
.final_borrow:
    ; p = (uint32_t)carry
    ; need = (uint64_t)p + borrow
    xor rax, rax
    mov eax, r10d          ; eax = p 
    add rax, rbx           ; need = p + borrow (64-bit)
    ; ui = u[j + t]
    mov rcx, rsi           ; rcx = j
    add rcx, r12           ; rcx = j + t
    xor rdx, rdx
    mov edx, [rdi + rcx*4] ; rdx = ui = u[j+t] 
    ; borrow = (uint32_t)(ui < need)
    cmp rdx, rax
    setb bl                
    ; u[j + t] = ui - (uint32_t)need
    mov edx, [rdi + rcx*4] 
    sub edx, eax           
    mov [rdi + rcx*4], edx
    ; return borrow
    movzx eax, bl
    pop r12
    pop rbx
    ret

trial:
    push rbx
    push r12
    push r13
    push r14
    push r15
    mov r15, rdi              ; u
    mov r14, rsi              ; j
    mov r13, rdx              ; v
    mov r12, rcx              ; t
    mov eax, r12d
    dec eax
    mov r11d, [r13 + rax*4]   ; vt1 = v[t-1]
    lea rax, [r14 + r12]
    mov r10d, [r15 + rax*4]   ; uj2 = u[j+t]
    ; if (uj2 == vt1) return UINT32_MAX
    cmp r10d, r11d
    je .return_max
    ; num = ((uint64_t)uj2 << 32) | u[j+t-1]
    lea rax, [r14 + r12 - 1]
    mov eax, [r15 + rax*4]    ; u[j+t-1]
    mov rdx, r10
    shl rdx, 32
    or rdx, rax               ; num
    ; qhat = num / vt1, rhat = num % vt1
    mov rax, rdx
    xor rdx, rdx
    mov rcx, r11
    div rcx
    mov r9d, eax              ; qhat
    mov r8d, edx              ; rhat
    ; if (!qhat || t==1) return qhat
    test r9d, r9d
    jz .return_qhat
    cmp r12, 1
    je .return_qhat
    ; vt2 = v[t-2]
    lea rax, [r12 - 2]
    mov ebx, [r13 + rax*4]    ; vt2
.correction_loop:
    ; left = qhat * vt2 (64-bit)
    mov rax, r9
    mul rbx                   ; rdx:rax = qhat * vt2
    ; right = ((uint64_t)rhat << 32) | u[j+t-2]
    lea rcx, [r14 + r12 - 2]
    mov ecx, [r15 + rcx*4]
    mov rsi, r8
    shl rsi, 32
    or rsi, rcx               ; right
    ; compare left and right
    cmp rdx, 0
    jne .do_correction        
    cmp rax, rsi
    jbe .end_correction       
.do_correction:
    dec r9d                   ; qhat--
    add r8d, r11d             ; rhat += vt1
    cmp r8d, r11d
    jb .end_correction        ; overflow -> stop
    jmp .correction_loop
.end_correction:
.return_qhat:
    mov eax, r9d
    jmp .trial_done
.return_max:
    mov eax, 0xFFFFFFFF
.trial_done:
    pop r15
    pop r14
    pop r13
    pop r12
    pop rbx
    ret

normalize_divisor:
    push rbx
    push r12
    push r13
    push r14
    push r15
    sub rsp, 48            
    mov [rsp], rdi         ; x32
    mov [rsp+8], rsi       ; m32
    mov [rsp+16], rdx      ; x32_ptr
    mov [rsp+24], rcx      ; y32_ptr
    mov [rsp+32], r8       ; t_out
    mov [rsp+40], r9       ; s_out
    ; size_t t = m32
    mov rbx, rsi           ; rbx = t = m32
    
.find_effective_length:
    cmp rbx, 1
    jle .length_found      ; if t <= 1, loop ends
    mov rax, rbx
    dec rax                ; rax = t-1
    mov rcx, [rsp]         ; rcx = x32
    mov eax, [rcx + rax*4] ; eax = x32[t-1]
    test eax, eax
    jnz .length_found      
    dec rbx                ; --t
    jmp .find_effective_length
    
.length_found:
    mov rax, rbx
    dec rax                ; rax = t-1
    mov rcx, [rsp]         ; rcx = x32
    mov edi, [rcx + rax*4] ; edi = x32[t-1]
    call clz32             ; eax = s
    mov r12d, eax          ; r12d = s
    ; uint32_t *v = (uint32_t*)malloc(t * sizeof(uint32_t))
    mov rdi, rbx           ; rdi = t
    shl rdi, 2             ; rdi = t * 4 (sizeof(uint32_t))
    call malloc wrt ..plt  ; rax = pointer on v
    test rax, rax
    jz .malloc_failed      
    mov r13, rax           ; r13 = v
    test r12d, r12d
    jnz .shift_left        
    xor r14, r14           ; i = 0
.copy_loop:
    cmp r14, rbx           ; compare i with t
    jge .copy_done         
    mov rcx, [rsp]         ; rcx = x32
    mov eax, [rcx + r14*4] ; eax = x32[i]
    mov [r13 + r14*4], eax ; v[i] = x32[i]
    inc r14                ; i++
    jmp .copy_loop
.copy_done:
    jmp .save_results
.shift_left:
    mov eax, 32
    sub eax, r12d           ; eax = rs = 32 - s
    mov r15d, eax           ; r15d = rs
    ; uint32_t prev = 0
    xor r11d, r11d          ; prev = 0
    xor r14, r14            ; i = 0
.shift_loop:
    cmp r14, rbx            
    jge .shift_done         
    ; uint32_t cur = x32[i]
    mov rcx, [rsp]          ; rcx = x32
    mov eax, [rcx + r14*4]  ; eax = cur = x32[i]
    ; v[i] = (cur << s) | (prev >> rs)
    mov edx, eax            ; edx = cur
    mov ecx, r12d           ; ecx = s
    shl edx, cl             ; edx = cur << s
    mov eax, r11d           ; eax = prev
    mov ecx, r15d           ; ecx = rs
    shr eax, cl             ; eax = prev >> rs
    or edx, eax             ; edx = (cur << s) | (prev >> rs)
    mov [r13 + r14*4], edx  ; v[i] = wynik
    ; prev = cur
    mov rcx, [rsp]          ; rcx = x32
    mov r11d, [rcx + r14*4] ; prev = cur
    inc r14                 ; i++
    jmp .shift_loop
.shift_done:
.save_results:
    mov rax, [rsp+32]       ; rax = t_out
    mov [rax], rbx          ; *t_out = t
    mov rax, [rsp+40]       ; rax = s_out
    mov [rax], r12d         ; *s_out = s
    mov rax, r13            
    jmp .normalize_done
.malloc_failed:
    mov rax, [rsp+16]       ; rax = x32_ptr
    mov rdi, [rax]          ; rdi = *x32_ptr
    call free wrt ..plt
    mov rax, [rsp+24]       ; rax = y32_ptr
    mov rdi, [rax]          ; rdi = *y32_ptr  
    call free wrt ..plt
    xor rax, rax            
.normalize_done:
    add rsp, 48             
    pop r15
    pop r14
    pop r13
    pop r12
    pop rbx
    ret

setup_dividend:
    push rbx
    push r12
    push r13
    push r14
    push r15
    mov rax, [rsp + 48] ; v_ptr (48 = 5 regs * 8 + 8 return address)
    mov rbx, [rsp + 56] ; qlen_out (56 = 5 regs * 8 + 8 return address + 8)
    sub rsp, 48
    mov [rsp], rdi          ; m32
    mov [rsp+8], rsi        ; n
    mov [rsp+16], rdx       ; s
    mov [rsp+24], rcx       ; t
    mov [rsp+32], r8        ; x32_ptr
    mov [rsp+40], r9        ; y32_ptr
    mov r12, rax            ; v_ptr
    mov r13, rbx            ; qlen_out
    ; size_t ulen = (size_t)m32+2
    mov rax, rdi            ; rax = m32
    add rax, 2              ; rax = ulen = m32 + 2
    mov r14, rax            ; r14 = ulen
    mov rdi, r14            ; rdi = ulen (count)
    mov rsi, 4              ; rsi = sizeof(uint32_t)
    call calloc wrt ..plt
    test rax, rax
    jz .calloc_failed       
    mov r15, rax            
    ; size_t w = n/32
    mov rax, [rsp+8]        ; rax = n
    shr rax, 5              ; rax = n / 32 = w
    mov rbx, rax            ; rbx = w
    ; unsigned b = n%32
    mov rax, [rsp+8]        ; rax = n
    and rax, 31             ; rax = n % 32 = b
    mov rcx, rax            ; rcx = b
    add rcx, [rsp+16]       ; rcx = b + s
    shr rcx, 5              ; rcx = (b + s) / 32
    add rbx, rcx            ; rbx = nw = w + (b+s)/32
    ; unsigned nb = (b + s) & 31U
    mov rax, [rsp+8]        ; rax = n
    and rax, 31             ; rax = n % 32 = b
    add rax, [rsp+16]       ; rax = b + s
    and rax, 31             ; rax = nb = (b+s) & 31
    mov rcx, rax            ; rcx = nb
    cmp rbx, r14           
    jge .skip_bit_set      
    mov rax, 1             ; rax = 1
    shl rax, cl            ; rax = 1 << nb
    mov [r15 + rbx*4], eax ; u[nw] = 1 << nb
.skip_bit_set:
    mov rax, r14            ; rax = ulen
    sub rax, [rsp+24]       ; rax = qlen = ulen - t
    mov [r13], rax          ; *qlen_out = qlen
    mov rax, r15            ; return u
    jmp .setup_done
.calloc_failed:
    ; free(*x32_ptr)
    mov rax, [rsp+32]       ; rax = x32_ptr
    mov rdi, [rax]          ; rdi = *x32_ptr
    call free wrt ..plt
    ; free(*y32_ptr)
    mov rax, [rsp+40]       ; rax = y32_ptr
    mov rdi, [rax]          ; rdi = *y32_ptr
    call free wrt ..plt
    ; free(*v_ptr)
    mov rdi, [r12]          ; rdi = *v_ptr
    call free wrt ..plt
    xor rax, rax            
.setup_done:
    add rsp, 48             
    pop r15
    pop r14
    pop r13
    pop r12
    pop rbx
    ret
perform_division:
    push rbx
    push r12
    push r13
    push r14
    push r15
    mov r10, [rsp + 48]      ; y (7th parameter)
    mov r11, [rsp + 56]      ; x32_ptr (8th parameter)
    mov rax, [rsp + 64]      ; y32_ptr (9th parameter)
    mov rbx, [rsp + 72]      ; v_ptr (10th parameter) 
    mov r12, [rsp + 80]      ; u_ptr (11th parameter)
    sub rsp, 88
    mov [rsp], rdi          ; u
    mov [rsp+8], rsi        ; v
    mov [rsp+16], rdx       ; t
    mov [rsp+24], rcx       ; qlen
    mov [rsp+32], r8        ; y32
    mov [rsp+40], r9        ; m32
    mov [rsp+48], r10       ; y
    mov [rsp+56], r11       ; x32_ptr
    mov [rsp+64], rax       ; y32_ptr
    mov [rsp+72], rbx       ; v_ptr
    mov [rsp+80], r12       ; u_ptr
    xor r15, r15            ; it = 0
.division_loop:
    cmp r15, [rsp+24]       ; comparer it and qlen
    jge .division_done      ; if it >= qlen, end
    mov rax, [rsp+24]       ; rax = qlen
    dec rax                 ; rax = qlen - 1
    sub rax, r15            ; rax = j = qlen - 1 - it
    mov r13, rax            ; r13 = j
    mov rdi, [rsp]          ; rdi = u
    mov rsi, r13            ; rsi = j
    mov rdx, [rsp+8]        ; rdx = v
    mov rcx, [rsp+16]       ; rcx = t
    call trial
    mov r14d, eax           ; r14d = qh
    test r14d, r14d
    jz .skip_submul         ; if qh == 0, skip sub_mul
    mov rdi, [rsp]          ; rdi = u
    mov rsi, r13            ; rsi = j
    mov rdx, [rsp+8]        ; rdx = v
    mov rcx, [rsp+16]       ; rcx = t
    mov r8d, r14d           ; r8d = qh
    call sub_mul
    test eax, eax
    jz .skip_correction ; if sub_mul returns 0, there is no correction
    dec r14d
    mov rdi, [rsp]          ; rdi = u
    mov rsi, r13            ; rsi = j
    mov rdx, [rsp+8]        ; rdx = v
    mov rcx, [rsp+16]       ; rcx = t
    call add_back
.skip_correction:
.skip_submul:
    cmp r13, [rsp+40]       ; compare j and m32
    jge .skip_store         ; if j >= m32, don't save
    mov rax, [rsp+32]       ; rax = y32
    mov [rax + r13*4], r14d ; y32[j] = qh
.skip_store:
    inc r15                 ; it++
    jmp .division_loop
.division_done:
    mov rdi, [rsp+48]   ; rdi = y
    mov rsi, [rsp+32]   ; rsi = y32
    mov rdx, [rsp+40]   ; rdx = m32
    shr rdx, 1          ; rdx = m32/2
    call conv32to64
    mov rax, [rsp+80]   ; rax = u_ptr
    mov rdi, [rax]      ; rdi = *u_ptr
    call free wrt ..plt
    mov rax, [rsp+72]   ; rax = v_ptr
    mov rdi, [rax]      ; rdi = *v_ptr
    call free wrt ..plt
    mov rax, [rsp+64]   ; rax = y32_ptr
    mov rdi, [rax]      ; rdi = *y32_ptr
    call free wrt ..plt
    mov rax, [rsp+56]   ; rax = x32_ptr
    mov rdi, [rax]      ; rdi = *x32_ptr
    call free wrt ..plt
    add rsp, 88         
    pop r15
    pop r14
    pop r13
    pop r12
    pop rbx
    ret
init_memory:
    push rbx
    push r12
    push r13
    push r14
    push r15
    mov r12, rdi        ; x32 
    mov r13, rsi        ; y32 
    mov r14, rdx        ; x 
    mov r15d, ecx       ; n (unsigned)
    mov rax, rcx        ; rax = n
    shr rax, 6          ; rax = n / 64 = m64
    mov rbx, rax        ; rbx = m64
    test rbx, rbx
    jz .return_zero     ; if m64 == 0, return 0
    shl rbx, 1          ; rbx = m32 = m64 * 2
    mov rdi, rbx        ; rdi = m32
    shl rdi, 2          ; rdi = m32 * 4 (sizeof(uint32_t))
    call malloc wrt ..plt
    mov [r12], rax      
    ; *y32 = (uint32_t*)calloc(m32, sizeof(uint32_t))
    mov rdi, rbx        ; rdi = m32 (count)
    mov rsi, 4          ; rsi = sizeof(uint32_t)
    call calloc wrt ..plt
    mov [r13], rax      
    ; if(!*x32 || !*y32)
    mov r10, [r12]      ; r10 = *x32
    mov r11, [r13]      ; r11 = *y32
    test r10, r10       ; check if *x32 != NULL
    jz .cleanup_memory  ; if *x32 == NULL, go to cleanup
    test r11, r11       ; check if *y32 != NULL
    jz .cleanup_memory  ; if *y32 == NULL, go to cleanup
    mov rdi, r10        ; rdi = *x32
    mov rsi, r14        ; rsi = x
    mov rax, rbx        ; rax = m32
    shr rax, 1          ; rax = m64 = m32/2
    mov rdx, rax        ; rdx = m64
    call conv64to32
    mov eax, ebx        ; return m32
    jmp .init_done
.cleanup_memory:
    ; free(*x32); free(*y32);
    mov rdi, [r12]      ; rdi = *x32
    call free wrt ..plt
    mov rdi, [r13]      ; rdi = *y32
    call free wrt ..plt
    ; *x32 = *y32 = NULL;
    mov qword [r12], 0  ; *x32 = NULL
    mov qword [r13], 0  ; *y32 = NULL
    ; return -1; 
    mov eax, -1
    jmp .init_done
.return_zero:
    mov eax, 0          
.init_done:
    pop r15
    pop r14
    pop r13
    pop r12
    pop rbx
    ret
perform_knuth:
    push rbp
    mov rbp, rsp
    sub rsp, 80             
    mov [rbp-8], rdi        ; x32
    mov [rbp-16], rsi       ; y32  
    mov [rbp-24], rdx       ; m32
    mov [rbp-32], ecx       ; n
    mov [rbp-40], r8        ; y (result)
    mov rdi, [rbp-8]        ; x32
    mov rsi, [rbp-24]       ; m32
    lea rdx, [rbp-8]        ; &x32
    lea rcx, [rbp-16]       ; &y32
    lea r8, [rbp-48]        ; &t
    lea r9, [rbp-56]        ; &s
    call normalize_divisor
    ; if(!v) return;
    test rax, rax
    jz .knuth_done          ; if v == NULL, end 
    mov [rbp-64], rax       ; v = result
    mov edi, [rbp-24]       ; m32
    mov esi, [rbp-32]       ; n
    mov edx, [rbp-56]       ; s
    mov rcx, [rbp-48]       ; t
    lea r8, [rbp-8]         ; &x32
    lea r9, [rbp-16]        ; &y32
    sub rsp, 16             
    lea rax, [rbp-64]       ; &v
    mov [rsp], rax          ; 7th parameter: &v
    lea rax, [rbp-72]       ; &qlen
    mov [rsp+8], rax        ; 8th parameter: &qlen
    call setup_dividend
    add rsp, 16             
    ; if(!u) return;
    test rax, rax
    jz .knuth_done          
    mov [rbp-80], rax       
    mov rdi, [rbp-80]       ; u
    mov rsi, [rbp-64]       ; v
    mov rdx, [rbp-48]       ; t
    mov rcx, [rbp-72]       ; qlen
    mov r8, [rbp-16]        ; y32
    mov r9, [rbp-24]        ; m32
    sub rsp, 48            
    mov rax, [rbp-40]       
    mov [rsp], rax          ; 7th parameter: y
    lea rax, [rbp-8]        ; &x32
    mov [rsp+8], rax        ; 8th parameter: &x32
    lea rax, [rbp-16]       ; &y32
    mov [rsp+16], rax       ; 9th parameter: &y32
    lea rax, [rbp-64]       ; &v
    mov [rsp+24], rax       ; 10th parameter: &v
    lea rax, [rbp-80]       ; &u
    mov [rsp+32], rax       ; 11th parameter: &u
    call perform_division
    add rsp, 48             
.knuth_done:
    mov rsp, rbp
    pop rbp
    ret
ninv:
    push rbp
    mov rbp, rsp
    sub rsp, 32             
    mov [rbp-8], rdi        ; y 
    mov [rbp-16], rsi       ; x 
    mov [rbp-24], edx       ; n 
    lea rdi, [rbp-40]       ; &x32 
    lea rsi, [rbp-48]       ; &y32  
    mov rdx, [rbp-16]       ; x
    mov ecx, [rbp-24]       ; n
    sub rsp, 16             
    call init_memory
    ; if(m32 <= 0) return;
    test rax, rax
    jle .core_done
    mov [rbp-32], rax       
    mov rdi, [rbp-40]       ; x32
    mov rsi, [rbp-48]       ; y32  
    mov rdx, [rbp-32]       ; m32
    mov ecx, [rbp-24]       ; n
    mov r8, [rbp-8]         ; y 
    call perform_knuth
.core_done:
    add rsp, 16             
    mov rsp, rbp
    pop rbp
    ret
