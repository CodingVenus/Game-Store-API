package com.company.productservice.dao;

import com.company.productservice.dto.Product;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ProductDaoTest {

    @Autowired
    private ProductDao dao;


    @Before
    public void setUp() throws Exception {
        List<Product> products = dao.getAllProducts();
        products.forEach(product -> dao.deleteProduct(product.getProductId()));
    }

    @Test
    public void addGetProduct() {

        Product product = new Product();
        product.setProductName("Nintendo");
        product.setProductDescription("Japanese multinational electronics ");
        product.setListPrice(new BigDecimal("199.99").setScale(2));
        product.setUnitCost(new BigDecimal("299.00").setScale(2));

        Product product1 = dao.addProduct(product);
        Product addedProduct = dao.getProduct(product1.getProductId());

        assertEquals(product1, addedProduct);

    }

    @Test
    public void getAllProducts() {

        Product product = new Product();
        product.setProductName("Nintendo");
        product.setProductDescription("Japanese multinational electronics ");
        product.setListPrice(new BigDecimal("199.99").setScale(2));
        product.setUnitCost(new BigDecimal("299.00").setScale(2));
        product = dao.addProduct(product);

        assertEquals(dao.getAllProducts().size(),1);
        assertEquals(dao.getAllProducts().get(0),product);
    }

    @Test
    public void updateProduct() {

        Product product = new Product();
        product.setProductName("Nintendo");
        product.setProductDescription("Japanese multinational electronics ");
        product.setListPrice(new BigDecimal("199.99").setScale(2));
        product.setUnitCost(new BigDecimal("299.00").setScale(2));
        product = dao.addProduct(product);

        product.setProductName("Nintendo new version");
        dao.updateProduct(product);

        assertEquals(dao.getProduct(product.getProductId()),product);
    }

    @Test
    public void deleteProduct() {

        Product product = new Product();
        product.setProductName("Nintendo");
        product.setProductDescription("Japanese multinational electronics ");
        product.setListPrice(new BigDecimal("199.99").setScale(2));
        product.setUnitCost(new BigDecimal("299.00").setScale(2));
        product = dao.addProduct(product);

        dao.deleteProduct(product.getProductId());

        assertNull(dao.getProduct(product.getProductId()));
    }


}