# 🎫 Ticket Management System - Complete Implementation Guide

## Project Overview
A comprehensive ticket management system built with **Spring Boot** that provides functionality for both **Users** and **Administrators** to manage support tickets efficiently.

---

## ✨ Key Features

### **User Features**
- Dashboard with ticket statistics
- Create new support tickets
- View all personal tickets
- Track ticket status (Open, In Progress, Resolved, Closed)
- Add comments to tickets
- View ticket details and history
- User profile management

### **Admin Features**
- Comprehensive admin dashboard
- View all system tickets
- Assign tickets to support staff
- Update ticket status
- User management (create, edit, deactivate users)
- Department management
- Generate reports and analytics
- System settings and configuration

---

## 📂 Project Structure

```
demo/
├── src/main/java/com/example/tckt/demo/
│   ├── entity/                 # Database entities
│   │   ├── User.java
│   │   ├── Ticket.java
│   │   ├── Department.java
│   │   └── Comment.java
│   ├── dto/                    # Data Transfer Objects
│   │   ├── UserDTO.java
│   │   ├── TicketDTO.java
│   │   ├── CommentDTO.java
│   │   └── DepartmentDTO.java
│   ├── repository/             # JPA Repositories
│   │   ├── UserRepository.java
│   │   ├── TicketRepository.java
│   │   ├── DepartmentRepository.java
│   │   └── CommentRepository.java
│   ├── service/                # Business Logic
│   │   ├── TicketService.java
│   │   ├── UserService.java
│   │   ├── DepartmentService.java
│   │   └── CommentService.java
│   ├── controller/             # Controllers
│   │   ├── UserTicketController.java
│   │   ├── AdminController.java
│   │   ├── TicketRestController.java
│   │   ├── CommentController.java
│   │   └── HomeController.java
│   ├── exception/              # Exception Handling
│   │   ├── TicketNotFoundException.java
│   │   └── GlobalExceptionHandler.java
│   ├── security/               # Security Configuration
│   ├── config/                 # Application Configuration
│   └── DemoApplication.java    # Main Application
├── resources/
│   ├── application.properties  # Application Configuration
│   ├── templates/
│   │   ├── user/               # User interface templates
│   │   ├── admin/              # Admin interface templates
│   │   ├── auth/               # Authentication templates
│   │   └── layout/             # Shared layout templates
│   └── static/
│       ├── css/                # Stylesheets
│       └── js/                 # JavaScript files
└── pom.xml                     # Maven dependencies
```

---

## 🚀 Step-by-Step Setup

### **Step 1: Prerequisites**
Ensure you have the following installed:
- Java 11 or higher (JDK)
- MySQL Server 5.7+
- Maven 3.6+
- VS Code or any Java IDE

### **Step 2: Create MySQL Database**
```sql
CREATE DATABASE ticket_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### **Step 3: Update Database Configuration**
Open `src/main/resources/application.properties` and configure:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ticket_db
spring.datasource.username=root
spring.datasource.password=your_password
```

### **Step 4: Add Dependencies to pom.xml**
Add the following dependencies (already included in application.properties guide):
- Spring Boot Web
- Spring Data JPA
- MySQL Connector
- Thymeleaf
- Spring Security
- Lombok

### **Step 5: Run the Application**
```bash
mvn spring-boot:run
```

The application will start at `http://localhost:8080`

### **Step 6: Access the Application**
- **User Login**: http://localhost:8080/login
- **Admin Panel**: http://localhost:8080/admin/dashboard
- **User Dashboard**: http://localhost:8080/user/dashboard

---

## 🗄️ Database Schema

