package world.sake.laspock.internal;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.dbflute.utflute.core.InjectionTestCase;
import org.dbflute.utflute.core.binding.BindingAnnotationRule;
import org.dbflute.utflute.core.binding.BindingRuleProvider;
import org.dbflute.utflute.core.binding.BoundResult;
import org.dbflute.utflute.core.binding.ComponentBinder;
import org.dbflute.utflute.core.binding.ComponentProvider;
import org.dbflute.utflute.lastadi.LastaDiTestCase;
import org.dbflute.util.Srl;
import org.lastaflute.core.util.ContainerUtil;
import org.lastaflute.di.naming.NamingConvention;

/**
 * 詳しくは UTFlute のソース参照！
 *
 * @see InjectionTestCase
 * @see LastaDiTestCase
 * @author neginuki
 */
public class TestInjectManager {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    private List<BoundResult> injectedBoundResultList = new ArrayList<>(2);

    private List<Object> mockInstanceList = new ArrayList<>();

    // ===================================================================================
    //                                                                Dependency Injection   
    //                                                                ====================
    /**
     * DIして返す。
     * @param DIしたい対象
     * @return DIした対象
     */
    public <T> T di(T target) {
        bind(target);
        return target;
    }

    /**
     * di してモック追加。
     * @param モック化する対象
     * @return 追加された対象
     */
    public <T> T diMock(T target) {
        di(target);
        addMock(target);
        return target;
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    protected BoundResult bind(Object bean) {
        final ComponentBinder binder = createOuterComponentBinder(bean);
        final BoundResult boundResult = binder.bindComponent(bean);

        injectedBoundResultList.add(boundResult);

        return boundResult;
    }

    /**
     * モックの追加。
     * @param モック化する対象
     */
    public void addMock(Object mock) {
        mockInstanceList.add(mock);
    }

    protected ComponentBinder createOuterComponentBinder(Object bean) {
        final ComponentBinder binder = createBasicComponentBinder();
        adjustOuterComponentBinder(bean, binder);

        return binder;
    }

    protected void adjustOuterComponentBinder(Object bean, ComponentBinder binder) {
        mockInstanceList.stream().filter(mock -> mock != bean).forEach(binder::addMockInstance);
    }

    protected ComponentBinder createBasicComponentBinder() {
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

            // 
            public boolean existsComponent(String name) { // 
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