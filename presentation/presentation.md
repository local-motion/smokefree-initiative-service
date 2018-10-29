# TODO: ADD IP NUMBER TO GOOGLE MAPS WHITELIST

# What improves productivity?

Accelerate: The Science of Lean Software and DevOps: Building and Scaling High Performing Technology Organizations Kindle Edition

Jez Humble, Gene Kim. Finally Scientific proof for Lean...

## Mean time of code-to-production 

### Technical

### Organizational

1. Autonomy
    1. Reduce dependencies
    1. Embed business owners as part of scrum team
    1. Team AWS Account
1. Purpose
    1. Control your own destiny. But also, Nowhere to hide.
    1. Measuring impact by having a clear measurable goal. 
        1. In our case, 80% or 600 out of 750 playgrounds smokefree in next year. How to do that?
    1. Understanding your hypotheses. Understanding your 'leap of faiths'. Validate early.
    

### Technical
1. fast dev cycle (distractions kick in at 15 seconds)
    1. fast startup, low memory
    1. fast and easy testing (axon, micronaut.io, locust.io)
    1. fast onboarding
1. autonomy, purpose, mastery
1. automate everything
    1. (idempotent) bootstrap scripts
    1. (idempotent) imports
    1. (idempotent) post, put & delete
1. Start features at the beginning. The consumer UI.

## Mean time to Failure
Explain.

## Mean time to Recovery
Explain.


# DEMO
- Rebuild cluster
- Explain Bootstrap and local dev
- Same local as production - Kube + Istio
    1. Service-to-Service authentication
    1. End-user authentication - Through e.g. JWT, or auth0, google auth, etc. (Next sprint) 
    1. Routing
    1. Circuit breaker
    1. Dark launches 
    1. Rate limiting 
    1. Whitelisting & blacklisting
    1. Canary releases
    1. Traffic shaping
    1. Distributed tracing
- Kube + Istio dashboards
- Renovate
- Micronaut.io
- jconsole
- Lombok
- CQRS/ES


### Interesting (Istio) sources

- https://developers.redhat.com/blog/2018/03/27/istio-circuit-breaker-when-failure-is-an-option/
- https://developers.redhat.com/blog/2018/04/24/istio-smart-canary-launch/
- https://github.com/redhat-developer-demos/istio-tutorial/tree/master/istiofiles
- https://istio.io/docs/concepts/security/
- http://guides.micronaut.io/micronaut-microservices-distributed-tracing-zipkin/guide/index.html#micronautandzipkin
