# Documentation Sync Workflow

## Purpose
Ensure that when agent instructions are updated, all related documentation files stay in sync and consistent across the project.

## Files to Keep in Sync
1. **Agent files** — `.github/agents/*.agent.md`
2. **Global instructions** — `.github/copilot-instructions.md`
3. **Initialization reference** — `prompt-initialisation.txt`
4. **Project overview** — `readme.md`
5. **Traceability matrix** — `traceability.md`
6. **Artifact templates** — `templates/**/*.md`

## Sync Checklist — When Agent Instructions Change

### Step 1: Agent File Updated
- [ ] File: `.github/agents/<agent-name>.agent.md`
- [ ] Change description: _____________________
- [ ] Affected gates/phases: _____________________

### Step 2: Update Global Instructions
- [ ] `.github/copilot-instructions.md`
  - [ ] Execution model (if workflow sequencing changed)
  - [ ] Gate rules (if conditions changed)
  - [ ] Quality rules (if new criteria added)
  - [ ] Traceability rules (if links changed)

### Step 3: Update Initialization Reference
- [ ] `prompt-initialisation.txt`
  - [ ] Agent responsibilities section
  - [ ] Gate rules (if applicable)
  - [ ] Traceability requirements (if applicable)
  - [ ] Completion criteria (if applicable)

### Step 4: Update Project README
- [ ] `readme.md`
  - [ ] Current Status section (if gate transitions changed)
  - [ ] Key Workflow Features (if quality improvements added)
  - [ ] Approved Agent Workflows (if responsibilities changed)

### Step 5: Update Traceability Matrix
- [ ] `traceability.md`
  - [ ] Gate Status table (if gates changed)
  - [ ] Coverage Rules (if quality standards changed)
  - [ ] Traceability links (if required connections changed)

### Step 6: Update Templates (if applicable)
- [ ] `templates/requirements/REQ-template.md` — if Need Collector workflow changed
- [ ] `templates/user-stories/US-template.md` — if Product Owner workflow changed
- [ ] `templates/technical-requirements/TREQ-template.md` — if Architect workflow changed
- [ ] `templates/e2e/E2E-test-case-template.md` — if Tester workflow changed
- [ ] `templates/features/FEATURE-GUIDE-template.md` — if guide structure changed

## Key Sync Principles

| Principle | Action |
|-----------|--------|
| **Gate alignment** | If agent workflow mentions gates, ensure gate rules in global instructions match |
| **Responsibility clarity** | If agent responsibilities change, update all references in documentation |
| **Artifact expectations** | If agent's "Must produce" changes, update corresponding templates |
| **Quality standards** | If quality criteria change, reflect in global rules, prompt-initialisation, and traceability |
| **Traceability links** | If phase prerequisites change, update traceability matrix immediately |

## Final Consistency Check
After updates, verify:
1. **copilot-instructions.md** and **prompt-initialisation.txt** describe the same gate rules ✓
2. **readme.md** agent workflow descriptions match agent files ✓
3. **traceability.md** gate conditions match copilot-instructions.md ✓
4. All **templates** reflect the agent's current "Must produce" expectations ✓

---

## Quality Feedback Patterns
When agents are updated with new feedback/review responsibilities, ensure the pattern is consistent:

- **Need Collector Review** (before REQ finalization):
  - `.github/agents/need-collector.agent.md` — references Product Owner feedback requirement
  - `.github/agents/product-owner.agent.md` — has review-only mode for REQ feedback
  - `.github/copilot-instructions.md` — mentions this workflow in Execution Model

- **Product Owner → Developer Review** (before US finalization):
  - `.github/agents/product-owner.agent.md` — includes Developer Gherkin feedback step
  - `.github/agents/developer.agent.md` — has review-only Gherkin review mode
  - `.github/copilot-instructions.md` — mentions Gherkin feedback in Execution Model and Quality Rules
  - `prompt-initialisation.txt` — lists both agents' feedback responsibilities
  - `readme.md` — shows Developer role includes Gherkin validation (Gate 2)

---

**Last Updated:** 2026-05-06  
**Update Trigger:** When any `.github/agents/*.agent.md` file is modified
