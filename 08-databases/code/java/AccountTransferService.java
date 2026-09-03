// System Design - Backend
// Chapter 08, Databases -> 09 Transactions
// Java 21 / Spring Boot 3.3 / JdbcClient

package com.example.db.transaction;

@Service
class AccountTransferService {
    
    private final JdbcClient jdbc;
    
    AccountTransferService(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }
    
    // @Transactional replaces begin / rollback / commit outright:
    // Spring opens a transaction before the method, commits when it
    // returns, and rolls back if it throws.
    @Transactional
    public void transfer(long from, long to, BigDecimal amount) {
        
        jdbc.sql("UPDATE accounts SET balance = balance - :amt WHERE id = :id")
            .param("amt", amount)
            .param("id", from)
            .update(); // debit A
        
        jdbc.sql("UPDATE accounts SET balance = balance + :amt WHERE id = :id")
            .param("amt", amount)
            .param("id", to)
            .update(); // credit B
        
        // Both writes become permanent together when this method returns.
        // Throw anywhere above and neither one survives.
    }
    // BigDecimal, never double: 0.1 + 0.2 is not 0.3 in binary floating
    // point, and money is the one place that always eventually shows.
    //
    // Two things that bite:
    // * only a RuntimeException rolls back by default. A checked
    // exception COMMITS unless you say @Transactional(rollbackFor=...)
    // * calling a @Transactional method from another method of the same
    // class bypasses the proxy, so no transaction ever starts
}
