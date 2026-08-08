# Evidences Experimental Comparison

According to the Laboratory we need to record the evidence, complete the table, and show the results of the different strategies we implemented.

To complete the table, in everyone ejecution, we put in te terminal the next command:
- *mvn exec:java "-Dexec.args="the parameters we are going to test""*

In this way, we will obtain the necessary information to copy into the .csv file and fill in the given table.

The laboratory tells us that we must record the following:
For every configuration, report:

- Average elapsed time in milliseconds.
- Minimum elapsed time.
- Maximum elapsed time.
- Number of matches.
- Number of consulted providers.
- Speedup relative to the sequential strategy in the same scenario.

And Calculate speedup as:

```text
Speedup = sequential average time / strategy average time
```


---

## Required Results Table

Corriendo cada comando con cada estrategía, tuvimos los siguientes resultados:

| Scenario | Strategy | Pool size | Average ms | Minimum ms | Maximum ms | Speedup | Matches | Consulted |
|---|---|---:|---:|---:|---:|---:|---:|---:|
| No simulated I/O | Sequential | 1 | 0.080 ms | 0.015 ms | 0.186 ms | 1.00 | 7 | 100 |
| No simulated I/O | Fixed pool | 2 | 0.310 ms | 0.255 ms | 0.407 ms | 0.258 | 7 | 100 |
| No simulated I/O | Fixed pool | 4 | 0.365 ms | 0.336 ms | 0.436 ms | 0.219 | 7 | 100 |
| No simulated I/O | Fixed pool | 8 | 0.666 ms | 0.550 ms | 0.740 ms | 0.120 ms | 7 | 100 |
| No simulated I/O | Virtual threads | N/A | 0.873 ms | 0.701 ms | 0.983 ms | 0.092 ms | 7 | 100 |
| Simulated I/O | Sequential | 1 | 11201.347 ms | 10998.114 ms | 11560.252 ms | 1.00 | 7 | 100 |
| Simulated I/O | Fixed pool | 2 | 5541.439 ms | 5515.921 ms | 5627.123 ms | 2.021 | 7 | 100 |
| Simulated I/O | Fixed pool | 4 | 2773.540 ms | 2770.816 ms | 2777.763 ms | 4.039 | 7 | 100 |
| Simulated I/O | Fixed pool | 8 | 1471.788 ms | 1470.578 ms | 1473.455 ms | 7.611 | 7 | 100 |
| Simulated I/O | Virtual threads | N/A | 201.233 ms | 199.544 ms | 203.248 ms | 55.664 | 7 | 100 |