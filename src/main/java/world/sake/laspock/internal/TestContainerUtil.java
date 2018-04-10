package world.sake.laspock.internal;

import java.util.Enumeration;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.dbflute.utflute.mocklet.MockletHttpServletRequest;
import org.dbflute.utflute.mocklet.MockletHttpServletRequestImpl;
import org.dbflute.utflute.mocklet.MockletHttpServletResponse;
import org.dbflute.utflute.mocklet.MockletHttpServletResponseImpl;
import org.dbflute.utflute.mocklet.MockletServletConfig;
import org.dbflute.utflute.mocklet.MockletServletConfigImpl;
import org.dbflute.utflute.mocklet.MockletServletContextImpl;
import org.lastaflute.di.core.ExternalContext;
import org.lastaflute.di.core.LaContainer;
import org.lastaflute.di.core.factory.SingletonLaContainerFactory;
import org.lastaflute.web.LastaFilter;

/**
 * @author neginuki
 */
public final class TestContainerUtil {

    private TestContainerUtil() {
    }

    /** コンテナの初期化 */
    public static void doInitializeContainerAsLibrary(String configFile) {
        SingletonLaContainerFactory.setConfigPath(configFile);
        SingletonLaContainerFactory.init();
    }

    /** コンテナの初期化(web) */
    public static void doInitializeContainerAsWeb(String configPath) {
        SingletonLaContainerFactory.setConfigPath(configPath);

        MockletServletConfig mockConfig = new MockletServletConfigImpl();
        mockConfig.setServletContext(new MockletServletContextImpl("utservlet"));

        final LastaFilter filter = new LastaFilter();
        try {
            filter.init(new FilterConfig() {
                public String getFilterName() {
                    return "containerFilter";
                }

                public ServletContext getServletContext() {
                    return mockConfig.getServletContext();
                }

                public Enumeration<String> getInitParameterNames() {
                    return null;
                }

                public String getInitParameter(String name) {
                    return null;
                }
            });
        } catch (ServletException e) {
            String msg = "Failed to initialize servlet config to servlet: " + mockConfig;
            throw new IllegalStateException(msg, e.getRootCause());
        }

        {
            final LaContainer container = SingletonLaContainerFactory.getContainer();
            final ExternalContext externalContext = container.getExternalContext();
            final MockletHttpServletRequest request = new MockletHttpServletRequestImpl(mockConfig.getServletContext(), "laspock");
            final MockletHttpServletResponse response = new MockletHttpServletResponseImpl(request);
            externalContext.setRequest(request);
            externalContext.setResponse(response);
        }
    }

    /** コンテナの破棄 */
    public static void destroyContainer() {
        SingletonLaContainerFactory.destroy();
        SingletonLaContainerFactory.setExternalContext(null);
    }
}
