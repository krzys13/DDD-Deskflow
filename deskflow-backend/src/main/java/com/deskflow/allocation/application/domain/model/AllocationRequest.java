package com.deskflow.allocation.application.domain.model;

import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.common.application.domain.DomainEvent;
import com.deskflow.allocation.application.domain.model.events.*;
import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;
import com.deskflow.allocation.application.domain.model.vo.DateRange;
import com.deskflow.allocation.application.domain.model.vo.RequestStatus;
import com.deskflow.common.application.domain.model.vo.ResourceId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AllocationRequest {

    private final AllocationRequestId id;
    private final EmployeeId employeeId;
    private final ResourceId resourceId;
    private final DateRange dateRange;
    private final String justification;
    private final LocalDateTime submittedAt;
    private RequestStatus status;
    private LocalDateTime issuedAt;
    private LocalDateTime returnedAt;
    private String rejectionReason;
    private final List<DomainEvent> events = new ArrayList<>();

    private AllocationRequest(AllocationRequestId id, EmployeeId employeeId, ResourceId resourceId,
                              DateRange dateRange, String justification, LocalDateTime submittedAt,
                              RequestStatus status, LocalDateTime issuedAt,
                              LocalDateTime returnedAt, String rejectionReason) {
        this.id = id;
        this.employeeId = employeeId;
        this.resourceId = resourceId;
        this.dateRange = dateRange;
        this.justification = justification;
        this.submittedAt = submittedAt;
        this.status = status;
        this.issuedAt = issuedAt;
        this.returnedAt = returnedAt;
        this.rejectionReason = rejectionReason;
    }

    public static AllocationRequest create(AllocationRequestId id, EmployeeId employeeId,
                                            ResourceId resourceId, DateRange dateRange,
                                            String justification, LocalDateTime submittedAt) {
        var r = new AllocationRequest(id, employeeId, resourceId, dateRange, justification, submittedAt,
                RequestStatus.SUBMITTED, null, null, null);
        r.events.add(new AllocationRequestSubmitted(id, employeeId, resourceId, dateRange, submittedAt));
        return r;
    }

    public static AllocationRequest reconstitute(AllocationRequestId id, EmployeeId employeeId,
                                                  ResourceId resourceId, DateRange dateRange,
                                                  String justification, LocalDateTime submittedAt,
                                                  RequestStatus status, LocalDateTime issuedAt,
                                                  LocalDateTime returnedAt, String rejectionReason) {
        return new AllocationRequest(id, employeeId, resourceId, dateRange, justification, submittedAt,
                status, issuedAt, returnedAt, rejectionReason);
    }

    public void approve() {
        requireStatus(RequestStatus.SUBMITTED, "approve");
        this.status = RequestStatus.APPROVED;
        events.add(new AllocationRequestApproved(id, employeeId, LocalDateTime.now()));
    }

    public void reject(String reason) {
        requireStatus(RequestStatus.SUBMITTED, "reject");
        this.status = RequestStatus.REJECTED;
        this.rejectionReason = reason;
        events.add(new AllocationRequestRejected(id, employeeId, reason, LocalDateTime.now()));
    }

    public void cancel() {
        if (status != RequestStatus.SUBMITTED && status != RequestStatus.APPROVED) {
            throw new IllegalStateException(
                    "Can only cancel a SUBMITTED or APPROVED request, current: " + status);
        }
        this.status = RequestStatus.CANCELLED;
        events.add(new AllocationRequestCancelled(id, employeeId, LocalDateTime.now()));
    }

    public void issueResource() {
        requireStatus(RequestStatus.APPROVED, "issue resource");
        this.status = RequestStatus.ACTIVE;
        this.issuedAt = LocalDateTime.now();
        events.add(new ResourceIssued(id, employeeId, resourceId, issuedAt));
    }

    public void returnResource() {
        if (status != RequestStatus.ACTIVE && status != RequestStatus.OVERDUE) {
            throw new IllegalStateException(
                    "Can only return resource for ACTIVE or OVERDUE request, current: " + status);
        }
        this.status = RequestStatus.COMPLETED;
        this.returnedAt = LocalDateTime.now();
        events.add(new ResourceReturned(id, employeeId, resourceId, returnedAt));
    }

    public void markAsOverdue() {
        requireStatus(RequestStatus.ACTIVE, "mark as overdue");
        this.status = RequestStatus.OVERDUE;
        events.add(new AllocationOverdue(id, employeeId, resourceId, LocalDateTime.now()));
    }

    private void requireStatus(RequestStatus required, String operation) {
        if (status != required) {
            throw new IllegalStateException(
                    "Operation '" + operation + "' requires status " + required + ", current: " + status);
        }
    }

    public List<DomainEvent> flushEvents() {
        var copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public AllocationRequestId getId() { return id; }
    public EmployeeId getEmployeeId() { return employeeId; }
    public ResourceId getResourceId() { return resourceId; }
    public DateRange getDateRange() { return dateRange; }
    public String getJustification() { return justification; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public RequestStatus getStatus() { return status; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public LocalDateTime getReturnedAt() { return returnedAt; }
    public String getRejectionReason() { return rejectionReason; }
}
