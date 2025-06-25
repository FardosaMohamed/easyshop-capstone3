package org.yearup.data.mysql;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired; // ✅ NEW: Import Autowired
import org.yearup.data.ShoppingCartDao; // Import ShoppingCartDao
import org.yearup.models.Product;

import java.math.BigDecimal;


import static org.junit.jupiter.api.Assertions.assertEquals;

class MySqlProductDaoTest extends BaseDaoTestClass
{
    private MySqlProductDao dao;

    @Autowired // NEW: Autowire ShoppingCartDao for the test context
    private ShoppingCartDao shoppingCartDao; // Declare shoppingCartDao field

    @BeforeEach
    public void setup()
    {
        //  THE FIX: Pass both dataSource and shoppingCartDao to the constructor
        dao = new MySqlProductDao(dataSource, shoppingCartDao);
    }

    @Test
    public void getById_shouldReturn_theCorrectProduct()
    {
        // arrange
        int productId = 1;
        Product expected = new Product()
        {{
            setProductId(1);
            setName("Smartphone");
            setPrice(new BigDecimal("499.99"));
            setCategoryId(1);
            setDescription("A powerful and feature-rich smartphone for all your communication needs.");
            setColor("Black");
            setStock(50);
            setFeatured(false);
            setImageUrl("smartphone.jpg");
        }};

        // act
        var actual = dao.getById(productId);

        // assert
        assertEquals(expected.getPrice(), actual.getPrice(), "Because I tried to get product 1 from the database.");
    }


}
