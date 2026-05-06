# Developer Documentation

This folder contains technical documentation and implementation notes maintained by the Developer agent.

## Structure
- `setup.md` — Initial development environment setup instructions
- `project-structure.md` — High-level module and package organization
- `deployment.md` — Deployment procedures and environment configuration
- `<feature-name>-implementation.md` — Implementation notes for specific features or complex domains
- `hexagonal-architecture-decisions.md` — Architecture boundary decisions and port/adapter implementations
- `cross-cutting-concerns.md` — Logging, error handling, configuration, observability setup

## Rules
- Keep this documentation aligned with approved technical requirements in technical-requirements/.
- Document **why** a technical choice was made, not just **what** was implemented.
- Link to related TREQ IDs for full traceability.
- Update documentation when architecture-impacting decisions are made during implementation.
- Include examples and code snippets when they clarify intent.
- Prefer readability over comprehensiveness — focus on decisions that affect future development.
- Use clear structure: Problem → Solution → Trade-offs → Related TREQs.

## When to Document
- Hexagonal architecture boundary decisions (ports, adapters, layers)
- Technology setup or configuration requiring future reference
- Complex domain logic or cross-cutting concerns
- Integration patterns between modules
- Performance or scalability considerations
- Operational/deployment procedures
