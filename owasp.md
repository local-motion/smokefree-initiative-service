## Owasp top 10 - 2017

See https://www.owasp.org/index.php/Top_10-2017_Top_10

- [x] A1:2017-Injection
```
Local Motion is using event sourcing. The EventStore is managed by
the Axon Framework. Axon uses prepared statements in case of a Jdbc
Event Store implementation.

See also org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine

The projections / views are re-created on server startup and reside
in memory. No possibility for injections there.
```
- [x] A2:2017-Broken Authentication
```
All endpoints are by default secured using Micronaut's `SecurityFilter`. Whitelisting endpoints to
allow for anonymous access is a conscious and specific configuration change. Configuration
changes are recorded through GitHub commits.

During local development, the authentication mechanism works identical to test and production
environments (incl. the use of AWS Cognito, access and refresh tokens).

During each build, we have unit tests in place that start the application and test for `401` when no JWT token
was provided.
```
- [x] A3:2017-Sensitive Data Exposure
```
Local Motion uses AWS Cognito to maintain a user pool. JWT tokens are used
to communicate identity and claims. JWT tokens are verified using JWK (AWS
public key).

Local Motion does not store financial data.

Local Motion does not store PII. It may store nickname and email in the future. Using
CQRS, it is particularly easy to create 'projections' or 'view models' containing only
a subset of properties of a typical 'user domain object'. In other words, we'd have
a volunteer accessible projection that simply does not contain sensitive data, while
a completely separate admin projection could contain PII (e.g. email). Accidental leakage
of data is reduced this way.
```

- [x] A4:2017-XML External Entities (XXE)
```
Local Motion does not use XML.
```

- [x] A5:2017-Broken Access Control
```
Local Motion's data is all accessible to the public with the exception of a user's settings. A user's
settings is retrieved through one's JWT token identity.

In general, anything user specific is always fetched through one's JWT token identity.

We currently only have one role: 'volunteer'. All is visible to this role.

Roles are configured using scopes in AWS Cognito's User Pools. If we were to introduce
additional roles, then Controllers/Endpoints would require @Secured({"ROLE_ADMIN", "ROLE_X"})
annotations.

See for example https://github.com/micronaut-projects/micronaut-core/blob/master/security/src/test/groovy/io/micronaut/docs/security/securityRule/secured/ExampleController.java
```
- [x] A6:2017-Security Misconfiguration
```
See A2 above.
```
- [x] A7:2017-Cross-Site Scripting (XSS)
```
Local Motion uses React which prevents XSS injection by default. One specifically
has to enable HTML execution through providing the `dangerouslySetInnerHTML`
attribute.

When using above attribute, the rule is to use `@SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)` on
the respective command property. `@SafeHtml` is part of the Hibernate Validation (JSR-303) and uses `jsoup` to sanitize
the incoming HTML contents using a WhiteList approach.

The above handles incoming `Strings`; Java's type system (`enums`, `ints`, `doubles`, etc.)
automatically removes XSS related input for the other types.
```
- [x] A8:2017-Insecure Deserialization
```
Local Motion uses Jackson for deserialization of JSON payload. With regards to known
vulnerabilities around 'Global default typing', Local Motion doesn't

https://github.com/FasterXML/jackson-docs/wiki/JacksonPolymorphicDeserialization#111-security-risks-using-global-default-typing
```
- [x] A9:2017-Using Components with Known Vulnerabilities
```
Local-Motion uses the following tools to mitigate:
    1. Snyk bot to find Python, Java and Node/JS vulnerabilities. Runs daily.
    2. Renovate bot to prevent dependency decay. Our NPM and Docker dependencies are always up-to-date. Runs daily.
    3. GitHub's internal scanning mechanism for High-risk security vulnerabilities

```
- [ ] A10:2017-Insufficient Logging&Monitoring
```
Local Motion makes use of Event Sourcing. Our event store is our audit trail for every write
in the system.
```

---
## Secure Coding

### Stacktrace

- Throwable is mapped to return a generic JSON response using Micronaut's `@Error(global = true)`.
- GraphQL errors are stripped of any stacktraces using `GraphQLError.toSpecification()`


### Headers

Local Motion headers do not leak unnecessary or server related information.

Headers are:
```
HTTP/1.1 200 OK
Access-Control-Allow-Methods: POST
Access-Control-Allow-Headers: authorization
Access-Control-Allow-Headers: content-type
Access-Control-Max-Age: 1800
Access-Control-Allow-Origin: http://foo.com
Vary: Origin
Access-Control-Allow-Credentials: true
Date: Wed, 21 Nov 2018 07:52:40 GMT
connection: keep-alive
transfer-encoding: chunked
```


### Sanitize logs

`logback.xml` uses OWASP's conversion rule to strip newline characters from log messages:

```
<conversionRule
    conversionWord="crlf"
    converterClass="org.owasp.security.logging.mask.CRLFConverter"/>
```