# Static Analysis Report — Online-Grocery-Ordering-System-323657 (FreshCoN)

This report summarizes build/configuration issues, security risks, code smells, and runtime risks found via static inspection of the Java Servlet/JSP codebase.

## Quick project facts
- Tech: Java Servlet/JSP app with Eclipse project metadata.
- Java sources: ~70 files (`src/main/java`).
- JSP pages: ~28 files (`src/main/webapp`).
- Build tooling: no `pom.xml`, `build.gradle`, or `build.xml` detected; uses `.project`/`.classpath`.
- Server: `.classpath` references Apache Tomcat v9 runtime container.

## High-risk findings (P0)

### P0.1 SQL Injection via string-concatenated SQL
**Evidence**
- `src/main/java/freshco/Model/AdminDBUtil.java` uses string concatenation for authentication:
  - `SELECT * FROM admin WHERE email='...'+email+'...' AND password='...'+password+'...'`
- `src/main/java/freshco/Model/CustomerDBUtil.java` same pattern for login and CRUD.
- `src/main/java/freshco/Model/ProductDBUtil.java` uses concatenated SQL for insert/update/delete and for `Product_Sale` insert.

**Risk**
Attackers can inject SQL through request parameters and bypass authentication, dump/modify data, and potentially achieve full DB compromise.

**Recommendation**
Use `PreparedStatement` with parameters for all queries. Remove any dynamic string building for values.

---

### P0.2 Hard-coded DB credentials in source
**Evidence**
- `src/main/java/freshco/Model/webDB.java`:
  - URL: `jdbc:mysql://localhost:3306/freshco`
  - User: `root`
  - Password: `Jingles@1`

**Risk**
Credential leakage (repo access, logs), insecure defaults, not deployable to other environments.

**Recommendation**
Externalize configuration:
- Prefer container-managed `DataSource` (JNDI) for Tomcat deployments.
- Alternatively, load from a properties file that is not committed, or environment variables.

---

### P0.3 Plaintext password storage + session password leakage
**Evidence**
- DB utils validate `password='...plaintext...'` and insert/update plaintext passwords.
- `src/main/java/freshco/Control/UserValidation.java` stores password in session:
  - `sess.setAttribute("password", isX.getPassword());`

**Risk**
Credential compromise, regulatory issues, higher blast radius if sessions leak.

**Recommendation**
- Store hashed passwords (BCrypt/Argon2) in the DB.
- Compare hashes on login.
- Never store password values in session.

---

### P0.4 Missing/unclear access control enforcement for sensitive endpoints
**Evidence**
- `src/main/webapp/WEB-INF/web.xml` maps numerous admin/employee/customer modifying endpoints.
- No `<security-constraint>` blocks detected in inspected `web.xml`.

**Risk**
Broken access control / IDOR: users can access privileged endpoints by guessing URLs.

**Recommendation**
Introduce an authentication/authorization mechanism:
- Add a servlet `Filter` that enforces login and role checks (`userType` and `ID`) for protected URL patterns.
- Consider declarative security constraints if supported by your deployment model.

---

### P0.5 Insecure file upload handling
**Evidence**
- `src/main/java/freshco/Control/ImageUploadServlet.java`
  - Uses `part.getSubmittedFileName()` directly.
  - Writes into web-accessible `/image` directory via `getRealPath("/image")`.
  - No file type validation, no size limits, no filename sanitization.

**Risk**
- Uploading malicious content into webroot (potential RCE depending on server config).
- Overwrite/poisoning attacks, XSS via uploaded SVG/HTML, path manipulation.

**Recommendation**
- Store uploads outside the deployed webroot.
- Generate random filenames; do not trust user filenames.
- Validate extension + MIME type; enforce size limits.
- Consider antivirus scanning for uploads in real deployments.

---

## Medium/high runtime risks (P1)

### P1.1 Shared static Connection (thread safety + resilience)
**Evidence**
- `src/main/java/freshco/Model/webDB.java` holds a single static `Connection connection;` reused.

**Risk**
- Multi-threaded servlet environment can concurrently use the same connection unsafely.
- Connection can become stale/closed; application fails under load.

