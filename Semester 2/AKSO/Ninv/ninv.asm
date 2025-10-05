; Implementacja ninv z wykorzystaniem Algorytmu D Donalda Knutha
; Cel: Obliczenie podłogi z 2^n / x.
; STRUKTURA ALGORYTMU (wg Knuth Alg. D):
; D1: Normalizacja – przesunięcie dzielnika i dzielnej w lewo o s bitów,
;     tak aby najwyższy bit dzielnika był ustawiony (leading 1).
; D2: Inicjalizacja dzielnej – przygotowanie rozszerzonej tablicy u
;     (o długości m+2 słów), zawierającej przesuniętą dzielną.
; D3: Szacowanie ilorazu (q) – wyznaczenie przybliżonego ilorazu z
;     dwóch najwyższych cyfr u i najwyższej cyfry v.
; D4: Mnożenie i odejmowanie – obliczenie u − q·v.
; D5: Korekta – sprawdzenie czy q było zbyt duże; jeśli tak, zmniejszenie
;     q i dodanie z powrotem v do u.
; D6: Iteracja – powtórzenie kroków D3–D5 dla kolejnych pozycji.
; D7: Denormalizacja – przesunięcie w prawo o s bitów, przywrócenie
;     poprawnej wartości reszty.
extern malloc
extern free
extern calloc

section .text
global ninv

; Funkcja clz32 - zlicza wiodące zera w 32-bitowym słowie
; ALGORYTM D - krok D1 (normalizacja)
;
; BSR znajduje indeks najwyższego ustawionego bitu (0..31),
; CLZ = 31 - indeks. Liczba wiodących zer
clz32:
    bsr eax, edi             ; EAX = pozycja najwyższego ustawionego bitu (0-31)
    xor eax, 31              ; clz = 31 XOR pozycja_najwyższego_bitu
    ret
; FUNKCJE KONWERSJI (64-bit -> 32-bit oraz 32-bit -> 64-bit).
; Algorytm D działa w bazie 2^32, dlatego wejściowe dane w bazie 2^64
; muszą zostać rozbite na słowa 32-bitowe (little endian).

; conv64to32(d, s, k): rozdziela k słów 64b na 2k słów 32b
; W ALGORYTMie D – przygotowanie danych wejściowych
;
; Każde słowo 64-bit -> dwa słowa 32-bit:
conv64to32:
    test rdx, rdx            ; sprawdź czy k != 0
    jz .conv64_done          ; jeśli k == 0, koniec
.conv64_loop:
    mov rax, [rsi]           ; rax = s[i] (64-bit słowo)
    mov [rdi], eax           ; d[2*i] = dolne 32 bity
    shr rax, 32              ; rax = górne 32 bity
    mov [rdi+4], eax         ; d[2*i+1] = górne 32 bity
    add rsi, 8               ; s++
    add rdi, 8               ; d += 2 (32-bit słowa)
    dec rdx                  ; k--
    jnz .conv64_loop         ; kontynuuj jeśli k != 0
.conv64_done:
    ret
; conv32to64(d, s, k): scala 2k słów 32b w k słów 64b
; W ALGORYTMIE D – przygotowanie wyniku
; Każde dwa słowa 32-bit -> jedno słowo 64-bit:
conv32to64:
    test rdx, rdx           ; sprawdź czy k != 0
    jz .conv32_done         ; jeśli k == 0, koniec
.conv32_loop:
    mov eax, [rsi]          ; eax = s[2*i] (dolne 32 bity)
    mov ecx, [rsi+4]        ; ecx = s[2*i+1] (górne 32 bity)
    shl rcx, 32             ; rcx = górne 32 bity << 32
    or rax, rcx             ; rax = (s[2*i+1] << 32) | s[2*i]
    mov [rdi], rax          ; d[i] = złożone słowo 64-bit
    add rsi, 8              ; s += 2 (32-bit słowa)
    add rdi, 8              ; d++
    dec rdx                 ; k--
    jnz .conv32_loop        ; kontynuuj jeśli k != 0
.conv32_done:
    ret
; add_back(u, j, v, t): dodaje v do u[j..] przy korekcie qhat
; ALGORYTM D – KROK D6
; Używane gdy poprzednia próba qhat była za duża.
; Sumuje każde słowo dzielnika v z odpowiadającym słowem w u, uwzględniając przeniesienie.
add_back:
    test rcx, rcx            ; sprawdź czy t != 0
    jz .done                 ; jeśli t == 0, koniec
    lea r10, [rdi+rsi*4]     ; r10 = &u[j]
    mov r11, rdx             ; r11 = v  (zachowujemy wskaźnik v)
    xor r8d, r8d             ; carry = 0 (r8d = 0)
