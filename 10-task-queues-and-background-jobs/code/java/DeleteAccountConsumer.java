// System Design - Backend
// Chapter 10, Task Queues & Background Jobs -> 16 Full transaction rollback
// Java 21 / Spring Boot 3.3

package com.example.tasks.idempotency;

@Component
class DeleteAccountConsumer {

    private final AccountRepository accounts;

    DeleteAccountConsumer(AccountRepository accounts) {
        this.accounts = accounts;
    }

    // One transaction around every write: any failure rolls all of
    // them back, and the message is retried from a clean slate.
    @Transactional
    @RabbitListener(queues = "account.delete")
    void handle(DeleteAccountPayload payload) {

        // Idempotency guard: a retry after a partial success must not
        // fail, it must simply find nothing left to do.
        if (!accounts.existsById(payload.userId())) {
            return; // already gone: ACK cleanly
        }

        accounts.deleteProjects(payload.userId());
        accounts.deleteSessions(payload.userId());
        accounts.deleteAssets(payload.userId());
        accounts.deleteAccount(payload.userId());

        // Returning commits all four. Throwing rolls back all four.
    }
}
// The transaction and the ack are two different commits: the database
// commits first, then the broker is acknowledged. A crash in the gap
// redelivers a message whose work is already done, which is exactly
// why the guard above is not optional.
