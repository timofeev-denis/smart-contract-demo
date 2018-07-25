package org.web3j.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Optional;

/**
 * A simple web3j application that demonstrates a number of core features of web3j:
 *
 * <ol>
 * <li>Connecting to a node on the Ethereum network</li>
 * <li>Loading an Ethereum wallet file</li>
 * <li>Sending Ether from one address to another</li>
 * <li>Deploying a smart contract to the network</li>
 * <li>Reading a value from the deployed smart contract</li>
 * <li>Updating a value in the deployed smart contract</li>
 * <li>Viewing an event logged by the smart contract</li>
 * </ol>
 *
 * <p>To run this demo, you will need to provide:
 *
 * <ol>
 * <li>Ethereum client (or node) endpoint. The simplest thing to do is
 * <a href="https://infura.io/register.html">request a free access token from Infura</a></li>
 * <li>A wallet file. This can be generated using the web3j
 * <a href="https://docs.web3j.io/command_line.html">command line tools</a></li>
 * <li>Some Ether. This can be requested from the
 * <a href="https://www.rinkeby.io/#faucet">Rinkeby Faucet</a></li>
 * </ol>
 *
 * <p>For further background information, refer to the project README.
 */
public class MyApplication {

    public static final BigInteger GAS_PRICE = BigInteger.valueOf(22_000_000_000L);
    public static final BigInteger GAS_LIMIT = BigInteger.valueOf(4_300_000);
    private static final int SLEEP_DURATION = 15000;
    private static final int ATTEMPTS = 40;
    private static final Logger log = LoggerFactory.getLogger(MyApplication.class);
    private String gatewayUrl = "https://rinkeby.infura.io/VIhu8Q3dA5fy5NbxDLjR";
    private String credentialsFilePath = "/home/denis/.ethereum/rinkeby/keystore/UTC--2018-07-15T14-29-16.262544014Z--93fcb1de0a0fef8528950043c3459641146203d3";
    private String wallet = "0x93fcb1de0a0fef8528950043c3459641146203d3";
    private String password = "1q2w#E$R";
    private Web3j web3j;
    private String contractAddress = "0xdCAB085368563516222740A336b29A6Ea998237E";

    //    private String gatewayUrl = "http://localhost:7545";


    public static void main(String[] args) throws Exception {
        new MyApplication().run();
    }

    private void run() throws Exception {

        // We start by creating a new web3j instance to connect to remote nodes on the network.
        web3j = Web3j.build(new HttpService(gatewayUrl));
        log.info("Connected to Ethereum client version: " + web3j.web3ClientVersion().send().getWeb3ClientVersion());

        // We then need to load our Ethereum wallet file
        Credentials credentials = WalletUtils.loadCredentials(password, credentialsFilePath);
        log.info("Credentials loaded");

        // AuthorizedContract contract = AuthorizedContract.load(contractAddress, web3j, credentials,
        //         ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT);
        // log.info("Smart contract address " + contractAddress);
        // log.info("View contract at https://rinkeby.etherscan.io/address/" + contractAddress);

        // contract.setGasPrice(BigInteger.valueOf(30000000L));
        // log.info("Authorizing Ethereum wallet...");
        // contract.addAddress("0x93fcb1de0a0fef8528950043c3459641146203d3").send();
        // log.info("Done!");

        // Send some wei to the contract
        BigInteger nonce = getNonce(wallet);
        BigInteger value = Convert.toWei("32000", Convert.Unit.GWEI).toBigInteger();

        Transaction transaction = Transaction.createEtherTransaction(
                wallet, nonce, GAS_PRICE, GAS_LIMIT, contractAddress, value);


        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                nonce, GAS_PRICE, GAS_LIMIT, contractAddress, value);
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String signHexValue = Numeric.toHexString(signedMessage);
        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(signHexValue).sendAsync().get();

        String transactionHash = ethSendTransaction.getTransactionHash();

        // assertFalse(transactionHash.isEmpty());

        TransactionReceipt transactionReceipt = waitForTransactionReceipt(transactionHash);
        log.info("Transaction receipt is: " + transactionReceipt.getTransactionHash());

        // assertThat(transactionReceipt.getTransactionHash(), is(transactionHash));

//        log.info("Sending 32000 GWei (" + Convert.fromWei("32000000000000", Convert.Unit.ETHER).toPlainString() + " to contract");
//
//        TransactionReceipt transferReceipt = Transfer.sendFunds(
//                web3j, credentials, contractAddress, BigDecimal.valueOf(32000), GWEI)
//                .send();
//        log.info("Transaction complete, view it at https://rinkeby.etherscan.io/tx/"
//                + transferReceipt.getTransactionHash());

    }

    public BigInteger getNonce(String address) throws Exception {
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                address, DefaultBlockParameterName.LATEST).sendAsync().get();

        return ethGetTransactionCount.getTransactionCount();
    }

    TransactionReceipt waitForTransactionReceipt(
            String transactionHash) throws Exception {

        Optional<TransactionReceipt> transactionReceiptOptional =
                getTransactionReceipt(transactionHash, SLEEP_DURATION, ATTEMPTS);

        if (!transactionReceiptOptional.isPresent()) {
            throw new RuntimeException("Transaction receipt not generated after " + ATTEMPTS + " attempts");
        }

        return transactionReceiptOptional.get();
    }

    private Optional<TransactionReceipt> getTransactionReceipt(
            String transactionHash, int sleepDuration, int attempts) throws Exception {

        Optional<TransactionReceipt> receiptOptional = sendTransactionReceiptRequest(transactionHash);
        for (int i = 0; i < attempts; i++) {
            if (!receiptOptional.isPresent()) {
                Thread.sleep(sleepDuration);
                receiptOptional = sendTransactionReceiptRequest(transactionHash);
            } else {
                break;
            }
        }

        return receiptOptional;
    }

    private Optional<TransactionReceipt> sendTransactionReceiptRequest(String transactionHash) throws Exception {
        EthGetTransactionReceipt transactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).sendAsync().get();
        return transactionReceipt.getTransactionReceipt();
    }
}
