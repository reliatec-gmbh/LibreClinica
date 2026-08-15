# LibreClinica i18n resource bundles

This directory is the **canonical location** for LibreClinica's user-facing message bundles,
both the English originals and their translations. `LibreClinica-core.jar` is on the
classpath of every LibreClinica WAR, so bundles placed here are available to the whole
application.

## What a translation consists of

A complete translation is **nine files**:

```
admin  audit_events  exceptions  format  notes  page_messages  terms  words  workflow
```

`licensing.properties` is **not** part of a translation — see below.

The locale is only activated when **all nine** files are present for that language; see
"How a locale is selected" below.

## Strings that must not be translated

Some strings must keep their English text. They are marked with comments in the properties
files themselves — please read those comments before translating a file.

| what | where | why |
|------|-------|-----|
| The entire `licensing.properties` | marked `# DO NOT CHANGE ANY OF THE TEXT BELOW` | licence text, copyright and the ReliaTec GmbH trademark notice — legally binding wording |
| Role names | `terms.properties`, marked `#DO NOT CHANGE THE TEXT FOR …` (10 occurrences: `Data_Entry_Person`, `Investigator`, `Monitor`, `Study_Coordinator`, `Study_Director`, `site_*`) | role labels are tied to system semantics, audit trails and exports |
| `footer.license.*`, `footer.tooltip` | `words.properties` | legal notices |
| `oc_date_format_string` | `format.properties` | a date pattern that affects behaviour, not a label |

## `format.properties` is configuration, not translation

`format.properties` holds **locale-dependent behaviour**, not user-facing prose. Changing a
value here changes how the application parses and renders data. The file's own comments
describe each property; the constraints that matter most are:

- `date_format` is used to build validation messages and **must match `date_format_string`**
- `date_format_string` is the pattern the application uses to generate dates
- `date_regexp` is the regular expression used to **validate dates the user types in**
- the date part of `date_time_format_string` **must match `date_format_string`**

If you change the date format for your locale you must change all of these consistently,
otherwise date entry will break. Unless you have a specific reason, keep the date-related
values identical to the English file.

The remaining properties are genuinely locale-specific and may be adapted:

- `jscalendar_language_file` — the date-picker language file under
  `web/src/main/webapp/includes/new_cal/lang/`. **If you point this at a new file, add that
  file in the same change**, otherwise the script tag 404s and the picker silently stays in
  English.
- `image_dir` — a subdirectory under `web/src/main/webapp/images/`. Only `en` exists.
  **Do not change this unless you also add the corresponding image directory**, otherwise the
  referenced image will 404.
- `phone_format`, `phone_regexp` — phone number display format and input validation. Note
  that changing `phone_regexp` changes what users are allowed to enter.

## File naming and encoding

- Name translated files `<bundle>_<language>.properties`, e.g. `words_ja.properties`,
  `words_de.properties`.
- Keep the key set, key order and comments aligned with the English file. This makes it
  possible to diff a translation against the English original when new keys are added.
- **Characters outside ISO-Latin-1 should be written as `\uXXXX` escape sequences.**
  How `PropertyResourceBundle` decodes a `.properties` file depends on the JDK: up to Java 8 it
  is always ISO-8859-1, and since Java 9 (JEP 226) the file is read as UTF-8 with a fallback to
  ISO-8859-1 for bytes that are not valid UTF-8. Escaping is unambiguous under both rules, it
  matches the existing `_ja` files, and it survives editors and tools that re-save the file in a
  different encoding.
- **Do not write `${...}` in a translated value.** All `.properties` files under
  `src/main/resources` go through Maven resource filtering, so `${...}` would be substituted
  at build time. (The English `words.properties` uses `version_number=${pom.version}` — leave
  such values as the placeholder, do not hard-code the current version.)
- Message placeholders (`{0}`, `{1}`, …) and embedded HTML markup must be preserved.

## How a locale is selected at runtime

`LocaleResolver` walks the browser's `Accept-Language` list and picks the first language for
which **all nine bundles listed above resolve to that language** (`isQualifiedLocale`).
`licensing` is deliberately excluded from this check, because it is never translated.

Consequences:

- If even one of the nine files is missing for a language, that language is **not** selected
  and the whole UI falls back to English. A partial language pack never produces a
  half-translated screen.
- Within a selected language, **individual missing keys fall back to the English value**
  through the resource-bundle parent chain. A translation therefore does not have to be
  complete to be useful, and it does not have to be updated in lockstep with the English
  bundles.

## Checking a translation for completeness

Because the English originals and the translations live in the same directory, the key sets
can be compared directly, for example:

```sh
keys() { grep -vE '^[[:space:]]*(#|$)' "$1" | sed 's/[=:].*//' | tr -d '[:blank:]' | sort; }
for b in admin audit_events exceptions format notes page_messages terms words workflow; do
    echo "== $b"
    diff <(keys "$b.properties") <(keys "${b}_ja.properties") | head
done
```

Lines prefixed `<` are keys that exist in English but not in the translation (they will be
rendered in English); lines prefixed `>` are keys in the translation that no longer exist in
English (they can be removed).

## Installing a translation into a running instance

Translations shipped with LibreClinica are already inside `LibreClinica-core.jar` and need no
installation. To add or override a translation in a deployed instance without rebuilding,
place the `.properties` files in the exploded WAR at:

```
<CATALINA_HOME>/webapps/<app>/WEB-INF/classes/org/akaza/openclinica/i18n/
```

and restart Tomcat. `WEB-INF/classes` takes precedence over `WEB-INF/lib/*.jar`, so files
placed there override the bundled ones; missing keys still fall back to the English
originals in the core jar. The English originals themselves can be taken from
`LibreClinica-core.jar` or from this directory on GitHub.

## Contributing a translation

Submit a pull request against this directory. For Asian languages in particular, translations
are maintained through GitHub rather than through a translation platform, because such
platforms tend to mis-handle the encoding of these files.

Whoever contributes a language is expected to keep it up to date as English keys are added.
Untranslated keys are not an error — they render in English — so updates can be submitted
whenever convenient.

## The `ws` module keeps its own copy

`ws/src/main/resources/org/akaza/openclinica/i18n/` still contains an **independent and
intentionally frozen** copy of the English bundles, which has diverged considerably from the
files here (for `page_messages` alone: several hundred differing values). It is left
untouched because the SOAP web API is legacy and not covered by tests, so changing the
strings it returns would be an unverifiable behaviour change.

Bundles added or changed **here** do not affect the SOAP module, and vice versa. If you ever
work on the SOAP module, be aware of this divergence.
