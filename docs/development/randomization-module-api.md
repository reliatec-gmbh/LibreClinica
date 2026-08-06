# External Randomization Module API

LibreClinica can **delegate subject randomization to an external module**
("module manager") instead of allocating subjects itself. The feature is
**opt-in**: it is inert unless a study is configured for it and the
`moduleManager` property is set.

LibreClinica contains **no randomization logic of its own**. It collects the
allocation factors, calls the endpoints described below, records the returned
allocation code in the CRF, and leaves the choice of method (simple, permuted
block, stratified, minimization, …), the blinding strategy, and the meaning of
the returned code entirely to the module.

This page is the reference for the **mandatory REST endpoints a module must
implement** to interoperate with LibreClinica, so that integrators do not have
to reverse-engineer them from the code.

## Configuration

| Property (`datainfo.properties`) | Meaning |
|---|---|
| `moduleManager` | Base URL of the external module. Leave empty to disable randomization; the application then makes no calls and starts normally. |
| `sysURL` / `sysURL.base` | Used to build the OpenRosa instance URL passed to the module. When `sysURL.base` is unset it is derived from `sysURL` by stripping `MainMenu`. |

## Two base URLs

There are **two distinct bases** and they are not the same host in general:

1. **`moduleManager`** — used for the per-study *configuration* call
   (`/app/rest/oc/se_randomizations`).
2. **`config.url`** — the value returned in the `url` field of the
   configuration response; used for the per-subject *randomisation* calls
   (`/api/...`). These calls use **HTTP Basic authentication** with the
   `username` / `password` from the configuration response.

## Endpoints

**Studies are registered in the module, not from LibreClinica.** LibreClinica
never creates or registers a study in the module and calls no registration
endpoint: which studies may be randomised, by which method, with which arms,
whether the trial is blinded, and who is allowed to see an allocation are
decisions that need authorisation and clinical design, and they are made in the
module's own administration. LibreClinica only reads the resulting
configuration (endpoint 1) and asks for allocations (endpoints 2 to 4).

### 1. Get study configuration — `GET {moduleManager}/app/rest/oc/se_randomizations`

Query parameters:

| Param | Value |
|---|---|
| `studyoid` | Study OID |
| `instanceurl` | `{sysURL.base}rest2/openrosa/{studyOid}` |

Returns a `SeRandomizationDTO` (JSON). Relevant fields LibreClinica reads:

| Field | Use |
|---|---|
| `url` | Base URL for the `/api/...` randomisation calls |
| `username`, `password` | Basic-auth credentials for the `/api/...` calls |
| `status` | Must be `ACTIVE` for randomisation to proceed |
| `studyOid` | Must be non-null; a null `studyOid` is treated as "no configuration" |

The configuration is cached per study.

### 2. Look up an existing allocation — `GET {config.url}/api/randomisation`

Auth: Basic. Query parameter:

| Param | Value |
|---|---|
| `identifier` | Study-subject OID |

If the subject is already randomised, return a JSON object containing a
`code` field with the allocation code. If not, return a response that is not a
`code`-bearing JSON object (e.g. `404`); LibreClinica then proceeds to
register the site and randomise.

### 3. Register / update a site — `POST {config.url}/api/sites`

Auth: Basic. Content type: `application/x-www-form-urlencoded`.

| Param | Value |
|---|---|
| `siteIdentifier` | Study OID |
| `name` | Study name |
| `timezone` | Server default timezone (IANA id) |

### 4. Randomise a subject — `POST {config.url}/api/randomise`

Auth: Basic. Content type: `application/x-www-form-urlencoded`.

| Param | Value |
|---|---|
| `identifier` | Study-subject OID |
| `siteIdentifier` | Study OID |
| `user` | Username of the operator who triggered the action |
| `question1` … `questionN` | Allocation factors, in the order the rule defines them |

Allocation factors are resolved as follows: a factor expression starting with
`SS.` is taken from subject attributes (`SS.SEX` → `Male`/`Female`,
`SS.BIRTHDATE`, `SS.STUDYGROUPCLASSLIST["<class>"]`); any other expression is
resolved from the corresponding eCRF item value. The response must be a JSON
object containing the allocation `code`, which LibreClinica writes back into
the designated CRF item.

## Activation gate

A `RANDOMIZE` rule action runs the randomisation calls only when **all** of the
following hold (see `RandomizeActionProcessor.mayProceed`):

- LibreClinica study parameter `randomization` = `enabled`
- parent study status = `available`
- site status = `available`
- configuration `status` (from endpoint 1) = `ACTIVE`

If any condition fails, LibreClinica records nothing and makes no `/api/...`
calls.

## Idempotency and double-allocation safety

LibreClinica does **not** keep a per-subject guard against re-running a
`RANDOMIZE` action. Unlike other rule actions (INSERT, EMAIL, …) it does not use
the `RuleActionRunLog`, so the action is re-evaluated whenever its target CRF is
saved again — for example an administrative edit of an already-entered form, or
a re-import of existing subject data. The activation gate above checks only
study/site status and the configuration `status`; it does not consider whether
the subject has already been allocated. Re-firing on the same subject is
therefore a legitimate, expected flow.

Because of this, **preventing double allocation is the module's responsibility**,
keyed on the subject identifier (plus study). A conforming module MUST:

- **`GET /api/randomisation`** — look up an existing allocation by `identifier`
  and, if one exists, return the **same** allocation `code` (HTTP 200). If the
  subject is not yet allocated, return a response that carries **no `code`**
  (e.g. HTTP 404). Do **not** return `200` with an empty / `{}` body: LibreClinica
  would otherwise write an empty code and never randomise.
- **`POST /api/randomise`** — be **idempotent**. If the same `identifier` (within
  the same study) is already allocated, return the existing `code` instead of
  creating a new allocation. This covers the case where the lookup is skipped, or
  a race occurs between the lookup and the randomise call.

The allocation identity key is the **subject identifier (subject OID) + study**.
Re-firing on the same subject then returns the same code, and LibreClinica
records it idempotently.

Note: this protects only against re-allocation of the **same** subject OID. It
cannot protect against the **same person enrolled under a different subject
OID** (a duplicate-enrollment data-entry error), which is outside the
randomization layer and must be handled by subject-registration controls.

## Notes

- **LibreClinica does not interpret the returned `code`** (arm name, kit
  number, or masked code). The method, the blinding, and the returned content
  are the module's responsibility. For a blinded trial the module can return
  only the allocation code; the CRF design (item placement, display settings,
  role permissions) controls who can see it. Keep the module's returned content
  and the CRF-side disclosure design consistent.
- **Graceful degradation:** if `moduleManager` is unset, or the configuration
  is missing or not `ACTIVE`, no calls are made and the application runs
  normally.
