// Main JavaScript Functions

// Alert/Toast notifications
function showAlert(message, type = 'success') {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    const container = document.querySelector('main');
    if (container) {
        container.insertBefore(alertDiv, container.firstChild);
        setTimeout(() => alertDiv.remove(), 5000);
    }
}

// Create Ticket Form Handler
document.getElementById('createTicketForm')?.addEventListener('submit', function(e) {
    e.preventDefault();
    
    const formData = {
        title: document.getElementById('title').value,
        description: document.getElementById('description').value,
        priority: document.getElementById('priority').value
        // createdById will be set server-side based on authenticated user
    };
    
    fetch('/user/create-ticket', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(formData)
    })
    .then(response => response.json())
    .then(data => {
        showAlert('Ticket created successfully!', 'success');
        setTimeout(() => window.location.href = '/user/my-tickets', 1500);
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('Error creating ticket', 'danger');
    });
});

// Load tickets via AJAX
function loadTickets(page = 0, size = 10) {
    fetch(`/api/tickets?page=${page}&size=${size}`)
        .then(response => response.json())
        .then(data => {
            populateTicketsTable(data.content);
        })
        .catch(error => console.error('Error loading tickets:', error));
}

// Populate tickets table
function populateTicketsTable(tickets) {
    const tableBody = document.querySelector('#allTicketsTable tbody');
    if (!tableBody) return;
    
    tableBody.innerHTML = tickets.map(ticket => `
        <tr>
            <td>${ticket.ticketNumber}</td>
            <td>${ticket.title}</td>
            <td><span class="badge bg-${getStatusColor(ticket.status)}">${ticket.status}</span></td>
            <td><span class="badge bg-${getPriorityColor(ticket.priority)}">${ticket.priority}</span></td>
            <td>${ticket.createdByName}</td>
            <td>${ticket.assignedToName || 'Unassigned'}</td>
            <td>${formatDate(ticket.createdAt)}</td>
            <td>
                <a href="/admin/ticket/${ticket.id}" class="btn btn-sm btn-primary">View</a>
                <button class="btn btn-sm btn-warning" onclick="assignTicket(${ticket.id})">Assign</button>
            </td>
        </tr>
    `).join('');
}

// Helper functions
function getStatusColor(status) {
    const colors = {
        'OPEN': 'warning',
        'IN_PROGRESS': 'info',
        'RESOLVED': 'success',
        'CLOSED': 'secondary'
    };
    return colors[status] || 'secondary';
}

function getPriorityColor(priority) {
    const colors = {
        'LOW': 'info',
        'MEDIUM': 'warning',
        'HIGH': 'danger',
        'CRITICAL': 'danger'
    };
    return colors[priority] || 'secondary';
}

function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US');
}

// Add Comment
function addComment(ticketId) {
    const commentText = document.getElementById('newComment');
    if (!commentText || !commentText.value.trim()) {
        showAlert('Please enter a comment', 'warning');
        return;
    }
    
    const commentData = {
        content: commentText.value,
        ticketId: ticketId,
        createdById: getCurrentUserId() // Needs implementation
    };
    
    fetch('/api/comments/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(commentData)
    })
    .then(response => response.json())
    .then(data => {
        showAlert('Comment added successfully!', 'success');
        commentText.value = '';
        loadComments(ticketId);
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('Error adding comment', 'danger');
    });
}

// Load Comments
function loadComments(ticketId) {
    fetch(`/api/comments/ticket/${ticketId}`)
        .then(response => response.json())
        .then(comments => {
            // Populate comments in the UI
            console.log('Comments:', comments);
        })
        .catch(error => console.error('Error loading comments:', error));
}

// Assign Ticket (Admin)
function assignTicket(ticketId) {
    const userId = prompt('Enter User ID to assign:');
    if (!userId) return;
    
    fetch(`/admin/ticket/${ticketId}/assign?userId=${userId}`, {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        showAlert('Ticket assigned successfully!', 'success');
        loadTickets();
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('Error assigning ticket', 'danger');
    });
}

// Update Ticket Status
function updateTicketStatus(ticketId, newStatus) {
    fetch(`/admin/ticket/${ticketId}/status?status=${newStatus}`, {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        showAlert('Ticket status updated!', 'success');
        loadTickets();
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('Error updating ticket', 'danger');
    });
}

// Get Current User ID (placeholder - needs to be fetched from session/JWT)
function getCurrentUserId() {
    return parseInt(localStorage.getItem('userId') || '1');
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    // Load tickets if on the all tickets page
    if (document.getElementById('allTicketsTable')) {
        loadTickets();
    }
});
