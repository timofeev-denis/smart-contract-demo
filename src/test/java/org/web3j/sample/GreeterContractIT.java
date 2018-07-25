package org.web3j.sample;

import org.junit.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Integration test to run our main application.
 */
public class GreeterContractIT {

    private String gatewayUrl = "https://rinkeby.infura.io/VIhu8Q3dA5fy5NbxDLjR";
    private Web3j web3j = Web3j.build(new HttpService(gatewayUrl));

    @Test
    public void testGreeterContract() throws IOException {

        BigInteger gasLimit = web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf("latest"),
                true).send().getBlock().getGasLimit();
        System.out.println(gasLimit);
    }

    @Test
    public void sha3Test() throws IOException {
        String result = web3j.web3Sha3("SOMETHING").send().getResult();
        System.out.println(result);
    }
}
