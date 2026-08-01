# ARSW — Blacklist Search Starter 

> **Java 21 Workshop on concurrency, performance measurement, fixed thread pools, and virtual threads.**

**Course:** Arquitecturas de Software — ARSW  
**Institution:** Universidad Escuela Colombiana de Ingeniería Julio Garavito  
**Professor:** Javier Iván Toquica  
**Work mode:** Teams of three students  
**Technology:** Java 21 · Maven · JUnit 5  
**Submission deadline:** Defined in the institutional platform

Starter project for Workshop 1 and Laboratory 1 of the Software Architectures course.

## Requirements

- Java 21
- Maven 3.9+

## Run the baseline implementation

```bash
mvn clean test
mvn exec:java
```

To change the IP address and enable or disable simulated latency:

```bash
mvn exec:java -Dexec.args="202.24.34.55 true"
mvn exec:java -Dexec.args="202.24.34.55 false"
```

## Use during Workshop 1

The workshop focuses on analysis, not implementation. Review the project structure, run the sequential version, and use the case to justify architectural decisions, quality attributes, metrics, and trade-offs.

Do not modify the classes during the workshop.

## Laboratory 1 Tasks

Complete the following implementations:

- `FixedPoolBlackListSearch`
- `VirtualThreadBlackListSearch`

Compare at least the following strategies:

- Sequential execution
- Fixed thread pools with 2, 4, and 8 threads
- Virtual threads

All alternatives must produce equivalent results and report reproducible measurements.

[Laboratory]( /README_ARSW_Laboratorio1.md )
