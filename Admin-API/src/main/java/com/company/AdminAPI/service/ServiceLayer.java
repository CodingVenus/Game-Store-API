package com.company.AdminAPI.service;

import com.company.AdminAPI.exception.NotFoundException;
import com.company.AdminAPI.util.feign.*;
import com.company.AdminAPI.views.*;
import com.company.AdminAPI.views.input.InventoryInputModel;
import com.company.AdminAPI.views.input.InvoiceInputModel;
import com.company.AdminAPI.views.input.LevelUpInputModel;
import com.company.AdminAPI.views.output.InventoryViewModel;
import com.company.AdminAPI.views.output.InvoiceItemViewModel;
import com.company.AdminAPI.views.output.InvoiceViewModel;
import com.company.AdminAPI.views.output.LevelUpViewModel;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ServiceLayer {

    private CustomerClient customerClient;
    private ProductClient productClient;
    private InventoryClient inventoryClient;
    private LevelUpClient levelUpClient;
    private InvoiceClient invoiceClient;

    @Autowired
    public ServiceLayer(CustomerClient customerClient, ProductClient productClient, InventoryClient inventoryClient, LevelUpClient levelUpClient, InvoiceClient invoiceClient) {
        this.customerClient = customerClient;
        this.productClient = productClient;
        this.inventoryClient = inventoryClient;
        this.levelUpClient = levelUpClient;
        this.invoiceClient = invoiceClient;
    }


    public CustomerViewModel saveCustomer(CustomerViewModel customerViewModel){
        CustomerViewModel customer = customerClient.createCustomer(customerViewModel);

        LevelUpInputModel newAccount = new LevelUpInputModel();
        newAccount.setCustomerId(customer.getCustomerId());
        newAccount.setMemberDate(LocalDate.now());
        newAccount.setPoints(0);
        saveLevelUp(newAccount);

        return customer;
    }

    public CustomerViewModel findCustomer(int customerId){
        return customerClient.getCustomer(customerId);
    }

    public List<CustomerViewModel> findAllCustomers(){
        return customerClient.getAllCustomers();
    }

    public CustomerViewModel updateCustomer(CustomerViewModel customerViewModel){
        return customerClient.updateCustomer(customerViewModel, customerViewModel.getCustomerId());
    }

    public String removeCustomer(int customerId){
        return customerClient.deleteCustomer(customerId);
    }

    public ProductViewModel saveProduct(ProductViewModel productViewModel){
        return productClient.createProduct(productViewModel);
    }

    public ProductViewModel findProduct(int productId){
        return productClient.getProduct(productId);
    }

    public List<ProductViewModel> findAllProducts(){
        return productClient.getAllProducts();
    }

    public ProductViewModel updateProduct(ProductViewModel productViewModel){
        return productClient.updateProduct(productViewModel, productViewModel.getProductId());
    }

    public String removeProduct(int productId){
        return productClient.deleteProduct(productId);
    }

    public LevelUpViewModel saveLevelUp(LevelUpInputModel levelUpInputModel){

        checkForCustomer(levelUpInputModel.getCustomerId());
        return buildLevelUpViewModel(levelUpClient.createLevelUp(levelUpInputModel));
    }

    public LevelUpViewModel findLevelUp(int levelUpId){
        return buildLevelUpViewModel(levelUpClient.getLevelUp(levelUpId));
    }

    public LevelUpViewModel findLevelUpByCustomerId(int customerId){
        return buildLevelUpViewModel(levelUpClient.getLevelUpByCustomerId(customerId));
    }

    public List<LevelUpViewModel> findAllLevelUps(){

        List<LevelUpInputModel> fromLevelUpService = levelUpClient.getAllLevelUps();
        List<LevelUpViewModel> levelUpViewModels = new ArrayList<>();
        fromLevelUpService.forEach(levelUp -> levelUpViewModels.add(buildLevelUpViewModel(levelUp)));

        return levelUpViewModels;
    }

    public LevelUpViewModel updateLevelUp(LevelUpInputModel levelUpInputModel){
        checkForCustomer(levelUpInputModel.getCustomerId());

        return buildLevelUpViewModel(levelUpClient.updateLevelUp(levelUpInputModel, levelUpInputModel.getLevelUpId()));
    }

    public String removeLevelUp(int levelUpId){
        return levelUpClient.deleteLevelUp(levelUpId);
    }

    public InventoryViewModel saveInventory(InventoryInputModel inventoryInputModel){

        checkForProduct(inventoryInputModel.getProductId());
        return buildInventoryViewModel(inventoryClient.createInventory(inventoryInputModel));
    }

    public InventoryViewModel findInventory(int inventoryId){
        return buildInventoryViewModel(inventoryClient.getInventory(inventoryId));
    }

    public List<InventoryViewModel> findAllInventories(){
        List<InventoryInputModel> fromInventoryService = inventoryClient.getAllInventories();

        List<InventoryViewModel> inventoryViewModels = new ArrayList<>();
        fromInventoryService.forEach(inventory -> inventoryViewModels.add(buildInventoryViewModel(inventory)));

        return inventoryViewModels;
    }

    public InventoryViewModel updateInventory(InventoryInputModel inventoryInputModel){
        checkForProduct(inventoryInputModel.getProductId());

        return buildInventoryViewModel(inventoryClient.updateInventory(inventoryInputModel, inventoryInputModel.getInventoryId()));
    }

    public String removeInventory(int inventoryId){
        return inventoryClient.deleteInventory(inventoryId);
    }

    public InvoiceViewModel saveInvoice(InvoiceInputModel invoiceInputModel){

        InvoiceViewModel invoiceViewModel = new InvoiceViewModel();
        List<InvoiceItemViewModel> invoiceItems = new ArrayList<>();
        int points;
        List<BigDecimal> totalPrice = new ArrayList<>();

        invoiceViewModel.setCustomer(findCustomer(invoiceInputModel.getCustomerId()));

        invoiceInputModel.getInvoiceItems().forEach(invoiceItem -> {
            InventoryViewModel inventory = findInventory(invoiceItem.getInventoryId());

            if(invoiceItem.getQuantity()>inventory.getQuantity()){
                throw new IllegalArgumentException("Inventory Id ["+invoiceItem.getInventoryId()+
                        "]: Only "+inventory.getQuantity()+" items available at storage.");
            }

            invoiceItem.setListPrice(inventory.getProduct().getListPrice());
            BigDecimal itemPrice = BigDecimal.valueOf( invoiceItem.getQuantity() ).multiply( inventory.getProduct().getListPrice() ).setScale(2, RoundingMode.HALF_UP);
            totalPrice.add(itemPrice);
        });
        System.out.println("Contacting Invoice Service client to create invoice...");
        invoiceInputModel = invoiceClient.createInvoice(invoiceInputModel);

        int result = totalPrice.stream().reduce(BigDecimal.ZERO, BigDecimal::add).intValue();
        points = (result/50)*10;
        LevelUpViewModel currentPoints = findLevelUpByCustomerId(invoiceViewModel.getCustomer().getCustomerId());
        currentPoints.setPoints(currentPoints.getPoints()+points);
        currentPoints = updateLevelUp(convertLevelUpToInputModel(currentPoints));

        invoiceViewModel.setInvoiceId(invoiceInputModel.getInvoiceId());
        invoiceViewModel.setPurchaseDate(invoiceInputModel.getPurchaseDate());
        invoiceViewModel.setMemberPoints(currentPoints.getPoints());
        invoiceInputModel.getInvoiceItems().forEach(invoiceItem -> {

            InventoryViewModel inventory = findInventory(invoiceItem.getInventoryId());

            inventory.setQuantity(inventory.getQuantity()-invoiceItem.getQuantity());
            inventory = updateInventory(convertInventoryToInputModel(inventory));

            InvoiceItemViewModel invoiceItemViewModel = new InvoiceItemViewModel();
            invoiceItemViewModel.setInvoiceId(invoiceItem.getInvoiceId());
            invoiceItemViewModel.setInvoiceItemId(invoiceItem.getInvoiceItemId());
            invoiceItemViewModel.setListPrice(invoiceItem.getListPrice());
            invoiceItemViewModel.setQuantity(invoiceItem.getQuantity());
            invoiceItemViewModel.setInventory(inventory);
            invoiceItems.add(invoiceItemViewModel); });
        invoiceViewModel.setInvoiceItems(invoiceItems);

        return invoiceViewModel;
    }

    public InvoiceViewModel findInvoice(int invoiceId){
        return buildInvoiceViewModel(invoiceClient.getInvoice(invoiceId));
    }

    public List<InvoiceViewModel> findAllInvoices(){
        List<InvoiceInputModel> fromInvoiceService = invoiceClient.getAllInvoices();

        List<InvoiceViewModel> invoiceViewModels = new ArrayList<>();
        fromInvoiceService.forEach(invoice -> invoiceViewModels.add(buildInvoiceViewModel(invoice)));

        return invoiceViewModels;
    }

    @Transactional
    public List<InvoiceViewModel> findInvoicesByCustomer(int customerId){
        List<InvoiceInputModel> fromInvoiceService = invoiceClient.getInvoicesByCustomer(customerId);

        List<InvoiceViewModel> invoiceViewModels = new ArrayList<>();
        fromInvoiceService.forEach(invoice -> invoiceViewModels.add(buildInvoiceViewModel(invoice)));

        return invoiceViewModels;
    }

    public InvoiceViewModel updateInvoice(InvoiceInputModel invoiceInputModel){

        checkForCustomer(invoiceInputModel.getCustomerId());

        invoiceInputModel.getInvoiceItems().forEach(invoiceItem -> {
            InventoryViewModel inventory = findInventory(invoiceItem.getInventoryId());

            if(invoiceItem.getQuantity()>inventory.getQuantity()){
                throw new IllegalArgumentException("Inventory Id ["+invoiceItem.getInventoryId()+
                        "]: Only "+inventory.getQuantity()+" items available at storage.");
            }
        });

        System.out.println("Contacting Invoice Service client to update invoice...");
        return buildInvoiceViewModel(invoiceClient.updateInvoice(invoiceInputModel, invoiceInputModel.getInvoiceId()));
    }

    public String removeInvoice(int invoiceId){
        return invoiceClient.deleteInvoice(invoiceId);
    }


    private void checkForProduct(int productId){
        System.out.println("Checking if product exists...");
        try{
            findProduct(productId);
        } catch (FeignException e){
            System.out.println("...product wasn't found in DB!");
            throw new NotFoundException("Product doesn't exist! Create the product first using: [POST] 'uri=/products' endpoint.");
        }
        System.out.println("...product found in DB!");
    }

    private void checkForCustomer(int customerId){
        System.out.println("Checking if customer exists...");
        try{
            findCustomer(customerId);
        } catch (FeignException e){
            System.out.println("...customer wasn't found in DB!");
            throw new NotFoundException("Customer doesn't exist! Create the customer first using: [POST] 'uri=/customers' endpoint.");
        }
        System.out.println("...customer found in DB!");
    }

    private InventoryViewModel buildInventoryViewModel(InventoryInputModel inventoryInputModel){
        InventoryViewModel inventoryViewModel = new InventoryViewModel();
        inventoryViewModel.setInventoryId(inventoryInputModel.getInventoryId());
        inventoryViewModel.setQuantity(inventoryInputModel.getQuantity());
        inventoryViewModel.setProduct(findProduct(inventoryInputModel.getProductId()));
        return inventoryViewModel;
    }

    private LevelUpViewModel buildLevelUpViewModel(LevelUpInputModel levelUpInputModel){
        LevelUpViewModel levelUpViewModel = new LevelUpViewModel();
        levelUpViewModel.setLevelUpId(levelUpInputModel.getLevelUpId());
        levelUpViewModel.setMemberDate(levelUpInputModel.getMemberDate());
        levelUpViewModel.setPoints(levelUpInputModel.getPoints());
        levelUpViewModel.setCustomer(findCustomer(levelUpInputModel.getCustomerId()));
        return levelUpViewModel;
    }

    private InvoiceViewModel buildInvoiceViewModel(InvoiceInputModel invoice){
        InvoiceViewModel invoiceViewModel = new InvoiceViewModel();
        List<InvoiceItemViewModel> invoiceItemsList = new ArrayList<>();

        invoice.getInvoiceItems().forEach(invoiceItem -> {
            InvoiceItemViewModel invoiceItemViewModel = new InvoiceItemViewModel();
            invoiceItemViewModel.setInvoiceId(invoiceItem.getInvoiceId());
            invoiceItemViewModel.setInvoiceItemId(invoiceItem.getInvoiceItemId());
            invoiceItemViewModel.setQuantity(invoiceItem.getQuantity());
            invoiceItemViewModel.setListPrice(invoiceItem.getListPrice());
            invoiceItemViewModel.setInventory(findInventory(invoiceItem.getInventoryId()));
            invoiceItemsList.add(invoiceItemViewModel);
        });

        invoiceViewModel.setInvoiceId(invoice.getInvoiceId());
        invoiceViewModel.setPurchaseDate(invoice.getPurchaseDate());
        invoiceViewModel.setCustomer(findCustomer(invoice.getCustomerId()));
        invoiceViewModel.setMemberPoints(findLevelUpByCustomerId(invoice.getCustomerId()).getPoints());
        invoiceViewModel.setInvoiceItems(invoiceItemsList);

        return invoiceViewModel;
    }

    private LevelUpInputModel convertLevelUpToInputModel(LevelUpViewModel levelUpViewModel){
        LevelUpInputModel levelUpInputModel = new LevelUpInputModel();
        levelUpInputModel.setLevelUpId(levelUpViewModel.getLevelUpId());
        levelUpInputModel.setCustomerId(levelUpViewModel.getCustomer().getCustomerId());
        levelUpInputModel.setMemberDate(levelUpViewModel.getMemberDate());
        levelUpInputModel.setPoints(levelUpViewModel.getPoints());
        return levelUpInputModel;
    }

    private InventoryInputModel convertInventoryToInputModel (InventoryViewModel ivm){
        InventoryInputModel inventoryInputModel = new InventoryInputModel();
        inventoryInputModel.setInventoryId(ivm.getInventoryId());
        inventoryInputModel.setProductId(ivm.getProduct().getProductId());
        inventoryInputModel.setQuantity(ivm.getQuantity());
        return inventoryInputModel;
    }
}
