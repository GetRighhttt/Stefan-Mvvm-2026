# Project context

Recorded 2026-10-17 at the user's request and updated with the completed refactor.

## Repository purpose

`MVVM_2026` is the user's reference guide to modern Android MVVM architecture. Keep the example modular, readable, and proportional to its size. Documentation should explain decisions behind layer boundaries, model mappings, state, effects, repository ownership, and lifecycle behavior.

The repository also records how an older example can be reverse engineered and rebuilt with a current toolchain and production-informed practices. It is a base structure for later work, including a possible API integration.

## Project identity

The root project is `MVVM_2026`. Its Android namespace and application ID are `com.example.mvvm2026`, and its visible app title is **MVVM 2026**. These names belong to this repository and are examples rather than defaults for other projects.

## Architecture decisions

- Presentation, domain, and data own separate models. Values cross layer boundaries through explicit mapping.
- Account, credential, and login concerns use separate model packages so the feature remains easy to scan.
- A standalone model or sealed hierarchy has its own file. Related cases stay with their sealed hierarchy.
- Domain owns validation, business models, results, use cases, and the repository contract.
- Data owns source models, the simulated source, repository implementation, credential comparison, completeness checks, and data-to-domain mapping.
- Presentation owns actions, durable UI state, temporary effects, dialogs, resources, renderer models, and domain-to-UI mapping.
- The activity is a host and lifecycle owner. XML and Compose render the same activity-scoped ViewModel state.
- Manual construction in `LoginViewModelFactory` is sufficient for this project. A dependency-injection framework can be added when the graph warrants it.

## State and effect decisions

Durable screen information uses `StateFlow`. Temporary Snackbar messages use a non-replaying `SharedFlow` with suspending `emit`. The current buffer uses `BufferOverflow.SUSPEND`; emission provides backpressure for an active collector but does not promise that later UI or external work will succeed.

The account request, loaded accounts, result dialogs, and dismissal countdown are durable state. The countdown runs only while the activity is resumed, respects Android's accessibility timeout, and survives renderer changes. Clearing the form cancels active login work.

## Renderer decisions

XML with View Binding and Jetpack Compose are two presentations of the same feature. Switching renderers must preserve validation, form state, loaded account data, countdown state, and repository work. Renderer-specific palettes and backgrounds stay in presentation resources. Shared status colors keep the same meaning in both renderers.

## Documentation style

Each Kotlin file has one focused KDoc before its main declaration. It explains responsibility and non-obvious implementation decisions and includes `@author Stefan Bayne` and `@since 2026-10-17`. Avoid file headers, refactor notes, repeated method comments, and narration of obvious syntax. The README is the complete guide; this file records durable project decisions.

The reusable Android cleanup skill remains project-neutral. Project names, package names, class names, and structures taken from this repository are illustrative examples. MVI guidance remains undecided and outside this project's scope.

## Growth strategy

This single app module fits one small feature with related account and credential concerns. As the codebase grows, organize presentation, domain, and data by feature. Move independently owned features into Gradle modules when that improves dependency direction, ownership, reuse, testing, or build isolation.

Larger applications can also introduce focused core modules for capabilities such as networking, persistence, design system, and test support. Core modules must have clear responsibilities and should not become containers for unrelated feature models or logic.
