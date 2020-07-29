package com.company.customerservice.service;

import com.company.customerservice.dao.CustomerDao;
import com.company.customerservice.dto.Customer;
import com.company.customerservice.exception.NotFoundException;
import com.company.customerservice.viewModel.CustomerViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServiceLayer {

    private CustomerDao dao;

    @Autowired
    public ServiceLayer(CustomerDao dao) {
        this.dao = dao;
    }


    public CustomerViewModel saveCustomer(CustomerViewModel customerViewModel){
        Customer customer = viewModelToModel(customerViewModel);

        customer = dao.addCustomer(customer);

        customerViewModel.setCustomerId(customer.getCustomerId());

        return customerViewModel;
    }

    public CustomerViewModel findCustomer(int customerId){
        Customer customer = dao.getCustomer(customerId);
        if(customer==null){
            throw new NotFoundException("Customer ID cannot be found in DB!");
        } else {
            return buildCustomerViewModel(customer);
        }
    }

    public List<CustomerViewModel> findAllCustomers(){
        List<Customer> customers = dao.getAllCustomers();
        List<CustomerViewModel> customerViewModels = new ArrayList<>();

        customers.forEach(customer -> customerViewModels.add(buildCustomerViewModel(customer)));

        return customerViewModels;
    }

    @Transactional
    public CustomerViewModel updateCustomer(CustomerViewModel customerViewModel){

        findCustomer(customerViewModel.getCustomerId());
        dao.updateCustomer(viewModelToModel(customerViewModel));
        return findCustomer(customerViewModel.getCustomerId());
    }

    @Transactional
    public String removeCustomer(int customerId){

        findCustomer(customerId);
        dao.deleteCustomer(customerId);
        return "Customer ["+customerId+"] deleted successfully!";
    }


    private Customer viewModelToModel(CustomerViewModel cvm){
        Customer customer = new Customer();
        customer.setCustomerId(cvm.getCustomerId());
        customer.setFirstName(cvm.getFirstName());
        customer.setLastName(cvm.getLastName());
        customer.setStreet(cvm.getStreet());
        customer.setCity(cvm.getCity());
        customer.setZip(cvm.getZip());
        customer.setEmail(cvm.getEmail());
        customer.setPhone(cvm.getPhone());
        return customer;
    }

    private CustomerViewModel buildCustomerViewModel(Customer customer){
        CustomerViewModel cvm = new CustomerViewModel();
        cvm.setCustomerId(customer.getCustomerId());
        cvm.setFirstName(customer.getFirstName());
        cvm.setLastName(customer.getLastName());
        cvm.setStreet(customer.getStreet());
        cvm.setCity(customer.getCity());
        cvm.setZip(customer.getZip());
        cvm.setEmail(customer.getEmail());
        cvm.setPhone(customer.getPhone());
        return cvm;
    }
}
