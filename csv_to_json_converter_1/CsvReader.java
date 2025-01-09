package internship.csv_to_json_converter_1;

import org.apache.commons.csv.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CsvReader {

    // Update the DateTimeFormatter to match your CSV date format
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<Transaction> readTransactions(String filePath) {
        List<Transaction> transactions = new ArrayList<>();
        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader())) {

            for (CSVRecord record : csvParser) {
                Transaction transaction = new Transaction();
                transaction.setTransactionId(record.get("transactionId"));
                transaction.setLoyaltyTransactionId(record.get("loyaltyTransactionId"));
                transaction.setAssignedToCustomerDate(LocalDateTime.parse(record.get("assignedToCustomerDate"), DATE_TIME_FORMATTER));  // Parsing to LocalDateTime
                transaction.setTransactionType(record.get("transactionType"));
                transaction.setTransactionPlace(record.get("transactionPlace"));
                transaction.setCustomerId(record.get("customerId"));
                transaction.setPosId(record.get("posId"));
                transaction.setTransactionValue(Double.parseDouble(record.get("transactionValue")));
                transaction.setStoreCode(record.get("storeCode"));
                transaction.setMatched(Boolean.parseBoolean(record.get("matched")));
                transaction.setUnitsDeducted(Integer.parseInt(record.get("unitsDeducted")));
                transaction.setPointsEarned(Integer.parseInt(record.get("pointsEarned")));
                transaction.setChannelId(record.get("channelId"));
                transaction.setTransactionDate(LocalDateTime.parse(record.get("transactionDate"), DATE_TIME_FORMATTER));  // Parsing to LocalDateTime
                transactions.add(transaction);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public List<Product> readProducts(String filePath) {
        List<Product> products = new ArrayList<>();
        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader())) {

            for (CSVRecord record : csvParser) {
                Product product = new Product();
                product.setSku(record.get("sku"));
                product.setCategory(record.get("category"));
                product.setGrossValue(Double.parseDouble(record.get("grossValue")));
                product.setMaker(record.get("maker"));
                product.setName(record.get("name"));
                product.setQuantity(Integer.parseInt(record.get("quantity")));
                product.setHighPrecisionQuantity(Integer.parseInt(record.get("highPrecisionQuantity")));
                product.setBranchName(record.get("branchName"));
                products.add(product);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return products;
    }

    public List<TransactionProductLink> readTransactionProductLinks(String filePath) {
        List<TransactionProductLink> links = new ArrayList<>();
        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader())) {

            for (CSVRecord record : csvParser) {
                TransactionProductLink link = new TransactionProductLink();
                link.setTransactionId(record.get("transactionId"));
                link.setSku(record.get("sku"));
                links.add(link);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return links;
    }
}
