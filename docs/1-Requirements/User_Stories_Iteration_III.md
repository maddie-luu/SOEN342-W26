# User Stories for Iteration III - Personal Task Management System

---

## US-1: Export Single Task to iCal

**As a** user,  
**I want to** export a single task to an iCal (.ics) file,  
**so that** I can import it into my calendar application.

**Description:**  
The user can select a specific task from the system and export it to an iCal file format. Only tasks with due dates can be exported. Subtasks are not exported as separate entries but are summarized within the task's description.

**Acceptance Criteria:**
- The system displays a list of tasks with due dates available for export
- The user can select a single task to export
- The exported .ics file includes: title, description, due date, status, priority, and project name
- Tasks without due dates cannot be exported
- Subtasks are included as a summary in the description field, not as separate calendar entries
- The system confirms successful export with a file path

---

## US-2: Export All Tasks in a Project to iCal

**As a** user,  
**I want to** export all tasks within a specific project to an iCal (.ics) file,  
**so that** I can view all project deadlines in my calendar application.

**Description:**  
The user can select a project and export all its tasks that have due dates to a single iCal file. Tasks without due dates are excluded from the export.

**Acceptance Criteria:**
- The system displays a list of available projects
- The user can select a project to export
- Only tasks with due dates within the selected project are exported
- Each task entry includes: title, description, due date, status, priority, and project name
- Subtasks are summarized within parent task descriptions, not exported separately
- The system reports how many tasks were exported and how many were skipped (no due date)

---

## US-3: Export Filtered Tasks to iCal

**As a** user,  
**I want to** export filtered tasks to an iCal (.ics) file,  
**so that** I can selectively add specific tasks to my calendar based on criteria.

**Description:**  
The user can apply filters (e.g., by status, priority, or date range) and export the resulting tasks to an iCal file. Only filtered tasks with due dates are included in the export.

**Acceptance Criteria:**
- The user can specify filter criteria before exporting
- Only tasks matching the filter AND having due dates are exported
- The exported .ics file includes: title, description, due date, status, priority, and project name for each task
- Subtasks are summarized in parent task descriptions, not exported as separate entries
- The system displays the number of tasks exported

---

## US-4: Gateway for iCal Export

**As a** developer,  
**I want to** use a Gateway pattern to connect domain logic to the iCal export functionality,  
**so that** the system maintains separation of concerns and follows good design principles.

**Description:**  
The system implements a Gateway class that acts as an intermediary between the domain objects (Task, Project) and the iCal file generation. This ensures the domain logic is decoupled from the export implementation details.

**Acceptance Criteria:**
- A Gateway class/interface exists to handle iCal export operations
- Domain objects (Task, Project) do not directly handle file I/O for iCal export
- The Gateway translates domain data into iCal format
- The Gateway handles file creation and writing
- Export requests from the CLI go through the Gateway

---

## US-5: View Overloaded Collaborators

**As a** user,  
**I want to** view a list of overloaded collaborators,  
**so that** I can identify team members who have too many tasks assigned.

**Description:**  
The system can detect collaborators who exceed their task capacity limits. A menu option allows the user to display all currently overloaded collaborators.

**Acceptance Criteria:**
- A new menu option "View overloaded collaborators" is available
- The system identifies collaborators whose assigned tasks exceed their category limits
- The list displays overloaded collaborators with their current task count and limit
- If no collaborators are overloaded, the system displays an appropriate message
- The detection runs based on current task assignments

---

## US-6: Limit Subtasks per Task

**As a** user,  
**I want** the system to prevent a task from having more than 20 subtasks,  
**so that** tasks remain manageable and organized.

**Description:**  
An OCL constraint ensures that no task can have more than 20 subtasks. The system enforces this limit when adding subtasks.

**Acceptance Criteria:**
- The system rejects adding a subtask if the parent task already has 20 subtasks
- An appropriate error message is displayed when the limit is reached
- The constraint is enforced at the domain level
- Existing subtasks can still be edited or removed

---

## US-7: Limit Open Tasks Without Due Date

**As a** user,  
**I want** the system to limit open tasks without a due date to a maximum of 50,  
**so that** I am encouraged to set deadlines and maintain productivity.

**Description:**  
An OCL constraint prevents the system from having more than 50 open tasks that do not have a due date assigned.

**Acceptance Criteria:**
- The system rejects creating a new open task without a due date if 50 such tasks already exist
- An appropriate error message is displayed when the limit is reached
- Tasks with due dates are not affected by this constraint
- Completed tasks without due dates do not count toward the limit
- Adding a due date to an existing task reduces the count

---

## US-8: Validate Collaborator Category Limits

**As a** user,  
**I want** collaborator category limits to be positive integers only,  
**so that** task capacity settings are always valid.

**Description:**  
An OCL constraint ensures that when setting capacity limits for collaborator categories, only positive integer values are accepted.

**Acceptance Criteria:**
- The system rejects zero or negative values for collaborator category limits
- The system rejects non-integer values for collaborator category limits
- An appropriate error message is displayed for invalid input
- Valid positive integers are accepted and saved

---

## US-9: Prevent Collaborator Overload

**As a** user,  
**I want** the system to warn me when assigning a task would overload a collaborator,  
**so that** I can maintain balanced workloads.

**Description:**  
An OCL constraint ensures that no collaborator becomes overloaded. The system checks capacity before task assignment and prevents assignments that would exceed limits.

**Acceptance Criteria:**
- The system checks collaborator capacity before assigning a task
- If assignment would cause overload, the system displays a warning
- The user is informed which collaborator would be overloaded and their current capacity
- The constraint is enforced consistently across all task assignment operations
