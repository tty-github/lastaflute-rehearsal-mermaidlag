/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.docksidestage.mylasta.direction;

import javax.annotation.Resource;

import org.docksidestage.mylasta.direction.sponsor.MermaidlagActionAdjustmentProvider;
import org.docksidestage.mylasta.direction.sponsor.MermaidlagApiFailureHook;
import org.docksidestage.mylasta.direction.sponsor.MermaidlagCookieResourceProvider;
import org.docksidestage.mylasta.direction.sponsor.MermaidlagCurtainBeforeHook;
import org.docksidestage.mylasta.direction.sponsor.MermaidlagJsonResourceProvider;
import org.docksidestage.mylasta.direction.sponsor.MermaidlagListedClassificationProvider;
import org.docksidestage.mylasta.direction.sponsor.MermaidlagMailDeliveryDepartmentCreator;
import org.docksidestage.mylasta.direction.sponsor.MermaidlagSecurityResourceProvider;
import org.docksidestage.mylasta.direction.sponsor.MermaidlagTimeResourceProvider;
import org.docksidestage.mylasta.direction.sponsor.MermaidlagUserLocaleProcessProvider;
import org.docksidestage.mylasta.direction.sponsor.MermaidlagUserTimeZoneProcessProvider;
import org.lastaflute.core.direction.CachedFwAssistantDirector;
import org.lastaflute.core.direction.FwAssistDirection;
import org.lastaflute.core.direction.FwCoreDirection;
import org.lastaflute.core.security.InvertibleCryptographer;
import org.lastaflute.core.security.OneWayCryptographer;
import org.lastaflute.db.dbflute.classification.ListedClassificationProvider;
import org.lastaflute.db.direction.FwDbDirection;
import org.lastaflute.mayaa.MayaaRenderingProvider;
import org.lastaflute.mayaa.policy.LaMayaaPolicy;
import org.lastaflute.thymeleaf.ThymeleafRenderingProvider;
import org.lastaflute.web.direction.FwWebDirection;
import org.lastaflute.web.response.HtmlResponse;
import org.lastaflute.web.ruts.NextJourney;
import org.lastaflute.web.ruts.process.ActionRuntime;
import org.lastaflute.web.ruts.renderer.HtmlRenderer;
import org.lastaflute.web.ruts.renderer.HtmlRenderingProvider;

/**
 * @author jflute
 */
public class MermaidlagFwAssistantDirector extends CachedFwAssistantDirector {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private MermaidlagConfig mermaidlagConfig;

    // ===================================================================================
    //                                                                              Assist
    //                                                                              ======
    @Override
    protected void prepareAssistDirection(FwAssistDirection direction) {
        direction.directConfig(nameList -> nameList.add("mermaidlag_config.properties"), "mermaidlag_env.properties");
    }

    // ===================================================================================
    //                                                                               Core
    //                                                                              ======
    @Override
    protected void prepareCoreDirection(FwCoreDirection direction) {
        // this configuration is on mermaidlag_env.properties because this is true only when development
        direction.directDevelopmentHere(mermaidlagConfig.isDevelopmentHere());

        // titles of the application for logging are from configurations
        direction.directLoggingTitle(mermaidlagConfig.getDomainTitle(), mermaidlagConfig.getEnvironmentTitle());

        // this configuration is on sea_env.properties because it has no influence to production
        // even if you set true manually and forget to set false back
        direction.directFrameworkDebug(mermaidlagConfig.isFrameworkDebug()); // basically false

        // you can add your own process when your application is booting
        direction.directCurtainBefore(createCurtainBeforeHook());

        direction.directSecurity(createSecurityResourceProvider());
        direction.directTime(createTimeResourceProvider());
        direction.directJson(createJsonResourceProvider());
        direction.directMail(createMailDeliveryDepartmentCreator().create());
    }

    protected MermaidlagCurtainBeforeHook createCurtainBeforeHook() {
        return new MermaidlagCurtainBeforeHook();
    }

