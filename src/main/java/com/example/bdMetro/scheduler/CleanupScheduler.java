package com.example.bdMetro.scheduler;

import com.example.bdMetro.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CleanupScheduler {

    @Autowired
    private AuthenticationService authenticationService;

    // Todos los días a las 2am: limpia datos de usuarios vencidos hace más de 30 días
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanExpiredUserData() {
        int cleaned = authenticationService.cleanExpiredDataOlderThan(30);
        if (cleaned > 0) {
            System.out.println("[CleanupScheduler] Datos eliminados de " + cleaned + " usuario(s) con código vencido hace más de 30 días.");
        }
    }
}
