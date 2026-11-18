package com.hospital.dao;

import com.hospital.model.Department;
import java.util.Optional;

public interface DepartmentDAO extends GenericDAO<Department, Integer> {
    Optional<Department> findByName(String name);
}
