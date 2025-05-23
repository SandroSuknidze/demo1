
# Task Tracker API

A Spring Boot application for managing projects and tasks with role-based access control.

## How to Run the Application

### Prerequisites
- Java 17 or higher
- Maven
- PostgreSQL database

### Database Setup
1. Create a PostgreSQL database named `task_tracker`
2. Configure the database connection in `application.properties` if needed:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/task_tracker
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   ```

### Building and Running
1. Clone the repository
2. Navigate to the project directory
3. Build the project:
   ```bash
   mvn clean install
   ```
4. Run the application:
   ```bash
   mvn spring-boot:run
   ```
5. The application will be available at `http://localhost:8080`

### API Documentation
The API documentation is available via Swagger UI:
- URL: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/api-docs`

### Postman Collection
A Postman collection is available for testing the API:
- Location: [docs/postman/Task Management API.postman_collection.json](docs/postman/Task%20Management%20API.postman_collection.json)

#### Using the Postman Collection
1. Import the collection into Postman using the "Import" button
2. Set up an environment with variables:
   - `baseUrl`: Your API base URL (e.g., `http://localhost:8080`)
   - `bearerToken`: Will be automatically set after login

#### Testing with Admin Access
For full access to all API features, you can log in with the following credentials:
- Email: `admin@example.com`
- Password: `password`

This admin account has unrestricted access to all endpoints and features.

### Predefined Data
The application automatically initializes the database with sample data on startup through the `DataInitializer` class. This happens only if the database is empty.

#### Predefined Users
| Email | Password | Role |
|-------|----------|------|
| admin@example.com | password | ADMIN |
| manager@example.com | password | MANAGER |
| manager2@example.com | password | MANAGER |
| user@example.com | password | USER |
| user2@example.com | password | USER |

#### Predefined Projects
1. **Website Redesign** - Owned by manager@example.com
2. **Mobile App Development** - Owned by manager2@example.com

#### Predefined Tasks
1. **Design Homepage** - Assigned to user@example.com (Website Redesign project)
2. **Implement User Authentication** - Assigned to user2@example.com (Website Redesign project)
3. **Create App Wireframes** - Assigned to user2@example.com (Mobile App Development project)
4. **Set Up CI/CD Pipeline** - Unassigned (Mobile App Development project)

Note: The database initialization happens because the application.properties has `spring.jpa.hibernate.ddl-auto=create`, which recreates the schema on each startup.

## Roles and Permissions Overview

The application has three user roles with different permission levels:

### ADMIN
- Full access to all resources in the system
- Can manage users, projects, and tasks
- Can view all data regardless of ownership

### MANAGER
- Can create and manage their own projects
- Can create and manage tasks within their projects
- Can assign tasks to users
- Cannot access or modify other managers' projects

### USER
- Can view and update only their assigned tasks
- Cannot create or manage projects
- Cannot create or assign tasks
- Limited view of the system based on their assignments

## API Endpoint Summary

### Authentication Endpoints
- `POST /api/v1/auth/login` - Authenticate a user and get JWT token
- `POST /api/v1/auth/register` - Register a new user (with USER role)

### User Endpoints
- `GET /api/v1/users` - Get all users (ADMIN only)
- `GET /api/v1/users/{id}` - Get user by ID (ADMIN only)
- `POST /api/v1/users` - Create a new user (ADMIN only)
- `DELETE /api/v1/users/{id}` - Delete a user (ADMIN only)
- `GET /api/v1/users/me` - Get current authenticated user

### Project Endpoints
- `GET /api/v1/projects` - Get all projects (ADMIN sees all, MANAGER sees own)
- `GET /api/v1/projects/{id}` - Get project by ID
- `POST /api/v1/projects` - Create a new project (ADMIN, MANAGER)
- `PUT /api/v1/projects/{id}` - Update a project (ADMIN, project owner)
- `DELETE /api/v1/projects/{id}` - Delete a project (ADMIN, project owner)
- `GET /api/v1/projects/{id}/tasks` - Get all tasks in a project

### Task Endpoints
- `GET /api/v1/tasks` - Get all tasks (filtered by user role)
- `GET /api/v1/tasks/{id}` - Get task by ID
- `GET /api/v1/tasks/user/{userId}` - Get tasks assigned to a user
- `POST /api/v1/tasks` - Create a new task (ADMIN, MANAGER)
- `PUT /api/v1/tasks/{id}` - Update a task (ADMIN, MANAGER)
- `PUT /api/v1/tasks/{id}/status` - Update task status (any authenticated user)
- `DELETE /api/v1/tasks/{id}` - Delete a task (ADMIN, MANAGER)
- `GET /api/v1/tasks/filter` - Get filtered tasks by status and/or priority

## How Authentication Works

The application uses JWT (JSON Web Token) based authentication:

1. **Registration and Login**:
   - Users register with email and password
   - Upon login, the server validates credentials and issues a JWT token

2. **JWT Token Structure**:
   - Contains user's email as the subject
   - Includes issuance and expiration timestamps
   - Signed with HMAC SHA-256 algorithm using a secret key

3. **Token Usage**:
   - Clients include the token in the Authorization header of requests:
     ```
     Authorization: Bearer <token>
     ```
   - The server validates the token for each protected endpoint

4. **Security Flow**:
   - `AuthTokenFilter` intercepts all HTTP requests
   - Extracts and validates the JWT token
   - Loads user details and sets up authentication context
   - Method-level security (`@PreAuthorize`) enforces role-based access

5. **Token Expiration**:
   - Tokens expire after the configured time (default: 24 hours)
   - Expired tokens are rejected, requiring the user to log in again