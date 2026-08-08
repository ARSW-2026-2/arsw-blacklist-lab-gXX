## 15.1 Correctness

#### **1. How did the team verify that the three strategies produce equivalent results?**
The team verified equivalence through automated testing and runtime assertions. The test class `ConcurrentBlackListSearchTest.java` automatically verifies that fixed pools of 2, 4, and 8 threads, as well as the virtual thread strategy, produce the exact same matches as the sequential baseline. The tests assert that exactly 100 providers are consulted, matching IDs contain no duplicates, and matches are ordered. Additionally, `BenchmarkRunner.java` verifies equivalence dynamically during measured runs by comparing results against the first execution, throwing an `IllegalStateException` if they differ.

#### **2. Why can concurrent tasks return matches in a different order?**
Concurrent tasks execute independently, and their completion order is inherently unpredictable due to thread scheduling and varying workloads. In this laboratory, `MockBlackListProvider` simulates blocking I/O with different latencies depending on the provider's ID (`20 + Math.floorMod(id * 29, 181)`). Because some providers naturally take longer to respond than others, faster tasks will finish and return their matches first, regardless of the order in which they were submitted to the executor.

#### **3. What mechanism or design prevented lost or duplicated matches?**
Lost or duplicated matches were prevented by strictly avoiding unsafe shared mutable state. In `FixedPoolBlackListSearch`, each concurrent task uses its own local `ArrayList` to collect matches and then returns them via a `PartialResult` record. In `VirtualThreadBlackListSearch`, each task returns a single `Integer` (the provider ID) or `null`. In both implementations, the main thread waits for all tasks to complete using `ExecutorService.invokeAll()` or tracking a list of futures, and then sequentially aggregates the returned values via `Future.get()`. This design completely eliminates race conditions.

#### **4. Why should performance not be compared before proving functional equivalence?**
As explicitly stated in the problem statement, correctness comes before performance, and a benchmark is fundamentally invalid if the compared strategies do not produce equivalent results. If a concurrent strategy were bugged—for instance, if it skipped providers, failed to wait for all threads, or lost matches due to race conditions—it would likely finish faster than the sequential baseline. Comparing performance under these conditions would lead to false conclusions, as the "faster" algorithm is not actually doing the same amount of work.

## 15.2 Fixed Thread Pool

#### **5. What changed when the pool increased from 2 to 4 threads?**
As we can see in the Time summary, when we have 2 threads and because of the strategy of dividing the providers according to the number of threads. We can see that it has an average time of 5670,937 dividing the providers by 50 and 50 for each thread, therefore the 4 thread pool size is much more efficient because we divide the providers 25% for each thread so it has a better distribution, that's the reason why the average time reduces by half with 2844,861 ms.

#### **6. What changed when the pool increased from 4 to 8 threads?**
Doing a little bit of investigation we found a metric that can show us how the threads are working, in the context of concurrency, this is measured using a metric called Speedup, this metric will help us to measure the linear scalability which formula is time(4threads) / time (8threads) = 1.88x so the time in 4 threads is 2844,861 and with 8 threads is 1510,311 it is not exactly the half because some resources of the cpu are used to run each thread and it is not equally divided.

#### **7. Was the improvement proportional to the number of threads? Explain.**
Yes it was almost linear the speedup between 2 and 4 threads was x1.99 it means that the time was almost 2 times faster but between 4 and 8 threads the speedup was about x1.88 so it started to decrease but it has a good proportion.

#### **8. What costs are introduced by task creation, scheduling, context switching, and result consolidation?**
***Task creation:*** Memory allocation, CPU cycles. ***Scheduling:*** Queue contention, OS intervention. ***Context Switching:*** Cache invalidation. ***Result consolidation:*** Synchronization Overhead, Data Aggregation.

#### **9. What would happen if the pool size were much larger than the available platform threads?** 
If this happens we'll be seeing a phenomenon known as resource thrashing, it means that instead of being a speedup it will drastically drop the efficiency of our program or in severe cases it will crash our program. A CPU can only execute 1 thread for example if we have an 8 core CPU but we create a 1000 pool thread the OS will need to pause, swap and do everything possible to make space for each thread reducing the performance consuming massive memory because each thread is in a queue waiting to be executed.

## 15.3 Virtual threads