.loop:
    mov eax, [r10]           ; eax = u[j+i]  (zero-extends do rax)
    mov edx, [r11]           ; edx = v[i]     (zero-extends do rdx)
    add rax, rdx             ; rax = u + v
    add rax, r8              ; rax = u + v + carry
    mov [r10], eax           ; zapisz dolne 32 bity z powrotem do u[j + i]
    shr rax, 32              ; rax = sum >> 32  (0 lub 1)
    mov r8d, eax             ; carry = (uint32_t)(sum >> 32)
    add r10, 4               ; u_ptr += 4
    add r11, 4               ; v_ptr += 4
    dec rcx                  ; t--
    jnz .loop                ; kontynuuj jeśli t != 0
    add dword [r10], r8d     ; teraz r10 wskazuje na u[j+t], dodaj carry (0/1)
.done:
    ret
; sub_mul(u,j,v,t,q): odejmuje v*q od u[j..j+t]
; ALGORYTM D – KROK D4
; Mnożenie dzielnika przez qhat i odejmowanie od fragmentu dzielnej - istota algorytmu.
; PĘTLA:
;   Każde słowo v[i] mnożone przez qhat, sumowane z poprzednim carry.
;   Wynik odejmowany od odpowiadającego słowa u[j+i] z uwzględnieniem borrow.
; FINALIZACJA:
;   Obsługa ostatniego carry w u[j+t].
; ZWRACA:
;   Końcowy borrow (0 lub 1), wskazujący czy potrzebna korekta (dla add_back).
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
    mov r10, rax           ; prod w r10
    shr r10, 32            ; nowe carry
    ; p = (uint32_t)prod (niższe 32 bity)
    ; need = (uint64_t)p + borrow
    xor rdx, rdx
    mov edx, eax           ; edx = p (niższe 32 bity prod)
    add rdx, rbx           ; need = p + borrow (64-bit)
    ; ui = u[j + i]
    mov rcx, rsi           ; rcx = j
    add rcx, r9            ; rcx = j + i
    xor rax, rax
    mov eax, [rdi + rcx*4] ; rax = ui = u[j+i] (rozszerz do 64-bit)
    ; borrow = (uint32_t)(ui < need) - porównaj 64-bitowe wartości
    cmp rax, rdx
    setb bl                ; bl = 1 jeżeli ui < need, 0 w p.p.
    movzx rbx, bl          ; rozszerz do pełnego rejestru.
    ; u[j + i] = ui - (uint32_t)need
    mov eax, [rdi + rcx*4] ; załaduj ui jako 32-bit.
    sub eax, edx           ; odejmij tylko niskie 32 bity need.
    mov [rdi + rcx*4], eax
    inc r9
    jmp .loop
.final_borrow:
    ; p = (uint32_t)carry
    ; need = (uint64_t)p + borrow
    xor rax, rax
    mov eax, r10d          ; eax = p (niższe 32 bity carry)
    add rax, rbx           ; need = p + borrow (64-bit)
    ; ui = u[j + t]
    mov rcx, rsi           ; rcx = j
    add rcx, r12           ; rcx = j + t
    xor rdx, rdx
    mov edx, [rdi + rcx*4] ; rdx = ui = u[j+t] (rozszerz do 64-bit)
    ; borrow = (uint32_t)(ui < need) - porównaj 64-bitowe wartości
    cmp rdx, rax
    setb bl                ; bl = 1 jeżeli ui < need, 0 w p.p.
    ; u[j + t] = ui - (uint32_t)need
    mov edx, [rdi + rcx*4] ; załaduj ui jako 32-bit.
    sub edx, eax           ; odejmij tylko niskie 32 bity need.
    mov [rdi + rcx*4], edx
    ; return borrow
    movzx eax, bl
    pop r12
    pop rbx
    ret
