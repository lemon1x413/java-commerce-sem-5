package com.example.spacecatsmarket.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.spacecatsmarket.repository.InMemoryCategoryRepository;
import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Product Controller IT")
class ProductControllerIT {

    private static final String URL = "/api/v1/products";
    private static final String YARN_ID = InMemoryCategoryRepository.COSMIC_YARN_ID.toString();
    private static final String VALIDATION_TYPE = "https://cosmo-cats.market/errors/validation";

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("positive scenarios")
    class Positive {

        @Test
        void createReturns201WithLocationAndBody() throws Exception {
            create(body("Star Yarn", "floats", "29.99", YARN_ID))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString(URL + "/")))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name").value("Star Yarn"))
                    .andExpect(jsonPath("$.price").value(29.99))
                    .andExpect(jsonPath("$.categoryId").value(YARN_ID))
                    .andExpect(jsonPath("$.categoryName").value("Cosmic Yarn"));
        }

        @Test
        void createAcceptsMissingDescription() throws Exception {
            create("{\"name\":\"Comet Ball\",\"price\":1,\"categoryId\":\"" + YARN_ID + "\"}")
                    .andExpect(status().isCreated());
        }

        @Test
        void getByIdReturnsCreatedProduct() throws Exception {
            String id = createdId();
            mockMvc.perform(get(URL + "/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id));
        }

        @Test
        void listReturnsPageWithHasNext() throws Exception {
            mockMvc.perform(get(URL).param("page", "0").param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(2))
                    .andExpect(jsonPath("$.hasNext").value(true));
        }

        @Test
        void listPageBeyondEndIsEmpty() throws Exception {
            mockMvc.perform(get(URL).param("page", "1000").param("size", "100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.hasNext").value(false));
        }

        @Test
        void updateReturns200WithNewValues() throws Exception {
            String id = createdId();
            mockMvc.perform(put(URL + "/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body("Lunar Yarn", "updated", "5.00",
                                    InMemoryCategoryRepository.SPACE_FOOD_ID.toString())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.name").value("Lunar Yarn"))
                    .andExpect(jsonPath("$.categoryName").value("Space Food"));
        }

        @Test
        void deleteReturns204ThenProductIsGone() throws Exception {
            String id = createdId();
            mockMvc.perform(delete(URL + "/{id}", id)).andExpect(status().isNoContent());
            mockMvc.perform(get(URL + "/{id}", id)).andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("negative scenarios")
    class Negative {

        @ParameterizedTest(name = "[{index}] {0}")
        @CsvSource(delimiter = '|', value = {
                "blank name           | {\"name\":\" \",\"price\":1,\"categoryId\":\"11111111-1111-1111-1111-111111111111\"} | name",
                "name too short       | {\"name\":\"S\",\"price\":1,\"categoryId\":\"11111111-1111-1111-1111-111111111111\"} | name",
                "name not cosmic      | {\"name\":\"Plain Yarn\",\"price\":1,\"categoryId\":\"11111111-1111-1111-1111-111111111111\"} | name",
                "missing price        | {\"name\":\"Star Yarn\",\"categoryId\":\"11111111-1111-1111-1111-111111111111\"} | price",
                "zero price           | {\"name\":\"Star Yarn\",\"price\":0,\"categoryId\":\"11111111-1111-1111-1111-111111111111\"} | price",
                "negative price       | {\"name\":\"Star Yarn\",\"price\":-5,\"categoryId\":\"11111111-1111-1111-1111-111111111111\"} | price",
                "missing categoryId   | {\"name\":\"Star Yarn\",\"price\":1} | categoryId",
        })
        void createRejectsInvalidBody(String scenario, String json, String field) throws Exception {
            create(json)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.type").value(VALIDATION_TYPE))
                    .andExpect(jsonPath("$.title").value("Validation failed"))
                    .andExpect(jsonPath("$.detail").value(containsString("Field '" + field + "'")))
                    .andExpect(jsonPath("$.instance").value(URL));
        }

        @Test
        void createRejectsTooLongDescription() throws Exception {
            create(body("Star Yarn", "x".repeat(501), "1", YARN_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value(containsString("Field 'description'")));
        }

        @Test
        void createReportsEveryInvalidField() throws Exception {
            create("{\"name\":\"\",\"price\":-1}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value(containsString("Field 'name'")))
                    .andExpect(jsonPath("$.detail").value(containsString("Field 'price'")))
                    .andExpect(jsonPath("$.detail").value(containsString("Field 'categoryId'")));
        }

        @Test
        void createRejectsUnknownCategory() throws Exception {
            create(body("Star Yarn", null, "1", UUID.randomUUID().toString()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.type").value("https://cosmo-cats.market/errors/unknown-category"))
                    .andExpect(jsonPath("$.title").value("Unknown category"));
        }

        @Test
        void createRejectsMalformedJson() throws Exception {
            create("{\"name\":").andExpect(status().isBadRequest());
        }

        @Test
        void updateRejectsInvalidBody() throws Exception {
            String id = createdId();
            mockMvc.perform(put(URL + "/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body("Plain Yarn", null, "1", YARN_ID)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value(containsString("Field 'name'")));
        }

        @ParameterizedTest(name = "page={0}, size={1}")
        @CsvSource({"-1, 10, page", "0, 0, size", "0, 101, size"})
        void listRejectsOutOfRangePaging(String page, String size, String param) throws Exception {
            mockMvc.perform(get(URL).param("page", page).param("size", size))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.type").value(VALIDATION_TYPE))
                    .andExpect(jsonPath("$.detail").value(containsString("Parameter '" + param + "'")));
        }

        @Test
        void getRejectsNonUuidId() throws Exception {
            mockMvc.perform(get(URL + "/not-a-uuid")).andExpect(status().isBadRequest());
        }

        @Test
        void getUnknownIdReturns404() throws Exception {
            UUID id = UUID.randomUUID();
            mockMvc.perform(get(URL + "/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Resource not found"))
                    .andExpect(jsonPath("$.instance").value(endsWith(id.toString())));
        }

        @Test
        void updateUnknownIdReturns404() throws Exception {
            mockMvc.perform(put(URL + "/{id}", UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body("Star Yarn", null, "1", YARN_ID)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void deleteUnknownIdReturns404() throws Exception {
            mockMvc.perform(delete(URL + "/{id}", UUID.randomUUID())).andExpect(status().isNotFound());
        }
    }

    private ResultActions create(String json) throws Exception {
        return mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private String createdId() throws Exception {
        String response = create(body("Star Yarn", "floats", "29.99", YARN_ID))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.id");
    }

    private static String body(String name, String description, String price, String categoryId) {
        String desc = description == null ? "" : ",\"description\":\"" + description + "\"";
        return "{\"name\":\"" + name + "\"" + desc + ",\"price\":" + price + ",\"categoryId\":\"" + categoryId + "\"}";
    }
}
