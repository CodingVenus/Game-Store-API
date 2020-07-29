package com.company.customerservice.dao;

import com.company.customerservice.dto.Customer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class CustomerDaoTest {

    @Autowired
    private CustomerDao dao;

    @Before
    public void setUp() throws Exception {
        List<Customer> customerList = dao.getAllCustomers();
        customerList.forEach(customer -> dao.deleteCustomer(customer.getCustomerId()));
    }

    @Test
    public void addGetCustomer() {

        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Cooper");
        customer.setStreet("230 Springfield");
        customer.setCity("SilverSpring");
        customer.setZip("07065");
        customer.setEmail("john@gmail.com");
        customer.setPhone("2035686745");

        Customer customer1 = dao.addCustomer(customer);
        Customer addedCustomer = dao.getCustomer(customer1.getCustomerId());

        assertEquals(customer1, addedCustomer);
    }

    @Test
    public void getAllCustomers() {
        //Arranging
        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Cooper");
        customer.setStreet("230 Springfield");
        customer.setCity("SilverSpring");
        customer.setZip("07065");
        customer.setEmail("john@gmail.com");
        customer.setPhone("2035686745");
        customer = dao.addCustomer(customer);

        //Asserting
        assertEquals(dao.getAllCustomers().size(),1);
        assertEquals(dao.getAllCustomers().get(0),customer);
    }

    @Test
    public void updateCustomer() {
        //Arranging
        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Cooper");
        customer.setStreet("230 Springfield");
        customer.setCity("SilverSpring");
        customer.setZip("07065");
        customer.setEmail("john@gmail.com");
        customer.setPhone("2035686745");
        customer = dao.addCustomer(customer);

        //Updating Customer
        customer.setEmail("john@yahoo.com");
        dao.updateCustomer(customer);

        //Asserting
        assertEquals(dao.getCustomer(customer.getCustomerId()),customer);


    }

    @Test
    public void deleteCustomer() {
        //Arranging
        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Cooper");
        customer.setStreet("230 Springfield");
        customer.setCity("SilverSpring");
        customer.setZip("07065");
        customer.setEmail("john@gmail.com");
        customer.setPhone("2035686745");
        customer = dao.addCustomer(customer);

        dao.deleteCustomer(customer.getCustomerId());
        //Asserting
        assertNull(dao.getCustomer(customer.getCustomerId()));

    }
}