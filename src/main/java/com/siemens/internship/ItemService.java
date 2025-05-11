package com.siemens.internship;

import jakarta.annotation.PreDestroy;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Service class for managing items.
 * Provides methods to perform CRUD operations and process items asynchronously.
 */
@Service
public class ItemService {
    private static final Logger LOGGER = Logger.getLogger(ItemService.class.getName());

    @Autowired
    private ItemRepository itemRepository;
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    @Getter
    private final List<Item> processedItems = new CopyOnWriteArrayList<>();
    @Getter
    private final AtomicInteger processedCount = new AtomicInteger(0);

    /**
     * Finds all items in the database.
     *
     * @return List of all items.
     */
    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    /**
     * Finds an item by its ID.
     * @param id The ID of the item to find.
     * @return An Optional containing the found item, or empty if not found.
     */
    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    /**
     * Saves an item to the database.
     * @param item The item to save.
     * @return The saved item.
     */
    public Item save(Item item) {
        return itemRepository.save(item);
    }

    /**
     * Deletes an item by its ID.
     * @param id The ID of the item to delete.
     */
    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }


    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     *
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */
    /**
     * Asynchronously processes all items from the database.
     *
     * This implementation:
     * 1. Creates a CompletableFuture for each item that needs processing
     * 2. Tracks all futures in a list to ensure we wait for all of them to complete
     * 3. Collects successfully processed items only after confirming they were processed
     * 4. Handles exceptions for individual item processing without failing the entire batch
     * 5. Returns a properly completed future with all successfully processed items
     *
     * @return A CompletableFuture that completes with a list of all successfully processed items
     */
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() throws ExecutionException, InterruptedException{
        List<Long> itemIds = itemRepository.findAllIds();
        LOGGER.info("Starting async processing of " + itemIds.size() + " items");

        // Folosim atributele de instanță deja existente
        // processedCount și processedItems sunt deja declarate la nivelul clasei, deci nu mai trebuie create local.

        // Lista de futures pentru a urmări finalizarea taskurilor asincrone
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Procesăm fiecare item asincron
        for (Long id : itemIds) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // Sleep pentru a simula procesarea unui item
                    Thread.sleep(100);

                    // Încercăm să găsim itemul
                    Optional<Item> optionalItem = itemRepository.findById(id);
                    if (optionalItem.isEmpty()) {
                        LOGGER.warning("Item not found with id: " + id);
                        return; // Dacă itemul nu există, continuăm cu următorul item
                    }

                    Item item = optionalItem.get();
                    LOGGER.info("Processing item: " + item.getId() + " - " + item.getName());

                    // Actualizăm statusul itemului
                    item.setStatus("PROCESSED");

                    // Salvăm itemul procesat în baza de date
                    itemRepository.save(item);

                    // Adăugăm itemul procesat în lista thread-safe
                    processedItems.add(item);

                    // Incrementăm contorul de iteme procesate folosind AtomicInteger pentru a asigura thread-safety
                    processedCount.incrementAndGet();

                } catch (InterruptedException e) {
                    LOGGER.severe("Processing interrupted for item id: " + id);
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    LOGGER.severe("Failed to process item with id: " + id + " due to: " + e.getMessage());
                }
            }, executor);

            // Adăugăm taskul asincron în lista de futures
            futures.add(future);
        }

        // Vom aștepta ca toate taskurile să se termine
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    // Logăm câte iteme au fost procesate cu succes
                    LOGGER.info("Completed processing. Successfully processed: " + processedCount.get() + " items.");

                    // Returnăm lista itemelor procesate
                    return new ArrayList<>(processedItems); // Returnăm o copie a listei procesate
                });
    }

}

