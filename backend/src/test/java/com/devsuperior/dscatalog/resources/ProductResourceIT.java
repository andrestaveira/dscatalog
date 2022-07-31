package com.devsuperior.dscatalog.resources;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.tests.Factory;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest // Teste de integracao
@AutoConfigureMockMvc
@Transactional
public class ProductResourceIT {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private long existingId;
	private long nonExistingId;
	private long countTotalProducts;

	@BeforeEach
	void setUp() throws Exception {
		existingId = 1L;
		nonExistingId = 1000L;
		countTotalProducts = 25L;
	}
	
	@Test
	public void findAllShouldReturnSortedPageWhenSortByName() throws Exception {
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders
				.get("/products?page=0&size=12&sort=name,asc")
				.accept(MediaType.APPLICATION_JSON));
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(countTotalProducts));
		result.andExpect(MockMvcResultMatchers.jsonPath("$.content").exists());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value("Macbook Pro"));
		result.andExpect(MockMvcResultMatchers.jsonPath("$.content[1].name").value("PC Gamer"));
		result.andExpect(MockMvcResultMatchers.jsonPath("$.content[2].name").value("PC Gamer Alfa"));
	}
	
	@Test
	public void updateShouldReturnProductDTOWhenIdExists() throws Exception {
		ProductDTO productDto = Factory.createProductDTO();
		String expectedName = productDto.getName();
		String jsonBody = objectMapper.writeValueAsString(productDto);
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders
				.put("/products/{id}", existingId)
				.content(jsonBody).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
		result.andExpect(status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(existingId));
		result.andExpect(MockMvcResultMatchers.jsonPath("$.name").value(expectedName));
	}
	
	@Test
	public void updateShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
		ProductDTO productDto = Factory.createProductDTO();
		String jsonBody = objectMapper.writeValueAsString(productDto);
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders
				.put("/products/{id}", nonExistingId)
				.content(jsonBody).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
		result.andExpect(status().isNotFound());
	}
}
