// System Design - Backend
// Chapter 10, Task Queues & Background Jobs -> 12 Recurring tasks
// Java 21 / Spring Boot 3.3

package com.example.tasks.scheduled;

@Configuration
@EnableScheduling
class RecurringTasks {

    private final ReportService reports;
    private final SessionService sessions;

    RecurringTasks(ReportService reports, SessionService sessions) {
        this.reports = reports;
        this.sessions = sessions;
    }

    // Every Sunday at midnight
    @Scheduled(cron = "0 0 0 * * SUN")
    void weeklyReport() {
        reports.generateWeekly();
    }

    // 03:00 on the 1st of every month
    @Scheduled(cron = "0 0 3 1 * *")
    void cleanupOrphanSessions() {
        sessions.deleteOrphans();
    }
}
// Two traps worth knowing:
// * Spring's cron has SIX fields, seconds first. A five-field
// string copied out of a crontab means something else here.
// * @Scheduled fires on EVERY instance. Run two replicas and the
// job runs twice. Use ShedLock, or Quartz in clustered mode, to
// make it run once across the fleet.
