package world.sake.laspock;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.dbflute.utflute.core.binding.BindingAnnotationRule;
import org.dbflute.utflute.core.binding.BindingRuleProvider;
import org.dbflute.utflute.core.binding.BoundResult;
import org.dbflute.utflute.core.binding.ComponentBinder;
import org.dbflute.utflute.core.binding.ComponentProvider;
import org.dbflute.util.Srl;
import org.lastaflute.core.util.ContainerUtil;
import org.lastaflute.di.core.factory.SingletonLaContainerFactory;
import org.lastaflute.di.core.smart.SmartDeployMode;
import org.lastaflute.di.naming.NamingConvention;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spockframework.runtime.extension.AbstractMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;

import world.sake.laspock.internal.AccessContextHelper;
import world.sake.laspock.internal.TestContainerUtil;
import world.sake.laspock.internal.TestInjectManager;
import world.sake.laspock.internal.TransactionResourceController;

/**
 * @author neginuki
 */
public class UTFluteInterceptor extends AbstractMethodInterceptor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger logger = LoggerFactory.getLogger(UTFluteInterceptor.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    private AccessContextHelper accessContext;

    protected String diConfigPath = "app.xml";

    protected final ComponentBinder testCaseComponentBinder = createTestCaseComponentBinder();

    /** バインドしたテストクラス */
    protected BoundResult testCaseBoundResult;

    // -----------------------------------------------------
    //                                    Transaction Object
    //                                    ------------------
    /** テストケースのトランザクション */
    protected TransactionResourceController testCaseTransactionResourceController;

    // ===================================================================================
    //                                                                               拡張
    //                                                                           =========

    @Override
    public void interceptSetupMethod(IMethodInvocation invocation) throws Throwable {
        SmartDeployMode.setValue(SmartDeployMode.WARM);

        if (SingletonLaContainerFactory.hasContainer()) { // 異なるフレームワークのテストケースが混同したとき、既に存在する可能性がある e.g. UTFlute x Laspock のテストが混同しているプロジェクトをまとめて実行した場合
            TestContainerUtil.destroyContainer(); // コンテナの破棄
        }

        prepareTestCaseContainer(); // コンテナを初期化
        prepareAccessContext(); // AccessContext を初期化

        testCaseBoundResult = testCaseComponentBinder.bindComponent(invocation.getInstance()); // バインド

        setupTestCaseTransaction();

        invocation.proceed();
    }

    @Override
    public void interceptCleanupMethod(IMethodInvocation invocation) throws Throwable {
        logger.debug("interceptクリーンアップ");

        testCaseTransactionResourceController.reflect(); // トランザクション開放
        TestContainerUtil.destroyContainer(); // コンテナの破棄
        clearAccessContext(); // AccessContext を破棄
    }

    // ===================================================================================
    //                                                                               Setup
    //                                                                               =====
    protected void setupTestCaseTransaction() {
        testCaseTransactionResourceController = TransactionResourceController.beginNewTransaction(false); // コミットしない
    }

    // ===================================================================================
    //                                                                       AccessContext
    //                                                                       =============
    /**
     * AccessContext を作ってスレッドに登録。
     */
    protected void prepareAccessContext() {
        new TestInjectManager().di(new AccessContextHelper()).prepare();

        logger.debug("AccessContext を登録しました。");
    }

    /**
     * AccessContext を破棄。
     */
    protected void clearAccessContext() {
        if (accessContext != null) {
            accessContext.clear();

            logger.debug("AccessContext を破棄しました。");
        }
    }

    // ===================================================================================
    //                                                                           Container
    //                                                                            ========
    /** テストケース用のコンテナの準備 */
    protected void prepareTestCaseContainer() {
        TestContainerUtil.doInitializeContainerAsWeb(diConfigPath);
    }

    // ===================================================================================
    //                                                                              Binder
    //                                                                            ========
    protected ComponentBinder createTestCaseComponentBinder() {
        final ComponentBinder binder = createBasicComponentBinder();

        return binder;
    }

    protected ComponentBinder createBasicComponentBinder() { // customize point
        return new ComponentBinder(createComponentProvider(), createBindingRuleProvider());
    }

    protected ComponentProvider createComponentProvider() {
        return new ComponentProvider() {

            public <COMPONENT> COMPONENT provideComponent(Class<COMPONENT> type) {
                return ContainerUtil.getComponent(type);
            }

            public <COMPONENT> COMPONENT provideComponent(String name) {
                return ContainerUtil.pickupComponentByName(name);
            }

            public boolean existsComponent(Class<?> type) {
                return ContainerUtil.hasComponent(type);
            }

            public boolean existsComponent(String name) {
                return ContainerUtil.proveComponentByName(name);
            }
        };
    }

    protected BindingRuleProvider createBindingRuleProvider() {
        return new BindingRuleProvider() {
            public Map<Class<? extends Annotation>, BindingAnnotationRule> provideBindingAnnotationRuleMap() {
                final Map<Class<? extends Annotation>, BindingAnnotationRule> ruleMap = new HashMap<>();
                ruleMap.put(Resource.class, new BindingAnnotationRule());
                return ruleMap;
            }

            public String filterByBindingNamingRule(String propertyName, Class<?> propertyType) {
                if (propertyType.getSimpleName().contains("_")) {
                    return null;
                }

                final NamingConvention convention = ContainerUtil.getComponent(NamingConvention.class);
                final String componentName;
                try {
                    componentName = convention.fromClassNameToComponentName(propertyType.getName());
                } catch (RuntimeException ignored) {
                    return null;
                }
                if (canUseComponentNameByBindingNamingRule(componentName, propertyName)) {
                    return componentName;
                }

                return null;
            }
        };
    }

    protected boolean canUseComponentNameByBindingNamingRule(String componentName, String propertyName) {
        if (componentName.contains("_")) {
            if (componentName.endsWith(propertyName)) {
                final String front = Srl.substringLastFront(componentName, propertyName);
                if (front.equals("") || front.endsWith("_")) {
                    return true;
                }
            }
        }
        return false;
    }
}
