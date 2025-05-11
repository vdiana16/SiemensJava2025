package com.siemens.internship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ItemServiceTest {
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    private List<Item> mockItems;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setăm o listă de itemi pentru a simula comportamentul repository-ului
        mockItems = Arrays.asList(
                new Item(1L, "Item 1", "Description 1", "NEW", "email1@example.com"),
                new Item(2L, "Item 2", "Description 2", "NEW", "email2@example.com")
        );
    }

    @Test
    void testFindAll() {
        when(itemRepository.findAll()).thenReturn(mockItems);

        List<Item> items = itemService.findAll();

        assertNotNull(items);
        assertEquals(2, items.size());
        verify(itemRepository, times(1)).findAll();
    }

    @Test
    void testFindById() {
        Item item = mockItems.get(0);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Optional<Item> foundItem = itemService.findById(1L);

        assertTrue(foundItem.isPresent());
        assertEquals(item, foundItem.get());
        verify(itemRepository, times(1)).findById(1L);
    }

    @Test
    void testSave() {
        Item item = mockItems.get(0);
        when(itemRepository.save(item)).thenReturn(item);

        Item savedItem = itemService.save(item);

        assertNotNull(savedItem);
        assertEquals(item.getId(), savedItem.getId());
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    void testDeleteById() {
        doNothing().when(itemRepository).deleteById(1L);

        itemService.deleteById(1L);

        verify(itemRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testProcessItemsAsync_success() throws ExecutionException, InterruptedException {
        Item item1 = new Item(1L, "Item 1", "Description 1", "NEW", "test1@example.com");
        Item item2 = new Item(2L, "Item 2", "Description 2", "NEW", "test2@example.com");

        when(itemRepository.findAllIds()).thenReturn(Arrays.asList(1L, 2L));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));
        when(itemRepository.save(any(Item.class))).thenAnswer((Answer<Item>) invocation -> invocation.getArgument(0));

        CompletableFuture<List<Item>> result = itemService.processItemsAsync();

        List<Item> processedItems = result.get();

        assertEquals(2, processedItems.size());
        assertTrue(processedItems.contains(item1));
        assertTrue(processedItems.contains(item2));
        assertEquals(2, itemService.getProcessedCount().get()); // Verificăm că procesarea a fost făcută corect
    }

    @Test
    public void testProcessItemsAsync_itemNotFound() throws ExecutionException, InterruptedException {
        Item item1 = new Item(1L, "Item 1", "Description 1", "NEW", "test1@example.com");

        when(itemRepository.findAllIds()).thenReturn(Arrays.asList(1L, 2L));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.empty()); // Al doilea item nu există
        when(itemRepository.save(any(Item.class))).thenAnswer((Answer<Item>) invocation -> invocation.getArgument(0));

        CompletableFuture<List<Item>> result = itemService.processItemsAsync();

        List<Item> processedItems = result.get();

        assertEquals(1, processedItems.size());
        assertTrue(processedItems.contains(item1));
        assertEquals(1, itemService.getProcessedCount().get()); // Verificăm că doar un item a fost procesat
    }

    @Test
    public void testProcessItemsAsync_withError() throws ExecutionException, InterruptedException {
        Item item1 = new Item(1L, "Item 1", "Description 1", "NEW", "test1@example.com");

        when(itemRepository.findAllIds()).thenReturn(Arrays.asList(1L));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.save(any(Item.class))).thenThrow(new RuntimeException("Database error"));

        CompletableFuture<List<Item>> result = itemService.processItemsAsync();

        List<Item> processedItems = result.get();

        assertEquals(0, processedItems.size());
        assertEquals(0, itemService.getProcessedCount().get());
    }
}
