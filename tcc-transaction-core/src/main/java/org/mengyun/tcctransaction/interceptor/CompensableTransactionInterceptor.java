package org.mengyun.tcctransaction.interceptor;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mengyun.tcctransaction.api.ParticipantStatus;
import org.mengyun.tcctransaction.api.TransactionStatus;
import org.mengyun.tcctransaction.exception.IllegalTransactionStatusException;
import org.mengyun.tcctransaction.exception.NoExistedTransactionException;
import org.mengyun.tcctransaction.transaction.Transaction;
import org.mengyun.tcctransaction.transaction.TransactionManager;
import org.mengyun.tcctransaction.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * 先执行
 * Created by changmingxie on 10/30/15.
 */
public class CompensableTransactionInterceptor {

    static final Logger logger = LoggerFactory.getLogger(CompensableTransactionInterceptor.class.getSimpleName());

    private TransactionManager transactionManager;

    private Set<Class<? extends Exception>> globalDelayCancelExceptions = new HashSet<>();

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void addExtraDelayCancelExceptions(Set<Class<? extends Exception>> extraDelayCancelExceptions) {
        this.globalDelayCancelExceptions.add(SocketTimeoutException.class);
        this.globalDelayCancelExceptions.addAll(extraDelayCancelExceptions);
    }

    public Object interceptCompensableMethod(TransactionMethodJoinPoint pjp) throws Throwable {
        Transaction transaction = transactionManager.getCurrentTransaction();
        CompensableMethodContext compensableMethodContext = new CompensableMethodContext(pjp, transaction);
        // if method is @Compensable and no transaction context and no transaction, then root
        // else if method is @Compensable and has transaction context and no transaction ,then provider
        switch (compensableMethodContext.getParticipantRole()) {
            case ROOT:
                return rootMethodProceed(compensableMethodContext);
            case PROVIDER:
                return providerMethodProceed(compensableMethodContext);
            default:
                return compensableMethodContext.proceed();
        }
    }

    private Object rootMethodProceed(CompensableMethodContext compensableMethodContext) throws Throwable {
        Object returnValue = null;
        Transaction transaction = null;
        boolean asyncConfirm = compensableMethodContext.getAnnotation().asyncConfirm();
        boolean asyncCancel = compensableMethodContext.getAnnotation().asyncCancel();
        Set<Class<? extends Exception>> allDelayCancelExceptions = new HashSet<>();
        allDelayCancelExceptions.addAll(this.globalDelayCancelExceptions);
        allDelayCancelExceptions.addAll(Arrays.asList(compensableMethodContext.getAnnotation().delayCancelExceptions()));
        try {
            transaction = transactionManager.begin(compensableMethodContext.getUniqueIdentity());
            try {
                returnValue = compensableMethodContext.proceed();
            } catch (Throwable tryingException) {
                try {
                    //二阶段回滚 or 提交可以设计成异步操作
                    transactionManager.rollback(asyncCancel, isDelayCancelException(tryingException, allDelayCancelExceptions));
                } catch (Exception rollbackException) {
                    logger.warn("compensable transaction rollback failed, recovery job will try to rollback later", rollbackException);
                }
                throw tryingException;
            }
            //二阶段回滚 or 提交可以设计成异步操作
            transactionManager.commit(asyncConfirm);
        } finally {
            transactionManager.cleanAfterCompletion(transaction);
        }
        return returnValue;
    }

    private Object providerMethodProceed(CompensableMethodContext compensableMethodContext) throws Throwable {
        Transaction transaction = null;
        boolean asyncConfirm = compensableMethodContext.getAnnotation().asyncConfirm();
        boolean asyncCancel = compensableMethodContext.getAnnotation().asyncCancel();
        try {
            switch (compensableMethodContext.getTransactionContext().getStatus()) {
                case TRYING:
                    transaction = transactionManager.propagationNewBegin(compensableMethodContext.getTransactionContext());
                    Object result = null;
                    try {
                        result = compensableMethodContext.proceed();
                        transactionManager.changeStatus(TransactionStatus.TRY_SUCCESS);
                    } catch (Throwable e) {
                        try {
                            transactionManager.changeStatus(TransactionStatus.TRY_FAILED);
                        } catch (Exception ignore) {
                            //ignore
                        }
                        throw e;
                    }
                    return result;
                case CONFIRMING:
                    try {
                        transaction = transactionManager.propagationExistBegin(compensableMethodContext.getTransactionContext());
                        transactionManager.commit(asyncConfirm);
                    } catch (NoExistedTransactionException excepton) {
                        //the transaction has been commit,ignore it.
                        logger.warn("no existed transaction found at CONFIRMING stage, will ignore and confirm automatically. transaction xid:{}", compensableMethodContext.getTransactionContext().getXid());
                    }
                    break;
                case CANCELLING:
                    try {
                        //The transaction' status of this branch transaction, passed from consumer side.
                        ParticipantStatus transactionStatusFromConsumer = compensableMethodContext.getTransactionContext().getParticipantStatus();
                        transaction = transactionManager.propagationExistBegin(compensableMethodContext.getTransactionContext());
                        // Only if transaction's status is at TRY_SUCCESS、TRY_FAILED、CANCELLING stage we can call rollback.
                        // If transactionStatusFromConsumer is TRY_SUCCESS, no mate current transaction is TRYING or not, also can rollback.
                        // transaction's status is TRYING while transactionStatusFromConsumer is TRY_SUCCESS may happen when transaction's changeStatus is async.
                        if (transaction.getStatus().equals(TransactionStatus.TRY_SUCCESS)
                                || transaction.getStatus().equals(TransactionStatus.TRY_FAILED)
                                || transaction.getStatus().equals(TransactionStatus.CANCELLING)
                                || ParticipantStatus.TRY_SUCCESS.equals(transactionStatusFromConsumer)) {
                            transactionManager.rollback(asyncCancel, false);
                        } else {
                            //in this case, transaction's Status is TRYING and transactionStatusFromConsumer is TRY_FAILED
                            // this may happen if timeout exception throws during rpc call.
                            throw new IllegalTransactionStatusException("Branch transaction status is TRYING, cannot rollback directly, waiting for recovery job to rollback.");
                        }
                    } catch (NoExistedTransactionException exception) {
                        //the transaction has been rollback,ignore it.
                        logger.info("no existed transaction found at CANCELLING stage, will ignore and cancel automatically. transaction xid:{}", compensableMethodContext.getTransactionContext().getXid());
                    }
                    break;
            }
        } finally {
            transactionManager.cleanAfterCompletion(transaction);
        }
        Method method = compensableMethodContext.getMethod();
        return ReflectionUtils.getNullValue(method.getReturnType());
    }

    private boolean isDelayCancelException(Throwable throwable, Set<Class<? extends Exception>> delayCancelExceptions) {

        Throwable rootCause = ExceptionUtils.getRootCause(throwable);

        if (delayCancelExceptions != null) {
            for (Class<? extends Exception> delayCancelException : delayCancelExceptions) {

                if (delayCancelException.isAssignableFrom(throwable.getClass())
                        || (rootCause != null && delayCancelException.isAssignableFrom(rootCause.getClass()))) {
                    return true;
                }
            }
        }

        return false;
    }
}