    protected MermaidlagSecurityResourceProvider createSecurityResourceProvider() { // #change_it_first
        final InvertibleCryptographer inver = InvertibleCryptographer.createAesCipher("harbor:dockside");
        final OneWayCryptographer oneWay = OneWayCryptographer.createSha256Cryptographer();
        return new MermaidlagSecurityResourceProvider(inver, oneWay);
    }

    protected MermaidlagTimeResourceProvider createTimeResourceProvider() {
        return new MermaidlagTimeResourceProvider(mermaidlagConfig);
    }

    protected MermaidlagJsonResourceProvider createJsonResourceProvider() {
        return new MermaidlagJsonResourceProvider();
    }

    protected MermaidlagMailDeliveryDepartmentCreator createMailDeliveryDepartmentCreator() {
        return new MermaidlagMailDeliveryDepartmentCreator(mermaidlagConfig);
    }

    // ===================================================================================
    //                                                                                 DB
    //                                                                                ====
    @Override
    protected void prepareDbDirection(FwDbDirection direction) {
        direction.directClassification(createListedClassificationProvider());
    }

    protected ListedClassificationProvider createListedClassificationProvider() {
        return new MermaidlagListedClassificationProvider();
    }

    // ===================================================================================
    //                                                                                Web
    //                                                                               =====
    @Override
    protected void prepareWebDirection(FwWebDirection direction) {
        direction.directRequest(createUserLocaleProcessProvider(), createUserTimeZoneProcessProvider());
        direction.directCookie(createCookieResourceProvider());
        direction.directAdjustment(createActionAdjustmentProvider());
        direction.directMessage(nameList -> nameList.add("mermaidlag_message"), "mermaidlag_label");
        direction.directApiCall(createApiFailureHook());
        direction.directHtmlRendering(createHtmlRenderingProvider());
    }

    protected MermaidlagUserLocaleProcessProvider createUserLocaleProcessProvider() {
        return new MermaidlagUserLocaleProcessProvider();
    }

    protected MermaidlagUserTimeZoneProcessProvider createUserTimeZoneProcessProvider() {
        return new MermaidlagUserTimeZoneProcessProvider();
    }

    protected MermaidlagCookieResourceProvider createCookieResourceProvider() { // #change_it_first
        final InvertibleCryptographer cr = InvertibleCryptographer.createAesCipher("dockside:harbor");
        return new MermaidlagCookieResourceProvider(mermaidlagConfig, cr);
    }

    protected MermaidlagActionAdjustmentProvider createActionAdjustmentProvider() {
        return new MermaidlagActionAdjustmentProvider();
    }

    protected MermaidlagApiFailureHook createApiFailureHook() {
        return new MermaidlagApiFailureHook();
    }

    protected HtmlRenderingProvider createHtmlRenderingProvider() {
        // #thiking: if all htmls are for mayaa, can we remove this?
        final ThymeleafRenderingProvider thymeleaf = createThymeleafRenderingProvider();
        final MayaaRenderingProvider mayaa = createMayaaRenderingProvider();
        final LaMayaaPolicy policy = new LaMayaaPolicy();
        return new HtmlRenderingProvider() {

            @Override
            public HtmlRenderer provideRenderer(ActionRuntime runtime, NextJourney journey) {
                return chooseRenderingProvider(runtime, journey).provideRenderer(runtime, journey);
            }

            private HtmlRenderingProvider chooseRenderingProvider(ActionRuntime runtime, NextJourney journey) {
                return policy.isMayaaTemplate(journey.getRoutingPath()) ? mayaa : thymeleaf;
            }

            @Override
            public HtmlResponse provideShowErrorsResponse(ActionRuntime runtime) {
                return mayaa.provideShowErrorsResponse(runtime);
            }
        };
    }

    protected MayaaRenderingProvider createMayaaRenderingProvider() { // for #mayaa
        return new MayaaRenderingProvider().asDevelopment(mermaidlagConfig.isDevelopmentHere());
    }

    protected ThymeleafRenderingProvider createThymeleafRenderingProvider() { // will be deleted
        return new ThymeleafRenderingProvider().asDevelopment(mermaidlagConfig.isDevelopmentHere());
    }
}