**Recommendation**
Use a pooled `DataSource` (Tomcat pool/HikariCP) and acquire connections per operation via try-with-resources.

---

### P1.2 Resource management (potential leaks)
**Evidence**
- `webDB.executeSearch/executeIUD` uses `connection.createStatement()` without try-with-resources.
- Some callers close `ResultSet`, but statements are not closed.

**Risk**
Connection/statement leaks cause slow degradation and outages.

**Recommendation**
Use try-with-resources for `Connection`, `PreparedStatement`, and `ResultSet`.

---

### P1.3 Missing transaction boundaries for order processing
**Evidence**
- `src/main/java/freshco/Control/OrderFacadeServlet.java` calls:
  - `PaymentDBUtil.createPayment(...)`
  - `SaleDBUtil.createSale(...)`
  - `ProductDBUtil.createProductSale(...)`
- No explicit transaction management shown.

**Risk**
Partial writes: payment recorded without sale/items, inventory updated without sale, etc.

**Recommendation**
Wrap order placement steps in a single DB transaction with rollback on failure.

---

### P1.4 Session attribute naming inconsistency
**Evidence**
- `OrderFacadeServlet` reads `cartItems` from session but writes back under `cartProducts`:
  - read: `session.getAttribute("cartItems")`
  - write: `session.setAttribute("cartProducts", cartProducts)`

**Risk**
Cart not cleared properly; stale cart remains for user.

**Recommendation**
Use consistent attribute keys throughout (prefer one: `cartItems`).

---

## Build / dependency risks (P1)

### P1.5 Non-reproducible builds; Eclipse/Tomcat coupling
**Evidence**
- No Maven/Gradle/Ant build file found.
- `.classpath` ties compilation to a Tomcat v9 container and local Eclipse setup.

**Risk**
Hard to build in CI/CD, to upgrade dependencies, or to deploy consistently.

**Recommendation**
Add a proper build:
- Maven `pom.xml` (recommended) producing a WAR.
- Declare servlet API as `provided`.
- Manage dependencies via Maven Central (avoid committing container APIs into `WEB-INF/lib`).

---

### P1.6 Servlet API JARs are bundled in `WEB-INF/lib`
**Evidence**
- `src/main/webapp/WEB-INF/lib/javax.servlet-3.1.jar`
- `src/main/webapp/WEB-INF/lib/servlet-api.jar`

**Risk**
Classpath conflicts because the container already provides these.

**Recommendation**
Remove servlet API jars from the deployed WAR and treat them as container-provided.

---

## Code smells / maintainability (P2)

### P2.1 JSP scriptlets and inline Java
**Evidence**
- `src/main/webapp/index.jsp` uses many `<% ... %>` blocks and `<%= ... %>` expressions.

**Risk**
Hard to maintain; XSS risk if output is not escaped properly.

**Recommendation**
Move logic to servlets; use JSTL/EL and ensure escaping.

---

### P2.2 Debug printing in production paths
**Evidence**
- `System.out.println` in `ProductDBUtil.createProductSale`
- `ImageUploadServlet` prints upload status

**Recommendation**
Use a logging framework (SLF4J) and avoid logging sensitive info.

---

## Prioritized fix list
### P0 (security blockers)
1. Parameterize all SQL with `PreparedStatement` (eliminate SQL injection).
2. Externalize DB config (remove hard-coded credentials from `webDB.java`).
3. Hash passwords in DB; never store plaintext or in session.
4. Enforce access control (Filter + role checks; consider security-constraints).
5. Harden file upload (sanitize, validate, store outside webroot, enforce size/type limits).

### P1 (stability/correctness/build hygiene)
6. Replace static shared Connection with pooled DataSource + try-with-resources.
7. Add transaction management for order placement flows.
8. Fix cart session attribute inconsistency in `OrderFacadeServlet`.
9. Provide a reproducible build system (Maven/Gradle) and remove servlet API jars from WAR.

### P2 (maintainability)
10. Reduce JSP scriptlets; use JSTL/EL + output escaping.
11. Replace `System.out.println` with proper logging.

---
Generated by static inspection (no dynamic execution).
