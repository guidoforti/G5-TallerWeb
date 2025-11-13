package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.net.MalformedURLException;
import java.net.URL;

public class VistaWeb {
    protected Page page;

    public VistaWeb(Page page) {
        this.page = page;
    }

    public URL obtenerURLActual() throws MalformedURLException {
        URL url = new URL(page.url());
        return url;
    }

    protected String obtenerTextoDelElemento(String selectorCSS){
        return this.obtenerElemento(selectorCSS).textContent();
    }

    protected void darClickEnElElemento(String selectorCSS){
        this.obtenerElemento(selectorCSS).click();
    }

    protected void darClickEnPrimerElemento(String selectorCSS){
        page.locator(selectorCSS).first().click();
    }

    protected void darClickEnUltimoElemento(String selectorCSS){
        page.locator(selectorCSS).last().click();
    }

    protected void escribirEnElElemento(String selectorCSS, String texto){
        this.obtenerElemento(selectorCSS).fill(texto);
    }

    protected void seleccionarEnTomSelect(String selectId, String searchText) {
        // Tom Select wraps the original select element with a custom UI
        // 1. Get the Tom Select wrapper for this specific select element
        String tomSelectWrapperSelector = "#" + selectId + " + .ts-wrapper";

        // 2. Get the input within this specific wrapper
        String tomSelectInputSelector = tomSelectWrapperSelector + " .ts-control input";

        // 3. Click to focus the input and type the search text
        page.locator(tomSelectInputSelector).click();
        page.locator(tomSelectInputSelector).fill(searchText);

        // 4. Wait for the Nominatim API response (this triggers the dropdown population)
        page.waitForResponse(
            response -> response.url().contains("/nominatim/buscar") && response.status() == 200,
            () -> {}
        );

        // 5. Get the dropdown specific to this Tom Select instance (scoped to the wrapper)
        String dropdownSelector = tomSelectWrapperSelector + " .ts-dropdown";

        // 6. Wait for dropdown to appear
        page.locator(dropdownSelector).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        // 7. Wait for at least one option to be rendered
        page.locator(dropdownSelector + " .option[data-selectable]").first().waitFor(
            new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
        );

        // 8. Click the first option
        page.locator(dropdownSelector + " .option[data-selectable]").first().click();

        // 9. Wait for dropdown to close (indicates selection is complete)
        page.locator(dropdownSelector).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }

    private Locator obtenerElemento(String selectorCSS){
        return page.locator(selectorCSS);
    }
}
