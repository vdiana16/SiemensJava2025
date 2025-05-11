package com.siemens.internship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository interface for Item entity.
 * Extends JpaRepository to inherit standard CRUD operations and custom query methods.
 */
public interface ItemRepository extends JpaRepository<Item, Long> {
    /**
     * Find all items in the database.
     * @return List of all items.
     */
    @Query("SELECT id FROM Item")
    List<Long> findAllIds();
}
