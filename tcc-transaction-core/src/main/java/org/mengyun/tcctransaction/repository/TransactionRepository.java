package org.mengyun.tcctransaction.repository;

import org.mengyun.tcctransaction.api.Xid;
import org.mengyun.tcctransaction.storage.Page;
import org.mengyun.tcctransaction.transaction.Transaction;

import java.io.Closeable;
import java.util.Date;

/**
 * 事务持久层 支持db、redis、zk等方式，具体通过TransactionStorage决定
 * Created by changmingxie on 11/12/15.
 */
public interface TransactionRepository extends Closeable {

    String getDomain();

    int create(Transaction transaction);

    int update(Transaction transaction);

    int delete(Transaction transaction);

    Transaction findByXid(Xid xid);

    boolean supportRecovery();

    Page<Transaction> findAllUnmodifiedSince(Date date, String offset, int pageSize);

    @Override
    default void close() {

    }
}
