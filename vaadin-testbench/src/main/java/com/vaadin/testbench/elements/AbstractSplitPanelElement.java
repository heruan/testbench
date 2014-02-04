package com.vaadin.testbench.elements;

import com.vaadin.testbench.TestBench;

@ServerClass("com.vaadin.ui.AbstractSplitPanel")
public class AbstractSplitPanelElement extends
        AbstractComponentContainerElement {

    /**
     * Gets the first component of a split panel and wraps it in given class.
     * 
     * @param clazz
     *          Components element class
     * @return First component wrapped in given class
     */
    public <T extends AbstractElement> T getFirstComponent(Class<T> clazz) {
        return TestBench.createElement(clazz, $$(AbstractComponentElement.class)
                .first().getWrappedElement(), getCommandExecutor());
    }

    /**
     * Gets the second component of a split panel and wraps it in given class.
     * 
     * @param clazz
     *          Components element class
     * @return Second component wrapped in given class
     */
    public <T extends AbstractElement> T getSecondComponent(Class<T> clazz) {
        return TestBench.createElement(clazz, $$(AbstractComponentElement.class)
                .get(1).getWrappedElement(), getCommandExecutor());
    }

}