; trial(u,j,v,t): oblicza przybliżone qhat z korektą
; ALGORYTM D – KROK D3 (SZACOWANIE QHAT) + D4 (KOREKTA)
;
; Szacowanie:
;   qhat = floor((u[j+t]*b + u[j+t-1]) / v[t-1])
;   rhat = reszta
; Korekta:
;   Jeśli qhat*vt2 > (rhat<<32 | u[j+t-2]), zmniejsz qhat
;   Działa w bazie b = 2^32, po normalizacji v[t-1] ≥ b/2
;   Maksymalna korekta qhat = -1 lub -2, zwykle tylko -1
; Zwraca:
;   qhat w eax (32-bit)
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
    ; porówanaj left i right
    cmp rdx, 0
    jne .do_correction        ; jeśli high != 0 -> lewa strona większa
    cmp rax, rsi
    jbe .end_correction       ; jeśli <= prawa strona -> koniec
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
; normalize_divisor: przygotowuje dzielnik do algorytmu D (normalizacja)
;
; Znajdź efektywną długość t dzielnika, usuwając wiodące zera.
; Oblicz s = liczba wiodących zer w najwyższym słowie.
; Jeśli s > 0, przesuń dzielnik w lewo o s bitów (wielosłowne przesunięcie).
; Zwraca wskaźnik do znormalizowanego dzielnika i zapisuje t oraz s.
; Zabezpieczenie: malloc_failed → zwolnij pamięć wejściową i zwróć NULL.
normalize_divisor:
    push rbx
    push r12
    push r13
    push r14
    push r15
    ; Parametry z rejestrów do lokalnych zmiennych na stosie
    sub rsp, 48            ; miejsce na lokalne zmienne
    mov [rsp], rdi         ; x32
    mov [rsp+8], rsi       ; m32
    mov [rsp+16], rdx      ; x32_ptr
    mov [rsp+24], rcx      ; y32_ptr
    mov [rsp+32], r8       ; t_out
    mov [rsp+40], r9       ; s_out
    ; size_t t = m32
    mov rbx, rsi           ; rbx = t = m32
    ; while (t>1 && x32[t-1]==0) --t
    ; ZNAJDOWANIE EFEKTYWNEJ DŁUGOŚCI DZIELNIKA:
    ; Usuwa wiodące zera z dzielnika. Prawdziwa długość t ≤ m32.
    ; Warunek t > 1 zapewnia, że zawsze zostanie co najmniej jedna cyfra.
.find_effective_length:
    cmp rbx, 1
    jle .length_found      ; jeśli t <= 1, koniec pętli
    mov rax, rbx
    dec rax                ; rax = t-1
    mov rcx, [rsp]         ; rcx = x32
    mov eax, [rcx + rax*4] ; eax = x32[t-1]
    test eax, eax
    jnz .length_found      ; jeśli x32[t-1] != 0, koniec pętli
    dec rbx                ; --t
    jmp .find_effective_length
    
.length_found:
    ; unsigned s = clz32(x32[t-1])
    ; OBLICZENIE PRZESUNIĘCIA NORMALIZUJĄCEGO:
    ; s = liczba wiodących zer w najwyższej cyfrze dzielnika
    ; Po przesunięciu w lewo o s bitów: najwyższy bit będzie = 1
    mov rax, rbx
    dec rax                ; rax = t-1
    mov rcx, [rsp]         ; rcx = x32
    mov edi, [rcx + rax*4] ; edi = x32[t-1] (argument dla clz32)
    call clz32             ; eax = s
    mov r12d, eax          ; r12d = s
    ; uint32_t *v = (uint32_t*)malloc(t * sizeof(uint32_t))
    mov rdi, rbx           ; rdi = t
    shl rdi, 2             ; rdi = t * 4 (sizeof(uint32_t))
    call malloc wrt ..plt  ; rax = wskaźnik na v
    test rax, rax
    jz .malloc_failed      ; jeśli malloc zwrócił NULL
    mov r13, rax           ; r13 = v (wskaźnik na bufor)
    ; if(!s) - jeśli już ma bit MSB
    test r12d, r12d
    jnz .shift_left        ; jeśli s != 0, przesuń w lewo
    ; PRZYPADEK s = 0: Dzielnik już znormalizowany
    ; Kopiuj bez zmian: for(size_t i=0;i<t;++i) v[i]=x32[i]
    xor r14, r14           ; i = 0
.copy_loop:
    cmp r14, rbx           ; porównaj i z t
    jge .copy_done         ; jeśli i >= t, koniec
    mov rcx, [rsp]         ; rcx = x32
    mov eax, [rcx + r14*4] ; eax = x32[i]
    mov [r13 + r14*4], eax ; v[i] = x32[i]
    inc r14                ; i++
    jmp .copy_loop
.copy_done:
    jmp .save_results
.shift_left:
    ; PRZYPADEK s > 0: Wymagane przesunięcie normalizujące
    ; Przesunięcie w lewo o s bitów
    ; ALGORYTM PRZESUNIĘCIA WIELOSŁOWNEGO:
    ; v[i] = (x32[i] << s) | (x32[i-1] >> (32-s))
    ; unsigned rs = 32 - s
    mov eax, 32
    sub eax, r12d           ; eax = rs = 32 - s
    mov r15d, eax           ; r15d = rs
    ; uint32_t prev = 0
    xor r11d, r11d          ; prev = 0
    xor r14, r14            ; i = 0
    ; PĘTLA PRZESUWANIA:
    ; Dla każdego słowa, bierze s bitów z bieżącego słowa i (32-s) bitów z poprzedniego
