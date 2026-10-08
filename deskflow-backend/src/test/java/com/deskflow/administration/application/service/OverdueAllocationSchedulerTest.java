package com.deskflow.administration.application.service;

import com.deskflow.administration.application.port.out.AdministrationAllocationPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OverdueAllocationSchedulerTest {

    @Mock private AdministrationAllocationPort allocationPort;

    @Test
    void detectOverdueAllocations_delegates_to_administration_service() {
        var adminService = new AdministrationAppService(allocationPort);
        var scheduler = new OverdueAllocationScheduler(adminService);

        scheduler.detectOverdueAllocations();

        verify(allocationPort).detectOverdueAllocations();
    }
}
