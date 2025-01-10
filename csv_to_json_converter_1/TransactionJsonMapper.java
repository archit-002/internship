package internship.csv_to_json_converter_1;

import org.apache.commons.csv.CSVRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TransactionJsonMapper {

    public List<Map<String, Object>> mapTransactionsToJson(List<Transaction> transactions,
                                                            List<Product> products,
                                                            List<TransactionProductLink> tpLinks,
                                                            List<CustomAttribute> customAttributes,
                                                            List<TransactionCustomAttributeLink> transactionCustomAttributesLinks,
                                                            List<Items> items,
                                                            List<ProductItemsLink> productItemsLinks) {

        List<Map<String, Object>> transactionJsonList = new ArrayList<>();

        try {
            // Create a map for SKU to Product
            Map<String, Product> productDetailsMap = new HashMap<>();
            for (Product product : products) {
                try {
                    productDetailsMap.put(product.getSku(), product);
                } catch (Exception e) {
                    System.err.println("Error mapping product with SKU " + product.getSku() + ": " + e.getMessage());
                }
            }

            // Create a map for transactionId to CustomAttribute for easy lookup
            Map<String, List<CustomAttribute>> transactionCustomAttributesMap = new HashMap<>();
            for (TransactionCustomAttributeLink link : transactionCustomAttributesLinks) {
                try {
                    if (!transactionCustomAttributesMap.containsKey(link.getTransactionId())) {
                        transactionCustomAttributesMap.put(link.getTransactionId(), new ArrayList<>());
                    }
                    for (CustomAttribute ca : customAttributes) {
                        if (link.getCustomAttributeId() == ca.getId()) {
                            transactionCustomAttributesMap.get(link.getTransactionId()).add(ca);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error processing custom attribute link for transactionId " + link.getTransactionId() + ": " + e.getMessage());
                }
            }

            // Create a map for SKU to items linked with products
            Map<String, List<Items>> productItemsMap = new HashMap<>();
            for (ProductItemsLink link : productItemsLinks) {
                try {
                    if (!productItemsMap.containsKey(link.getSku())) {
                        productItemsMap.put(link.getSku(), new ArrayList<>());
                    }
                    for (Items item : items) {
                        if (link.getItemId() == item.getId()) {
                            productItemsMap.get(link.getSku()).add(item);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error processing product-items link for SKU " + link.getSku() + ": " + e.getMessage());
                }
            }

            // Create a list of transactions with nested products, customAttributes, and items
            for (Transaction transaction : transactions) {
                try {
                    Map<String, Object> transactionMap = new LinkedHashMap<>(); // LinkedHashMap ensures order
                    // Fields in the exact specified order
                    transactionMap.put("loyaltyTransactionId", transaction.getLoyaltyTransactionId());
                    transactionMap.put("transactionId", transaction.getTransactionId());
                    transactionMap.put("assignedToCustomerDate", transaction.getAssignedToCustomerDate());
                    transactionMap.put("transactionType", transaction.getTransactionType());
                    transactionMap.put("transactionPlace", transaction.getTransactionPlace());
                    transactionMap.put("customerId", transaction.getCustomerId());
                    
                    // Custom attribute
                    List<Map<String, Object>> customAttributesForTransaction = new ArrayList<>();
                    if (transactionCustomAttributesMap.containsKey(transaction.getTransactionId())) {
                        List<CustomAttribute> linkedCustomAttributes = transactionCustomAttributesMap.get(transaction.getTransactionId());
                        for (CustomAttribute ca : linkedCustomAttributes) {
                            Map<String, Object> customAttributeMap = new HashMap<>();
                            customAttributeMap.put("key", ca.getKey());
                            customAttributeMap.put("value", ca.getValue());
                            customAttributesForTransaction.add(customAttributeMap);
                        }
                    }
                    transactionMap.put("customAttribute", customAttributesForTransaction);

                    // Remaining fields
                    transactionMap.put("posId", transaction.getPosId());
                    transactionMap.put("transactionValue", transaction.getTransactionValue());
                    transactionMap.put("storeCode", transaction.getStoreCode());

                    // Find and add the products for this transaction
                    List<Map<String, Object>> productsForTransaction = new ArrayList<>();
                    for (TransactionProductLink link : tpLinks) {
                        try {
                            if (link.getTransactionId().equals(transaction.getTransactionId())) {
                                Product product = productDetailsMap.get(link.getSku());
                                if (product != null) {
                                    Map<String, Object> productMap = new HashMap<>();
                                    productMap.put("category", product.getCategory());
                                    productMap.put("grossValue", product.getGrossValue());
                                    productMap.put("maker", product.getMaker());
                                    productMap.put("name", product.getName());
                                    productMap.put("quantity", product.getQuantity());
                                    productMap.put("highPrecisionQuantity", product.getHighPrecisionQuantity());
                                    productMap.put("sku", product.getSku());
                                    productMap.put("branchName", product.getBranchName());

                                    // Add the items linked with the product under "items"
                                    List<Map<String, Object>> itemsForProduct = new ArrayList<>();
                                    if (productItemsMap.containsKey(product.getSku())) {
                                        for (Items linkedItem : productItemsMap.get(product.getSku())) {
                                            Map<String, Object> itemMap = new HashMap<>();
                                            itemMap.put("key", linkedItem.getKey());
                                            itemMap.put("value", linkedItem.getValue());
                                            itemsForProduct.add(itemMap);
                                        }
                                    }

                                    productMap.put("items", itemsForProduct);

                                    productsForTransaction.add(productMap);
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error processing transaction product link for transaction " + transaction.getTransactionId() + ": " + e.getMessage());
                        }
                    }
                    transactionMap.put("products", productsForTransaction);

                    boolean isMatched = false;
                    if (transaction.isMatched() == true || "1".equals(String.valueOf(transaction.isMatched())) || "true".equalsIgnoreCase(String.valueOf(transaction.isMatched()))) {
                        isMatched = true;
                    }
                    transactionMap.put("matched", isMatched);

                    transactionMap.put("unitsDeducted", transaction.getUnitsDeducted());
                    transactionMap.put("pointsEarned", transaction.getPointsEarned());
                    transactionMap.put("channelId", transaction.getChannelId());

                    // Dates formatted correctly
                    transactionMap.put("transactionDate", transaction.getTransactionDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")));
                    transactionMap.put("assignedToCustomerDate", transaction.getAssignedToCustomerDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")));

                    transactionJsonList.add(transactionMap);
                } catch (Exception e) {
                    System.err.println("Error processing transaction " + transaction.getTransactionId() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error mapping transactions to JSON: " + e.getMessage());
        }

        return transactionJsonList;
    }

    public static void main(String[] args) {
        // Provide the updated file paths
        String transactionsFilePath = "C:/Users/archi/Documents/workspace-sts/csv_to_json_converter/src/main/java/internship/csv_to_json_converter_1/transactions1.csv";
        String productsFilePath = "C:/Users/archi/Documents/workspace-sts/csv_to_json_converter/src/main/java/internship/csv_to_json_converter_1/products1.csv";
        String tpFilePath = "C:/Users/archi/Documents/workspace-sts/csv_to_json_converter/src/main/java/internship/csv_to_json_converter_1/transaction_product_link.csv";
        
        String customAttributeFilePath = "C:/Users/archi/Documents/workspace-sts/csv_to_json_converter/src/main/java/internship/csv_to_json_converter_1/customattribute1.csv";
        String transactionCustomAttributesLinkFilePath = "C:/Users/archi/Documents/workspace-sts/csv_to_json_converter/src/main/java/internship/csv_to_json_converter_1/transaction_customattribute_link.csv";
        String itemsFilePath = "C:/Users/archi/Documents/workspace-sts/csv_to_json_converter/src/main/java/internship/csv_to_json_converter_1/items1.csv";
        String productItemsLinkFilePath = "C:/Users/archi/Documents/workspace-sts/csv_to_json_converter/src/main/java/internship/csv_to_json_converter_1/product_items_link.csv";

        CsvReader csvReader = new CsvReader();

        // Read CSV data into Java objects
        List<Transaction> transactions = null;
        try {
            transactions = csvReader.readTransactions(transactionsFilePath);
        } catch (Exception e) {
            System.err.println("Error reading transactions: " + e.getMessage());
        }

        List<Product> products = null;
        try {
            products = csvReader.readProducts(productsFilePath);
        } catch (Exception e) {
            System.err.println("Error reading products: " + e.getMessage());
        }

        List<TransactionProductLink> tpLinks = null;
        try {
            tpLinks = csvReader.readTransactionProductLinks(tpFilePath);
        } catch (Exception e) {
            System.err.println("Error reading transaction-product links: " + e.getMessage());
        }

        List<CustomAttribute> customAttributes = null;
        try {
            customAttributes = csvReader.readCustomAttributes(customAttributeFilePath);
        } catch (Exception e) {
            System.err.println("Error reading custom attributes: " + e.getMessage());
        }

        List<TransactionCustomAttributeLink> transactionCustomAttributesLinks = null;
        try {
            transactionCustomAttributesLinks = csvReader.readTransactionCustomAttributeLinks(transactionCustomAttributesLinkFilePath);
        } catch (Exception e) {
            System.err.println("Error reading transaction custom attribute links: " + e.getMessage());
        }

        List<Items> items = null;
        try {
            items = csvReader.readItems(itemsFilePath);
        } catch (Exception e) {
            System.err.println("Error reading items: " + e.getMessage());
        }

        List<ProductItemsLink> productItemsLinks = null;
        try {
            productItemsLinks = csvReader.readProductItemsLinks(productItemsLinkFilePath);
        } catch (Exception e) {
            System.err.println("Error reading product-items links: " + e.getMessage());
        }

        // Create an instance of the JSON mapper
        TransactionJsonMapper jsonMapper = new TransactionJsonMapper();

        // Map the data to JSON format
        List<Map<String, Object>> jsonData = jsonMapper.mapTransactionsToJson(
                transactions, products, tpLinks, customAttributes, transactionCustomAttributesLinks, items, productItemsLinks);

        // Create an ObjectMapper instance with custom date format for LocalDateTime
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Ensure JavaTimeModule is registered

        // Write the JSON output to a file
        try (FileWriter fileWriter = new FileWriter("C:/Users/archi/Documents/workspace-sts/csv_to_json_converter/src/main/java/internship/csv_to_json_converter_1/output.json")) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(fileWriter, jsonData);
            System.out.println("JSON output written to output.json");
        } catch (IOException e) {
            System.err.println("Error writing JSON output: " + e.getMessage());
        }
    }
}