.shift_loop:
    cmp r14, rbx            ; porównaj i z t
    jge .shift_done         ; jeśli i >= t, koniec
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
    ; Zapisz wyniki
    mov rax, [rsp+32]       ; rax = t_out
    mov [rax], rbx          ; *t_out = t
    mov rax, [rsp+40]       ; rax = s_out
    mov [rax], r12d         ; *s_out = s
    mov rax, r13            ; zwróć wskaźnik na v
    jmp .normalize_done
.malloc_failed:
    ; Zwolnij pamięć x32_ptr i y32_ptr
    mov rax, [rsp+16]       ; rax = x32_ptr
    mov rdi, [rax]          ; rdi = *x32_ptr
    call free wrt ..plt
    mov rax, [rsp+24]       ; rax = y32_ptr
    mov rdi, [rax]          ; rdi = *y32_ptr  
    call free wrt ..plt
    xor rax, rax            ; zwróć NULL
.normalize_done:
    add rsp, 48             ; przywróć stos
    pop r15
    pop r14
    pop r13
    pop r12
    pop rbx
    ret
; setup_dividend: tworzy dzielną reprezentującą 2^n
; Alokuje tablicę ulen słów 32-bit, wszystkie bity zerowane.
; Ustawia pojedynczy bit odpowiadający 2^n, uwzględniając przesunięcie s z normalizacji dzielnika.
; Oblicza długość quotient (qlen = ulen - t).
; Zabezpieczenie calloc_failed -> zwalnia pamięć wejściową i zwraca NULL.
setup_dividend:
    push rbx
    push r12
    push r13
    push r14
    push r15
    ; Pobierz parametry ze stosu (po push i return address)
    mov rax, [rsp + 48] ; v_ptr (48 = 5 regs * 8 + 8 return address)
    mov rbx, [rsp + 56] ; qlen_out (56 = 5 regs * 8 + 8 return address + 8)
    ; Zachowaj parametry w rejestrach
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
    ; ALOKACJA DZIELNEJ:
    ; Używamy calloc() aby wyzerować tablicę - większość bitów dzielnej 2^n to zera
    ; uint32_t *u = (uint32_t*)calloc(ulen, sizeof(uint32_t))
    mov rdi, r14            ; rdi = ulen (count)
    mov rsi, 4              ; rsi = sizeof(uint32_t)
    call calloc wrt ..plt
    test rax, rax
    jz .calloc_failed       ; jeśli calloc zwrócił NULL
    mov r15, rax            ; r15 = u (wskaźnik na bufor)
    ; size_t w = n/32
    mov rax, [rsp+8]        ; rax = n
    shr rax, 5              ; rax = n / 32 = w
    mov rbx, rax            ; rbx = w
    ; unsigned b = n%32
    mov rax, [rsp+8]        ; rax = n
    and rax, 31             ; rax = n % 32 = b
    mov rcx, rax            ; rcx = b
    ; OBLICZENIE POZYCJI BITU 2^n PO NORMALIZACJI:
    ; Oryginalnie: bit na pozycji n
    ; Po przesunięciu o s: bit na pozycji n+s
    ; 
    ; size_t nw = w + (b+s) / 32   // nowe słowo
    ; unsigned nb = (b + s) & 31U      // nowa pozycja w słowie
    add rcx, [rsp+16]       ; rcx = b + s
    shr rcx, 5              ; rcx = (b + s) / 32
    add rbx, rcx            ; rbx = nw = w + (b+s)/32
    ; unsigned nb = (b + s) & 31U
    mov rax, [rsp+8]        ; rax = n
    and rax, 31             ; rax = n % 32 = b
    add rax, [rsp+16]       ; rax = b + s
    and rax, 31             ; rax = nb = (b+s) & 31
    mov rcx, rax            ; rcx = nb
    ; if(nw < ulen) u[nw] = 1U << nb
    ; USTAWIENIE POJEDYNCZEGO BITU REPREZENTUJĄCEGO 2^n:
    ; Jeśli pozycja mieści się w tablicy, ustaw bit na pozycji nb w słowie nw.
    cmp rbx, r14           ; porównaj nw z ulen
    jge .skip_bit_set      ; jeśli nw >= ulen, pomiń
    mov rax, 1             ; rax = 1
    shl rax, cl            ; rax = 1 << nb
    mov [r15 + rbx*4], eax ; u[nw] = 1 << nb
