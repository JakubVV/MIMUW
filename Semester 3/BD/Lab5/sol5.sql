SELECT SUM(level5_count) AS liczba_sciezek
   FROM (
       SELECT e5.tgt, COUNT(*) AS level5_count
       FROM (
           SELECT e4.tgt AS n4
           FROM (
               SELECT e3.tgt AS n3
               FROM (
                   SELECT e2.tgt AS n2
                   FROM (
                       SELECT e1.tgt AS n1
                       FROM E e1
                       WHERE e1.src = 0
                   ) e2src
                   JOIN E e2 ON e2.src = e2src.n1
               ) e3src
               JOIN E e3 ON e3.src = e3src.n2
           ) e4src
           JOIN E e4 ON e4.src = e4src.n3
       ) e5src
       JOIN E e5 ON e5.src = e5src.n4
       GROUP BY e5.tgt
   );
