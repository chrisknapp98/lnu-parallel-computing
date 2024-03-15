
# Parallel Computing - Exam Questions

## Problem 1 (2 + 2 + 4 + 2 = 10 p)
### 1. What is the difference between task and data parallelism?

- Task parallelism
  - focuses on distributing tasks (distinct functions) across processors
  - those may work in the same or different datasets
- Data parallelism
  - involves distributing data across processors
  - the same operation is performed on different pieces of data


### 2. Explain MISD and MIMD.

- MISD (Multiple Instruction streams, Single Data streams)
  - a theoretical model where multiple instructions operate on the same data
  - not commonly used in practice
  - An example could be applying different filters to the same video frame simultaneously.
- MIMD (Multiple Instruction streams, Multiple Data streams)
  - allows for multiple autonomous processors to execute different instructions on different data
  - widely used in modern parallel computing environments, 
  - enabling diverse computational tasks to be performed in parallel, enhancing computing efficiency and speed

### 3. Explain the decomposition, assignment, and orchestration steps when creating parallel programs.

- decomposition involves dividing computation into tasks 
  - the objective is to identify portions of the computation that can be executed concurrently, considering the dependencies among tasks
- assignment distributes these tasks to workers
  - involves decisions about task scheduling, prioritizing tasks based on dependencies, and balancing the load among the processors to avoid bottlenecks
- orchestration manages data access, communication, and synchronization among processes, and mapping assigns processes to processors
  - includes synchronizing tasks to ensure that data dependencies are respected, managing communication between tasks, especially in distributed systems, and handling any runtime dynamics that may affect the execution order or the assignment of tasks

### 4. Is uneven load a problem when we create parallel programs? Motivate and explain what we can do to avoid it.

- uneven load can be a problem, leading to inefficiencies as some processors may remain idle while others are overburdened.
- strategies to avoid uneven load include dynamic workload distribution and load balancing techniques.
  - load balancing can be achieved through various strategies, such as work-stealing, where idle processors can "steal" tasks from busier processors, or 
  - by partitioning the problem domain in a way that allows for more flexible task assignment.

## Problem 2 (2 + 4 + 4 = 10 p)
### 1. Explain message-passing and shared memory.

- Message-Passing
  - processes communicate by explicitly sending and receiving messages
  - this model is well-suited for distributed systems where processes may run on different physical machines 
  - it provides a clear and structured way to handle communication but requires careful design to avoid deadlocks and ensure efficient data transfer
- Shared Memory 
  - the shared memory model allows multiple processes to access common memory spaces
  - this model is more intuitive for programmers as it resembles the traditional single-threaded programming model 
  - however, it requires mechanisms to ensure consistency and prevent race conditions, such as locks, semaphores, or barriers

### 2. Explain how we can automatically parallelise a program.

- term automatic parallelization can be misleading as a programmer still needs to provide hints or structure the code in a way that makes parallelization more straightforward for the compiler or runtime system
- the automatic part often refers more to the dynamic assignment of tasks to available processing units rather than the complete absence of manual intervention in the parallelization process
- while automatic parallelization aims to reduce the manual effort required to parallelize code, achieving optimal parallel performance often requires a blend of compiler/runtime support and intelligent guidance from the programmer
  - this can include using specific parallel libraries, adding annotations, restructuring code to make it more amenable to parallelization, and explicitly managing some aspects of task distribution and execution

### 3. Why is it difficult to automatically parallelise?

Automatic parallelization faces several challenges, 
- including accurately identifying independent tasks that can be executed in parallel, 
- dealing with data dependencies that limit parallelism, 
- and determining the most efficient way to distribute tasks and data across processors 
- Additionally, optimizing communication and synchronization among parallel tasks to minimize overhead and ensure correct execution is also a significant challenge.

## Problem 3 (5 + 2 + 3 = 10 p)
### 1. Explain ILP. What is it, what is its relation to parallel computing, and what issues are there?

- Instruction Level Parallelism (ILP) 
  - is the measure of how many of the operations in a computer program can be performed simultaneously 
  - reflects the ability of a processor to execute multiple instructions at the same time, without having to complete one instruction before starting the next 
  - is a key factor in the performance of a processor, exploiting parallelism that exists within a single program thread
  - Key concepts of ILP
    - Pipelining
      - is a technique where different stages of instruction execution (like fetching, decoding, executing, and writing back) are overlapped. 
      - that means that a CPU can have issued a command, but doesn't need to actively manage every step of that operation's execution after it's initiated (like a read or write operation to memory, or sending data to an I/O device)
    - Superscalar Execution
      - refers to the ability of a CPU to execute more than one instruction during a single clock cycle
      - CPUs have several execution units (arithmetic logic units, floating-point units, load/store units, etc.) that can operate in parallel, assuming the instructions are independent and there are no data hazards
      - its design allows it to decode and prepare several instructions for execution simultaneously, further enhancing its ability to process instructions in parallel
    - Out-of-order Execution
      - allows the CPU to pick and choose which instructions to execute next, based on availability of data and execution units, rather than following the program order strictly
      - if the CPU encounters an instruction that it can't execute right away (perhaps because it's waiting for data to be fetched from memory), it can execute another instruction that's ready to go 
      - this helps keep the CPU busy and improves efficiency
      - it is also beneficial in handling branch predictions and data dependencies more efficiently
        - if a branch prediction goes wrong, the CPU might need to discard or roll back executed instructions
  - Issues with ILP
    - **Data Hazards:** Situations where instructions that are scheduled to execute in parallel depend on each other's results, leading to delays.
    - **Control Hazards:** Caused by branch instructions (like if-else or loops) that can alter the flow of execution, making it hard to predict which instructions can be executed in parallel.
    - **Resource Conflicts:** Occur when instructions compete for the same resources (like memory or execution units), causing stalls.
    - **Diminishing Returns:** Beyond a certain point, adding more parallelism does not yield significant performance improvements, due to increased complexity and overheads in managing it.

### 2. Explain the memory wall. What is the issue and why do we consider it a "wall"?
### 3. Give two examples of how we can reduce the impact of the memory wall.

