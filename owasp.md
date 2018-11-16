- [x] A1:2017-Injection
```
Local Motion is using event sourcing. The EventStore is managed by
the Axon Framework. Axon uses prepared statements in case of a Jdbc 
Event Store implementation. 

See also org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine

The projections / views are re-created on server startup and reside 
in memory. No possibility for injections there.
```
- [ ] A2:2017-Broken Authentication


- [ ] A3:2017-Sensitive Data Exposure
- [x] A4:2017-XML External Entities (XXE)
```
Local Motion does not use XML.
```

- [ ] A5:2017-Broken Access Control
- [ ] A6:2017-Security Misconfiguration
- [ ] A7:2017-Cross-Site Scripting (XSS)
- [ ] A8:2017-Insecure Deserialization
- [x] A9:2017-Using Components with Known Vulnerabilities
```
Local-Motion uses the following tools to mitigate:
    1. Snyk bot to find Python, Java and Node/JS vulnerabilities. Runs daily.
    2. Renovate bot to prevent dependency decay. Our NPM and Docker dependencies are always up-to-date. Runs daily.
    3. GitHub's internal scanning mechanism for High-risk security vulnerabilities

```
- [ ] A10:2017-Insufficient Logging&Monitoring