#### **10. In which scenario did virtual threads provide the clearest benefit?**
The Virtual Threads provided the clearest benefit in the I/O scenario. The evidence is that the average time with virtual threads was 201.233 ms, while the sequential strategy took 11201.347 ms, giving a 55.664 speedup. In comparison, without simulated I/O, virtual threads took 0.873 ms, while sequential took only 0.080 ms, so they were actually slower. As the table shows:
| Scenario | Strategy | Pool size | Average ms | Minimum ms | Maximum ms | Speedup | Matches | Consulted |
|---|---|---:|---:|---:|---:|---:|---:|---:|
| No simulated I/O | Virtual threads | N/A | 0.873 ms | 0.701 ms | 0.983 ms | 0.092 ms | 7 | 100 |
| Simulated I/O | Virtual threads | N/A | 201.233 ms | 199.544 ms | 203.248 ms | 55.664 | 7 | 100 |

#### **11. Why are virtual threads especially relevant for blocking operations?**
The Virtual Threads are very relevant for blocking operations because they allow many tasks to wait for I/O concurrently without requiring a manually sized platform-thread pool. If we can see in the experiment support this because there were 100 providers and the virtual-thread strategy reduced the average time from 11201.347 ms with sequential execution to 201.233 ms, while still consulting all 100 providers and obtaining the same 7 matches. This represents a 55.664 speedup.

#### **12. Why do virtual threads not make local CPU work automatically faster?**
Virtual threads do not magically make processor work faster. When the task is short, the overhead of Java creating and organizing all the tasks outweighs the benefit of distributing them.
This is clearly evident in the results without I/O: the sequential approach took an average of just 0.080 ms, whereas using virtual threads increased the time to 0.873 ms. In other words, virtual threads were approximately 10.9 times slower in this case, even though they checked the same 100 providers and found the same 7 matches.

#### **13. What trade-offs remain even when virtual threads are lightweight?**
Virtual threads reduce the cost of performing many concurrent tasks, but they do not eliminate the overhead associated with creating and scheduling them, coordinating their results, and processing the final collection. The experiment illustrates the difference based on the type of work: with simulated I/O, virtual threads took 201.233 ms, whereas without I/O they took 0.873 ms—compared to 0.080 ms for sequential execution. This indicates that virtual threads are not inherently faster; their advantage depends on the presence of sufficient waiting or blocking work to offset the overhead of concurrency. Furthermore, in a real-world system, external services could introduce constraints such as network capacity, rate limits, or response times.

To clearly see the answers to the questions, we rely on this data from the table:
| Scenario | Strategy | Pool size | Average ms | Minimum ms | Maximum ms | Speedup | Matches | Consulted |
|---|---|---:|---:|---:|---:|---:|---:|---:|
| No simulated I/O | Sequential | 1 | 0.080 ms | 0.015 ms | 0.186 ms | 1.00 | 7 | 100 |
| No simulated I/O | Virtual threads | N/A | 0.873 ms | 0.701 ms | 0.983 ms | 0.092 ms | 7 | 100 |
| Simulated I/O | Sequential | 1 | 11201.347 ms | 10998.114 ms | 11560.252 ms | 1.00 | 7 | 100 |
| Simulated I/O | Virtual threads | N/A | 201.233 ms | 199.544 ms | 203.248 ms | 55.664 | 7 | 100 |

## 15.4 Architectural decision

### **14. Which strategy would the team recommend for a system dominated by blocking external calls?**
We recommend the Virtual Threads strategy. Virtual threads are designed specifically for blocking I/O operations. When a virtual thread is waiting for an external provider to respond, it releases the underlying OS carrier thread. This allows the system to handle thousands of concurrent requests efficiently without exhausting memory or crashing.

### **15. Which strategy would the team recommend for a small local workload?**
We recommend the Sequential execution strategy. Because the work is entirely local and very fast (no waiting times), the overhead of creating tasks, coordinating threads, and consolidating results actually takes longer than just executing the work directly in a single thread.

### **16. Under what conditions would a fixed pool still be preferable?**
A fixed thread pool is preferable for heavy, CPU-bound tasks (like complex mathematical calculations or large data processing). It allows the system to utilize all physical CPU cores while strictly limiting the maximum number of active threads, which prevents the CPU from becoming overloaded by excessive context switching.

### **17. What evidence from the measurements supports the recommendation?**
In the "Simulated I/O" scenario, Virtual Threads finished the scan significantly faster than the Sequential baseline because they handled the waiting periods concurrently. Conversely, in the "No simulated I/O" scenario, the Sequential strategy was the fastest, and we observed that increasing the number of threads in the Fixed Pool actually worsened the execution time due to the overhead of thread coordination.

### **18 .What limitations prevent generalizing the conclusion to every production system?**
Our experiment was small, ran on a single machine, and used simulated `Thread.sleep()` pauses instead of real network calls. In a real production system, there are other bottlenecks that this lab does not account for, such as database connection limits, memory constraints, network latency, and external API rate limits, which require more complex architectural evaluations.