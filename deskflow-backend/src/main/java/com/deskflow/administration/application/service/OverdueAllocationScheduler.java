package com.deskflow.administration.application.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OverdueAllocationScheduler {

    private final AdministrationAppService administrationService;

    public OverdueAllocationScheduler(AdministrationAppService administrationService) {
        this.administrationService = administrationService;
    }

    @Scheduled(fixedRateString = "${app.scheduler.przeterminowania-rate-ms:900000}")
    public void detectOverdueAllocations() {
        administrationService.detectOverdueAllocations();
    }
}
