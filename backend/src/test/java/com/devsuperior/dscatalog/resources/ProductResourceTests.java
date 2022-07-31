package com.devsuperior.dscatalog.resources;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.services.ProductService;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.Factory;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ProductResource.class)
public class ProductResourceTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProductService service;

	@Autowired
	private ObjectMapper objectMapper;

	private PageImpl<ProductDTO> page;
	private long existingId;
	private long nonExistingId;
	private long dependentId;
	private ProductDTO productDto;

	@BeforeEach
	void setUp() throws Exception {
		existingId = 1L;
		nonExistingId = 2L;
		dependentId = 3L;
		productDto = Factory.createProductDTO();
		page = new PageImpl<>(List.of(productDto));
		
		Mockito.when(service.insert(ArgumentMatchers.any())).thenReturn(productDto);
		
		Mockito.when(service.findAllPaged(ArgumentMatchers.any())).thenReturn(page);
		Mockito.when(service.findById(existingId)).thenReturn(productDto);
		Mockito.when(service.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);
		
		Mockito.when(service.update(ArgumentMatchers.eq(existingId), ArgumentMatchers.any())).thenReturn(productDto);
		Mockito.when(service.update(ArgumentMatchers.eq(nonExistingId), ArgumentMatchers.any())).thenThrow(ResourceNotFoundException.class);

		Mockito.doNothing().when(service).delete(existingId);
		Mockito.doThrow(ResourceNotFoundException.class).when(service).delete(nonExistingId);
		Mockito.doThrow(DatabaseException.class).when(service).delete(dependentId);
	}

	@Test
	public void insertShouldReturnProductDTO() throws Exception {
		String jsonBody = objectMapper.writeValueAsString(productDto);
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders
				.post("/products")
				.content(jsonBody).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
		result.andExpect(status().isCreated());
	}
	
	@Test
	public void updateShouldReturnProductDTOWhenIdExists() throws Exception {
		String jsonBody = objectMapper.writeValueAsString(productDto);
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders
				.put("/products/{id}", existingId)
				.content(jsonBody).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
		result.andExpect(status().isOk());
	}

	@Test
	public void updateShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
		String jsonBody = objectMapper.writeValueAsString(productDto);
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders
				.put("/products/{id}", nonExistingId)
				.content(jsonBody).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
		result.andExpect(status().isNotFound());
	}

	@Test
	public void findAllShouldReturnPage() throws Exception {
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/products").accept(MediaType.APPLICATION_JSON));
		result.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void findByIdShouldReturnProductWhenIdExists() throws Exception {
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders
				.get("/products/{id}", existingId)
				.accept(MediaType.APPLICATION_JSON));
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.name").exists());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.description").exists());
	}

	@Test
	public void findByIdShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders
				.get("/products/{id}", nonExistingId)
				.accept(MediaType.APPLICATION_JSON));
		result.andExpect(MockMvcResultMatchers.status().isNotFound());
	}
	
	@Test
	public void deleteShouldReturnNoContentWhenIdExists() throws Exception {
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders
				.delete("/products/{id}", existingId)
				.accept(MediaType.APPLICATION_JSON));
		result.andExpect(MockMvcResultMatchers.status().isNoContent());
	}
	
	@Test
	public void deleteShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders
				.delete("/products/{id}", nonExistingId)
				.accept(MediaType.APPLICATION_JSON));
		result.andExpect(MockMvcResultMatchers.status().isNotFound());
	}
}