.skip_bit_set:
    ; size_t qlen = ulen - t
    ; OBLICZENIE DŁUGOŚCI QUOTIENT:
    ; Quotient będzie miał qlen cyfr w bazie 2^32
    mov rax, r14            ; rax = ulen
    sub rax, [rsp+24]       ; rax = qlen = ulen - t
    mov [r13], rax          ; *qlen_out = qlen
    ; Zwróć wskaźnik na u
    mov rax, r15            ; zwróć u
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
    xor rax, rax            ; zwróć NULL
.setup_done:
    add rsp, 48             ; przywróć stos
    pop r15
    pop r14
    pop r13
    pop r12
    pop rbx
    ret
; perform_division: wykonuje główne dzielenie Knutha
; Iteruje od najwyższej cyfry quotient do najniższej.
; Każda iteracja:
; 1. Szacuje qhat = trial(u,j,v,t)
; 2. Odejmuję qhat*v od u[j..j+t] (sub_mul)
; 3. Jeśli sub_mul zwróci borrow → korekta qhat-- i add_back(u,j,v,t)
; 4. Zapisuje qhat w tablicy wyniku y32[j] jeśli w zakresie
; Po zakończeniu pętli: konwersja quotient z bazy 2^32 na 2^64.
; Na końcu zwalnia wszystkie tymczasowe bufory użyte w dzieleniu.
perform_division:
    push rbx
    push r12
    push r13
    push r14
    push r15
    ; Pobierz parametry ze stosu (po push i return address)
    ; Offsty: po zapisaniu 5 rejestrów (5*8=40) + return address (8) = 48
    mov r10, [rsp + 48]      ; y (7. parametr)
    mov r11, [rsp + 56]      ; x32_ptr (8. parametr)
    mov rax, [rsp + 64]      ; y32_ptr (9. parametr)
    mov rbx, [rsp + 72]      ; v_ptr (10. parametr) 
    mov r12, [rsp + 80]      ; u_ptr (11. parametr)
    ; Zachowaj wszystkie parametry na stosie
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
    ; for(size_t it=0; it<qlen; ++it)
    ; GŁÓWNA PĘTLA ALGORYTMU D:
    ; Iterujemy od najwyższej cyfry quotient do najniższej
    xor r15, r15            ; it = 0
.division_loop:
    cmp r15, [rsp+24]       ; porównaj it z qlen
    jge .division_done      ; jeśli it >= qlen, koniec
    ; size_t j = qlen-1-it
    ; POZYCJA BIEŻĄCEJ CYFRY:
    ; j indeksuje pozycję w quotient od najwyższej (qlen-1) do najniższej (0)
    mov rax, [rsp+24]       ; rax = qlen
    dec rax                 ; rax = qlen - 1
    sub rax, r15            ; rax = j = qlen - 1 - it
    mov r13, rax            ; r13 = j
    ; uint32_t qh = trial(u,j,v,t)
    ; KROK D3: OSZACOWANIE CYFRY QUOTIENT
    ; trial() implementuje wzór Knutha + korekty z kroku D4
    mov rdi, [rsp]          ; rdi = u
    mov rsi, r13            ; rsi = j
    mov rdx, [rsp+8]        ; rdx = v
    mov rcx, [rsp+16]       ; rcx = t
    call trial
    mov r14d, eax           ; r14d = qh
    ; if(qh && sub_mul(u,j,v,t,qh))
    ; KROKI D4-D5: MNOŻENIE, ODEJMOWANIE I TEST
    ; Jeśli qh = 0, pomijamy (nic do odejmowania)
    ; W przeciwnym razie: wykonuj u[j..j+t] := u[j..j+t] - qhxv
    ; sub_mul zwraca borrow:
    ; - borrow = 0: qh było poprawne  
    ; - borrow = 1: qh było za duże o 1, potrzebna korekta
    test r14d, r14d
    jz .skip_submul         ; jeśli qh == 0, pomiń sub_mul
    mov rdi, [rsp]          ; rdi = u
    mov rsi, r13            ; rsi = j
    mov rdx, [rsp+8]        ; rdx = v
    mov rcx, [rsp+16]       ; rcx = t
    mov r8d, r14d           ; r8d = qh
    call sub_mul
    test eax, eax
    jz .skip_correction ; jeśli sub_mul zwrócił 0, nie ma korekty
    ; KROK D6: KOREKTA GDY QH BYŁO ZA DUŻE
    ; Jeśli borrow = 1, oznacza że qh było za duże o 1
    ; --qh
    dec r14d
    ; add_back(u,j,v,t)  
    ; Przywrócenie: u[j..j+t] := u[j..j+t] + v
    ; To anuluje "nadmiarowe" odjęcie z sub_mul
    mov rdi, [rsp]          ; rdi = u
    mov rsi, r13            ; rsi = j
    mov rdx, [rsp+8]        ; rdx = v
    mov rcx, [rsp+16]       ; rcx = t
    call add_back
