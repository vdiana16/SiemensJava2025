package com.siemens.internship;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/items")
/**
 * Controller class for managing items.
 * Provides RESTful endpoints for CRUD operations and processing items asynchronously.
 */
public class ItemController {
    private static final Logger LOGGER = Logger.getLogger(ItemController.class.getName());

    @Autowired
    private ItemService itemService;

    /**
     * Endpoint to retrieve all items
     * @return ResponseEntity containing list of all items
     */
    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        LOGGER.info("GET request received for all items");
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    /**
     * Endpoint to create a new item
     * @param item The item data from request body
     * @param result Validation result
     * @return ResponseEntity with the created item or error
     */
    @PostMapping
    public ResponseEntity<Item> createItem(@Valid @RequestBody Item item, BindingResult result) {
        LOGGER.info("POST request received to create new item");

        if (result.hasErrors()) {
            LOGGER.warning("Validation errors found: " + result.getAllErrors());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(itemService.save(item), HttpStatus.CREATED);
    }

    /**
     * Endpoint to retrieve a specific item by ID
     * @param id The ID of the item to retrieve
     * @return ResponseEntity with the item if found
     */
    //Change the status code
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        LOGGER.info("GET request received for item with id: " + id);
        return itemService.findById(id)
                .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Endpoint to update an existing item
     * @param id The ID of the item to update
     * @param item The new item data
     * @return ResponseEntity with the updated item
     */
    //Change the status code
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
        LOGGER.info("PUT request received to update item with id: " + id);
        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isPresent()) {
            item.setId(id);
            return new ResponseEntity<>(itemService.save(item), HttpStatus.OK);
        } else {
            LOGGER.warning("Update failed: Item not found with id: " + id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Endpoint to delete an item
     * @param id The ID of the item to delete
     * @return ResponseEntity indicating success or failure
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        LOGGER.info("DELETE request received for item with id: " + id);
        /**itemService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.CONFLICT);
         */
        if (itemService.findById(id).isPresent()) {
            itemService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            LOGGER.warning("Delete failed: Item not found with id: " + id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Endpoint to process all items asynchronously
     * @return ResponseEntity with the list of successfully processed items
     */
    @GetMapping("/process")
    public ResponseEntity<List<Item>> processItems() {
        LOGGER.info("GET request received to process all items");

        try {
            CompletableFuture<List<Item>> futureItems = itemService.processItemsAsync();
            List<Item> processedItems = futureItems.get();
            return new ResponseEntity<>(processedItems, HttpStatus.OK);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.SEVERE, "Item processing was interrupted", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ExecutionException e) {
            LOGGER.log(Level.SEVERE, "Error during item processing", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
