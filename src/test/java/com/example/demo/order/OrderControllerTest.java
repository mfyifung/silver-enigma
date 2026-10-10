package com.example.demo.order;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.oneOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/** Full context: the real seeder populates the test SQLite DB (14 rows). */
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired MockMvc mvc;
    @Autowired OrderRepository repository;

    @Test
    void listsAllSeededOrders() throws Exception {
        mvc.perform(get("/api/orders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(14)))
            .andExpect(jsonPath("$[0].orderId", is("00001")))
            .andExpect(jsonPath("$[0].date", is("2026-09-04")));
    }

    @Test
    void filtersByStatus() throws Exception {
        mvc.perform(get("/api/orders").param("status", "Completed"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(4)))
            .andExpect(jsonPath("$[*].status", everyItem(is("Completed"))));
    }

    @Test
    void statusAcceptsCsvAndRepeatedParams() throws Exception {
        mvc.perform(get("/api/orders").param("status", "Completed,On Hold"))
            .andExpect(jsonPath("$", hasSize(6)))
            .andExpect(jsonPath("$[*].status", everyItem(oneOf("Completed", "On Hold"))));
        mvc.perform(get("/api/orders").param("status", "Completed").param("status", "On Hold"))
            .andExpect(jsonPath("$", hasSize(6)));
    }

    @Test
    void mixedRepeatedAndCsvParamsAreAllApplied() throws Exception {
        mvc.perform(get("/api/orders").param("status", "Completed,On Hold").param("status", "Rejected"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(8)))
            .andExpect(jsonPath("$[*].status", everyItem(oneOf("Completed", "On Hold", "Rejected"))))
            .andExpect(jsonPath("$[?(@.status == 'Completed')]", hasSize(4)))
            .andExpect(jsonPath("$[?(@.status == 'On Hold')]", hasSize(2)))
            .andExpect(jsonPath("$[?(@.status == 'Rejected')]", hasSize(2)));
        mvc.perform(get("/api/orders").param("status", "Completed,Rejected").param("status", "On Hold"))
            .andExpect(jsonPath("$", hasSize(8)));
        mvc.perform(get("/api/orders").param("type", "Electronics, Accessories").param("type", "Mobile & Phone"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(6)))
            .andExpect(jsonPath("$[*].type", everyItem(oneOf("Electronics", "Accessories", "Mobile & Phone"))));
    }

    @Test
    void blankCsvPartsAreIgnored() throws Exception {
        mvc.perform(get("/api/orders").param("status", "Completed,,"))
            .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void combinesTypeStatusAndDate() throws Exception {
        mvc.perform(get("/api/orders").param("type", "Electronics,Health & Medicine").param("status", "Completed"))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[*].orderId", org.hamcrest.Matchers.contains("00001", "00013")));
        mvc.perform(get("/api/orders").param("date", "2026-09-04").param("type", "Mobile & Phone"))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].orderId", is("00010")));
    }

    @Test
    void rejectsMalformedDate() throws Exception {
        mvc.perform(get("/api/orders").param("date", "04/09/2026")).andExpect(status().isBadRequest());
    }

    @Test
    void metaIsDerivedFromData() throws Exception {
        mvc.perform(get("/api/orders/meta"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.types", hasSize(8)))
            .andExpect(jsonPath("$.statuses", hasSize(5)))
            .andExpect(jsonPath("$.statuses[0]", is("Completed")));
    }

    @Test
    void getsOneOrderOr404() throws Exception {
        Long id = repository.findAll().get(0).getId();
        mvc.perform(get("/api/orders/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.id", is(id.intValue())));
        mvc.perform(get("/api/orders/999999")).andExpect(status().isNotFound());
        mvc.perform(get("/api/orders/abc")).andExpect(status().isBadRequest());
    }
}
