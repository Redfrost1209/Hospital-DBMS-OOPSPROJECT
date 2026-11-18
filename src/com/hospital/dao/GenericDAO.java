package com.hospital.dao;

import java.util.List;
import java.util.Optional;

/**
 * Generic Data Access Object (DAO) interface.
 * Demonstrates OOP Generics, Abstraction, and the DAO Design Pattern.
 *
 * @param <T>  Entity Type
 * @param <ID> Primary Key Identifier Type
 */
public interface GenericDAO<T, ID> {
    T save(T entity);
    boolean update(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    boolean deleteById(ID id);
}
