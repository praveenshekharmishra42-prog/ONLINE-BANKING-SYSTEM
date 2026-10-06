package online.banking.system.dao;

import online.banking.system.exception.DatabaseException;
import java.util.List;
import java.util.Optional;

/**
 * GENERIC INTERFACE shared by the DAO classes (DAO = Data Access Object).
 *
 *   T = the object type stored (Customer, Account, Transaction)
 *   K = the type of its primary key (Integer, String, Long)
 *
 * update() is a DEFAULT method: it has a body, so a DAO whose rows must never
 * change (TransactionDAO) does not have to implement it.
 */
public interface GenericDAO<T, K> {

    void add(T entity) throws DatabaseException;

    Optional<T> findById(K id) throws DatabaseException;

    List<T> findAll() throws DatabaseException;

    default boolean update(T entity) throws DatabaseException {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support update.");
    }
}