### **Users Table**
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    role ENUM('ADMIN', 'USER') NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    department_id BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### **Tickets Table**
```sql
CREATE TABLE tickets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ticket_number VARCHAR(50) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status ENUM('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'),
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'),
    created_by_id BIGINT NOT NULL,
    assigned_to_id BIGINT,
    department_id BIGINT,
    resolved_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### **Comments Table**
```sql
CREATE TABLE comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content TEXT NOT NULL,
    ticket_id BIGINT NOT NULL,
    created_by_id BIGINT NOT NULL,
    created_at TIMESTAMP
);
```

### **Departments Table**
```sql
CREATE TABLE departments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP
);
```

---

## 🔌 API Endpoints

### **Ticket Management (User)**
```
GET    /api/tickets                 # Get all tickets (paginated)
GET    /api/tickets/{id}           # Get ticket by ID
POST   /api/tickets                 # Create new ticket
PUT    /api/tickets/{id}/status    # Update ticket status
PUT    /api/tickets/{id}/assign    # Assign ticket to user
DELETE /api/tickets/{id}           # Delete ticket
```

### **Comments**
```
POST   /api/comments/add           # Add comment to ticket
GET    /api/comments/ticket/{id}   # Get comments for ticket
DELETE /api/comments/{id}          # Delete comment
```

### **User Pages**
```
GET    /user/dashboard             # User dashboard
GET    /user/create-ticket         # Create ticket form
GET    /user/my-tickets            # List user tickets
GET    /user/ticket/{id}           # View ticket details
GET    /user/profile               # User profile
```

### **Admin Pages**
```
GET    /admin/dashboard            # Admin dashboard
GET    /admin/all-tickets          # All tickets
GET    /admin/users                # User management
GET    /admin/departments          # Department management
GET    /admin/reports              # Reports & analytics
GET    /admin/settings             # Settings
```

---

## 🎨 User Interface

### **User Interface Features**
- Modern, responsive design with Bootstrap 5
- Intuitive navigation menu
- Dashboard with ticket statistics
- Ticket creation form with validation
- Ticket list view with filtering
- Detailed ticket view with comments
- Profile management page

### **Admin Interface Features**
- Dark-themed admin panel
- Dashboard with KPIs
- Advanced ticket management
- User administration
- Department management
- Analytics and reports
- System settings

---

## 🔐 Security Considerations

1. **Password Encryption**: All passwords are hashed using bcrypt
2. **CORS Configuration**: Configure according to your frontend URL
3. **SQL Injection Prevention**: Use JPA parameterized queries
4. **XSS Prevention**: Thymeleaf auto-escaping enabled
5. **CSRF Protection**: Enable CSRF tokens in forms

### **Recommended Security Enhancements**
```java
// Add to SecurityConfig.java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http
        .authorizeRequests()
            .antMatchers("/admin/**").hasRole("ADMIN")
            .antMatchers("/user/**").hasRole("USER")
            .antMatchers("/login", "/register").permitAll()
            .anyRequest().authenticated()
        .and()
        .formLogin()
            .loginPage("/login")
            .defaultSuccessUrl("/user/dashboard")
        .and()
        .logout()
            .logoutSuccessUrl("/login");
}
```

---

## 📊 Main Workflows

### **User Ticket Creation Workflow**
1. User logs in → Dashboard
2. Click "Create Ticket" → Fill form
3. Submit → Ticket created with status "OPEN"
4. Ticket assigned ticket number (TKT-XXXXXX)
5. Can add comments and track status

### **Admin Ticket Assignment Workflow**
1. Admin views all tickets → All Tickets page
2. Filter/search for unassigned tickets
3. Click "Assign" → Select support staff
4. Status changes to "IN_PROGRESS"
5. Support staff can update status as they work

### **Ticket Resolution Workflow**
1. Ticket starts as "OPEN"
2. Admin assigns to support staff
3. Status changes to "IN_PROGRESS"
4. Support staff adds comments with updates
5. Status changed to "RESOLVED"
6. Finally marked as "CLOSED"

---

## 🛠️ Technology Stack

| Component | Technology |
|-----------|-----------|
| **Backend** | Spring Boot 2.7+ |
| **Database** | MySQL 5.7+ |
| **ORM** | Hibernate/JPA |
| **Frontend** | HTML5, CSS3, JavaScript |
| **UI Framework** | Bootstrap 5 |
| **Template Engine** | Thymeleaf |
| **Build Tool** | Maven |
| **Java Version** | 11+ |

---

## 📝 Configuration Examples

### **Email Configuration** (Optional)
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### **Logging Configuration**
```properties
logging.level.root=INFO
logging.level.com.example.tckt.demo=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

---

## 🧪 Testing

### **Manual Testing Checklist**
- [ ] User registration and login
- [ ] Create ticket with all fields
- [ ] Update ticket status
- [ ] Assign ticket to user
- [ ] Add comments to ticket
- [ ] View ticket history
- [ ] Admin user management
- [ ] Department management
- [ ] Search and filter functionality

---

## 📚 Future Enhancements

1. **Email Notifications** - Notify users of ticket updates
2. **File Attachments** - Allow file uploads to tickets
3. **SLA Tracking** - Track Service Level Agreements
4. **Knowledge Base** - Self-service ticket solutions
5. **Mobile App** - React/Flutter mobile application
6. **Analytics Dashboard** - Advanced reporting features
7. **Integration APIs** - Connect with external systems
8. **Multi-language Support** - i18n implementation

---

## 🐛 Troubleshooting

### **Common Issues**

**Issue**: "Connection refused to MySQL"
```
Solution: Ensure MySQL is running and connection string is correct
mysql -u root -p
```

**Issue**: "Port 8080 already in use"
```
Solution: Change port in application.properties
server.port=8081
```

**Issue**: "Thymeleaf template not found"
```
Solution: Verify templates are in /resources/templates/ directory
```

---

## 📞 Support

For issues or questions, please create an issue in the project repository or contact the development team.

---

## 📄 License

This project is licensed under the MIT License.

---

**Happy Coding! 🚀**
