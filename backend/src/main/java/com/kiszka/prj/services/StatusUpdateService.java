package com.kiszka.prj.services;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class StatusUpdateService {
    private final JdbcTemplate jdbcTemplate;

    public StatusUpdateService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    @Scheduled(cron = "0 */5 * * * *")
    public void updateStatus(){
        jdbcTemplate.execute("SELECT update_statuses()");
    }
}
