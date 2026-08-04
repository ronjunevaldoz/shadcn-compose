# Native JNI Audit Template

> Copy this file to `docs/audit_native_jni.md` in your project and fill in findings.
> The `jni-kotlin-pro` skill reads `docs/audit_native_jni.md` if it exists in the project.

## Scope

Date: _fill in_
Files audited: _list *-jni.cpp, *-wrapper.cpp files_
Method: Read all source files; cross-checked against reference library source.

---

## JNI Bridge Audit

### Memory safety findings

| File:Line | Issue | Severity | Status |
|---|---|---|---|
| _(fill in)_ | | | |

### Type mapping findings

| File:Line | Issue | Severity | Status |
|---|---|---|---|
| _(fill in)_ | | | |

### Symbol conflict findings

| Libraries | Conflicting symbols | Resolution | Status |
|---|---|---|---|
| _(fill in)_ | | | |

---

## Checklist

### Per JNI function

- [ ] Every `GetStringUTFChars` has a matching `ReleaseStringUTFChars` on all exit paths
- [ ] Every `Get*ArrayElements` uses `JNI_ABORT` for read-only access
- [ ] Native handle null-checked before cast; exception thrown if null
- [ ] `malloc`/`new` result null-checked; exception thrown on OOM
- [ ] No inference logic in JNI bridge — exactly one wrapper call per function
- [ ] Exception thrown immediately followed by `return` — no continued execution

### Per shared library

- [ ] `nm -D lib.so | grep 'U '` — all undefined symbols accounted for
- [ ] No symbol name collision with other `.so` files in the same JVM process
- [ ] SONAME symlinks present if runtime linker expects versioned names
- [ ] CMake target is enabled (`BUILD_X=ON`) in all build scripts and Dockerfiles
- [ ] COPY instruction for the built `.so` present in Dockerfile runtime stage

### Per algorithm port

- [ ] Checked library source for existing implementation before writing custom code
- [ ] If library has it: using library API or verbatim port with source citation
- [ ] All normalization constants verified against reference (not guessed)
- [ ] All default values verified against library header (not assumed to be 0 or 1)

---

## Known gaps (open items)

| ID | File:Line | Issue | Impact | Resolution plan |
|---|---|---|---|---|
| _(fill in)_ | | | | |
