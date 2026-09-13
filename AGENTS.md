# EchoBox — Project Rules for AI Agents

## Entry Point

This file is auto-loaded by Devin at every session start. It is the entry point to the full prompt system in `.devin/prompt/`. Read `.devin/prompt/map.md` before starting any task — it is the system map.

## Resource Discipline (mandatory for non-trivial tasks)

Before starting any non-trivial task:
1. Read `docs/toolset.md` intent-map (one table, task type → resources)
2. Identify the task type row; invoke every skill and sub-agent listed there
3. Read every rule listed for that task type (from `.devin/rules/`)
4. At task end: `code-reviewer` sub-agent on the final diff (non-negotiable)
5. Append learnings via `/ce-compound` if a durable lesson was learned

For phase implementations (any task completing a row in docs/plan.md), /ce-work is mandatory.

Skip this for single-line edits, pure Q&A, or reading files.

## Communication Style

Default to **caveman-lite** compression (lightly compressed, still readable, full technical accuracy). Use the `/caveman` skill for full / ultra / wenyan modes when tighter compression is needed.

## Quick Task Flow

For quick tasks, follow `.devin/prompt/quick.md` (commandments) and `.devin/prompt/rules.md` (scoping, verification, escalation). For phased work, follow `.devin/prompt/phase.md`.

## Key References

- `docs/toolset.md` — intent map (task type → skills, sub-agents, rules)
- `docs/plan.md` — phased plan and current status
- `docs/project.md` — project state and structure
- `docs/agent.md` — past implementations and decisions
- `docs/research.md` — technical research, ADRs, gotchas
