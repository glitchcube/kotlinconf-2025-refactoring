# Auction Service Project Guidelines

### Purpose
The application serves as an example of how to implement complex business logic in a microservices architecture, going beyond simple CRUD operations to handle various auction types and business rules.

### Development Guidelines
1. **Testing Requirements**
   - Implement both functional and unit tests
   - Test database transactions thoroughly
   - Include fuzz tests for Controllers
   - Verify proper error handling (e.g., 404 for invalid auction IDs)

2. **Code Organization**
   - Follow domain-driven design principles
   - Maintain clear separation of concerns
   - Use polymorphic objects for different auction types
   - Implement proper transaction boundaries

3. **Performance Considerations**
   - Use repository pattern for bid management
   - Consider database performance in bid handling
   - Implement proper transaction management

4. **Integration Patterns**
   - Use Transactional Outbox pattern for async operations
   - Implement proper service-to-service communication
   - Follow established security patterns


## Contributing
When contributing to this project, ensure:
1. All new features have appropriate test coverage
2. Business logic is properly encapsulated
3. Transaction boundaries are clearly defined
4. Performance implications are considered
5. Security aspects are addressed

## Junie
When you are refactoring, run all the tests in the project, not just those that you think are affected by a change.
