# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Stack

Existing Vue 3 application with Vue Router, Axios, a Java Spring Boot API, and MySQL. The frontend is built with Vue CLI and native CSS.

## Users

Inferred from the repository and the user's brief: Chinese-speaking English learners who practice vocabulary independently, including exam-preparation and work-study contexts.

## Product Purpose

SEKAI is an English vocabulary learning system. It lets a learner practice words, record answers, review mistakes, maintain personal vocabulary, and inspect study history. Success means the learner can open the app and quickly understand what to study next.

## Positioning

The product combines answer history, wrong-word tracking, custom vocabulary, and membership planning features so practice can be organized around the learner's weak points instead of only browsing a larger word list.

## Operating Context

The learner signs in, chooses a practice source, answers Chinese-meaning prompts or spelling prompts, listens to pronunciation when available, and returns to wrong words, records, plans, or target preparation pages. The owner can generate membership activation codes in a separate admin surface.

## Capabilities and Constraints

- Existing routes and API contracts are part of the product surface and must remain stable.
- Free flows include public-word browsing, practice, wrong words, favorites, custom words, and study records.
- Membership flows include daily plans, review prioritization, reading-word extraction, goal planning, dictation, mock exams, reports, and spreadsheet import.
- Authentication uses the existing local storage token flow and the backend's user-scoped data.
- The interface is currently Chinese-first and must keep product terminology understandable in Chinese.
- No commercial claims, testimonials, or fabricated learning metrics should be added.

## Brand Commitments

The product name is SEKAI. The existing code presents it as an AI language lab with a dark, technology-led atmosphere. The approved visual direction keeps the night-study mood and replaces the dense cyber decoration with a focused midnight study-workspace language using a mint signal accent.

## Evidence on Hand

Product truth is present in `README.md`, `frontend/src/api`, `frontend/src/router/index.js`, and the Vue views under `frontend/src/views`. No local photographic or illustration asset was present before this redesign.

## Product Principles

- Make the next study action obvious.
- Turn mistakes into a useful review queue.
- Keep the learner's own vocabulary close to practice.
- Explain paid features through saved time and clearer decisions.
- Preserve honest states when data is empty, loading, or unavailable.

## Accessibility & Inclusion

The surface is web-based and form-heavy. Preserve keyboard access, visible focus states, readable contrast, semantic links and buttons, and a reduced-motion path for all authored motion.
