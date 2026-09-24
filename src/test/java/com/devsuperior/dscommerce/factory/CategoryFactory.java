package com.devsuperior.dscommerce.factory;

import com.devsuperior.dscommerce.entities.Category;

public class CategoryFactory {

    public static Category createCategory() {
        return new Category();
    }

    public static Category createCategory(Long id, String name) {
        return new Category(id, name);
    }


}
