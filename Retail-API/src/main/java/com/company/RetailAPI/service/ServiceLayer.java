package com.company.RetailAPI.service;

import com.company.RetailAPI.util.feign.*;
import com.company.RetailAPI.util.messages.LevelUpMessage;
import com.company.RetailAPI.views.CustomerViewModel;
import com.company.RetailAPI.views.ProductViewModel;
import com.company.RetailAPI.views.input.InventoryInputModel;
import com.company.RetailAPI.views.input.InvoiceInputModel;
import com.company.RetailAPI.views.input.LevelUpInputModel;
import com.company.RetailAPI.views.output.InventoryViewModel;
import com.company.RetailAPI.views.output.InvoiceViewModel;
import com.company.RetailAPI.views.output.LevelUpViewModel;
import com.company.RetailAPI.views.products.ProductFromInventory;
import com.company.RetailAPI.views.products.ProductFromInvoice;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class ServiceLayer {


    //QUEUE

    private static final String EXCHANGE = "level-up-exchange";
    private static final String ROUTING_KEY = "level-up.update.Retail";


    // PROPERTIES

    private RabbitTemplate rabbitTemplate;
    private CustomerClient customerClient;
    private ProductClient productClient;
    private InventoryClient inventoryClient;
    private LevelUpClient levelUpClient;
    private InvoiceClient invoiceClient;

    @Autowired
    public ServiceLayer(RabbitTemplate rabbitTemplate, CustomerClient customerClient, ProductClient productClient, InventoryClient inventoryClient, LevelUpClient levelUpClient, InvoiceClient invoiceClient) {
        this.rabbitTemplate = rabbitTemplate;
        this.customerClient = customerClient;
        this.productClient = productClient;
        this.inventoryClient = inventoryClient;
        this.levelUpClient = levelUpClient;
        this.invoiceClient = invoiceClient;
    }


    // LEVEL UP

    @Transactional
    public LevelUpViewModel findLevelUpByCustomerId(int customerId){
        return buildLevelUpViewModel(levelUpClient.getLevelUpByCustomerId(customerId));
    }


    // INVENTORY

    @Transactional
    public ProductFromInventory getProductFromInventory(int id){
        InventoryViewModel inventoryItem = findInventory(id);

        ProductFromInventory product = new ProductFromInventory();
        product.setInventoryId(inventoryItem.getInventoryId());
        product.setListPrice(inventoryItem.getProduct().getListPrice());
        product.setProductName(inventoryItem.getProduct().getProductName());
        product.setProductDescription(inventoryItem.getProduct().getProductDescription());
        product.setQuantity(inventoryItem.getQuantity());

        return product;
    }

    @Transactional
    public List<ProductFromInventory> getAllProductsFromInventory(){
        List<InventoryViewModel> inventoryItems = findAllInventories();
        List<ProductFromInventory> products = new ArrayList<>();

        inventoryItems.forEach(inventoryItem -> {
            ProductFromInventory product = new ProductFromInventory();
            product.setInventoryId(inventoryItem.getInventoryId());
            product.setListPrice(inventoryItem.getProduct().getListPrice());
            product.setProductName(inventoryItem.getProduct().getProductName());
            product.setProductDescription(inventoryItem.getProduct().getProductDescription());
            product.setQuantity(inventoryItem.getQuantity());
            products.add(product);
        });

        return products;
    }

    private InventoryViewModel findInventory(int inventoryId){
        return buildInventoryViewModel(inventoryClient.getInventory(inventoryId));
    }

    private List<InventoryViewModel> findAllInventories(){
        // Getting inventory list
        List<InventoryInputModel> fromInventoryService = inventoryClient.getAllInventories();
        // Building ViewModels
        List<InventoryViewModel> inventoryViewModels = new ArrayList<>();
        fromInventoryService.forEach(inventory -> inventoryViewModels.add(buildInventoryViewModel(inventory)));

        return inventoryViewModels;
    }

    private InventoryViewModel updateInventory(InventoryInputModel inventoryInputModel){
        findProduct(inventoryInputModel.getProductId());

        return buildInventoryViewModel(inventoryClient.updateInventory(inventoryInputModel, inventoryInputModel.getInventoryId()));
    }

    //
    // INVOICE

    @Transactional
    public InvoiceViewModel saveInvoice(InvoiceInputModel invoiceInputModel){

        InvoiceViewModel invoiceViewModel = new InvoiceViewModel();
        List<ProductFromInvoice> invoiceItems = new ArrayList<>();
        int points;
        List<BigDecimal> totalPrice = new ArrayList<>();

        invoiceViewModel.setCustomer(findCustomer(invoiceInputModel.getCustomerId()));

        invoiceInputModel.getInvoiceItems().forEach(invoiceItem -> {
            InventoryViewModel inventory = findInventory(invoiceItem.getInventoryId());

            if(invoiceItem.getQuantity()>inventory.getQuantity()){
                throw new IllegalArgumentException( inventory.getQuantity() +" items available");
            }

            invoiceItem.setListPrice(inventory.getProduct().getListPrice());
            BigDecimal itemPrice = BigDecimal.valueOf( invoiceItem.getQuantity() ).multiply( inventory.getProduct().getListPrice() ).setScale(2, RoundingMode.HALF_UP);
            totalPrice.add(itemPrice);
        });

        invoiceInputModel = invoiceClient.createInvoice(invoiceInputModel);

        // POINTS

        int result = totalPrice.stream().reduce(BigDecimal.ZERO, BigDecimal::add).intValue();
        points = (result/50)*10;
        LevelUpInputModel currentPoints = levelUpClient.getLevelUpByCustomerId(invoiceViewModel.getCustomer().getCustomerId());
        currentPoints.setPoints(currentPoints.getPoints()+points);

        if(currentPoints.getLevelUpId()==0){
            currentPoints.setCustomerId(invoiceViewModel.getCustomer().getCustomerId());
            invoiceViewModel.setMemberPoints("");
        } else {
            invoiceViewModel.setMemberPoints(String.valueOf(currentPoints.getPoints()));
        }

        // POINTS - QUEUE
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, (convertLevelUpToInputModel(currentPoints)));

        invoiceViewModel.setInvoiceId(invoiceInputModel.getInvoiceId());
        invoiceViewModel.setPurchaseDate(invoiceInputModel.getPurchaseDate());
        invoiceInputModel.getInvoiceItems().forEach(invoiceItem -> {

            InventoryViewModel inventory = findInventory(invoiceItem.getInventoryId());

            inventory.setQuantity(inventory.getQuantity()-invoiceItem.getQuantity());
            inventory = updateInventory(convertInventoryToInputModel(inventory));

            ProductFromInvoice product = new ProductFromInvoice();
            product.setInvoiceId(invoiceItem.getInvoiceId());
            product.setInvoiceItemId(invoiceItem.getInvoiceItemId());
            product.setInventoryId(inventory.getInventoryId());
            product.setProductName(inventory.getProduct().getProductName());
            product.setProductDescription(inventory.getProduct().getProductDescription());
            product.setUnitPrice(invoiceItem.getListPrice());
            product.setQuantity(invoiceItem.getQuantity());
            invoiceItems.add(product); });
        invoiceViewModel.setInvoiceItems(invoiceItems);

        return invoiceViewModel;
    }

    @Transactional
    public InvoiceViewModel findInvoice(int invoiceId){
        return buildInvoiceViewModel(invoiceClient.getInvoice(invoiceId));
    }

    @Transactional
    public List<InvoiceViewModel> findAllInvoices(){
        List<InvoiceInputModel> fromInvoiceService = invoiceClient.getAllInvoices();

        // Building ViewModels
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

    // CUSTOMER
    private CustomerViewModel findCustomer(int customerId){
        return customerClient.getCustomer(customerId);
    }


    // PRODUCT

    private ProductViewModel findProduct(int productId){
        return productClient.getProduct(productId);
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
        List<ProductFromInvoice> invoiceItemsList = new ArrayList<>();


        invoice.getInvoiceItems().forEach(invoiceItem -> {

            ProductFromInvoice product = new ProductFromInvoice();


            InventoryViewModel inventory = findInventory(invoiceItem.getInventoryId());


            product.setInvoiceId(invoiceItem.getInvoiceId());
            product.setInvoiceItemId(invoiceItem.getInvoiceItemId());
            product.setInventoryId(inventory.getInventoryId());
            product.setProductName(inventory.getProduct().getProductName());
            product.setProductDescription(inventory.getProduct().getProductDescription());
            product.setUnitPrice(invoiceItem.getListPrice());
            product.setQuantity(invoiceItem.getQuantity());

            invoiceItemsList.add(product);
        });


        LevelUpViewModel levelUp = findLevelUpByCustomerId(invoice.getCustomerId());


        invoiceViewModel.setInvoiceId(invoice.getInvoiceId());
        invoiceViewModel.setPurchaseDate(invoice.getPurchaseDate());
        invoiceViewModel.setCustomer(levelUp.getCustomer());
        invoiceViewModel.setMemberPoints(String.valueOf(levelUp.getPoints()));
        invoiceViewModel.setInvoiceItems(invoiceItemsList);

        return invoiceViewModel;
    }

    private LevelUpMessage convertLevelUpToInputModel(LevelUpInputModel levelUpInputModel){
        LevelUpMessage msg = new LevelUpMessage();
        msg.setLevelUpId(levelUpInputModel.getLevelUpId());
        msg.setCustomerId(levelUpInputModel.getCustomerId());
        msg.setMemberDate(levelUpInputModel.getMemberDate());
        msg.setPoints(levelUpInputModel.getPoints());
        return msg;
    }

    private InventoryInputModel convertInventoryToInputModel (InventoryViewModel ivm){
        InventoryInputModel inventoryInputModel = new InventoryInputModel();
        inventoryInputModel.setInventoryId(ivm.getInventoryId());
        inventoryInputModel.setProductId(ivm.getProduct().getProductId());
        inventoryInputModel.setQuantity(ivm.getQuantity());
        return inventoryInputModel;
    }
}
