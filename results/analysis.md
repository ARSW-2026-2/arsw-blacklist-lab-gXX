## 15.1 Correctness

**How did the team verify that the three strategies produce equivalent results?**
The team verified equivalence through automated testing and runtime assertions. The test class `ConcurrentBlackListSearchTest.java` automatically verifies that fixed pools of 2, 4, and 8 threads, as well as the virtual thread strategy, produce the exact same matches as the sequential baseline. The tests assert that exactly 100 providers are consulted, matching IDs contain no duplicates, and matches are ordered. Additionally, `BenchmarkRunner.java` verifies equivalence dynamically during measured runs by comparing results against the first execution, throwing an `IllegalStateException` if they differ.

**Why can concurrent tasks return matches in a different order?**
Concurrent tasks execute independently, and their completion order is inherently unpredictable due to thread scheduling and varying workloads. In this laboratory, `MockBlackListProvider` simulates blocking I/O with different latencies depending on the provider's ID (`20 + Math.floorMod(id * 29, 181)`). Because some providers naturally take longer to respond than others, faster tasks will finish and return their matches first, regardless of the order in which they were submitted to the executor.

**What mechanism or design prevented lost or duplicated matches?**
Lost or duplicated matches were prevented by strictly avoiding unsafe shared mutable state. In `FixedPoolBlackListSearch`, each concurrent task uses its own local `ArrayList` to collect matches and then returns them via a `PartialResult` record. In `VirtualThreadBlackListSearch`, each task returns a single `Integer` (the provider ID) or `null`. In both implementations, the main thread waits for all tasks to complete using `ExecutorService.invokeAll()` or tracking a list of futures, and then sequentially aggregates the returned values via `Future.get()`. This design completely eliminates race conditions.

**Why should performance not be compared before proving functional equivalence?**
As explicitly stated in the problem statement, correctness comes before performance, and a benchmark is fundamentally invalid if the compared strategies do not produce equivalent results. If a concurrent strategy were bugged—for instance, if it skipped providers, failed to wait for all threads, or lost matches due to race conditions—it would likely finish faster than the sequential baseline. Comparing performance under these conditions would lead to false conclusions, as the "faster" algorithm is not actually doing the same amount of work.

## 15.2 Fixed Thread Pool

**What changed when the pool increased from 2 to 4 threads?**
As we can see in the Time summary, when we have 2 threads and because of the strategy of dividing the providers according to the number of threads. We can see that it has an average time of 5670,937 dividing the providers by 50 and 50 for each thread, therefore the 4 thread pool size is much more efficient because we divide the providers 25% for each thread so it has a better distribution, that's the reason why the average time reduces by half with 2844,861 ms.

**What changed when the pool increased from 4 to 8 threads?**
Doing a little bit of investigation we found a metric that can show us how the threads are working, in the context of concurrency, this is measured using a metric called Speedup, this metric will help us to measure the linear scalability which formula is time(4threads) / time (8threads) = 1.88x so the time in 4 threads is 2844,861 and with 8 threads is 1510,311 it is not exactly the half because some resources of the cpu are used to run each thread and it is not equally divided.

**Was the improvement proportional to the number of threads? Explain.**
Yes it was almost linear the speedup between 2 and 4 threads was x1.99 it means that the time was almost 2 times faster but between 4 and 8 threads the speedup was about x1.88 so it started to decrease but it has a good proportion.

**What costs are introduced by task creation, scheduling, context switching, and result consolidation?**
***Task creation:*** Memory allocation, CPU cycles. ***Scheduling:*** Queue contention, OS intervention. ***Context Switching:*** Cache invalidation. ***Result consolidation:*** Synchronization Overhead, Data Aggregation.

**What would happen if the pool size were much larger than the available platform threads?** 
If this happens we'll be seeing a phenomenon known as resource thrashing, it means that instead of being a speedup it will drastically drop the efficiency of our program or in severe cases it will crash our program. A CPU can only execute 1 thread for example if we have an 8 core CPU but we create a 1000 pool thread the OS will need to pause, swap and do everything possible to make space for each thread reducing the performance consuming massive memory because each thread is in a queue waiting to be executed.
