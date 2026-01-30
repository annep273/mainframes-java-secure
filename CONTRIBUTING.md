# Contributing to Mainframes Java Application

Thank you for your interest in contributing! This document provides guidelines for contributing to this project.

## Code of Conduct

Be respectful, inclusive, and collaborative. We welcome contributions from everyone.

## How to Contribute

### Reporting Bugs

1. Check if the bug has already been reported in [Issues](https://github.com/yourusername/mainframes-java/issues)
2. Create a new issue with:
   - Clear title
   - Detailed description
   - Steps to reproduce
   - Expected vs actual behavior
   - Environment details (OS, Java version, OpenShift version)
   - Relevant logs or screenshots

### Suggesting Features

1. Open an issue with the `enhancement` label
2. Describe the feature and its use case
3. Explain why it would be beneficial
4. Consider implementation details

### Pull Requests

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes following our coding standards
4. Write or update tests
5. Update documentation
6. Commit with clear messages
7. Push to your fork
8. Open a Pull Request

## Development Setup

```bash
# Clone your fork
git clone https://github.com/yourusername/mainframes-java.git
cd mainframes-java

# Install dependencies
mvn clean install

# Run tests
mvn test

# Run application
mvn spring-boot:run
```

## Coding Standards

### Java Code Style

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable names
- Add JavaDoc comments for public methods
- Keep methods small and focused
- Maximum line length: 120 characters

### Testing

- Write unit tests for new features
- Maintain > 80% code coverage
- Use meaningful test names
- Follow AAA pattern (Arrange, Act, Assert)

```java
@Test
void testCreateMessage() {
    // Arrange
    MessageRequest request = MessageRequest.builder()
            .content("Test")
            .author("Author")
            .build();
    
    // Act
    Message result = messageService.createMessage(request);
    
    // Assert
    assertNotNull(result.getId());
    assertEquals("Test", result.getContent());
}
```

### Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add user authentication
fix: resolve memory leak in message service
docs: update deployment guide
test: add tests for health controller
chore: update dependencies
```

## Pull Request Process

1. Update README.md with any new features
2. Update documentation in `docs/`
3. Add tests for new functionality
4. Ensure all tests pass locally
5. Update CHANGELOG.md if applicable
6. Request review from maintainers

## Review Process

- All PRs require at least one approval
- CI checks must pass
- Code coverage must not decrease
- Documentation must be updated

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.

## Questions?

Open an issue or contact the maintainers.

Thank you for contributing! 🎉
