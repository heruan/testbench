package com.vaadin.testbench;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Augmenter;

import com.vaadin.testbench.commands.TestBenchCommandExecutor;

/**
 */
public class TestBench {

    private static final Map<Class<?>, MethodFilter> methodFilters;

    private static final class ElementMethodFilter implements MethodFilter {

        private Class<?> proxyClass;
        private Map<Method, Boolean> invocationNeeded;

        public ElementMethodFilter(Class<?> clazz) {
            proxyClass = clazz;
            invocationNeeded = new HashMap<Method, Boolean>();
        }

        @Override
        public boolean isHandled(Method method) {
            if (!invocationNeeded.containsKey(method)) {
                try {
                    proxyClass.getMethod(method.getName(),
                            method.getParameterTypes());
                    invocationNeeded.put(method, false);
                } catch (Exception e) {
                    invocationNeeded.put(method, true);
                }
            }

            return invocationNeeded.get(method);
        }
    }

    private static final class ElementInvocationHandler implements
            MethodHandler {

        private Object actualElement;

        public ElementInvocationHandler(Object actualElement) {
            this.actualElement = actualElement;
        }

        @Override
        public Object invoke(Object self, Method thisMethod, Method proceed,
                Object[] args) throws Throwable {
            if (null != proceed) {
                // This is a protected method
                return proceed.invoke(self, args);
            }
            return thisMethod.invoke(actualElement, args);
        }

    }

    static {
        LicenseChecker.nag();
        methodFilters = new HashMap<Class<?>, MethodFilter>();
    }

    public static WebDriver createDriver(WebDriver driver) {
        if (!(driver instanceof TakesScreenshot)) {
            driver = new Augmenter().augment(driver);
        }

        Set<Class<?>> allInterfaces = extractInterfaces(driver);
        Class<TestBenchDriverProxy> driverClass = TestBenchDriverProxy.class;
        allInterfaces.addAll(extractInterfaces(driverClass));
        final Class<?>[] allInterfacesArray = allInterfaces
                .toArray(new Class<?>[allInterfaces.size()]);

        ProxyFactory pFactory = new ProxyFactory();
        pFactory.setInterfaces(allInterfacesArray);
        pFactory.setSuperclass(driverClass);

        Object proxy;
        try {
            proxy = pFactory.create(new Class[] { WebDriver.class },
                    new Object[] { driver },
                    new DriverInvocationHandler(driver));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return (WebDriver) proxy;
    }

    public static TestBenchElement createElement(WebElement webElement,
            TestBenchCommandExecutor tbCoimmandExecutor) {
        return createElement(TestBenchElement.class, webElement,
                tbCoimmandExecutor);
    }

    /**
     * Create new Element of given type. Initialize it with WebElement and
     * TestBenchCommandExecutor. This feature is advanced and potentially
     * dangerous.
     * 
     * @param clazz
     *            Class of wanted Element
     * @param webElement
     *            Selenium WebElement to be wrapped into given Class
     * @param tbCommandExecutor
     *            TestBenchCommandExecutor instance
     * @return Element of given Class wrapping given WebElement
     */
    public static <T extends TestBenchElement> T createElement(Class<T> clazz,
            WebElement webElement, TestBenchCommandExecutor tbCommandExecutor) {

        Set<Class<?>> allInterfaces = extractInterfaces(webElement);

        final Class<?>[] allInterfacesArray = allInterfaces
                .toArray(new Class<?>[allInterfaces.size()]);

        ProxyFactory pFactory = new ProxyFactory();
        pFactory.setSuperclass(clazz);
        pFactory.setInterfaces(allInterfacesArray);
        pFactory.setFilter(getMethodFilter(clazz));

        Object proxyObject;
        try {
            proxyObject = pFactory.create(new Class[0], new Object[0],
                    new ElementInvocationHandler(webElement));
        } catch (Exception e) {
            return null;
        }

        @SuppressWarnings("unchecked")
        T proxy = (T) proxyObject;
        proxy.init(webElement, tbCommandExecutor);
        return proxy;
    }

    private static MethodFilter getMethodFilter(
            Class<? extends TestBenchElement> clazz) {
        if (!methodFilters.containsKey(clazz)) {
            methodFilters.put(clazz, new ElementMethodFilter(clazz));
        }
        return methodFilters.get(clazz);
    }

    private static Set<Class<?>> extractInterfaces(final Object object) {
        return extractInterfaces(object.getClass());
    }

    private static Set<Class<?>> extractInterfaces(final Class<?> clazz) {
        final Set<Class<?>> allInterfaces = new HashSet<Class<?>>();
        extractInterfaces(allInterfaces, clazz);

        return allInterfaces;
    }

    private static void extractInterfaces(final Set<Class<?>> addTo,
            final Class<?> clazz) {
        if (clazz == null || Object.class.equals(clazz)) {
            return; // Done
        }

        final Class<?>[] classes = clazz.getInterfaces();
        for (final Class<?> interfaceClass : classes) {
            addTo.add(interfaceClass);
            for (final Class<?> superInterface : interfaceClass.getInterfaces()) {
                addTo.add(superInterface);
                extractInterfaces(addTo, superInterface);
            }
        }
        extractInterfaces(addTo, clazz.getSuperclass());
    }
}
