# Personal Task List

Build a small browser app that helps one person track daily tasks. The first version runs locally and stores tasks in the same browser across page reloads.

## Core models

### Task

| Field     | Type      | Rules                                                                    |
| --------- | --------- | ------------------------------------------------------------------------ |
| id        | String    | Generated on creation; unique and stable throughout the task's lifetime. |
| title     | String    | Trim surrounding whitespace; require 1 to 120 characters after trimming. |
| completed | Boolean   | Defaults to false.                                                       |
| createdAt | Timestamp | Set on creation and preserve on later changes.                           |

## Required behavior

### TASK 01 Add a task

The user enters a title and presses Add or Enter. A valid title creates an active task and clears the input. Duplicate titles are allowed and create separate tasks. An invalid title displays a message beside the input and leaves the task list unchanged.

### TASK 02 View tasks

Display tasks in creation order, oldest first. Each row contains a completion checkbox, the title, and a Delete button. Display titles as plain text. Completed tasks remain visible and have a checked checkbox and a crossed-out title.

### TASK 03 Change completion

Selecting a task's checkbox marks it complete. Clearing the checkbox makes it active again. Preserve its identity, title, creation time, and position.

### TASK 04 Delete a task

Delete removes only the selected task immediately. Other tasks remain unchanged.

### TASK 05 Filter tasks

Provide All, Active, and Completed filters. All is selected when the app opens. Filtering changes only which rows are displayed. Show the number of active tasks across the whole list, regardless of the selected filter.

### TASK 06 Keep tasks after reload

Persist additions, completion changes, and deletions in browser local storage. Reloading restores the saved tasks with their original identities and order. The selected filter resets to All.

## Screen layout

Use one page with the heading My Tasks, a labeled task-title input, an Add button, the three filters, the active-task count, and the task list. Show No tasks yet when the full list is empty. Show No matching tasks when tasks exist but the selected filter has no results.

## Acceptance example

1. Add Buy milk and Read project docs. Both appear as active; the active count is 2.
2. Complete Buy milk. It stays in place; the active count becomes 1.
3. Select Active. Only Read project docs is visible.
4. Reload. Both tasks appear under All; Buy milk is still completed.
5. Delete Buy milk and reload. Only Read project docs remains.
6. Try adding a title containing only spaces. Show a validation message and create no task.

## Scope

This version has one local task list. Accounts, shared lists, due dates, notifications, server APIs, and synchronization between devices are outside its scope.
