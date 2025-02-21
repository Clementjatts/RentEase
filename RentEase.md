You are tasked with coding a minimalist mobile and web application for property advertisement as per the CSYM030 assignment requirements. The app must meet basic system requirements (Admin, Landlord, User privileges) using Kotlin for the mobile platform, PHP for the backend, and Docker for server management. The design should be simple, functional, and completed within a tight timeline using an agile methodology.

- **Current Date**: February 20, 2025
- **Milestone**: May 26, 2025 (assume this is the submission deadline)
- **Tools**: Kotlin (Android Studio), PHP (backend), Docker (server), SQLite (database)
- **Goal**: Deliver a working app with minimal UI and core functionality, avoiding unnecessary complexity.

---

## Project Requirements

### Basic System Requirements

1. **Admin Privileges**:
   - CRUD operations for landlords (Create, Read, Update, Delete).
2. **Landlord Privileges**:
   - CRUD operations for properties (Create, Read, Update, Delete).
3. **User Privileges**:
   - View properties.
   - Request to contact a landlord about a property.

### Deliverables

1. **Technical Report** (Word doc):
   - Username/password for accounts (e.g., Admin: admin/pass).
   - Feature table (e.g., "Create Landlord: Full").
   - Brief code explanation for developers.
   - Screenshots of key features.
   - Blackbox test logs (tables/screenshots) + bug list.
2. **Source Code**:
   - ZIP archive: Kotlin (.java, .xml), PHP files.
   - Word doc: Full commented code listing.
3. **Video Demo**: 10-min Kaltura URL showing all features.

---

## Development Plan (Agile - 3 Sprints)

### Sprint 1: Backend Setup (Feb 20 - Mar 5, 2025)

**Goal**: Build a minimal backend and database.
**Tasks**:

1. **Server Setup**:
   - Install Docker locally.
   - Test with a `hello.php` script (`echo "Hello World";`).
2. **Database (SQLite)**:
   - Create tables:
     - `admins` (id, username, password)
     - `landlords` (id, name, contact, admin_id)
     - `properties` (id, title, description, landlord_id)
     - `user_requests` (id, user_id, property_id, message)
   - Use Room in Kotlin for SQLite integration.
3. **PHP Backend**:
   - Write RESTful APIs (save as `.php` files):
     - `/createLandlord.php` (POST: name, contact)
     - `/readLandlord.php` (GET: id)
     - `/updateLandlord.php` (PUT: id, name, contact)
     - `/deleteLandlord.php` (DELETE: id)
     - `/createProperty.php` (POST: title, description, landlord_id)
     - `/readProperties.php` (GET: landlord_id or all)
     - `/updateProperty.php` (PUT: id, title, description)
     - `/deleteProperty.php` (DELETE: id)
     - `/viewProperties.php` (GET: all properties)
     - `/requestContact.php` (POST: user_id, property_id, message)
   - Return JSON responses (e.g., `{"status": "success"}`).
   - No complex authentication—just a basic admin check (e.g., `if ($username == "admin")`).

**Deliverables**: Working APIs, SQLite schema (provide `.sql` file or Room code).

---

### Sprint 2: Mobile App Core (Mar 6 - Mar 20, 2025)

**Goal**: Code a minimal Kotlin app with basic UI and functionality.
**Tasks**:

1. **Project Setup**:
   - Create Android Studio project (Kotlin).
   - Add dependencies in `build.gradle`:
     - Retrofit (API calls)
     - Gson Converter
     - Room
2. **UI Design**:
   - Single Activity (`MainActivity.kt`) with XML layouts:
     - `activity_main.xml`: Dropdown (Admin/Landlord/User) + Login button.
     - `admin_screen.xml`: ListView (landlords) + CRUD buttons.
     - `landlord_screen.xml`: ListView (properties) + CRUD buttons.
     - `user_screen.xml`: RecyclerView (properties) + Contact button.
   - Use default components, no custom styles.
3. **Functionality**:
   - Define Retrofit API interface (`ApiService.kt`):
   - Connect to backend APIs (e.g., `retrofit.create(ApiService::class.java)`).
   - Implement CRUD for Admin/Landlord, property viewing/request for User.
   - Store data locally with Room for offline access.

**Deliverables**: Functional app (provide `.apk` or screenshots).

---

### Sprint 3: Web App, Testing, Documentation (Mar 21 - Apr 4, 2025)

**Goal**: Add a web app, test, and document everything.
**Tasks**:

1. **Web App**:
   - Create PHP/HTML files (e.g., `index.php`, `admin.php`):
     - Reuse backend APIs (e.g., `file_get_contents("http://localhost/viewProperties.php")`).
     - Simple HTML tables for CRUD, forms for input.
     - CSS for basic alignment (no frameworks).
2. **Testing**:
   - Perform blackbox tests (e.g., “Input: Create Landlord, Expected: Success”).
   - Log in a table (provide `.docx` snippet):
     ```
     | Test Case         | Input             | Expected Output | Actual Output | Pass/Fail |
     |-------------------|-------------------|-----------------|---------------|-----------|
     | Create Landlord   | Name: John        | Success         | Success       | Pass      |
     ```
   - List bugs (e.g., “No input validation”).
3. **Documentation**:
   - **Report** (`Report.docx`):
     - Credentials: Admin (admin/pass), Landlord (landlord/pass), User (user/pass).
     - Feature table (e.g., “View Properties: Full”).
     - Code explanation (e.g., “Retrofit handles API calls”).
     - Screenshots (capture UI).
     - Test logs + bugs.
   - **Source Code**:
     - ZIP: All `.kt`, `.xml`, `.php` files.
     - `FullSourceCodeListing.docx`: Paste code with comments.
   - **Video**: Record 10-min demo (screen + voice), upload to Kaltura, share URL.

**Deliverables**: Web app, report, code ZIP, video URL.

---

## Guidelines for AI Agent

- **Minimalism**: Use default UI components, avoid complex logic (e.g., no Fragments unless requested later).
- **Code Style**: Add minimal comments, use consistent indentation.
- **Output**: Provide code snippets, files, or instructions for each sprint.
- **Timeline**: Complete Sprint 1 by Mar 5, Sprint 2 by Mar 20, Sprint 3 by Apr 4, 2025.
- **Questions**: If unclear, ask me (e.g., “Do you want Fragments?”).

---

## Action Plan

1. **Start Now**: Begin Sprint 1 (Docker setup, SQLite, PHP APIs).
2. **Deliver**: Share backend code/files by Mar 5, 2025.
3. **Feedback**: I’ll review and guide you for Sprint 2.