.skip_correction:
.skip_submul:
    ; if(j < (size_t)m32) y32[j] = qh
    ; KROK D7: ZAPIS CYFRY QUOTIENT
    ; Zapisujemy cyfrę tylko jeśli mieści się w tablicy wyniku
    ; (niektóre cyfry mogą być poza zakresem)
    cmp r13, [rsp+40]       ; porównaj j z m32
    jge .skip_store         ; jeśli j >= m32, nie zapisuj
    mov rax, [rsp+32]       ; rax = y32
    mov [rax + r13*4], r14d ; y32[j] = qh
.skip_store:
    inc r15                 ; it++
    jmp .division_loop
.division_done:
    ; conv32to64(y,y32,m32/2)
    ; KROK D8: KONWERSJA WYNIKU Z POWROTEM DO BAZY 2^64
    ; Przekształcamy quotient z bazy 2^32 na bazę 2^64 dla wyjścia
    mov rdi, [rsp+48]   ; rdi = y
    mov rsi, [rsp+32]   ; rsi = y32
    mov rdx, [rsp+40]   ; rdx = m32
    shr rdx, 1          ; rdx = m32/2
    call conv32to64
    ; Zwolnij pamięć: free(*u_ptr); free(*v_ptr); free(*y32_ptr); free(*x32_ptr)
    ; CZYSZCZENIE PAMIĘCI:
    ; Zwalniamy wszystkie tymczasowe bufory użyte w algorytmie
    ; free(*u_ptr)
    mov rax, [rsp+80]   ; rax = u_ptr
    mov rdi, [rax]      ; rdi = *u_ptr
    call free wrt ..plt
    ; free(*v_ptr)
    mov rax, [rsp+72]   ; rax = v_ptr
    mov rdi, [rax]      ; rdi = *v_ptr
    call free wrt ..plt
    ; free(*y32_ptr)
    mov rax, [rsp+64]   ; rax = y32_ptr
    mov rdi, [rax]      ; rdi = *y32_ptr
    call free wrt ..plt
    ; free(*x32_ptr)
    mov rax, [rsp+56]   ; rax = x32_ptr
    mov rdi, [rax]      ; rdi = *x32_ptr
    call free wrt ..plt
    add rsp, 88         ; przywróć stos
    pop r15
    pop r14
    pop r13
    pop r12
    pop rbx
    ret
; init_memory - przygotowanie pamięci i konwersja wyjścia.
; 1. Oblicza liczbę słów 32-bit potrzebnych do reprezentacji dzielnika
;    w bazie 2^32 (m32 = 2 * m64, gdzie m64 = n/64)
; 2. Alokuje pamięć dla dzielnika (x32) i wyniku (y32)
; 3. Konwertuje dzielnik z bazy 2^64 na 2^32 (conv64to32)
; 4. Sprawdza błędy alokacji pamięci i czy n > 0
init_memory:
    push rbx
    push r12
    push r13
    push r14
    push r15
    ; Zachowaj parametry
    mov r12, rdi        ; x32 (wskaźnik do wskaźnika)
    mov r13, rsi        ; y32 (wskaźnik do wskaźnika)
    mov r14, rdx        ; x (tablica wejściowa)
    mov r15d, ecx       ; n (unsigned)
    ; size_t m64 = n/64
    mov rax, rcx        ; rax = n
    shr rax, 6          ; rax = n / 64 = m64
    mov rbx, rax        ; rbx = m64
    ; if(!m64) return 0
    ; WALIDACJA: n musi być > 0 i wielokrotnością 64
    test rbx, rbx
    jz .return_zero     ; jeśli m64 == 0, zwróć 0
    ; size_t m32 = m64*2
    ; OBLICZENIE ROZMIARU W BAZIE 2^32:
    ; Każde słowo 64-bit = 2 słowa 32-bit
    shl rbx, 1          ; rbx = m32 = m64 * 2
    ; *x32 = (uint32_t*)malloc(m32 * sizeof(uint32_t))
    mov rdi, rbx        ; rdi = m32
    shl rdi, 2          ; rdi = m32 * 4 (sizeof(uint32_t))
    call malloc wrt ..plt
    mov [r12], rax      ; *x32 = wynik malloc (może być NULL)
    ; *y32 = (uint32_t*)calloc(m32, sizeof(uint32_t))
    mov rdi, rbx        ; rdi = m32 (count)
    mov rsi, 4          ; rsi = sizeof(uint32_t)
    call calloc wrt ..plt
    mov [r13], rax      ; *y32 = wynik calloc (może być NULL)
    ; if(!*x32 || !*y32)
    mov r10, [r12]      ; r10 = *x32
    mov r11, [r13]      ; r11 = *y32
    test r10, r10       ; sprawdź czy *x32 != NULL
    jz .cleanup_memory  ; jeśli *x32 == NULL, idź do cleanup
    test r11, r11       ; sprawdź czy *y32 != NULL
    jz .cleanup_memory  ; jeśli *y32 == NULL, idź do cleanup
    ; conv64to32(*x32, x, m64)
    ; KONWERSJA DZIELNIKA Z BAZY 2^64 NA 2^32:
    ; Przekształca wejściowy dzielnik do formatu wymaganego przez algorytm D
    mov rdi, r10        ; rdi = *x32
    mov rsi, r14        ; rsi = x
    mov rax, rbx        ; rax = m32
    shr rax, 1          ; rax = m64 = m32/2
    mov rdx, rax        ; rdx = m64
    call conv64to32
    ; return m32
    ; Zwraca rozmiar w słowach 32-bit lub kod błędu
    mov eax, ebx        ; zwróć m32
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
    ; return -1; (brak pamięci -> błąd)
    mov eax, -1
    jmp .init_done
