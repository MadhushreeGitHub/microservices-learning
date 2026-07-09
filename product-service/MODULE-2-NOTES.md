# If tomorrow my company asked me to split our monolith into microservices, 
# here are the 5 decomposition mistakes I'd make sure to avoid.

- Avoid using a shared database across microservices. This causes deployment coupling (any schema change forces all services to redeploy in sync), hidden data ownership conflicts, and reduces each service to a fake microservice. Instead, each service owns its own database exclusively. If services need each other's data, they exchange it via APIs or async events — never by querying each other's tables. 
- The duplicated data and eventual consistency are the deliberate price of independence.
- I will decompose service based on business capabilities rather than actions or technical layers. 
  This ensures that each microservice is cohesive and aligned with the business domain, making it easier to maintain and evolve.
- I will avoid to store Password or sensitive information in database as it is instead I will usr bcrypt/argon2 hashing algorithms to securely  
  store passwords and sensitive data. This helps protect user information in case of a data breach.
- I will avoid creating microservices that are too fine-grained, as this can lead to increased inter-service communication overhead and complexity. 
  Instead, I will aim for a balance between granularity and maintainability, ensuring that each microservice has a clear purpose and manageable scope.
- I will avoid tightly coupling microservices together, as this can lead to cascading failures and make the system more difficult to manage and scale.