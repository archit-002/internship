package internship.csv_to_json_converter_1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TransactionJsonMapper {

    public List<Map<String, Object>> mapTransactionsToJson(List<Transaction> transactions,
                                                            List<Product> products,
                                                            List<TransactionProductLink> tpLinks) {
        // Create a map for SKU to Product
        Map<String, Product> productDetailsMap = new HashMap<>();
        for (Product product : products) {
            productDetailsMap.put(product.getSku(), product);
        }

        // Create a list of transactions with nested products
        List<Map<String, Object>> transactionJsonList = new ArrayList<>();
        for (Transaction transaction : transactions) {
            Map<String, Object> transactionMap = new HashMap<>();
            transactionMap.put("loyaltyTransactionId", transaction.getLoyaltyTransactionId());
            transactionMap.put("transactionId", transaction.getTransactionId());
            transactionMap.put("assignedToCustomerDate", transaction.getAssignedToCustomerDate());
            transactionMap.put("transactionType", transaction.getTransactionType());
            transactionMap.put("transactionPlace", transaction.getTransactionPlace());
            transactionMap.put("customerId", transaction.getCustomerId());
            transactionMap.put("posId", transaction.getPosId());
            transactionMap.put("transactionValue", transaction.getTransactionValue());
            transactionMap.put("storeCode", transaction.getStoreCode());
            transactionMap.put("matched", transaction.isMatched());
            transactionMap.put("unitsDeducted", transaction.getUnitsDeducted());
            transactionMap.put("pointsEarned", transaction.getPointsEarned());
            transactionMap.put("channelId", transaction.getChannelId());
            transactionMap.put("transactionDate", transaction.getTransactionDate());

            // Find and add the products for this transaction
            List<Map<String, Object>> productsForTransaction = new ArrayList<>();
            for (TransactionProductLink link : tpLinks) {
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
                        productsForTransaction.add(productMap);
                    }
                }
            }

            transactionMap.put("products", productsForTransaction);

            transactionJsonList.add(transactionMap);
        }
        return transactionJsonList;
    }

    public static void main(String[] args) {
        // Provide the updated file paths
        String transactionsFilePath = "C:/Users/archi/Documents/workspace-sts/csv_to_json_converter/src/main/java/internship/csv_to_json_converter_1/transactions1.csv";
        String productsFilePath = "C:/Users/archi/Documents/workspace-sts/csv_to_json_converter/src/main/java/internship/csv_to_json_converter_1/products1.csv";
        String tpFilePath = "C:/Users/archi/Documents/workspace-sts/csv_to_json_converter/src/main/java/internship/csv_to_json_converter_1/transaction_product_link.csv";

        CsvReader csvReader = new CsvReader();

        // Read CSV data into Java objects
        List<Transaction> transactions = csvReader.readTransactions(transactionsFilePath);
        List<Product> products = csvReader.readProducts(productsFilePath);
        List<TransactionProductLink> tpLinks = csvReader.readTransactionProductLinks(tpFilePath);

        // Create an instance of the JSON mapper
        TransactionJsonMapper jsonMapper = new TransactionJsonMapper();

        // Map the data to JSON format
        List<Map<String, Object>> jsonData = jsonMapper.mapTransactionsToJson(transactions, products, tpLinks);

        // Convert the result to JSON string (using Jackson ObjectMapper)
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());  // Register the JavaTimeModule for LocalDateTime
        try {
            // Write the JSON output to a file
            FileWriter fileWriter = new FileWriter("output.json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(fileWriter, jsonData);
            System.out.println("JSON output written to output.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
