package online.banking.system.service;

import online.banking.system.dao.AccountDAO;
import online.banking.system.exception.DatabaseException;
import java.security.SecureRandom;
import java.util.Set;

/**
 * Creates a unique 12-digit account number: bank code 5021 + 8 random digits.
 * "static synchronized" locks on the class, so two registrations happening at
 * the same moment can never receive the same number.
 */
public final class AccountNumberGenerator {

    private static final String BANK_CODE = "5021";
    private static final SecureRandom RANDOM = new SecureRandom();

    private AccountNumberGenerator() {
    }

    public static synchronized String generate() throws DatabaseException {
        Set<String> used = new AccountDAO().getAllAccountNumbers();
        String candidate;
        do {
            candidate = BANK_CODE + String.format("%08d", RANDOM.nextInt(100_000_000));
        } while (used.contains(candidate));
        return candidate;
    }
}
