package com.example.spacecatsmarket.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@DisplayName("CosmicInfoClient with WireMock stubs")
class CosmicInfoClientIT {

    private static final String FACTS_PATH = "/api/v1/facts";

    @RegisterExtension
    static WireMockExtension cosmicInfo = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private CosmicInfoClient client;

    @BeforeEach
    void setUp() {
        client = new CosmicInfoClient(cosmicInfo.baseUrl());
    }

    @Test
    void returnsFactFromStubAndSendsKeyword() {
        cosmicInfo.stubFor(get(urlPathEqualTo(FACTS_PATH))
                .withQueryParam("keyword", equalTo("comet"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("Comets are icy bodies that release gas near the Sun.")));

        assertThat(client.getCosmicFact("comet")).isEqualTo("Comets are icy bodies that release gas near the Sun.");
        cosmicInfo.verify(1, getRequestedFor(urlPathEqualTo(FACTS_PATH)).withQueryParam("keyword", equalTo("comet")));
    }

    @Test
    void encodesKeywordWithSpaces() {
        cosmicInfo.stubFor(get(urlPathEqualTo(FACTS_PATH))
                .withQueryParam("keyword", equalTo("black hole"))
                .willReturn(aResponse().withStatus(200).withBody("dense")));

        assertThat(client.getCosmicFact("black hole")).isEqualTo("dense");
    }

    @Test
    void throwsClientErrorOn404() {
        cosmicInfo.stubFor(get(urlPathEqualTo(FACTS_PATH)).willReturn(aResponse().withStatus(404)));

        assertThatThrownBy(() -> client.getCosmicFact("unknown"))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    @Test
    void throwsServerErrorOn503() {
        cosmicInfo.stubFor(get(urlPathEqualTo(FACTS_PATH)).willReturn(aResponse().withStatus(503)));

        assertThatThrownBy(() -> client.getCosmicFact("star"))
                .isInstanceOf(HttpServerErrorException.ServiceUnavailable.class);
    }
}
