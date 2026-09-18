# Development Prompts

## Architecture Documentation

Create high-level and low-level design documents for a Spring Boot movie ticket booking system. Cover system architecture, entities, APIs, database design, seat-hold workflow, concurrency prevention, payment, cancellation, refunds, and testing.

## Implementation Plan

Act as a Technical Project Manager and Senior Spring Boot Architect.

I am building a monolithic Movie Ticket Booking System. I have attached the High-Level Design (HLD) and Low-Level Design (LLD) documents for this system.

Step 1: Document Review
First, read and analyze the attached HLD and LLD files. Understand the database schema, API contracts, entity relationships, concurrency strategy, and architectural constraints. Do not generate code yet.

Step 2: Generate Implementation Plan
Based strictly on the provided designs, generate a detailed, phase-wise implementation plan to build this Spring Boot application from scratch. Structure the plan to maximize development efficiency and handle dependencies logically (e.g., build database entities before service layers).

Format the output into sequential phases (e.g., Phase 1: DB Scaffolding, Phase 2: Core Inventory APIs, Phase 3: Booking & Concurrency, etc.). For each phase, provide:

Objective: The core goal of the phase.

Key Tasks: Bulleted list of specific Spring Boot components to build (e.g., "Implement SeatRepository with pessimistic lock query," "Configure global @ControllerAdvice").

Dependencies: What must be completed before starting this phase.

Deliverable: The tangible outcome (e.g., "REST APIs for Admin inventory management are functional").

Keep the plan strictly aligned with the provided LLD and HLD. Do not introduce new technologies or microservices not mentioned in the documents.

## Coding

Implement Phase 1 of the implementation plan

Implement Phase 2 of the implementation plan

Implement Phase 3 of the implementation plan and verify all test cases

Implement Phase 4 of the implementation plan and verify all test cases