.return_zero:
    mov eax, 0          ; zwróć 0 (n == 0)
.init_done:
    pop r15
    pop r14
    pop r13
    pop r12
    pop rbx
    ret
; ALGORYTM D KNUTHA - pełne dzielenie.
; 1. Normalizacja dzielnika (normalize_divisor)
;    - zapewnia, że najwyższy bit dzielnika jest ustawiony
;    - usuwa wiodące zera i oblicza przesunięcie s
; 2. Przygotowanie dzielnej (setup_dividend)
;    - tworzy dzielną reprezentującą 2^n
;    - uwzględnia przesunięcie normalizujące dzielnika
;    - oblicza długość quotient (qlen)
; 3. Właściwe dzielenie (perform_division)
;    - oszacowanie cyfr quotient (D3)
;    - odejmowanie i korekta (D4-D6)
;    - zapis cyfr wyniku (D7)
;    - konwersja wyniku do bazy 2^64 (D8)
; 4. Zarządzanie pamięcią
;    - alokacja, użycie i zwolnienie wszystkich tymczasowych buforów
perform_knuth:
    push rbp
    mov rbp, rsp
    sub rsp, 80             ; Miejsce na zmienne lokalne
    ; Zachowaj argumenty
    mov [rbp-8], rdi        ; x32
    mov [rbp-16], rsi       ; y32  
    mov [rbp-24], rdx       ; m32
    mov [rbp-32], ecx       ; n
    mov [rbp-40], r8        ; y (wynik)
    ; Zmienne lokalne:
    ; [rbp-48] : t (size_t)
    ; [rbp-56] : s (unsigned)
    ; [rbp-64] : v (uint32_t *)
    ; [rbp-72] : qlen (size_t)
    ; [rbp-80] : u (uint32_t *)
    ; KROK 1: normalize_divisor(x32, m32, &x32, &y32, &t, &s)
    ; ALGORYTM D - KROK D1: NORMALIZACJA DZIELNIKA
    ; Przesunięcie dzielnika tak aby najwyższy bit był = 1
    mov rdi, [rbp-8]        ; x32
    mov rsi, [rbp-24]       ; m32
    lea rdx, [rbp-8]        ; &x32
    lea rcx, [rbp-16]       ; &y32
    lea r8, [rbp-48]        ; &t
    lea r9, [rbp-56]        ; &s
    call normalize_divisor
    ; if(!v) return;
    test rax, rax
    jz .knuth_done          ; Jeśli v == NULL, zakończ (normalize_divisor posprzątał)
    mov [rbp-64], rax       ; v = wynik
    ; ALGORYTM D - KROK D2: PRZYGOTOWANIE DZIELNEJ
    ; Tworzenie dzielnej reprezentującej 2^n z uwzględnieniem normalizacji
    mov edi, [rbp-24]       ; m32
    mov esi, [rbp-32]       ; n
    mov edx, [rbp-56]       ; s
    mov rcx, [rbp-48]       ; t
    lea r8, [rbp-8]         ; &x32
    lea r9, [rbp-16]        ; &y32
    ; Argumenty na stosie (7. i 8. parametr)
    sub rsp, 16             ; Wyrównanie do 16 bajtów
    lea rax, [rbp-64]       ; &v
    mov [rsp], rax          ; 7. parametr: &v
    lea rax, [rbp-72]       ; &qlen
    mov [rsp+8], rax        ; 8. parametr: &qlen
    call setup_dividend
    add rsp, 16             ; Przywróć stos
    ; if(!u) return;
    test rax, rax
    jz .knuth_done          ; Jeśli u == NULL, zakończ (setup_dividend posprząta)
    mov [rbp-80], rax       ; u = wynik
    ; ALGORYTM D - KROKI D3-D8: GŁÓWNA PĘTLA DZIELENIA I FINALIZACJA
    ; Wykonanie dzielenia wielosłownego z automatycznym zarządzaniem pamięcią
    mov rdi, [rbp-80]       ; u
    mov rsi, [rbp-64]       ; v
    mov rdx, [rbp-48]       ; t
    mov rcx, [rbp-72]       ; qlen
    mov r8, [rbp-16]        ; y32
    mov r9, [rbp-24]        ; m32
    ; Argumenty na stosie (7-11 parametrów)
    sub rsp, 48             ; Wyrównanie do 16 bajtów (5*8 = 40, round to 48)
    mov rax, [rbp-40]       ; y (oryginalny argument)
    mov [rsp], rax          ; 7. parametr: y
    lea rax, [rbp-8]        ; &x32
    mov [rsp+8], rax        ; 8. parametr: &x32
    lea rax, [rbp-16]       ; &y32
    mov [rsp+16], rax       ; 9. parametr: &y32
    lea rax, [rbp-64]       ; &v
    mov [rsp+24], rax       ; 10. parametr: &v
    lea rax, [rbp-80]       ; &u
    mov [rsp+32], rax       ; 11. parametr: &u
    call perform_division
    add rsp, 48             ; Przywróć stos
    ; perform_division zwolniła całą pamięć - zakończ
    ; ZAKOŃCZENIE:
    ; perform_division automatycznie zwolniła wszystkie bufory tymczasowe
    ; Wynik został zapisany w y poprzez konwersję z bazy 2^32 na 2^64
