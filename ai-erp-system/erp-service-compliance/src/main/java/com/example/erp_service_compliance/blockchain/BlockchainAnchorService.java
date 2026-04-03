package com.example.erp_service_compliance.blockchain;

import com.example.erp_service_compliance.document.AuditLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.CompletableFuture;

@Service
public class BlockchainAnchorService {

    private final Web3j web3j;
    private final Credentials credentials;
    private BigInteger localNonce = null;

    public BlockchainAnchorService(
            @Value("${blockchain.arbitrum.node-url}") String nodeUrl,
            @Value("${blockchain.arbitrum.private-key}") String privateKey) {

        this.web3j = Web3j.build(new HttpService(nodeUrl));

        Credentials tempCreds = null;
        try {
            tempCreds = Credentials.create(privateKey);
        } catch (Exception e) {
            System.out.println("⚠️ Blockchain wallet not configured. Running in simulation mode.");
        }
        this.credentials = tempCreds;
    }

    // 🌟 1. Added @Async so Kafka doesn't wait and crash
    // 🌟 2. Added synchronized so threads don't steal each other's Nonce
    // 🌟 3. Changed return type to CompletableFuture<String>
    @Async
    public synchronized CompletableFuture<String> anchorLogToArbitrum(AuditLog log) {
        try {
            System.out.println("\n==========================================");
            System.out.println("⛓️ WEB3 ANCHOR: Securing Audit Log on Arbitrum...");

            String rawData = log.getTenantId() + log.getAction() + log.getDetails() + log.getTimestamp();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(rawData.getBytes(StandardCharsets.UTF_8));
            String documentHash = "0x" + bytesToHex(encodedhash);

            System.out.println("📄 MongoDB Document ID: " + log.getId());
            System.out.println("🔐 Cryptographic Hash: " + documentHash);

            if (credentials != null) {

                // 🌟 1. Fetch from the network ONLY if we don't know the nonce yet
                if (localNonce == null) {
                    EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                            credentials.getAddress(), DefaultBlockParameterName.PENDING).send();
                    localNonce = ethGetTransactionCount.getTransactionCount();
                }

                // 🌟 2. Grab the current ticket, then immediately increment the tracker for the next thread
                BigInteger nonceToUse = localNonce;
                localNonce = localNonce.add(BigInteger.ONE);

                BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice().multiply(BigInteger.valueOf(2));
                BigInteger gasLimit = BigInteger.valueOf(100_000);

                RawTransaction rawTransaction = RawTransaction.createTransaction(
                        nonceToUse, // 🌟 Use our perfectly tracked local nonce
                        gasPrice,
                        gasLimit,
                        credentials.getAddress(),
                        BigInteger.ZERO,
                        documentHash
                );

                byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
                String hexValue = Numeric.toHexString(signedMessage);
                EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();

                if (ethSendTransaction.hasError()) {
                    System.out.println("❌ Web3 Error: " + ethSendTransaction.getError().getMessage());

                    // 🌟 3. If it failed for ANY reason, reset the tracker to null so it fetches fresh next time!
                    localNonce = null;

                    return CompletableFuture.completedFuture(null);
                } else {
                    String txHash = ethSendTransaction.getTransactionHash();
                    System.out.println("✅ Successfully Anchored to Arbitrum!");
                    System.out.println("🔍 View Transaction: https://sepolia.arbiscan.io/tx/" + txHash);
                    System.out.println("==========================================\n");

                    return CompletableFuture.completedFuture(txHash);
                }
            }

            System.out.println("==========================================\n");
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            System.err.println("Failed to anchor log: " + e.getMessage());
            return CompletableFuture.completedFuture(null);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}