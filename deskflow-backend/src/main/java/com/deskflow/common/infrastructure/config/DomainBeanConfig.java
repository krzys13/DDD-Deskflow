package com.deskflow.common.infrastructure.config;

import com.deskflow.allocation.application.domain.service.AllocationRequestFactory;
import com.deskflow.allocation.application.domain.service.ApproveAllocationRequestDomainService;
import com.deskflow.allocation.application.domain.service.CancelAllocationRequestDomainService;
import com.deskflow.allocation.application.domain.service.EnsureEmployeeWithinLimitDomainService;
import com.deskflow.allocation.application.domain.service.EnsureResourceAvailableInPeriodDomainService;
import com.deskflow.allocation.application.domain.service.EnsureResourceEligibleForAllocationDomainService;
import com.deskflow.allocation.application.domain.service.IssueAllocationResourceDomainService;
import com.deskflow.allocation.application.domain.service.MarkAllocationRequestOverdueDomainService;
import com.deskflow.allocation.application.domain.service.RejectAllocationRequestDomainService;
import com.deskflow.allocation.application.domain.service.ReturnAllocationResourceDomainService;
import com.deskflow.resources.application.domain.service.OfficeResourceFactory;
import com.deskflow.resources.application.domain.service.DeactivateOfficeResourceDomainService;
import com.deskflow.resources.application.domain.service.MarkOfficeResourceAvailableDomainService;
import com.deskflow.resources.application.domain.service.MarkOfficeResourceUnavailableDomainService;
import com.deskflow.resources.application.domain.service.UpdateOfficeResourceDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainBeanConfig {

    @Bean
    public AllocationRequestFactory allocationRequestFactory() {
        return new AllocationRequestFactory();
    }

    @Bean
    public OfficeResourceFactory officeResourceFactory() {
        return new OfficeResourceFactory();
    }

    @Bean
    public EnsureResourceEligibleForAllocationDomainService ensureResourceEligibleForAllocationDomainService() {
        return new EnsureResourceEligibleForAllocationDomainService();
    }

    @Bean
    public EnsureResourceAvailableInPeriodDomainService ensureResourceAvailableInPeriodDomainService() {
        return new EnsureResourceAvailableInPeriodDomainService();
    }

    @Bean
    public EnsureEmployeeWithinLimitDomainService ensureEmployeeWithinLimitDomainService() {
        return new EnsureEmployeeWithinLimitDomainService();
    }

    @Bean
    public CancelAllocationRequestDomainService cancelAllocationRequestDomainService() {
        return new CancelAllocationRequestDomainService();
    }

    @Bean
    public ApproveAllocationRequestDomainService approveAllocationRequestDomainService() {
        return new ApproveAllocationRequestDomainService();
    }

    @Bean
    public RejectAllocationRequestDomainService rejectAllocationRequestDomainService() {
        return new RejectAllocationRequestDomainService();
    }

    @Bean
    public IssueAllocationResourceDomainService issueAllocationResourceDomainService() {
        return new IssueAllocationResourceDomainService();
    }

    @Bean
    public ReturnAllocationResourceDomainService returnAllocationResourceDomainService() {
        return new ReturnAllocationResourceDomainService();
    }

    @Bean
    public MarkAllocationRequestOverdueDomainService markAllocationRequestOverdueDomainService() {
        return new MarkAllocationRequestOverdueDomainService();
    }

    @Bean
    public UpdateOfficeResourceDomainService updateOfficeResourceDomainService() {
        return new UpdateOfficeResourceDomainService();
    }

    @Bean
    public MarkOfficeResourceUnavailableDomainService markOfficeResourceUnavailableDomainService() {
        return new MarkOfficeResourceUnavailableDomainService();
    }

    @Bean
    public MarkOfficeResourceAvailableDomainService markOfficeResourceAvailableDomainService() {
        return new MarkOfficeResourceAvailableDomainService();
    }

    @Bean
    public DeactivateOfficeResourceDomainService deactivateOfficeResourceDomainService() {
        return new DeactivateOfficeResourceDomainService();
    }
}
