package com.company.customerservice.service;

import com.company.customerservice.dao.CustomerDao;
import com.company.customerservice.dao.CustomerDaoJdbcTemplateImpl;
import com.company.customerservice.dto.Customer;
import com.company.customerservice.exception.NotFoundException;
import com.company.customerservice.viewModel.CustomerViewModel;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ServiceLayerTest {

    private ServiceLayer serviceLayer;
    private CustomerDao customerDao;

    @Before
    public void setUp() throws Exception {
        setUpCustomerDaoMock();
        serviceLayer = new ServiceLayer(customerDao);
    }

    @Test
    public void saveCustomer() {

        CustomerViewModel viewModel = new CustomerViewModel();
        viewModel.setFirstName("John");
        viewModel.setLastName("Cooper");
        viewModel.setStreet("230 Springfield");
        viewModel.setCity("SilverSpring");
        viewModel.setZip("07065");
        viewModel.setEmail("john@gmail.com");
        viewModel.setPhone("2035686745");

        CustomerViewModel fromService = serviceLayer.saveCustomer(viewModel);

        viewModel.setCustomerId(1);

        //Asserting
        assertEquals(viewModel, fromService);
    }

    @Test
    public void findCustomer() {

        CustomerViewModel viewModel = new CustomerViewModel();
        viewModel.setCustomerId(1);
        viewModel.setFirstName("John");
        viewModel.setLastName("Cooper");
        viewModel.setStreet("230 Springfield");
        viewModel.setCity("SilverSpring");
        viewModel.setZip("07065");
        viewModel.setEmail("john@gmail.com");
        viewModel.setPhone("2035686745");

        //Asserting
        assertEquals(viewModel, serviceLayer.findCustomer(1));
    }

    @Test
    public void findAllCustomers() {

        CustomerViewModel viewModel = new CustomerViewModel();
        viewModel.setCustomerId(1);
        viewModel.setFirstName("John");
        viewModel.setLastName("Cooper");
        viewModel.setStreet("230 Springfield");
        viewModel.setCity("SilverSpring");
        viewModel.setZip("07065");
        viewModel.setEmail("john@gmail.com");
        viewModel.setPhone("2035686745");

        //Asserting
        assertEquals(1,serviceLayer.findAllCustomers().size());
        assertEquals(viewModel, serviceLayer.findAllCustomers().get(0));

    }

    @Test(expected = NotFoundException.class)
    public void updateCustomer() {

        CustomerViewModel viewModel = new CustomerViewModel();
        viewModel.setCustomerId(1);
        viewModel.setFirstName("John");
        viewModel.setLastName("Cooper");
        viewModel.setStreet("230 Springfield");
        viewModel.setCity("SilverSpring");
        viewModel.setZip("07065");
        viewModel.setEmail("john@gmail.com");
        viewModel.setPhone("2035686745");

        assertEquals(viewModel, serviceLayer.updateCustomer(viewModel));

        //A customer that doesn't exist in DB
        CustomerViewModel fakeCustomer = new CustomerViewModel();
        fakeCustomer.setCustomerId(2);
        fakeCustomer.setFirstName("John");
        fakeCustomer.setLastName("Cooper");
        fakeCustomer.setStreet("230 Springfield");
        fakeCustomer.setCity("SilverSpring");
        fakeCustomer.setZip("07065");
        fakeCustomer.setEmail("john@gmail.com");
        fakeCustomer.setPhone("2035686745");

        serviceLayer.updateCustomer(fakeCustomer);
    }

    @Test(expected = NotFoundException.class)
    public void removeCustomer() {
        assertEquals(serviceLayer.removeCustomer(1), "Customer [1] deleted successfully!");

        //A customer that doesn't exist in DB
        serviceLayer.removeCustomer(2);
    }

    private void setUpCustomerDaoMock() {

        customerDao = mock(CustomerDaoJdbcTemplateImpl.class);

        // Output
        Customer output = new Customer();
        output.setCustomerId(1);
        output.setFirstName("John");
        output.setLastName("Cooper");
        output.setStreet("230 Springfield");
        output.setCity("SilverSpring");
        output.setZip("07065");
        output.setEmail("john@gmail.com");
        output.setPhone("2035686745");

        // Input
        Customer input = new Customer();
        input.setFirstName("John");
        input.setLastName("Cooper");
        input.setStreet("230 Springfield");
        input.setCity("SilverSpring");
        input.setZip("07065");
        input.setEmail("john@gmail.com");
        input.setPhone("2035686745");

        // All Customers
        List<Customer> customers = new ArrayList<>();
        customers.add(output);

        doReturn(output).when(customerDao).addCustomer(input);
        doReturn(output).when(customerDao).getCustomer(1);
        doReturn(customers).when(customerDao).getAllCustomers();
        doNothing().when(customerDao).updateCustomer(input);
    }
}