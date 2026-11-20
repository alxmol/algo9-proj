**V	    Density	Fastest version**
100	    Sparse	Lazy Heap
300	    Sparse	Indexed Heap
1,000	Sparse	Indexed Heap
3,000	Sparse	Indexed Heap
10,000	Sparse	Indexed Heap
100,000	Sparse	Indexed Heap
100	    Medium	Indexed Heap
300	    Medium	Indexed Heap
1,000	Medium	Indexed Heap
3,000	Medium	Indexed Heap
100	    Dense	Array Scan
300	    Dense	Indexed Heap
1,000	Dense	Indexed Heap
3,000	Dense	Array Scan

Both runs generally had the same winners, except for a couple minor exceptions.
- V=100 Dense graph winner was Array Scan in run 1, and Lazy Heap in run 2
- V=100000 Sparse graph winner was Lazy Heap in run 1, and Indexed Heap in run 2.
These were both very close, and the determined "fastest version" in this case was selected as the faster median time between the two runs.

**Specs:**
Java version: 24.0.2
JVM: OpenJDK 64-Bit Server VM
OS: Mac OS X aarch64
CPU cores: 8
Max heap: 4294 MB
CPU model: Apple M2
RAM: 8 GB