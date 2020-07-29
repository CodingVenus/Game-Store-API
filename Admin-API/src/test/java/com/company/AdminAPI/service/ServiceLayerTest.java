package com.company.AdminAPI.service;

import com.company.AdminAPI.util.feign.*;
import com.company.AdminAPI.views.CustomerViewModel;
import com.company.AdminAPI.views.ProductViewModel;
import com.company.AdminAPI.views.input.InventoryInputModel;
import com.company.AdminAPI.views.input.InvoiceInputModel;
import com.company.AdminAPI.views.input.InvoiceItem;
import com.company.AdminAPI.views.input.LevelUpInputModel;
import com.company.AdminAPI.views.output.InventoryViewModel;
import com.company.AdminAPI.views.output.InvoiceItemViewModel;
import com.company.AdminAPI.views.output.InvoiceViewModel;
import com.company.AdminAPI.views.output.LevelUpViewModel;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ServiceLayerTest {

    private ServiceLayer serviceLayer;
    private ProductClient productClient;
    private InventoryClient inventoryClient;
    private CustomerClient customerClient;
    private InvoiceClient invoiceClient;
    private LevelUpClient levelUpClient;

    @Before
    public void setUp() {
        setUpCustomerClientMock();
        setUpProductClientMock();
        setUpInventoryClientMock();
        setUpInvoiceClientMock();
        setUpLevelUpClientMock();

        serviceLayer = new ServiceLayer(customerClient, productClient, inventoryClient, levelUpClient, invoiceClient);
    }


    @Test
    public void testLevelUp() {

        LevelUpInputModel input = new LevelUpInputModel();
        input.setCustomerId(1);
        input.setMemberDate(LocalDate.of(2005,5,5));
        input.setPoints(1000);


        CustomerViewModel customer = new CustomerViewModel();
        customer.setCustomerId(1);
        customer.setFirstName("Bradley");
        customer.setLastName("Cooper");
        customer.setStreet("230 Briant Park");
        customer.setCity("Jersey City");
        customer.setZip("67542");
        customer.setPhone("3047655463");
        customer.setEmail("cooper@gmail.com");

        LevelUpViewModel expectedOutput = new LevelUpViewModel();
        expectedOutput.setLevelUpId(5);
        expectedOutput.setCustomer(customer);
        expectedOutput.setMemberDate(LocalDate.of(2005,5,5));
        expectedOutput.setPoints(1000);

        List<LevelUpViewModel> expectedList = new ArrayList<>();
        expectedList.add(expectedOutput);

        LevelUpViewModel fromService = serviceLayer.saveLevelUp(input);
        assertEquals(expectedOutput, fromService);

        fromService = serviceLayer.findLevelUp(5);
        assertEquals(expectedOutput, fromService);

        fromService = serviceLayer.findLevelUpByCustomerId(1);
        assertEquals(expectedOutput, fromService);

        List<LevelUpViewModel> fromServiceList = serviceLayer.findAllLevelUps();
        assertEquals(expectedList, fromServiceList);

        expectedOutput.setPoints(1050);
        input.setPoints(1050);
        input.setLevelUpId(5);
        fromService = serviceLayer.updateLevelUp(input);
        assertEquals(expectedOutput, fromService);

        assertEquals("LevelUp [5] deleted successfully!",serviceLayer.removeLevelUp(5));
    }

    @Test
    public void testInventory() {

        InventoryInputModel input = new InventoryInputModel();
        input.setProductId(11);
        input.setQuantity(2);

        ProductViewModel product = new ProductViewModel();
        product.setProductId(11);
        product.setProductName("Nintendo");
        product.setProductDescription("Console");
        product.setListPrice(new BigDecimal(299.00));
        product.setUnitCost(new BigDecimal(199.00));

        InventoryViewModel expectedOutput = new InventoryViewModel();
        expectedOutput.setInventoryId(21);
        expectedOutput.setQuantity(2);
        expectedOutput.setProduct(product);

        List<InventoryViewModel> expectedList = new ArrayList<>();
        expectedList.add(expectedOutput);

        InventoryViewModel fromService = serviceLayer.saveInventory(input);
        assertEquals(expectedOutput, fromService);

        fromService = serviceLayer.findInventory(21);
        assertEquals(expectedOutput, fromService);

        List<InventoryViewModel> fromServiceList = serviceLayer.findAllInventories();
        assertEquals(expectedList, fromServiceList);

        expectedOutput.setQuantity(1);
        input.setQuantity(1);
        input.setInventoryId(21);
        fromService = serviceLayer.updateInventory(input);
        assertEquals(expectedOutput, fromService);

        assertEquals("Inventory [21] deleted successfully!",serviceLayer.removeInventory(21));
    }

    @Test
    public void findInvoiceAndFindAll() {
        // Expected Output
        // customer
        CustomerViewModel customer = new CustomerViewModel();
        customer.setCustomerId(1);
        customer.setFirstName("Bradley");
        customer.setLastName("Cooper");
        customer.setStreet("230 Briant Park");
        customer.setCity("Jersey City");
        customer.setZip("67542");
        customer.setPhone("3047655463");
        customer.setEmail("cooper@gmail.com");
        // product
        ProductViewModel product = new ProductViewModel();
        product.setProductId(11);
        product.setProductName("Nintendo");
        product.setProductDescription("Console");
        product.setListPrice(new BigDecimal(299.00));
        product.setUnitCost(new BigDecimal(199.00));
        // inventory
        InventoryViewModel inventory = new InventoryViewModel();
        inventory.setInventoryId(21);
        inventory.setQuantity(2);
        inventory.setProduct(product);
        // invoiceItem
        InvoiceItemViewModel invoiceItem = new InvoiceItemViewModel();
        invoiceItem.setInvoiceId(31);
        invoiceItem.setInvoiceItemId(41);
        invoiceItem.setInventory(inventory);
        invoiceItem.setListPrice(new BigDecimal(299.00));
        invoiceItem.setQuantity(1);
        List<InvoiceItemViewModel> invoiceItems = new ArrayList<>();
        invoiceItems.add(invoiceItem);
        //invoice
        InvoiceViewModel expectedOutput = new InvoiceViewModel();
        expectedOutput.setInvoiceId(31);
        expectedOutput.setPurchaseDate(LocalDate.of(2019,10,10));
        expectedOutput.setMemberPoints(1050);
        expectedOutput.setCustomer(customer);
        expectedOutput.setInvoiceItems(invoiceItems);

        // Test 1 : findInvoice()
        expectedOutput.setMemberPoints(1000);
        InvoiceViewModel fromFind = serviceLayer.findInvoice(31);
        assertEquals(expectedOutput, fromFind);

        // Test 2 : findAllInvoices()
        List<InvoiceViewModel> allInvoices = serviceLayer.findAllInvoices();
        assertEquals(allInvoices.size(),1);
        assertEquals(allInvoices.get(0), expectedOutput);
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveInvoice() {

        InvoiceItem invoiceItemIn = new InvoiceItem();
        invoiceItemIn.setInventoryId(21);
        invoiceItemIn.setListPrice(new BigDecimal(299.00));
        invoiceItemIn.setQuantity(1);
        List<InvoiceItem> invoiceItemsIn = new ArrayList<>();
        invoiceItemsIn.add(invoiceItemIn);
        //// invoice
        InvoiceInputModel input = new InvoiceInputModel();
        input.setCustomerId(1);
        input.setPurchaseDate(LocalDate.of(2019,10,10));
        input.setInvoiceItems(invoiceItemsIn);

        // customer
        CustomerViewModel customer = new CustomerViewModel();
        customer.setCustomerId(1);
        customer.setFirstName("Bradley");
        customer.setLastName("Cooper");
        customer.setStreet("230 Briant Park");
        customer.setCity("Jersey City");
        customer.setZip("67542");
        customer.setPhone("3047655463");
        customer.setEmail("cooper@gmail.com");
        // product
        ProductViewModel product = new ProductViewModel();
        product.setProductId(11);
        product.setProductName("Nintendo");
        product.setProductDescription("Console");
        product.setListPrice(new BigDecimal(299.00));
        product.setUnitCost(new BigDecimal(199.00));
        // inventory
        InventoryViewModel inventory = new InventoryViewModel();
        inventory.setInventoryId(21);
        inventory.setQuantity(1);
        inventory.setProduct(product);
        // invoiceItem
        InvoiceItemViewModel invoiceItem = new InvoiceItemViewModel();
        invoiceItem.setInvoiceId(31);
        invoiceItem.setInvoiceItemId(41);
        invoiceItem.setInventory(inventory);
        invoiceItem.setListPrice(new BigDecimal(299.00));
        invoiceItem.setQuantity(1);
        List<InvoiceItemViewModel> invoiceItems = new ArrayList<>();
        invoiceItems.add(invoiceItem);
        //invoice
        InvoiceViewModel expectedOutput = new InvoiceViewModel();
        expectedOutput.setInvoiceId(31);
        expectedOutput.setPurchaseDate(LocalDate.of(2019,10,10));
        expectedOutput.setMemberPoints(1050);
        expectedOutput.setCustomer(customer);
        expectedOutput.setInvoiceItems(invoiceItems);

        // Test 1 : saveInvoice()
        InvoiceViewModel fromSave = serviceLayer.saveInvoice(input);
        assertEquals(expectedOutput, fromSave);

        invoiceItemIn.setQuantity(100);
        serviceLayer.saveInvoice(input);
    }

    @Test
    public void updateAndRemoveInvoice(){

        InvoiceItem invoiceItemIn = new InvoiceItem();
        invoiceItemIn.setInvoiceId(31);
        invoiceItemIn.setInvoiceItemId(41);
        invoiceItemIn.setInventoryId(21);
        invoiceItemIn.setListPrice(new BigDecimal(299.00));
        invoiceItemIn.setQuantity(1);
        List<InvoiceItem> invoiceItemsIn = new ArrayList<>();
        invoiceItemsIn.add(invoiceItemIn);
        //// invoice
        InvoiceInputModel input = new InvoiceInputModel();
        input.setInvoiceId(31);
        input.setCustomerId(1);
        input.setPurchaseDate(LocalDate.of(2005,5,5));
        input.setInvoiceItems(invoiceItemsIn);

        // Expected Output
        // customer
        CustomerViewModel customer = new CustomerViewModel();
        customer.setCustomerId(1);
        customer.setFirstName("Bradley");
        customer.setLastName("Cooper");
        customer.setStreet("230 Briant Park");
        customer.setCity("Jersey City");
        customer.setZip("67542");
        customer.setPhone("3047655463");
        customer.setEmail("cooper@gmail.com");
        // product
        ProductViewModel product = new ProductViewModel();
        product.setProductId(11);
        product.setProductName("Nintendo");
        product.setProductDescription("Console");
        product.setListPrice(new BigDecimal(299.00));
        product.setUnitCost(new BigDecimal(199.00));
        // inventory
        InventoryViewModel inventory = new InventoryViewModel();
        inventory.setInventoryId(21);
        inventory.setQuantity(2);
        inventory.setProduct(product);
        // invoiceItem
        InvoiceItemViewModel invoiceItem = new InvoiceItemViewModel();
        invoiceItem.setInvoiceId(31);
        invoiceItem.setInvoiceItemId(41);
        invoiceItem.setInventory(inventory);
        invoiceItem.setListPrice(new BigDecimal(299.00));
        invoiceItem.setQuantity(1);
        List<InvoiceItemViewModel> invoiceItems = new ArrayList<>();
        invoiceItems.add(invoiceItem);
        //invoice
        InvoiceViewModel expectedOutput = new InvoiceViewModel();
        expectedOutput.setInvoiceId(31);
        expectedOutput.setPurchaseDate(LocalDate.of(2005,5,5));
        expectedOutput.setMemberPoints(1000);
        expectedOutput.setCustomer(customer);
        expectedOutput.setInvoiceItems(invoiceItems);

        // Test 1 : updateInvoice()
        InvoiceViewModel fromUpdate = serviceLayer.updateInvoice(input);
        assertEquals(expectedOutput, fromUpdate);

        // Test 2 : removeInvoice()
        assertEquals("Invoice [31] deleted successfully!",serviceLayer.removeInvoice(31));
    }

    private void setUpCustomerClientMock(){
        customerClient = mock(CustomerClient.class);

        // output
        CustomerViewModel output = new CustomerViewModel();
        output.setCustomerId(1);
        output.setFirstName("Bradley");
        output.setLastName("Cooper");
        output.setStreet("230 Briant Park");
        output.setCity("Jersey City");
        output.setZip("67542");
        output.setPhone("3047655463");
        output.setEmail("cooper@gmail.com");

        doReturn(output).when(customerClient).getCustomer(1);
    }

    private void setUpProductClientMock(){
        productClient = mock(ProductClient.class);

        // output
        ProductViewModel output = new ProductViewModel();
        output.setProductId(11);
        output.setProductName("Nintendo");
        output.setProductDescription("Console");
        output.setListPrice(new BigDecimal(299.00));
        output.setUnitCost(new BigDecimal(199.00));

        doReturn(output).when(productClient).getProduct(11);
    }

    private void setUpInventoryClientMock(){
        inventoryClient = mock(InventoryClient.class);

        // input
        InventoryInputModel input = new InventoryInputModel();
        input.setProductId(11);
        input.setQuantity(2);

        // output
        InventoryInputModel output = new InventoryInputModel();
        output.setInventoryId(21);
        output.setProductId(11);
        output.setQuantity(2);

        // updated
        InventoryInputModel updated = new InventoryInputModel();
        updated.setInventoryId(21);
        updated.setProductId(11);
        updated.setQuantity(1);

        //// List
        List<InventoryInputModel> inventoryList = new ArrayList<>();
        inventoryList.add(output);

        doReturn(output).when(inventoryClient).createInventory(input);
        doReturn(output).when(inventoryClient).getInventory(21);
        doReturn(inventoryList).when(inventoryClient).getAllInventories();
        doReturn(updated).when(inventoryClient).updateInventory(updated,updated.getInventoryId());
        doReturn("Inventory [21] deleted successfully!").when(inventoryClient).deleteInventory(21);
    }

    private void setUpInvoiceClientMock(){
        invoiceClient = mock(InvoiceClient.class);

        // input
        //// invoice items
        InvoiceItem invoiceItemIn = new InvoiceItem();
        invoiceItemIn.setInventoryId(21);
        invoiceItemIn.setListPrice(new BigDecimal(299.00));
        invoiceItemIn.setQuantity(1);
        List<InvoiceItem> invoiceItemsIn = new ArrayList<>();
        invoiceItemsIn.add(invoiceItemIn);
        //// invoice
        InvoiceInputModel input = new InvoiceInputModel();
        input.setCustomerId(1);
        input.setPurchaseDate(LocalDate.of(2019,10,10));
        input.setInvoiceItems(invoiceItemsIn);

        // output
        //// invoice items
        InvoiceItem invoiceItemOut = new InvoiceItem();
        invoiceItemOut.setInvoiceItemId(41);
        invoiceItemOut.setInvoiceId(31);
        invoiceItemOut.setInventoryId(21);
        invoiceItemOut.setListPrice(new BigDecimal(299.00));
        invoiceItemOut.setQuantity(1);
        List<InvoiceItem> invoiceItemsOut = new ArrayList<>();
        invoiceItemsOut.add(invoiceItemOut);
        //// invoice
        InvoiceInputModel output = new InvoiceInputModel();
        output.setInvoiceId(31);
        output.setCustomerId(1);
        output.setPurchaseDate(LocalDate.of(2019,10,10));
        output.setInvoiceItems(invoiceItemsOut);

        // List
        List<InvoiceInputModel> outputList = new ArrayList<>();
        outputList.add(output);

        // Updated
        InvoiceInputModel updated = new InvoiceInputModel();
        updated.setInvoiceId(31);
        updated.setCustomerId(1);
        updated.setPurchaseDate(LocalDate.of(2005,5,5));
        updated.setInvoiceItems(invoiceItemsOut);

        doReturn(output).when(invoiceClient).createInvoice(input);
        doReturn(output).when(invoiceClient).getInvoice(31);
        doReturn(outputList).when(invoiceClient).getInvoicesByCustomer(1);
        doReturn(outputList).when(invoiceClient).getAllInvoices();
        doReturn(updated).when(invoiceClient).updateInvoice(updated,updated.getInvoiceId());
        doReturn("Invoice [31] deleted successfully!").when(invoiceClient).deleteInvoice(31);
    }

    private void setUpLevelUpClientMock(){
        levelUpClient = mock(LevelUpClient.class);

        // input
        LevelUpInputModel input = new LevelUpInputModel();
        input.setCustomerId(1);
        input.setMemberDate(LocalDate.of(2005,5,5));
        input.setPoints(1000);

        // output
        LevelUpInputModel output = new LevelUpInputModel();
        output.setLevelUpId(5);
        output.setCustomerId(1);
        output.setMemberDate(LocalDate.of(2005,5,5));
        output.setPoints(1000);

        // all
        List<LevelUpInputModel> levelUps = new ArrayList<>();
        levelUps.add(output);

        // updated
        LevelUpInputModel updated = new LevelUpInputModel();
        updated.setLevelUpId(5);
        updated.setCustomerId(1);
        updated.setMemberDate(LocalDate.of(2005,5,5));
        updated.setPoints(1050);

        doReturn(output).when(levelUpClient).createLevelUp(input);
        doReturn(output).when(levelUpClient).getLevelUp(5);
        doReturn(output).when(levelUpClient).getLevelUpByCustomerId(1);
        doReturn(levelUps).when(levelUpClient).getAllLevelUps();
        doReturn(updated).when(levelUpClient).updateLevelUp(updated,updated.getLevelUpId());
        doReturn("LevelUp [5] deleted successfully!").when(levelUpClient).deleteLevelUp(5);
    }
}