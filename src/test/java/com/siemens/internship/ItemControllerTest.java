package com.siemens.internship;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.siemens.internship.Item;
import com.siemens.internship.ItemController;
import com.siemens.internship.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@SpringBootTest
class ItemControllerTest {

    @Autowired
    private ItemController itemController;

    @MockBean
    private ItemService itemService;

    private List<Item> mockItems;
    private Item mockItem;

    @BeforeEach
    void setUp() {
        mockItem = new Item(1L, "Item 1", "Description 1", "NEW", "email1@example.com");
        mockItems = Arrays.asList(mockItem);
    }

    @Test
    void testGetAllItems() {
        when(itemService.findAll()).thenReturn(mockItems);

        ResponseEntity<List<Item>> response = itemController.getAllItems();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(itemService, times(1)).findAll();
    }


    @Test
    void testGetItemById() {
        when(itemService.findById(1L)).thenReturn(Optional.of(mockItem));

        ResponseEntity<Item> response = itemController.getItemById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Item 1", response.getBody().getName());
        verify(itemService, times(1)).findById(1L);
    }

    @Test
    void testGetItemByIdNotFound() {
        when(itemService.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Item> response = itemController.getItemById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(itemService, times(1)).findById(1L);
    }

    @Test
    void testUpdateItem() {
        when(itemService.findById(1L)).thenReturn(Optional.of(mockItem));
        when(itemService.save(any(Item.class))).thenReturn(mockItem);

        ResponseEntity<Item> response = itemController.updateItem(1L, mockItem);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Item 1", response.getBody().getName());
        verify(itemService, times(1)).findById(1L);
        verify(itemService, times(1)).save(any(Item.class));
    }

    @Test
    void testUpdateItemNotFound() {
        when(itemService.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Item> response = itemController.updateItem(1L, mockItem);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(itemService, times(1)).findById(1L);
        verify(itemService, times(0)).save(any(Item.class)); // Verificăm că nu s-a apelat metoda save
    }

    @Test
    void testDeleteItem() {
        when(itemService.findById(1L)).thenReturn(Optional.of(mockItem));
        doNothing().when(itemService).deleteById(1L);

        ResponseEntity<Void> response = itemController.deleteItem(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(itemService, times(1)).findById(1L);
        verify(itemService, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteItemNotFound() {
        when(itemService.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = itemController.deleteItem(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(itemService, times(1)).findById(1L);
        verify(itemService, times(0)).deleteById(1L); // Verificăm că nu s-a apelat metoda deleteById
    }

    @Test
    void testProcessItems() throws Exception {
        when(itemService.processItemsAsync()).thenReturn(CompletableFuture.completedFuture(mockItems));

        ResponseEntity<List<Item>> response = itemController.processItems();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(itemService, times(1)).processItemsAsync();
    }

    @Test
    void testProcessItemsWithError() throws Exception {
        // Arrange: Mocking the service to throw ExecutionException using doThrow
        doThrow(new ExecutionException("Error", new Throwable())).when(itemService).processItemsAsync();

        // Act: Call the controller method
        ResponseEntity<List<Item>> response = itemController.processItems();

        // Assert: Verify that the response returns an INTERNAL_SERVER_ERROR status code
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

}
