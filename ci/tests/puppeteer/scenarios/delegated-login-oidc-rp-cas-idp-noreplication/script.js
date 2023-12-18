const puppeteer = require("puppeteer");
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?" +
        "client_id=client&" +
        "redirect_uri=https%3A%2F%2Flocalhost%3A9859%2Fanything%2F1&" +
        "scope=openid%20email%20profile%20address%20phone&" +
        "response_type=code&" +
        "response_mode=form_post&" +
        "nonce=vn4qulthnx";
    await cas.goto(page, url);

    await cas.assertVisibility(page, "li #CasClient");
    await cas.click(page, "li #CasClient");
    await page.waitForNavigation();

    await page.waitForTimeout(3000);
    await cas.screenshot(page);
    await cas.loginWith(page);
    await page.waitForTimeout(1000);

    const result = new URL(page.url());
    await cas.log(result.searchParams.toString());

    assert(result.searchParams.has("ticket") === false);
    assert(result.searchParams.has("client_id"));
    assert(result.searchParams.has("redirect_uri"));
    assert(result.searchParams.has("scope"));

    await cas.log("Allowing release of scopes and claims...");
    await cas.click(page, "#allow");
    await page.waitForNavigation();
    await page.waitForTimeout(2000);

    await cas.log(await page.url());
    assert(await page.url().startsWith("https://localhost:9859/anything/1"));
    await page.waitForTimeout(2000);
    await cas.assertInnerTextContains(page, "pre", "OC-1-");

    await cas.gotoLogout(page);
    assert(page.url().startsWith("https://localhost:8444/cas/logout"));
    await page.waitForTimeout(2000);

    await browser.close();
})();
