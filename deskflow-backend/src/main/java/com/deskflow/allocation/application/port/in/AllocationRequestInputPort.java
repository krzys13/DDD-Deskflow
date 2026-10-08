package com.deskflow.allocation.application.port.in;

import com.deskflow.allocation.application.model.AllocationRequestView;
import com.deskflow.allocation.application.model.ResourceCatalogItem;
import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;
import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.common.application.domain.model.vo.ResourceId;

import java.time.LocalDate;
import java.util.List;

public interface AllocationRequestInputPort {

    AllocationRequestId submitRequest(EmployeeId employeeId, ResourceId resourceId,
                                      LocalDate dateFrom, LocalDate dateTo, String justification);

    void cancelRequest(AllocationRequestId requestId, EmployeeId employeeId);

    List<ResourceCatalogItem> browseResources();

    List<AllocationRequestView> browseHistory(EmployeeId employeeId);

    List<AllocationRequestView> findActiveForResource(ResourceId resourceId);
}
