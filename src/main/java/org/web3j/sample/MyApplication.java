package org.web3j.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.sample.contracts.generated.AuthorizedContract;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.math.BigDecimal;

import static org.web3j.utils.Convert.Unit.GWEI;

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

    private static final Logger log = LoggerFactory.getLogger(MyApplication.class);
    private String gatewayUrl = "https://rinkeby.infura.io/VIhu8Q3dA5fy5NbxDLjR";
//    private String gatewayUrl = "http://localhost:7545";


    public static void main(String[] args) throws Exception {
        new MyApplication().run();
    }

    private void run() throws Exception {

        // We start by creating a new web3j instance to connect to remote nodes on the network.
        // Note: if using web3j Android, use Web3jFactory.build(...
        Web3j web3j = Web3j.build(new HttpService(gatewayUrl));
        log.info("Connected to Ethereum client version: " + web3j.web3ClientVersion().send().getWeb3ClientVersion());

        // We then need to load our Ethereum wallet file
        Credentials credentials =
                WalletUtils.loadCredentials(
                        "1q2w#E$R",
                        "/home/denis/.ethereum/rinkeby/keystore/UTC--2018-07-15T14-29-16.262544014Z--93fcb1de0a0fef8528950043c3459641146203d3");
        log.info("Credentials loaded");

        // Now lets deploy a smart contract
//        log.info("Deploying smart contract");
//        AuthorizedContract contract = AuthorizedContract.deploy(
//                web3j, credentials,
//                ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT).send();
//
//        String contractAddress = contract.getContractAddress();
        String contractAddress = "0xdCAB085368563516222740A336b29A6Ea998237E";
        AuthorizedContract contract = AuthorizedContract.load(contractAddress, web3j, credentials,
                ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT);
        log.info("Smart contract address " + contractAddress);
        log.info("View contract at https://rinkeby.etherscan.io/address/" + contractAddress);

//        contract.setGasPrice(BigInteger.valueOf(30000000L));

//        log.info("Authorizing Ethereum wallet...");
//        contract.addAddress("0x93fcb1de0a0fef8528950043c3459641146203d3").send();
//        log.info("Done!");

        // Send some wei to the contract
        log.info("Sending 32000 GWei (" + Convert.fromWei("32000000000000", Convert.Unit.ETHER).toPlainString() + " to contract");

        TransactionReceipt transferReceipt = Transfer.sendFunds(
                web3j, credentials, contractAddress, BigDecimal.valueOf(32000), GWEI)
                .send();
        log.info("Transaction complete, view it at https://rinkeby.etherscan.io/tx/"
                + transferReceipt.getTransactionHash());

//         Lets modify the value in our smart contract
//        TransactionReceipt transactionReceipt = contract..newGreeting("Well hello again").send();

//        log.info("New value stored in remote smart contract: " + contract.greet().send());

        // Events enable us to log specific events happening during the execution of our smart
        // contract to the blockchain. Index events cannot be logged in their entirety.
        // For Strings and arrays, the hash of values is provided, not the original value.
        // For further information, refer to https://docs.web3j.io/filters.html#filters-and-events

//        for (Greeter.ModifiedEventResponse event : contract.getModifiedEvents(transactionReceipt)) {
//            log.info("Modify event fired, previous value: " + event.oldGreeting
//                    + ", new value: " + event.newGreeting);
//            log.info("Indexed event previous value: " + Numeric.toHexString(event.oldGreetingIdx)
//                    + ", new value: " + Numeric.toHexString(event.newGreetingIdx));
//        }
    }
}
