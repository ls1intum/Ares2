# AGENTS.md

Repository conventions for automated agents and contributors working on Ares.

## Testing network access (incoming and outgoing)

A sandboxed test JVM must **never spin up its own server** (echo server, socket
listener, etc.) to test incoming or outgoing connections.

**Why:** Ares is the security boundary under test. Any server started inside the
same JVM as the student code is itself subject to the active security policy, so
Ares intercepts its thread, its `ServerSocket` bind and its `accept()`. A
failure then cannot be attributed to the behaviour being tested (the student's
client connection, or the student's own server) versus the test fixture failing
to start. A fixture must live **outside** the boundary it helps test.

**Rule:**

- Outgoing-connection tests connect to an **external echo server** at a
  configurable endpoint. The server runs as a separate process or CI service on
  the loopback at the agreed port (currently `25565`). The test exercises only
  the student's client behaviour.
- If the external echo server is not reachable, the test **skips** (JUnit
  `Assumptions.abort`) rather than fails. "Missing echo server" is an expected
  environmental condition locally; CI provides the server.
- An Ares `SecurityException` on an explicitly allowed connection is always a
  real failure and must propagate (never skipped).
- Do not hard-code a self-hosted listener as the connection counterpart. Port
  `25565` (Minecraft's default) collides easily; an external service avoids the
  in-JVM `BindException`/thread/lifecycle flakiness entirely.

`NetworkUser` follows this rule: it no longer starts an in-process echo server;
`connectLocallyAllowed` targets the external echo server and skips when it is
absent.