.knuth_done:
    ; Przywróć stos i rejestry
    mov rsp, rbp
    pop rbp
    ret
; FUNKCJA INTERFEJSOWA DLA ALGORYTMU D KNUTHA
; Koordynuje pełny algorytm obliczania y = floor(2^n / x)
; 1. Alokuje pamięć i konwertuje dzielnik do formatu 32-bitowego (init_memory)
; 2. Wykonuje pełne dzielenie Knutha 2^n / x (perform_knuth)
; 3. Zwalnia wszystkie tymczasowe bufory
ninv:
    push rbp
    mov rbp, rsp
    sub rsp, 32             ; Miejsce na zmienne lokalne
    ; Zachowaj argumenty
    mov [rbp-8], rdi        ; y (wynik)
    mov [rbp-16], rsi       ; x (dzielnik)
    mov [rbp-24], edx       ; n (wykładnik)
    ; Zmienne lokalne:
    ; [rbp-32] : m32 (size_t)
    ; INICJALIZACJA I KONWERSJA WEJŚCIA:
    ; Alokuje pamięć i konwertuje dane z bazy 2^64 na 2^32
    lea rdi, [rbp-40]       ; &x32 (miejsce na wskaźnik)
    lea rsi, [rbp-48]       ; &y32 (miejsce na wskaźnik) 
    mov rdx, [rbp-16]       ; x
    mov ecx, [rbp-24]       ; n
    sub rsp, 16             ; Wyrównanie + miejsce na x32, y32
    call init_memory
    ; if(m32 <= 0) return;
    test rax, rax
    jle .core_done
    mov [rbp-32], rax       ; m32 = wynik init_memory
    ; WYKONANIE PEŁNEGO ALGORYTMU D KNUTHA:
    ; Łączy kroki D1-D8 w jednej funkcji dla wydajności
    mov rdi, [rbp-40]       ; x32
    mov rsi, [rbp-48]       ; y32  
    mov rdx, [rbp-32]       ; m32
    mov ecx, [rbp-24]       ; n
    mov r8, [rbp-8]         ; y (wskaźnik do wyniku)
    call perform_knuth
.core_done:
    add rsp, 16             ; Przywróć stos
    mov rsp, rbp
    pop rbp
    ret
