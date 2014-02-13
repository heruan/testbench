/**
 * Copyright (C) 2012 Vaadin Ltd
 *
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 *
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the license along with this program.
 * If not, see <http://vaadin.com/license/cval-3.0>.
 */
package com.vaadin.testbench.elements;

import java.util.List;

import org.openqa.selenium.support.ui.Select;

import com.vaadin.testbench.By;
import com.vaadin.testbench.TestBenchElement;

@ServerClass("com.vaadin.ui.NativeSelect")
public class NativeSelectElement extends AbstractSelectElement {
    private Select selectElement;

    @Override
    protected void init() {
        super.init();
        selectElement = new Select(findElement(By.tagName("select")));
    }

    public void deselectByText(String text) {
        selectElement.deselectByVisibleText(text);
    }

    public List<TestBenchElement> getAllSelectedOptions() {
        return wrapElements(selectElement.getAllSelectedOptions(),
                getCommandExecutor());
    }

    public TestBenchElement getFirstSelectedOption() {
        return wrapElement(selectElement.getFirstSelectedOption(),
                getCommandExecutor());
    }

    public List<TestBenchElement> getOptions() {
        return wrapElements(selectElement.getOptions(), getCommandExecutor());
    }

    public boolean isMultiple() {
        return selectElement.isMultiple();
    }

    public void selectByText(String text) {
        selectElement.selectByVisibleText(text);
    }
}
