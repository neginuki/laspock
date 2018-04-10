package world.sake.laspock.internal;

import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.dbflute.utflute.core.transaction.TransactionFailureException;
import org.dbflute.utflute.core.transaction.TransactionResource;
import org.lastaflute.core.util.ContainerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neginuki
 */
public class TransactionResourceController {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger logger = LoggerFactory.getLogger(TransactionResourceController.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    private boolean commit;
    private TransactionResource transactionResource;

    // ===================================================================================
    //                                                                         Constractor
    //                                                                         ===========
    private TransactionResourceController(boolean commit) {
        this.commit = commit;
        transactionResource = begin();
    }

    public static TransactionResourceController beginNewTransaction(boolean commit) {
        return new TransactionResourceController(commit);
    }

    // ===================================================================================
    //                                                                               Begin
    //                                                                               =====
    protected TransactionResource begin() {
        final Class<TransactionManager> managerType = TransactionManager.class;
        if (!ContainerUtil.hasComponent(managerType)) {
            return null;
        }
        final TransactionManager manager = ContainerUtil.getComponent(managerType);
        final Transaction suspendedTx;
        try {
            if (manager.getStatus() != Status.STATUS_NO_TRANSACTION) {
                suspendedTx = manager.suspend();
            } else {
                suspendedTx = null;
            }
        } catch (SystemException e) {
            throw new TransactionFailureException("Failed to suspend current", e);
        }
        TransactionResource resource = null;
        try {
            manager.begin();
            resource = new TransactionResource() {
                public void commit() {
                    try {
                        manager.commit();
                    } catch (Exception e) {
                        throw new TransactionFailureException("Failed to commit the transaction.", e);
                    } finally {
                        resumeSuspendedTxQuietly(manager, suspendedTx);
                    }
                }

                public void rollback() {
                    try {
                        manager.rollback();
                    } catch (Exception e) {
                        throw new TransactionFailureException("Failed to roll-back the transaction.", e);
                    } finally {
                        resumeSuspendedTxQuietly(manager, suspendedTx);
                    }
                }
            }; // for thread-fire's transaction or manual transaction
        } catch (NotSupportedException e) {
            throw new TransactionFailureException("Failed to begin new transaction.", e);
        } catch (SystemException e) {
            throw new TransactionFailureException("Failed to begin new transaction.", e);
        }

        logger.debug("トランザクションを開始しました。");

        return resource;
    }

    protected void resumeSuspendedTxQuietly(TransactionManager manager, Transaction suspendedTx) {
        try {
            if (suspendedTx != null) {
                manager.resume(suspendedTx);
            }
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
    }

    // ===================================================================================
    //                                                                             Reflect
    //                                                                             =======
    public void reflect() {
        if (transactionResource == null) {
            return;
        }

        if (commit) {
            commit();
        } else {
            rollback();
        }

        transactionResource = null;
    }

    // ===================================================================================
    //                                                                              Commit
    //                                                                              ======
    public void commit() {
        transactionResource.commit();
        logger.debug("トランザクションをコミットしました");
    }

    // ===================================================================================
    //                                                                            Rollback
    //                                                                            ========
    public void rollback() {
        transactionResource.rollback();
        logger.debug("トランザクションをロールバックしました");
    }
}
