package online.banking.system.service;

import online.banking.system.exception.BankException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * GENERIC FUNCTIONAL INTERFACE: "some database work that returns a T".
 * It is like Callable<T>, but it receives the Connection and may throw our
 * checked exceptions. Lambdas are passed to BankingService.inTransaction().
 */
@FunctionalInterface
public interface TransactionWork<T> {

    T execute(Connection con) throws BankException, SQLException;
}
